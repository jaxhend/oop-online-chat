rom flask import Flask, request, jsonify
from llama_cpp import Llama

app = Flask(__name__)
llm = Llama(model_path="/home/robinjuul/llama.cpp/models/llammas/llammasQ8.gguf", n_ctx=2048)

# Dictionary to store chat history per user (in-memory)
user_histories = {}

@app.route("/chat", methods=["POST"])
def chat():
    data = request.json
    user_id = data.get("user_id")
    prompt = data.get("prompt")

    if not user_id or not prompt:
        return jsonify({"error": "Missing user_id or prompt"}), 400

    history = user_histories.get(user_id, "")
    full_prompt = history + f"\nUser: {prompt}\nAI:"

    # Generate response
    output = llm(full_prompt, max_tokens=512, stop=["User:", "AI:"])
    response = output["choices"][0]["text"]

    # Update history
    user_histories[user_id] = full_prompt + response

    return jsonify({"response": response})
