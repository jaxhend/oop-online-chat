import json
from sentence_transformers import SentenceTransformer
import faiss
import numpy as np
import subprocess
from pymongo import MongoClient
from flask import Flask, request, jsonify

app = Flask(__name__)

# MongoDB connection
client = MongoClient("mongodb://localhost:27017/")
db = client["webscraping"]
collection = db["testing"]

# Data loading
data = list(collection.find())

chunks = []
meta = []
for item in data:
    texts = []
    for key in [
        "õppekava", "õppeaste", "programmi_juht", "kirjeldus", "sisu",
        "opivaljundid", "valdkond", "instituut", "moodulid",
        "vastuvõtutingimused", "lõpetamise_tingimused"
    ]:
        if key in item and isinstance(item[key], str):
            texts.append(f"{key.capitalize()}: {item[key].strip()}")
    combined = "\n".join(texts)
    if combined.strip():
        chunks.append(combined)
        meta.append(item.get("õppekava", "Tundmatu õppekava"))

# Embedding generation
model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
embeddings = model.encode(chunks)

index = faiss.IndexFlatL2(embeddings.shape[1])
index.add(np.array(embeddings))

@app.route("/chatbot", methods=["POST"])
def rag_chat():
    data = request.json
    query = data.get('query')

    if not query:
        return jsonify({"error": "Query not provided"}), 400

    # Query embedding
    query_embed = model.encode([query])
    _, indices = index.search(np.array(query_embed), k=3)

    retrieved_chunks = [chunks[i] for i in indices[0]]
    retrieved_meta = [meta[i] for i in indices[0]]

    # Truncate if necessary
    max_chunk_length = 1000
    retrieved_chunks = [chunk if len(chunk) <= max_chunk_length else chunk[:max_chunk_length] + "..." for chunk in retrieved_chunks]

    # Construct prompt
    prompt = f"Kontekst:\n\n{chr(10).join(retrieved_chunks)}\n\nKüsimus: {query}\nVastus:"

    # Write prompt to a file
    with open("prompt.txt", "w", encoding="utf-8") as f:
        f.write(prompt)

    # Run the model using llama.cpp
    result = subprocess.run([
        "./llama.cpp/build/bin/llama-run",
        "file://llama.cpp/models/llammas/llammasQ8.gguf",
        prompt
    ], capture_output=True, text=True)

    return jsonify({"response": result.stdout.strip()})

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)