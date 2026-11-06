
from fastapi import FastAPI, File, UploadFile, HTTPException
from pipeline import analyse_image

app = FastAPI(
    title="Plons AI API",
    version="0.4.0"
)

MAX_IMAGE_BYTES = 15 * 1024 * 1024


@app.get("/")
def home():
    return {
        "app": "Plons AI",
        "status": "online",
        "version": "0.4.0"
    }


@app.get("/health")
def health():
    return {
        "status": "ok",
        "ai_model_loaded": False
    }


@app.post("/detect")
async def detect(file: UploadFile = File(...)):
    if file.content_type not in (
        "image/jpeg",
        "image/png"
    ):
        raise HTTPException(
            status_code=415,
            detail="Alleen JPEG en PNG zijn toegestaan."
        )

    try:
        image_bytes = await file.read(
            MAX_IMAGE_BYTES + 1
        )
    finally:
        await file.close()

    if not image_bytes:
        raise HTTPException(
            status_code=400,
            detail="Lege afbeelding ontvangen."
        )

    if len(image_bytes) > MAX_IMAGE_BYTES:
        raise HTTPException(
            status_code=413,
            detail="Afbeelding is groter dan 15 MB."
        )

    try:
        result = analyse_image(image_bytes)

    except ValueError as error:
        raise HTTPException(
            status_code=400,
            detail=str(error)
        ) from error

    result["ai_model_loaded"] = False
    result["message"] = (
        "Foto verwerkt; AI-detectie nog niet actief."
    )

    return result
