package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askTutor(userQuestion: String, subjectContext: String = "Computer Science"): String =
        withContext(Dispatchers.IO) {
            val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext getOfflineSmartAnswer(userQuestion)
            }

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

                val systemPrompt = """
                    You are EduSync AI Tutor, an expert computer science teacher at a prestigious computer education institute.
                    You assist students with topics including DCA, ADCA, CCC, Tally Prime, Web Development, Python, C++, Operating Systems, and MS Office.
                    Explain concepts clearly, provide simple code examples or accounting shortcuts, and encourage the student.
                    Keep responses structured, helpful, and concise (under 250 words when possible).
                    Subject context: $subjectContext.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val userPart = JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nStudent asks: $userQuestion"))
                            })
                        }
                        put(userPart)
                    }
                    put("contents", contentsArray)
                }

                val body = requestJson.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w("GeminiService", "API error: ${response.code}")
                        return@withContext getOfflineSmartAnswer(userQuestion)
                    }

                    val respBody = response.body?.string() ?: return@withContext getOfflineSmartAnswer(userQuestion)
                    val json = JSONObject(respBody)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.optJSONObject(0)
                            ?.optString("text")
                        if (!text.isNullOrBlank()) return@withContext text
                    }
                    getOfflineSmartAnswer(userQuestion)
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Gemini call failed", e)
                getOfflineSmartAnswer(userQuestion)
            }
        }

    suspend fun analyzeSuspiciousActivity(input: String): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            val lower = input.lowercase()
            // High-risk SQL injection, privilege escalation, or script tags
            val dangerPatterns = listOf(
                "drop table", "union select", "or '1'='1", "or 1=1",
                "<script>", "javascript:", "eval(", "exec(", "/etc/passwd",
                "admin' --", "delete from students", "shutdown", "-- -", ";--"
            )

            for (pattern in dangerPatterns) {
                if (lower.contains(pattern)) {
                    return@withContext true to "Malicious pattern detected: contains '$pattern' (SQL/Script injection attempt)."
                }
            }

            false to "Normal student interaction."
        }

    private fun getOfflineSmartAnswer(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("dca") -> """
                **Diploma in Computer Applications (DCA):**
                DCA is a 6-month foundational program covering:
                1. Computer Fundamentals & OS (Windows/Linux)
                2. MS Office Suite: MS Word (Docs), MS Excel (Spreadsheets & Formulas), MS PowerPoint (Presentations)
                3. Internet, Emailing, and Cyber Safety
                4. Basic Database concepts and PC Hardware.
            """.trimIndent()

            q.contains("tally") || q.contains("gst") -> """
                **Tally Prime with GST Key Shortcuts & Concepts:**
                • F4: Contra Voucher (Bank/Cash deposit/transfer)
                • F5: Payment Voucher (Paying vendors/expenses)
                • F6: Receipt Voucher (Receiving payments from clients)
                • F7: Journal Voucher (Adjustments & Depreciation)
                • F8: Sales Voucher (Billing with CGST/SGST/IGST)
                • F9: Purchase Voucher (Inward goods receipt)
                *GST Tip:* CGST & SGST are applied for Intra-state (same state) trades, while IGST applies to Inter-state trades.
            """.trimIndent()

            q.contains("python") -> """
                **Python Programming Quick Guide:**
                Python is a high-level interpreted programming language.
                • Key Features: Easy syntax, dynamic typing, rich standard libraries.
                • Variables: `name = 'Student'`, `marks = 95`
                • Loop: `for i in range(1, 6): print(i)`
                • Function: `def greet(n): return f'Hello, {n}!'`
                *Common Modules:* NumPy (arrays), Pandas (data analysis), Matplotlib (charts).
            """.trimIndent()

            q.contains("web") || q.contains("html") || q.contains("css") || q.contains("javascript") -> """
                **Web Development Essentials:**
                • HTML5: Defines webpage structure (`<header>`, `<main>`, `<section>`, `<footer>`).
                • CSS3: Controls layout and appearance (Flexbox, Grid, Media Queries for mobile responsiveness).
                • JavaScript: Powers client-side interactivity, DOM manipulation, and asynchronous fetch APIs.
            """.trimIndent()

            q.contains("attendance") -> """
                **Institute Attendance Rules:**
                A minimum of 75% attendance is required to be eligible for quarterly & final examination admit cards.
                You can track your real-time attendance percentage and calendar directly in the 'Attendance & Fees' tab.
            """.trimIndent()

            q.contains("fees") || q.contains("fee") || q.contains("receipt") -> """
                **Fees & Payment Information:**
                Students can view their total course fees, paid amounts, and due dates in the Fees section.
                Official digital receipts with transaction IDs are generated automatically upon every payment confirmation.
            """.trimIndent()

            q.contains("admit card") || q.contains("exam") -> """
                **Exam Admit Card Instructions:**
                Admit cards are issued before each semester examination. Ensure:
                1. Minimum 75% attendance is maintained.
                2. Pending tuition installments are cleared.
                Download and carry your printed or digital Admit Card with your student photo and roll number to the exam center.
            """.trimIndent()

            else -> """
                **EduSync AI Tutor Response:**
                Thank you for your question regarding "$query".
                In Computer Institute education, mastering both practical keyboard shortcuts and theoretical concepts is key.
                • Practice hands-on lab exercises daily.
                • Complete all chapter assignments before the deadline.
                • Ask your batch instructor or prompt me with specific questions about Tally, Python, MS Excel, or Web Development!
            """.trimIndent()
        }
    }
}
