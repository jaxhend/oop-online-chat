import faiss
import numpy as np
from sentence_transformers import SentenceTransformer
import subprocess

# Lae oma tekstifail
with open("tartu_opped.txt", "r", encoding="utf-8") as f:
    data = f.read()

chunks = data.strip().split("\n\n")  # Võid soovi korral peenhäälestada

model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
embeddings = model.encode(chunks)

index = faiss.IndexFlatL2(embeddings.shape[1])
index.add(np.array(embeddings))

query = input("💬 Sisesta küsimus: ")
query_embed = model.encode([query])
_, indices = index.search(np.array(query_embed), k=3)
retrieved = [chunks[i] for i in indices[0]]

prompt = f"Kontekst:\n{''.join(retrieved)}\n\nKüsimus: {query}\nVastus:"

# Kui tahame hiljem prompto salvestada faili
with open("prompt.txt", "w", encoding="utf-8") as f:
    f.write(prompt)

print("\n🧠 Llammas mõtleb...\n")
cmd = [
    "./llama.cpp/build/bin/llama-run",
    "file://llama.cpp/models/llammas/llammasQ8.gguf",
    prompt
]

subprocess.run(cmd)