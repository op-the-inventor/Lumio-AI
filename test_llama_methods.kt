import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters

fun test() {
    val p = ModelParameters()
    p.setModelFilePath("test")
    val model = LlamaModel(p)
    val outputs = model.generate("Hello")
}
