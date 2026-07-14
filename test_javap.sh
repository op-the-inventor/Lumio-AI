#!/bin/bash
mkdir -p ~/.gradle/caches
find ~/.gradle/caches -name "llama-4.2.0.jar" | head -n 1 | xargs -I {} javap -p -cp {} de.kherud.llama.LlamaModel
