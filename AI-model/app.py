from flask import Flask, request, jsonify

app = Flask(__name__)

user_histories = {}

@app.route("/flask/chat", methods=["POST"])
def chat():
    data = request.json
    user_id = data.get("user_id")
    prompt = data.get("prompt")

    if not user_id or not prompt:
        return jsonify({"error": "Missing user_id or prompt"}), 400

    response_text = f"Võtsin vastu sinu küsimuse: '{prompt}'"

    history = user_histories.get(user_id, [])
    history.append({"user": prompt, "bot": response_text})


    history = history[-2:]
    user_histories[user_id] = history

    return jsonify({
        "response": response_text,
        "history": history
    })

@app.route('/flask/päevapakumised', methods=['GET'])
def get_deals():
    return "Siin on päevapakkumised!"

@app.route('/flask/ilm', methods=['GET'])
def get_weather():
    return "Ilm on päikeseline."

@app.route('/flask/uudised', methods=['GET'])
def get_news():
    news_list = [
        "Online Chat töötab hästi!",
        "Täna on päikeseline ilm.",
        "Krüptorahade hinnad tõusevad!",
        "Uus tarkvaraversioon on saadaval.",
        "Programmeerijate päev läheneb!",
        "Tehisintellekt on tulevik!",
        "Õpilased võitsid robootikavõistluse."
    ]
    return jsonify(news_list)

@app.route('/flask/õppekava', methods=['GET'])
def get_curriculum():
    return "Psühholoogia peamiste teoreetiliste ja metodoloogiliste aluste tundmine."
@app.route("/flask/chatbot", methods=["POST"])
def chatbot_alias():
    return chat()

if __name__ == "__main__":
    app.run(port=5001, debug=True)