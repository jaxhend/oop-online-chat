import faiss
import json
import logging
import numpy as np
import os
import threading
import time
import torch
import uvicorn
from cachetools import TTLCache
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer
from typing import List, Dict
from vllm import LLM, SamplingParams

# -------- Konf --------
EMBEDDING_MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"  # Lausete sisendusmudel
LLM_MODEL = "Qwen/Qwen3-4B"
EMBED_BATCH = 64
TOP_K = 15  # Võtab vektorandmebaasist top 15 kirjet.
CONTEXT_TOKEN_LIMIT = 10000  # Maksimum token limit on 10000.
PRIOR_MESSAGES_TOKEN_LIMIT = 200  # 200 tokeni väärtuses on kasutaja varasem vestlus AI-ga
FILE_NAME = "data.json"
FAISS_INDEX_PATH = "faiss_index.bin"
EMBEDDINGS_PATH = "embeddings.npy"

# --------- FastAPI ---------
app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://www.utchat.ee"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --------- Päringute piirang ---------
RATE_LIMIT = 8
RATE_PERIOD = 60 # sekundit

ip_cache = TTLCache(maxsize=1000, ttl=RATE_PERIOD)


def is_rate_limited(ip: str):
    now = time.time()
    if ip not in ip_cache:
        ip_cache[ip] = [now]
        return False

    ip_cache[ip] = [ts for ts in ip_cache[ip] if now - ts < RATE_PERIOD]
    if len(ip_cache[ip]) >= RATE_LIMIT:
        return True

    ip_cache[ip].append(now)
    return False


# --------- Veebikraabitsa andmed ---------
with open(FILE_NAME, "r", encoding="utf-8") as f:
    data = json.load(f)

chunks = []
for item in data:
    for key, value in item.items():
        chunks.append(f"{key}: {value}")


# --------- Vektorandmebaas ---------
torch.cuda.empty_cache()
embedder = SentenceTransformer(EMBEDDING_MODEL)

# Kui vektorandmebaas on juba olemas, siis laeb selle mällu.
if os.path.exists(FAISS_INDEX_PATH) and os.path.exists(EMBEDDINGS_PATH):
    index = faiss.read_index(FAISS_INDEX_PATH)
    embeddings = np.load(EMBEDDINGS_PATH)
else:
    embeddings = embedder.encode(
        chunks,
        device="cpu",
        batch_size=EMBED_BATCH,
        show_progress_bar=True
    )
    np.save(EMBEDDINGS_PATH, embeddings)
    dim = embeddings.shape[1]
    index = faiss.IndexFlatL2(dim)  # Ehitab indeksid.
    index.add(np.array(embeddings, dtype='float32'))  # Lisab vektorid indeksile.
    faiss.write_index(index, FAISS_INDEX_PATH)

# --------- vLLM ---------
sampling_params = SamplingParams(
    temperature=0.7,
    top_p=0.8,
    top_k=20,
    max_tokens=32768
)
llm = LLM(
    model=LLM_MODEL,
    dtype=torch.float16,
    max_model_len=12000,
    gpu_memory_utilization=0.97
)
tokenizer = AutoTokenizer.from_pretrained(LLM_MODEL)
lock = threading.Lock()  # Tekitame queue.


# --------- Konteksti pikkuse piiramine ---------
def truncate_to_token_limit(text, limit):
    tokens = tokenizer.encode(text)
    if len(tokens) > limit:
        tokens = tokens[:limit]
        return tokenizer.decode(tokens)
    return text


# --------- REST API skeem ---------
class QueryRequest(BaseModel):  # JSON body sisaldab 'query' ja 'history' välja
    query: str
    history: List[Dict[str, str]]


# --------- Kõik muud leheküljed ---------
@app.exception_handler(404)
async def not_found_html(request: Request, exc):
    return HTMLResponse(
        content="<h1>404 - Page Not Found</h1><p>The page you requested does not exist.</p>",
        status_code=404
    )


# --------- RAG ---------
@app.post("/chatbot")
def rag_chat(req: QueryRequest, request: Request):
    client_ip = request.client.host
    if is_rate_limited(client_ip):
        return {"response": "Liiga palju päringuid. Proovi ühe minuti pärast uuesti!"}

    q = req.query.strip()
    prior_messages = ""
    for elem in req.history:
        sender = elem.get("sender", "")
        text = elem.get("text", "")
        if sender == "Robot":
            prior_messages += "Sinu vastus oli: "
        elif sender == "Sina":
            prior_messages += "Kasutaja küsimus: "
        prior_messages += text + "\n"

    if not q:
        raise HTTPException(status_code=400, detail="Päring ei tohi tühi olla.")

    with lock:  # Ühe päringu töötlemine korraga
        q_emb = embedder.encode([q], device="cpu")
        D, I = index.search(np.array(q_emb, dtype='float32'), TOP_K)
        ctxs = [chunks[i] for i in I[0]]

        combined_context = "\n\n".join(ctxs)
        context = truncate_to_token_limit(combined_context, CONTEXT_TOKEN_LIMIT)
        truncated_prior_messages = truncate_to_token_limit(prior_messages, PRIOR_MESSAGES_TOKEN_LIMIT)
        prompt = (
            f"""Oled abivalmis tehisintellekti assistent veebilehel UTchat. See veebileht on mõeldud Tartu Ülikooli üliõpilastele. 
            Sinu peamine ülesanne on vastata kasutaja sisendile ainult antud konteksti põhjal. Ole sõbralik ja abivalmis.

                Antud kontekst:
                {context}
                
                Kasutaja varasem sisend ja sinu vastused:
                {truncated_prior_messages}
                
                Kasutaja sisend: {q}
            
                Vastus: """
        )
        messages = [
            {"role": "user", "content": prompt}
        ]
        text = tokenizer.apply_chat_template(
            messages,
            tokenize=False,
            add_generation_prompt=True,
            enable_thinking=False
        )

        # LLM Genereerib vastus
        outputs = llm.generate([text], sampling_params)
        text = outputs[0].outputs[0].text.strip()

    return {"response": text}
