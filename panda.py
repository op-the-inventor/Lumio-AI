import os
import json

try:
    svg_path = "panda.svg"

    if not os.path.exists(svg_path):
        raise FileNotFoundError(f"{svg_path} not found in the repository root.")

    with open(svg_path, "r", encoding="utf-8") as f:
        html = f.read()

    print("Found local icon")

except Exception as e:
    print(e)
