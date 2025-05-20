import faiss
import json
import numpy as np
import threading
import torch
import uvicorn
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer
from vllm import LLM, SamplingParams

# -------- Konf --------
MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
LLM_MODEL = "Qwen/Qwen3-4B"
EMBED_BATCH = 64
TOP_K = 3
CONTEXT_TOKEN_LIMIT = 15000   # Max token limit on 4096. Igale promptile anname ette paar lauset, mis on 82 tokenit.

# --------- FastAPI ---------
app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://www.utchat.ee"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --------- ÕIS andmed ---------
with open("data.json", "r", encoding="utf-8") as f:
    data = json.load(f)

chunks = []
split_keys = ["sisu", "õpivaljundid", "lõpetamise_tingimused"]
for item in data:
    title = item.get("õppekava")
    level = item.get("õppeaste")
    base = f"ÕPPEAINE: {title}\n ÕPPEASTE: {level}\n"
    for key, val in item.items():
        if key in split_keys or key == "õppeaine" or key == "õppeaste" or key == "moodulid":  # Jätame moodulite info vahele hetkel.
            continue
        base += f"{key}: {val}\n"
    for sk in split_keys:
        val = item.get(sk)
        if val:
            chunks.append(base + f"{sk.upper()}: {val}")

# --------- Embedding mudel ---------
torch.cuda.empty_cache()
embedder = SentenceTransformer(MODEL_NAME)
embeddings = embedder.encode(
    chunks,
    device="cpu",
    batch_size=EMBED_BATCH,
    show_progress_bar=True
)

# --------- FAISS indeks ---------
dim = embeddings.shape[1]
index = faiss.IndexFlatL2(dim)
index.add(np.array(embeddings, dtype='float32'))

# --------- vLLM ---------
sampling_params = SamplingParams(temperature=0.6, top_p=0.95, top_k=20, max_tokens=4096)

llm = LLM(
    model=LLM_MODEL,
    dtype=torch.float16,
    max_model_len = 18000,
    gpu_memory_utilization=0.90
)

tokenizer = AutoTokenizer.from_pretrained(LLM_MODEL)

lock = threading.Lock()  # Tekitame queue.


# --------- Tokenite lugemine ---------
def count_tokens(text: str) -> int:
    tokens = tokenizer.encode(text)
    return len(tokens)


# --------- REST API skeem ---------
class QueryRequest(BaseModel):
    query: str  # JSON body sisaldab 'query' välja


# --------- Kõik muud leheküljed ---------
@app.exception_handler(404)
async def not_found_html(request: Request, exc):
    return HTMLResponse(
        content="<h1>404 - Page Not Found</h1><p>The page you requested does not exist.</p>",
        status_code=404
    )


# --------- RAG ---------
@app.post("/chatbot")
def rag_chat(req: QueryRequest):
    q = req.query.strip()
    if not q:
        raise HTTPException(status_code=400, detail="Päring ei tohi tühi olla.")

    with lock:  # Ühe päringu töötlemine korraga
        q_emb = embedder.encode([q], device="cpu")
        D, I = index.search(np.array(q_emb, dtype='float32'), TOP_K)
        ctxs = [chunks[i] for i in I[0]]

        prompt_chunks = []
        total_tokens = count_tokens(q)  # Prompti tokenite pikkus
        for c in ctxs:
            t = count_tokens(c + "\n--------\n")
            if total_tokens + t > CONTEXT_TOKEN_LIMIT:
                break  # Rohkem chunke juurde ei lisa.
            prompt_chunks.append(c)
            total_tokens += t

        if not prompt_chunks:
            # Juhul kui kui esimene chunk on liiga suur.
            if total_tokens > CONTEXT_TOKEN_LIMIT:
                q = q[: int(CONTEXT_TOKEN_LIMIT * 3.5)]
            else:  # Juhul kui kontekst on liiga suur-
                prompt_chunks = [ctxs[0][: int(CONTEXT_TOKEN_LIMIT - total_tokens * 3.5)] + "..."]

        context = "\n\n".join(prompt_chunks)
        prompt = (
            f"""You are a helpful AI assistant. Your primary task is to answer the user's question based *only* on the provided context. If the answer is not found in the context, you may then use your general knowledge.
                
                Provided Context:
                {context}
                
                User's question: {q}
                
                Please provide your answer in Estonian.
                Vastus (Answer in Estonian): """
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


        # Genereeri vastus
        outputs = llm.generate([text], sampling_params)
        text = outputs[0].outputs[0].text.strip()

    return {"response": text}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)
