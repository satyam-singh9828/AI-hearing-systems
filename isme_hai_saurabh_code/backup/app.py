from flask import Flask, render_template, Response, request
from vosk import Model, KaldiRecognizer
import sounddevice as sd
import queue
import json
import os
from pydub import AudioSegment
from gtts import gTTS
from transformers import pipeline

app = Flask(__name__)

# -------------------------------
# Load Models
# -------------------------------
print("Loading Vosk model...")
vosk_model = Model("vosk_models/vosk-model-small-en-us-0.15")

print("Loading summarization model...")
summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

q = queue.Queue()
os.makedirs("recordings", exist_ok=True)

# -------------------------------
# Live Audio Stream (SSE)
# -------------------------------
def live_audio_stream(duration_sec=100):
    rec = KaldiRecognizer(vosk_model, 16000)
    frames = []

    def callback(indata, frames_count, time, status):
        if status:
            print(status)
        q.put(bytes(indata))

    with sd.RawInputStream(
        samplerate=16000,
        blocksize=8000,
        dtype='int16',
        channels=1,
        callback=callback
    ):
        print("🎤 Listening...")
        for _ in range(int(16000 / 8000 * duration_sec)):
            data = q.get()
            frames.append(data)

            if rec.AcceptWaveform(data):
                result = json.loads(rec.Result())
                text = result.get("text", "")
                yield f"data: {text}\n\n"
            else:
                partial = json.loads(rec.PartialResult())
                text = partial.get("partial", "")
                if text:
                    yield f"data: {text}\n\n"

    # Save audio
    audio_data = b''.join(frames)
    audio_segment = AudioSegment(
        audio_data,
        sample_width=2,
        frame_rate=16000,
        channels=1
    )

    wav_path = "recordings/recorded.wav"
    audio_segment.export(wav_path, format="wav")
    print("✅ Audio saved:", wav_path)

# -------------------------------
# Generate Summary (AI)
# -------------------------------
def generate_summary(text):
    text = text.strip()

    # If too small → don't summarize
    if len(text.split()) < 10:
        return "⚡ Short conversation: " + text

    try:
        # dynamic length
        max_len = max(30, int(len(text.split()) * 0.6))
        min_len = max(10, int(len(text.split()) * 0.3))

        result = summarizer(
            text,
            max_length=max_len,
            min_length=min_len,
            do_sample=False
        )

        return result[0]['summary_text']

    except Exception as e:
        print("❌ Summarization error:", e)
        return text
# -------------------------------
# Routes
# -------------------------------
@app.route("/")
def index():
    return render_template("index.html")

@app.route("/live")
def live():
    return Response(live_audio_stream(200), mimetype="text/event-stream")

@app.route("/summarize", methods=["POST"])
def summarize():
    wav_file = "recordings/recorded.wav"

    if not os.path.exists(wav_file):
        return render_template("index.html", transcript="❌ No recording found.", summary="")

    rec = KaldiRecognizer(vosk_model, 16000)
    transcript = ""

    with open(wav_file, "rb") as f:
        data = f.read()
        if rec.AcceptWaveform(data):
            result = json.loads(rec.Result())
            transcript = result.get("text", "")
        else:
            partial = json.loads(rec.PartialResult())
            transcript = partial.get("partial", "")

    summary = generate_summary(transcript)

    return render_template("index.html", transcript=transcript, summary=summary)

@app.route("/reply", methods=["POST"])
def reply():
    user_text = request.form.get("user_text", "").strip()
    audio_path = ""

    if user_text:
        os.makedirs("static", exist_ok=True)
        tts = gTTS(user_text)
        audio_path = "static/reply.mp3"
        tts.save(audio_path)

    return render_template("index.html", reply_audio=audio_path)

# -------------------------------
if __name__ == "__main__":
    app.run(debug=True)