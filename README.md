# AI Hearing Assist

AI Hearing Assist is a hearing support project with a native Android app and a small Python web prototype for transcript summaries and spoken replies.

## Android App

The main app is a Kotlin/Jetpack Compose Android application designed for local-first communication support.

### Features
- Live captions from the phone microphone.
- High Accuracy mode using the Android system speech recognizer when available.
- Offline mode using bundled Vosk speech models.
- Language/accent choices for English India, English US, Hindi, Hinglish, Bhojpuri, English UK, and English Australia.
- Transcript collection with local save, restore, clear, and Android share sheet export.
- Typed response panel with Android text-to-speech playback.
- Local extractive conversation summary.
- Context-aware next-word suggestions using an on-device predictor.
- Accessible Compose UI with large controls, dark/light themes, and readable contrast.

### Bundled Offline Models
- `app/src/englishIn/assets/model-en-in` provides the Indian English offline flavor.
- `app/src/englishUs/assets/model-en-us` provides the US English offline flavor.
- `app/src/hindi/assets/model-hi` provides the Hindi offline flavor.
- The `full` Android product flavor bundles all three model folders.

Hinglish and Bhojpuri use the Hindi model as the closest lightweight bundled model. Speaker labels are pseudo diarization, not biometric speaker identification. Summary generation is local/extractive, not an LLM call.

### Android Requirements
- Android Studio with Android SDK 34.
- JDK 17 or newer supported by your Android Gradle plugin setup.
- The included Gradle wrapper (`gradlew` / `gradlew.bat`).
- A real Android device or emulator with microphone permission enabled.

### Build And Run
Open this folder in Android Studio, let Gradle sync complete, connect a phone or start an emulator, then run the `app` module.

Useful command-line builds on Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleEnglishInDebug
.\gradlew.bat assembleEnglishUsDebug
.\gradlew.bat assembleHindiDebug
.\gradlew.bat assembleFullDebug
```

On macOS or Linux, use `./gradlew` with the same task names.

## Python Prototype

The repository also includes a Flask prototype:

- `app.py` accepts uploaded audio, transcribes it with Whisper, summarizes it with BART, and creates a spoken reply with gTTS.

Install Python dependencies:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

Run a prototype:

```powershell
python app.py
```

Python notes:
- Whisper needs FFmpeg installed on the system path.
- gTTS requires internet access because it uses Google's text-to-speech service.
- Generated recordings and MP3 replies are intentionally ignored by Git.

## Testing Offline

For the Android offline flow, install an offline flavor APK, enable airplane mode, open the app, grant microphone permission, and verify captions, transcript, summary, suggestions, save/restore, and TTS response flow.
