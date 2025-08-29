import io, os, json, time
import requests
from fastapi import FastAPI
from pydantic import BaseModel, Field
from ultralytics import YOLO
from PIL import Image
import boto3

AWS_REGION = os.getenv("AWS_REGION", "ap-northeast-2")
S3_BUCKET = os.getenv("S3_BUCKET")           # 런타임에 환경변수로 주입(필수)
MODEL_PATH = os.getenv("YOLO_MODEL_PATH", "yolov8n.pt")  # custom pt 가능
DEFAULT_OUTPUT_SIZE = int(os.getenv("OUTPUT_SIZE", "512"))

model = YOLO(MODEL_PATH)
app = FastAPI()

class DetectCropReq(BaseModel):
    imagePresignedUrl: str = Field(..., description="S3 GET presigned URL")
    targetS3Prefix: str = Field(..., description="ex) univ/1/return-requests/42/crops/")
    padRatio: float = 0.30
    topk: int = 1
    minConf: float = 0.25
    outputSize: int = DEFAULT_OUTPUT_SIZE

class DetectCropResp(BaseModel):
    cropKey: str | None
    detectionMetaJson: str | None

def pad_center_square(x1, y1, x2, y2, img_w, img_h, pad_ratio):
    w = x2 - x1; h = y2 - y1
    cx = (x1 + x2) / 2.0; cy = (y1 + y2) / 2.0
    side = max(w, h) * (1 + pad_ratio * 2)
    nx1 = int(max(0, round(cx - side/2)))
    ny1 = int(max(0, round(cy - side/2)))
    nx2 = int(min(img_w, round(cx + side/2)))
    ny2 = int(min(img_h, round(cy + side/2)))
    return nx1, ny1, nx2, ny2

@app.get("/health")
def health():
    return {"status": "ok", "ts": int(time.time())}

@app.post("/detect-crop", response_model=DetectCropResp)
def detect_crop(req: DetectCropReq):
    r = requests.get(req.imagePresignedUrl, timeout=20)
    r.raise_for_status()
    img = Image.open(io.BytesIO(r.content)).convert("RGB")
    w, h = img.size

    results = model.predict(img, conf=req.minConf, verbose=False)
    boxes = results[0].boxes
    if boxes is None or len(boxes) == 0:
        return DetectCropResp(cropKey=None, detectionMetaJson=json.dumps({"status":"no_detection"}))

    confs = boxes.conf.cpu().numpy()
    order = confs.argsort()[::-1][:max(1, req.topk)]
    idx = int(order[0])

    xyxy = boxes.xyxy.cpu().numpy()[idx].tolist()
    cls  = int(boxes.cls.cpu().numpy()[idx])
    sc   = float(confs[idx])

    x1, y1, x2, y2 = [int(round(v)) for v in xyxy]
    px1, py1, px2, py2 = pad_center_square(x1,y1,x2,y2, w,h, req.padRatio)

    crop = img.crop((px1, py1, px2, py2)).resize((req.outputSize, req.outputSize), Image.LANCZOS)
    buf = io.BytesIO(); crop.save(buf, format="JPEG", quality=92); buf.seek(0)

    if not S3_BUCKET:
        raise RuntimeError("S3_BUCKET env is required")
    s3 = boto3.client("s3", region_name=AWS_REGION)
    crop_key = f"{req.targetS3Prefix.rstrip('/')}/crop_00.jpg"
    s3.upload_fileobj(buf, S3_BUCKET, crop_key, ExtraArgs={"ContentType":"image/jpeg"})

    det = {
        "class_id": cls, "score": sc,
        "bbox_xyxy": [x1,y1,x2,y2], "padded_xyxy":[px1,py1,px2,py2],
        "image_size":[w,h], "pad_ratio": req.padRatio,
        "output_size": req.outputSize, "model": os.path.basename(MODEL_PATH)
    }
    return DetectCropResp(cropKey=crop_key, detectionMetaJson=json.dumps(det))
