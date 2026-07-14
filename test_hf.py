import urllib.request
import json

url = "https://huggingface.co/api/models?search=Llama-3.2-1B&filter=gguf&limit=5"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read())
        print(f"search result: {data}")
except Exception as e:
    print(f"Error: {e}")

url2 = "https://huggingface.co/api/models/bartowski/Llama-3.2-1B-Instruct-GGUF/tree/main"
req2 = urllib.request.Request(url2, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req2) as response:
        data = json.loads(response.read())
        print(f"tree result: {[x['path'] for x in data]}")
except Exception as e:
    print(f"Error: {e}")
