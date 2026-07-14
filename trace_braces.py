with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

depth = 0
for i, line in enumerate(lines):
    depth += line.count('{') - line.count('}')
    if i > 2400 and i < 2440:
        print(f"Line {i+1} depth {depth}: {line.strip()}")
print(f"Final depth: {depth}")
