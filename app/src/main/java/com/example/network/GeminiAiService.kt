package com.example.network

import com.example.BuildConfig
import com.example.data.PyqQuestion
import com.example.data.PyqQuestionBank
import com.example.data.QuestionType
import com.example.data.StudyPlanTask
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object GeminiAiService {

    // Using Complex STEM/Math model 'gemini-3.1-pro-preview' as primary and 'gemini-3.5-flash' as fast fallback per gemini-api skill
    private const val MODEL_PRO = "gemini-3.1-pro-preview"
    private const val MODEL_FLASH = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private suspend fun callGeminiJson(
        prompt: String,
        systemInstructionText: String,
        model: String = MODEL_PRO
    ): String? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) return@withContext null

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "$BASE_URL$model:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().apply {
                    put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", systemInstructionText))
                    )
                }
            )
            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    }
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.6)
                }
            )
        }

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (model == MODEL_PRO) {
                        return@withContext callGeminiJson(prompt, systemInstructionText, MODEL_FLASH)
                    }
                    return@withContext null
                }
                val rawResponse = response.body?.string() ?: return@withContext null
                val root = JSONObject(rawResponse)
                val text = root.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                text
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateCustomAiMockTest(
        subjectFilter: String,
        focusTopics: List<String>,
        difficulty: String,
        questionCount: Int
    ): Pair<List<PyqQuestion>, Boolean> {
        val targetTopics = if (focusTopics.isNotEmpty()) focusTopics.joinToString(", ") else "High-Weightage JEE Main & Advanced Chapters"
        val prompt = """
            Generate exactly $questionCount authentic JEE Main / JEE Advanced level questions for subject: "$subjectFilter",
            focusing on weak/priority topics: [$targetTopics], difficulty: "$difficulty".
            Return a JSON array of objects where each object has:
            - "subject": one of "Physics", "Chemistry", "Mathematics"
            - "topic": string topic name
            - "examTag": string like "AI Custom Mock • JEE 2026 Pattern"
            - "difficulty": "$difficulty"
            - "questionText": rigorous STEM problem statement with numerical values
            - "options": array of 4 distinct option strings
            - "correctOptionIndex": integer 0, 1, 2, or 3
            - "targetTimeSeconds": integer between 90 and 150
            - "stepByStepSolution": array of 3 strings explaining Step 1, Step 2, Step 3
            - "keyFormulas": array of 2 key formula strings
            - "conceptSummary": concise 2-sentence explanation of the core concept
            - "commonPitfall": 1 sentence describing the most common student mistake
        """.trimIndent()

        val sysPrompt = "You are a senior IIT JEE academic paper setter. Always output strictly valid JSON array."
        val jsonText = callGeminiJson(prompt, sysPrompt)

        if (!jsonText.isNullOrBlank()) {
            try {
                val arr = JSONArray(jsonText)
                val parsed = mutableListOf<PyqQuestion>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val optsArr = obj.optJSONArray("options") ?: JSONArray()
                    val options = (0 until optsArr.length()).map { optsArr.getString(it) }
                    val stepsArr = obj.optJSONArray("stepByStepSolution") ?: JSONArray()
                    val steps = (0 until stepsArr.length()).map { stepsArr.getString(it) }
                    val formsArr = obj.optJSONArray("keyFormulas") ?: JSONArray()
                    val formulas = (0 until formsArr.length()).map { formsArr.getString(it) }
                    val subj = obj.optString("subject", if (subjectFilter == "All Subjects") "Physics" else subjectFilter)
                    val top = obj.optString("topic", focusTopics.firstOrNull() ?: "Rotational Motion")

                    if (options.size >= 4) {
                        parsed.add(
                            PyqQuestion(
                                id = "ai_${UUID.randomUUID().toString().take(8)}",
                                subject = subj,
                                topic = top,
                                examTag = obj.optString("examTag", "AI Custom Mock • JEE 2026"),
                                year = 2026,
                                difficulty = obj.optString("difficulty", difficulty),
                                questionType = QuestionType.MCQ,
                                questionText = obj.optString("questionText"),
                                options = options.take(4),
                                correctOptionIndex = obj.optInt("correctOptionIndex", 0).coerceIn(0, 3),
                                targetTimeSeconds = obj.optInt("targetTimeSeconds", 120),
                                stepByStepSolution = if (steps.isNotEmpty()) steps else listOf("Apply standard JEE formula and substitute values."),
                                keyFormulas = if (formulas.isNotEmpty()) formulas else listOf("Core chapter equation"),
                                conceptSummary = obj.optString("conceptSummary", "Fundamental JEE concept application."),
                                commonPitfall = obj.optString("commonPitfall", "Watch units and sign conventions."),
                                videoResourceTitle = "Concept Masterclass: $top",
                                videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+${top.replace(" ", "+")}+PYQ",
                                isAiGenerated = true
                            )
                        )
                    }
                }
                if (parsed.isNotEmpty()) {
                    return Pair(parsed, true)
                }
            } catch (ignored: Exception) {
            }
        }

        // Fallback: Curated adaptive mock synthesized from question bank matching weak topics & subject
        val matchingPool = PyqQuestionBank.allQuestions.filter { q ->
            val subjectMatch = subjectFilter == "All Subjects" || q.subject.equals(subjectFilter, ignoreCase = true)
            val topicMatch = focusTopics.isEmpty() || focusTopics.any { t ->
                q.topic.contains(t, ignoreCase = true) || t.contains(q.topic, ignoreCase = true)
            }
            subjectMatch && topicMatch
        }.ifEmpty {
            PyqQuestionBank.allQuestions.filter { q ->
                subjectFilter == "All Subjects" || q.subject.equals(subjectFilter, ignoreCase = true)
            }
        }.ifEmpty {
            PyqQuestionBank.allQuestions
        }

        val selected = matchingPool.shuffled().take(questionCount.coerceAtLeast(3)).map {
            it.copy(
                id = "ai_adapt_${it.id}_${UUID.randomUUID().toString().take(4)}",
                examTag = "AI Adaptive Mock • ${it.topic}",
                isAiGenerated = true
            )
        }
        return Pair(selected, false)
    }

    suspend fun generateAiConceptBooster(
        subject: String,
        topic: String,
        userAccuracyPercent: Int
    ): String {
        val prompt = """
            A JEE aspirant has $userAccuracyPercent% accuracy in "$topic" ($subject).
            Return a JSON object with:
            - "intuition": A crystal-clear 2-3 sentence physical/mathematical intuition for mastering $topic
            - "algorithm": A 3-step problem-solving checklist to solve any JEE PYQ on $topic without negative marking
            - "memoryTrick": A memorable shortcut or dimensional/limiting-case verification trick
        """.trimIndent()

        val jsonText = callGeminiJson(
            prompt = prompt,
            systemInstructionText = "You are an elite IIT JEE mentor. Output strictly valid JSON."
        )
        if (!jsonText.isNullOrBlank()) {
            try {
                val obj = JSONObject(jsonText)
                val intuition = obj.optString("intuition")
                val algorithm = obj.optString("algorithm")
                val trick = obj.optString("memoryTrick")
                if (intuition.isNotBlank()) {
                    return "Core Intuition:\n$intuition\n\n3-Step PYQ Algorithm:\n$algorithm\n\nTopper Shortcut:\n$trick"
                }
            } catch (ignored: Exception) {
            }
        }

        val guide = PyqQuestionBank.getConceptGuideForTopic(topic)
        return "Core Intuition:\n${guide.conceptSummary}\n\nKey Formulas to Memorize:\n• ${guide.keyFormulas.joinToString("\n• ")}\n\nExam Trap Alert:\n${guide.examTrapTip}"
    }

    suspend fun generateAiStudySchedule(
        weakTopics: List<Pair<String, String>>, // (subject, topic)
        dailyHours: Int,
        targetExam: String
    ): List<StudyPlanTask> {
        val prioritized = weakTopics.ifEmpty {
            listOf(
                "Physics" to "Rotational Motion",
                "Chemistry" to "Electrochemistry & Kinetics",
                "Mathematics" to "Definite Integration",
                "Physics" to "Electrostatics",
                "Chemistry" to "Organic GOC & Carbonyls",
                "Mathematics" to "Vectors & 3D Geometry"
            )
        }

        val topicsSummary = prioritized.joinToString("; ") { "${it.first}: ${it.second}" }
        val prompt = """
            Create a 6-task personalized JEE study plan for target exam "$targetExam" with $dailyHours hours/day available.
            Prioritize these weak areas: [$topicsSummary].
            Return a JSON array of 6 objects with:
            - "dayLabel": string (max 35 chars, e.g. "Day 1 • Morning Drill", "Day 2 • Concept Fix", "Day 3 • Timed PYQs")
            - "subject": strictly one of "Physics", "Chemistry", "Mathematics"
            - "topic": string (max 70 chars)
            - "activityType": strictly one of "CONCEPT_REVIEW", "PYQ_DRILL", "AI_MOCK_TEST", "FORMULA_REVISION"
            - "targetPyqCount": integer between 8 and 30
            - "durationMinutes": integer between 30 and 120
            - "priorityLevel": strictly one of "HIGH_WEAK_AREA", "MEDIUM_PRACTICE", "REVISION"
        """.trimIndent()

        val jsonText = callGeminiJson(
            prompt = prompt,
            systemInstructionText = "You are a JEE study planner. Output strictly valid JSON array."
        )

        if (!jsonText.isNullOrBlank()) {
            try {
                val arr = JSONArray(jsonText)
                val tasks = mutableListOf<StudyPlanTask>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val subj = obj.optString("subject", "Physics").let {
                        if (it in listOf("Physics", "Chemistry", "Mathematics")) it else "Physics"
                    }
                    val act = obj.optString("activityType", "PYQ_DRILL").let {
                        if (it in listOf("CONCEPT_REVIEW", "PYQ_DRILL", "AI_MOCK_TEST", "FORMULA_REVISION")) it else "PYQ_DRILL"
                    }
                    val prio = obj.optString("priorityLevel", "HIGH_WEAK_AREA").let {
                        if (it in listOf("HIGH_WEAK_AREA", "MEDIUM_PRACTICE", "REVISION")) it else "HIGH_WEAK_AREA"
                    }
                    tasks.add(
                        StudyPlanTask(
                            dayLabel = obj.optString("dayLabel", "Day ${i + 1} • Priority").take(40),
                            subject = subj,
                            topic = obj.optString("topic", "Rotational Motion").take(80),
                            activityType = act,
                            targetPyqCount = obj.optInt("targetPyqCount", 15).coerceIn(5, 60),
                            durationMinutes = obj.optInt("durationMinutes", 45).coerceIn(15, 180),
                            priorityLevel = prio,
                            completed = false
                        )
                    )
                }
                if (tasks.isNotEmpty()) return tasks
            } catch (ignored: Exception) {
            }
        }

        // Deterministic personalized fallback based on the user's actual weak areas
        val baseMinutes = (dailyHours * 15).coerceIn(30, 90)
        return prioritized.take(5).mapIndexed { index, (subject, topic) ->
            val dayLabels = listOf(
                "Day 1 • Weak Area Fix",
                "Day 2 • Timed PYQ Drill",
                "Day 3 • Formula & Concept",
                "Day 4 • High-Yield PYQs",
                "Day 5 • Weekly AI Mock"
            )
            val activities = listOf(
                "CONCEPT_REVIEW",
                "PYQ_DRILL",
                "FORMULA_REVISION",
                "PYQ_DRILL",
                "AI_MOCK_TEST"
            )
            StudyPlanTask(
                dayLabel = dayLabels.getOrElse(index) { "Day ${index + 1} • Practice" },
                subject = subject,
                topic = topic,
                activityType = activities.getOrElse(index) { "PYQ_DRILL" },
                targetPyqCount = if (index % 2 == 0) 12 else 20,
                durationMinutes = baseMinutes,
                priorityLevel = if (index < 3) "HIGH_WEAK_AREA" else "MEDIUM_PRACTICE",
                completed = false
            )
        }
    }
}
