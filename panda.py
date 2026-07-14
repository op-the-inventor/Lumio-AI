import urllib.request
import json

url = "https://raw.githubusercontent.com/Templarian/MaterialDesign/master/svg/panda.svg"
try:
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    html = urllib.request.urlopen(req).read().decode('utf-8')
    print("Found icon")
    with open("panda.svg", "w") as f:
        f.write(html)
except Exception as e:
    print(e)
