FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0 libgl1 curl ca-certificates && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 요구사항 및 제약 파일 복사 (캐시 최적화)
COPY requirements.torch.cpu.txt requirements.txt constraints.txt /app/

# 1) torch/vision은 CPU 인덱스에서만 설치
# 2) 나머지는 PyPI에서 설치하되 torch/vision + numpy(1.26.4)를 제약으로 '고정'
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cpu \
        -r requirements.torch.cpu.txt && \
    pip install --no-cache-dir --index-url https://pypi.org/simple \
        -r requirements.txt \
        --constraint requirements.torch.cpu.txt \
        --constraint constraints.txt && \
    pip check

COPY test.py /app/test.py

EXPOSE 8000
CMD ["uvicorn","test:app","--host","0.0.0.0","--port","8000"]