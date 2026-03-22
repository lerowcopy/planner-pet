package com.example.pet.data.audio

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.IOException
import javax.inject.Inject

class SpeechToTextService @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    var isModelLoaded = false
        private set

    suspend fun loadModel() = withContext(Dispatchers.IO) {
        File(context.filesDir, "model").deleteRecursively()
        LibVosk.setLogLevel(LogLevel.WARNINGS)

        val modelPath = copyModelFromAssets()

        val modelDir = File(modelPath)
        Log.d("Vosk", "Model path: $modelPath")
        Log.d("Vosk", "Model exists: ${modelDir.exists()}")
        Log.d("Vosk", "Model files: ${modelDir.listFiles()?.map { it.name }}")

        model = Model(modelPath)
        isModelLoaded = true
    }

    fun startListening(
        onPartialResult: (String) -> Unit = {},
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isModelLoaded || model == null) {
            onError("Модель не загружена")
            return
        }

        try {
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(object : RecognitionListener {

                override fun onPartialResult(hypothesis: String?) {
                    val text = parseText(hypothesis)
                    Log.i("ai", "1: $text")
                    if (text.isNotBlank()) onPartialResult(text)
                }

                override fun onResult(hypothesis: String?) {
                    val text = parseText(hypothesis)
                    Log.i("ai", "2: $text")
                    if (text.isNotBlank()) {
                        onResult(text)
                        stopListening()
                    }
                }

                override fun onFinalResult(hypothesis: String?) {
                    val text = parseText(hypothesis)
                    Log.i("ai", "3: $text")
                    if (text.isNotBlank()) {
                        onResult(text)
                        stopListening()
                    }
                }

                override fun onError(exception: Exception?) {
                    onError(exception?.message ?: "Ошибка распознавания")
                }

                override fun onTimeout() {
                    Log.i("ai", "timeout is working")
                    onError("Таймаут")
                }
            })
        } catch (e: IOException) {
            onError("Ошибка запуска: ${e.message}")
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService = null
    }

    fun release() {
        speechService?.shutdown()
        speechService = null
        model?.close()
        model = null
    }

    private fun copyModelFromAssets(): String {
        val modelDir = File(context.filesDir, "model")
        if (modelDir.exists()) return modelDir.absolutePath

        modelDir.mkdirs()
        copyAssetFolder("model", modelDir.absolutePath)
        return modelDir.absolutePath
    }

    private fun copyAssetFolder(assetPath: String, outPath: String) {
        val assets = context.assets
        val files = assets.list(assetPath) ?: return

        files.forEach { file ->
            val subAsset = "$assetPath/$file"
            val outFile = File(outPath, file)

            if (assets.list(subAsset)?.isNotEmpty() == true) {
                outFile.mkdirs()
                copyAssetFolder(subAsset, outFile.absolutePath)
            } else {
                assets.open(subAsset).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private fun parseText(hypothesis: String?): String {
        return try {
            JSONObject(hypothesis ?: "").optString("text", "").trim()
        } catch (e: Exception) {
            ""
        }
    }
}