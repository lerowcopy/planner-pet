package com.example.pet.data.remote

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.pet.BuildConfig
import com.example.pet.data.model.CreateTaskDto
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiTaskParser @Inject constructor(
    private val gson: Gson
) {
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            temperature = 0.3f
        }
    )

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun parse(input: String): CreateTaskDto? {
        return try {
            val prompt = """
                Сегодня ${LocalDate.now()}
                Извлеки задачу из текста и верни JSON строго в этом формате:
                {"title": "string", "description": "string|null", "day": "yyyy-MM-dd", "isCompleted": false, "isSynced": false}

                Исходный текст: $input

                Верни только JSON, без пояснений:
            """.trimIndent()

            Log.i("ai", prompt)
            var response: GenerateContentResponse? = null
            try {
                response = model.generateContent(prompt)
            }catch (e: Exception){
                Log.i("ai", e.message.toString())
            }

            Log.i("ai", "Response is null: ${response}")
            Log.i("ai", "Response text: ${response?.text}")
            
            val json = response?.text ?: run {
                Log.e("ai", "Response text is null")
                return null
            }
            
            Log.i("ai", "JSON: $json")
            gson.fromJson(json, CreateTaskDto::class.java)
        } catch (e: Exception) {
            Log.e("ai", "Exception in parse()", e)
            null
        }
    }
}