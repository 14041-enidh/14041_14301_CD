FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY Server.py .
COPY static/ ./static/
COPY templates/ ./templates/

EXPOSE 80

CMD ["python", "Server.py"]