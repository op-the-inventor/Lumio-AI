sed -i '1169,1217c\
                _streamingMessage.value = ""\
                _streamingEmotion.value = "NORMAL"\
                var finalAiText = ""\
                if (isLocalModel) {\
                    val file = java.io.File(model)\
                    if (!file.exists()) {\
                        throw Exception("Local model file not found: $model")\
                    }\
                    val fullPrompt = StringBuilder()\
                    fullPrompt.append("<|im_start|>system\\n").append(customSystemPrompt).append("<|im_end|>\\n")\
                    for (msg in history) {\
                        if (msg.sender == "user") {\
                            fullPrompt.append("<|im_start|>user\\n").append(msg.text).append("<|im_end|>\\n")\
                        } else {\
                            fullPrompt.append("<|im_start|>assistant\\n").append(msg.text).append("<|im_end|>\\n")\
                        }\
                    }\
                    fullPrompt.append("<|im_start|>user\\n").append(text).append("<|im_end|>\\n")\
                    fullPrompt.append("<|im_start|>assistant\\n")\
                    val responseText = StringBuilder()\
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {\
                        val params = de.kherud.llama.ModelParameters().setModel(model)\
                        de.kherud.llama.LlamaModel(params).use { llamaModel ->\
                            val inferParams = de.kherud.llama.InferenceParameters(fullPrompt.toString())\
                            for (out in llamaModel.generate(inferParams)) {\
                                responseText.append(out.text)\
                                _streamingMessage.value = responseText.toString()\
                            }\
                        }\
                    }\
                    val rawText = responseText.toString().trim()\
                    val regex = Regex("\\\\[EMOTION: (.*?)\\\\]", RegexOption.IGNORE_CASE)\
                    val match = regex.find(rawText)\
                    if (match != null) {\
                        _streamingEmotion.value = match.groupValues[1].uppercase()\
                        finalAiText = rawText.replace(match.value, "").trim()\
                    } else {\
                        finalAiText = rawText\
                    }\
                } else {\
                    repository.streamAICompletion(\
                        apiKey = apiKey,\
                        modelName = model,\
                        userMessage = text,\
                        history = history,\
                        systemPrompt = customSystemPrompt\
                    ).collect { chunk ->\
                        if (chunk.startsWith("[ERROR]")) throw Exception(chunk)\
                        finalAiText += chunk\
                        _streamingMessage.value = finalAiText\
                    }\
                    val regex = Regex("\\\\[TONE:\\\\s*(ANGRY|SAD|EXCITED|NORMAL)\\\\]", RegexOption.IGNORE_CASE)\
                    val matchResult = regex.find(finalAiText)\
                    if (matchResult != null) {\
                        _streamingEmotion.value = matchResult.groupValues[1].uppercase()\
                        finalAiText = finalAiText.replace(matchResult.value, "").trim()\
                    }\
                }\
                val aiText = finalAiText\
                val aiEmotion = _streamingEmotion.value\
                _streamingMessage.value = null\
' app/src/main/java/com/example/ui/CallViewModel.kt
