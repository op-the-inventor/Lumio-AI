package com.example

import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters

class TestLlama {
    fun test() {
        val p = ModelParameters()
        val m = LlamaModel(p)
        for (out in m.generate("hello")) {
             println(out.text)
        }
    }
}
