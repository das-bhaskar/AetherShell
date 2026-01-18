# analyzer.py

import os
from pathlib import Path
from dotenv import load_dotenv
from PIL import Image
import sys

from google import genai
from google.genai import types


if getattr(sys, 'frozen', False):
    # Running as binary: look one level up from .backend/
    bundle_dir = os.path.dirname(os.path.dirname(sys.executable))
else:
    # Running as script: look one level up from .backend/
    bundle_dir = str(Path(__file__).resolve().parent.parent)

env_path = os.path.join(bundle_dir, ".env")
load_dotenv(dotenv_path=env_path)

API_KEY = os.getenv("GEMINI_API_KEY")

client = None
if API_KEY:
    client = genai.Client(
        api_key=API_KEY,
        http_options=types.HttpOptions(
            api_version="v1",
            timeout=10_000,  # 10 seconds hard cap
        ),
    )


LABELS = {
    "A": "stickman",
    "B": "cat",
    "C": "dog",
    "D": "ghost",
    "E": "monster",
    "F": "house",
    "G": "tree",
    "H": "car",
    "I": "flower",
    "J": "face",
    "K": "unknown"
}

# Core analyzer (ONE IMAGE PER SESSION)

def analyze_with_gemini(image_path: str) -> str:
    """
    Sends exactly one PNG image to Gemini and returns
    a single-letter classification (A–K) or '?' on failure.
    """

    if not client:
        return "AI Offline"

    if not image_path or not os.path.exists(image_path):
        return "No Image"

    try:
        img = Image.open(image_path)

        prompt = (
            "You are classifying a simple hand-drawn sketch.\n\n"
            "Respond with ONLY ONE LETTER.\n\n"
            "Options:\n"
            "A. stickman\n"
            "B. cat\n"
            "C. dog\n"
            "D. ghost\n"
            "E. monster\n"
            "F. house\n"
            "G. tree\n"
            "H. car\n"
            "I. flower\n"
            "J. face\n"
            "K. unknown\n\n"
        )

        response = client.models.generate_content(
            model="gemini-2.5-flash-lite",
            contents=[
                prompt,
                img,
            ],
            config=types.GenerateContentConfig(
                temperature=0.0,        # deterministic
                max_output_tokens=5,    # cheap + safe
            ),
        )

        if not response.text:
            return "?"

        letter = response.text.strip()[:1]

        if letter in LABELS:
            return letter

        return "?"

    except Exception as e:
        print("Gemini Vision Error:", e)
        return "?"


def decode_label(letter: str) -> str:
    return LABELS.get(letter, "unknown")
