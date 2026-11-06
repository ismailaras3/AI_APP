
from fastapi import FastAPI, File, UploadFile, HTTPException

app = FastAPI(
    title="Plons AI API",
    version="0.3.0"
)

MAX_IMAGE_BYTES = 15 * 1024 * 1024


@app.get("/")
def home():
    return {
        "app": "Plons AI",
        "status": "online",
        "version": "0.3.0"
    }


@app.get("/health")
def health():
    return {
        "status": "ok",
        "ai_model_loaded": False
    }


@app.post("/detect")
async def detect(file: UploadFile = File(...)):
    if file.content_type not in ("image/jpeg", "image/png"):
        raise HTTPException(
            status_code=415,
            detail="Alleen JPEG- en PNG-afbeeldingen zijn toegestaan."
        )

    try:
        image_bytes = await file.read(MAX_IMAGE_BYTES + 1)
    finally:
        await file.close()

    if len(image_bytes) > MAX_IMAGE_BYTES:
        raise HTTPException(
            status_code=413,
            detail="Afbeelding is groter dan 15 MB."
        )

    if not image_bytes:
        raise HTTPException(
            status_code=400,
            detail="Lege afbeelding ontvangen."
        )

    # Er is nog geen AI-model aangesloten.
    # Daarom geven we geen verzonnen detecties terug.
    return {
        "detections": [],
        "ai_model_loaded": False,
        "message": "Foto ontvangen; AI-detectie nog niet actief."
    }
