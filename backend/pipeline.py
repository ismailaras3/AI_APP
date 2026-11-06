
from io import BytesIO

from PIL import Image, ImageOps, UnidentifiedImageError

MAX_PIXELS = 25_000_000


def preprocess_image(image_bytes: bytes):
    """Lees, controleer en normaliseer de afbeelding."""
    try:
        with Image.open(BytesIO(image_bytes)) as image:
            if image.format not in ("JPEG", "PNG"):
                raise ValueError(
                    "Alleen JPEG- en PNG-afbeeldingen zijn toegestaan."
                )

            if image.width * image.height > MAX_PIXELS:
                raise ValueError("Afbeelding heeft te veel pixels.")

            corrected = ImageOps.exif_transpose(image)
            return corrected.convert("RGB")

    except (UnidentifiedImageError, OSError) as error:
        raise ValueError(
            "Het bestand bevat geen geldige afbeelding."
        ) from error


def run_detector(image):
    """
    Hier komt later ons getrainde AI-model.

    None betekent: detectie is niet uitgevoerd.
    Een lege lijst zou betekenen: detectie uitgevoerd,
    maar niets gevonden.
    """
    return None


def postprocess_detections(detections):
    """Verwerk en tel modeldetecties."""
    if detections is None:
        return {
            "detections": [],
            "total_count": None,
            "counts_by_category": {},
            "analysis_status": "model_not_loaded",
        }

    counts = {}

    for detection in detections:
        category = detection["category"]
        counts[category] = counts.get(category, 0) + 1

    return {
        "detections": detections,
        "total_count": len(detections),
        "counts_by_category": counts,
        "analysis_status": "completed",
    }


def analyse_image(image_bytes: bytes):
    """Voer de volledige inferencepipeline uit."""
    image = preprocess_image(image_bytes)

    detections = run_detector(image)

    result = postprocess_detections(detections)

    result["image"] = {
        "width": image.width,
        "height": image.height,
    }

    return result
