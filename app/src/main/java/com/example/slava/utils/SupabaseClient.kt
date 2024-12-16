package com.example.slava.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slava.models.Answer
import com.example.slava.models.Challenge
import com.example.slava.models.Progress
import com.example.slava.models.Question
import com.example.slava.models.QuestionWithAnswers
import com.example.slava.models.Rating
import com.example.slava.models.RatingItem
import com.example.slava.models.RatingWithDetails
import com.example.slava.models.User
import com.example.slava.models.UserChallengeWithDetails
import com.example.slava.models.UserChallenges
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseClient : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> get() = _userState

    private val _challenges = MutableStateFlow<List<Challenge>>(emptyList())
    val challenges: StateFlow<List<Challenge>> get() = _challenges

    private val _activeChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val activeChallenges: StateFlow<List<Challenge>> get() = _activeChallenges

    private val _ratingItems = MutableStateFlow<List<RatingItem>>(emptyList())
    val ratingItems: StateFlow<List<RatingItem>> get() = _ratingItems

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://ycybrdkzpztnwhobwwrg.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InljeWJyZGt6cHp0bndob2J3d3JnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzI3NzA2MzEsImV4cCI6MjA0ODM0NjYzMX0.EeDaj5f8z8q-jYphPDN2fd5QSDXTsS_E_qc__85-EPs"
    ) {
        install(Auth)
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json)
    }

    fun signUp(
        context: Context,
        userEmail: String,
        userPassword: String,
        userFullName: String,
        userPhone: String,
        userDate: String
    ) {
        viewModelScope.launch {
            try {
                supabase.auth.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                    data = buildJsonObject {
                        put("full_name", userFullName)
                        put("phone_number", userPhone)
                        put("date_of_birth", userDate)
                    }
                }
                val user = User(
                    uid = supabase.auth.currentUserOrNull()!!.id,
                    name = userFullName,
                    phone = userPhone,
                    date_of_birth = userDate
                )
                supabase.from("User").insert(user)
                saveToken(context)
                _userState.value = UserState.Success("Успешная регистрация!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun login(
        context: Context,
        userEmail: String,
        userPassword: String
    ) {
        viewModelScope.launch {
            try {
                supabase.auth.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }
                saveToken(context)
                _userState.value = UserState.Success("Успешная авторизация!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun sendEmailOtp(
        userEmail: String
    ) {
        viewModelScope.launch {
            try {
                supabase.auth.resetPasswordForEmail(userEmail)
                _userState.value = UserState.Success("Успешно")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun checkOtp(
        code: String,
        userEmail: String
    ) {
        viewModelScope.launch {
            try {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = userEmail,
                    token = code
                )
                _userState.value = UserState.Success("Успешно")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun updatePassword(userPassword: String) {
        viewModelScope.launch {
            try {
                supabase.auth.updateUser {
                    password = userPassword
                }
                _userState.value = UserState.Success("Успешно")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    private fun saveToken(context: Context) {
        viewModelScope.launch {
            val accessToken = supabase.auth.currentUserOrNull()!!.id
            val sharedPref = SharedPreferenceHelper(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val response = supabase.from("User")
                .select() {
                    filter {
                        eq("uid", userId)
                    }
                }
                .decodeSingle<User>()

            response
        } catch (e: Exception) {
            println("Ошибка получения пользователя: ${e.message}")
            null
        }
    }

    fun fetchChallenges() {
        viewModelScope.launch {
            try {
                val response = supabase.from("Challenge").select().decodeList<Challenge>()
                _challenges.value = response
                _userState.value = UserState.Success("Успешно")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun fetchRating(challengeId: Int) {
        viewModelScope.launch {
            try {
                val columns = Columns.raw("""
                id,
                id_challenge,
                score,
                User(
                    id,
                    uid,
                    name,
                    phone,
                    date_of_birth
                )
            """.trimIndent())
                val response = supabase.from("Rating").select(columns) {
                    order("score", Order.DESCENDING)
                    filter {
                        eq("id_challenge", challengeId)
                    }
                }.decodeList<RatingWithDetails>()

                // Преобразуем в список RatingItem
                _ratingItems.value = response.map { rating ->
                    RatingItem(
                        name = rating.user.name,
                        score = rating.score
                    )
                }
            } catch (e: Exception) {
                Log.e("123", e.message.toString())
            }
        }
    }

    suspend fun getTotalScore(userId: Int) : Int {
        return try {
            val data = Columns.raw("""
                id_challenge,
                score,
                id_user
            """.trimIndent())
            val response = supabase.from("Rating")
                .select(data) {
                    filter { eq("id_user", userId) }
                }
                .decodeList<Rating>()

            // Суммируем все очки
            response.sumOf { it.score }
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
            0
        }
    }

    suspend fun getChallengeUsers(challengeId: Int) : Long {
        return try {
            val count = supabase.from("UserChallenges")
                .select {
                    count(Count.EXACT)
                    filter {
                        eq("id_challenge", challengeId)
                    }
                }
                .countOrNull()!!
            count
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
            0
        }
    }

    fun insertUserChallenge(userId: Int, challengeId: Int) {
        viewModelScope.launch {
            try {
                Log.e("123", userId.toString())
                val data = UserChallenges(id_user = userId, id_challenge = challengeId)
                supabase.from("UserChallenges").insert(data)
                _userState.value = UserState.Success("Успешно")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun fetchAcceptedChallenges(userId: Int) {
        viewModelScope.launch {
            try {
                val columns = Columns.raw(
                    """
                    id,
                    id_user,
                    Challenge(
                        id,
                        id_Категории_челленджа,
                        Название,
                        Описание,
                        Задание,
                        Награда,
                        Время_начала,
                        Время_окончания,
                        Дата_начала_проведения,
                        Дата_окончание_проведения
                    )
                """.trimIndent()
                )
                val challenges = supabase.from("UserChallenges")
                    .select(columns) {
                        filter { eq("id_user", userId) }
                    }
                    .decodeList<UserChallengeWithDetails>()

                Log.d("SupabaseClient", "Полученные вызовы: ${challenges.size}")

                _activeChallenges.value = challenges.map { it.challenge }
                _userState.value = UserState.Success("Вызовы получены!")
            } catch (e: Exception) {
                Log.e("123", e.message.toString())
                _userState.value = UserState.Error("Ошибка: ${e.message}")
            }
        }
    }

    suspend fun isChallengeActive(userId: Int, challengeId: Int): Boolean {
        return try {
            val response = supabase.from("UserChallenges")
                .select() {
                    filter {
                        eq("id_user", userId)
                        eq("id_challenge", challengeId)
                    }
                }
                .decodeSingle<UserChallenges>()

            true // Если запись существует, значит челлендж активен
        } catch (e: Exception) {
            println("Ошибка проверки активности челленджа: ${e.message}")
            false
        }
    }

    suspend fun fetchQuestionsByTestId(testId: Int): List<QuestionWithAnswers> {
        try {
            val questions = supabase.from("Questions")
                .select {
                    filter { eq("id_test", testId) }
                }
                .decodeList<Question>()

            val questionWithAnswers = questions.map { question ->
                val answers = supabase.from("Answers")
                    .select {
                        filter { eq("id_questions", question.id) }
                    }
                    .decodeList<Answer>()
                QuestionWithAnswers(question.id, question.text, answers)
            }

            return questionWithAnswers
        } catch (e : Exception) {
            Log.e("123", e.message.toString())
        }
        return emptyList()
    }

    suspend fun saveTestResult(userId: Int, challengeId: Int, score: Int) {
        try {
            val data = Rating(id_challenge =  challengeId, score = score, id_user = userId)
            supabase.from("Rating").insert(data)
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Ошибка сохранения результата: ${e.message}")
            throw e
        }
    }

    suspend fun updateUser(context: Context, userId : Int, name: String, phone: String, date: String) {
        try {
            supabase.from("User").update({
                set("name", name)
                set("phone", phone)
                set("date_of_birth", date)
            }) {
                filter {
                    eq("id", userId)
                }
            }
            Toast.makeText(context, "Данные обновлены!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
        }
    }

    suspend fun getChallengeProgress(userId: Int, challengeId: Int): Progress? {
        return try {
            val response = supabase.from("Progress")
                .select() {
                    filter { eq("id_user", userId) }
                    filter { eq("id_challenge", challengeId) }
                }
                .decodeSingle<Progress>()

            response
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Ошибка получения прогресса: ${e.message}")
            null
        }
    }

    suspend fun insertProgress(id_challenge: Int, date: String, steps: String, progress: Int, id_user: Int) {
        try {
            val data = Progress(
                id_challenge = id_challenge,
                date = date,
                steps = steps,
                progress = progress,
                id_user = id_user
            )
            supabase.from("Progress").insert(data)
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Ошибка сохранения данных: ${e.message}")
            throw e
        }

    }

    suspend fun updateProgress(progress: Int, challengeId: Int, userId: Int, date: String) {
        try {
            supabase.from("Progress").update({
                set("progress", progress)
                set("date", date)
            }) {
                filter {
                    eq("id_challenge", challengeId)
                    eq("id_user", userId)
                }
            }
        } catch (e: Exception) {
            Log.e("123", e.message.toString())
        }
    }
}