FROM python:3.9-slim

WORKDIR /app

COPY src/requirements.txt requirements.txt
RUN pip install -r requirements.txt

COPY src/ /app

CMD ["python", "etl.py"]
