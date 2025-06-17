package com.example.slava.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.slava.adapters.ChatAdapter
import com.example.slava.exceptions.translateError
import com.example.slava.models.Answer
import com.example.slava.models.Challenge
import com.example.slava.models.Chat
import com.example.slava.models.Message
import com.example.slava.models.Question
import com.example.slava.models.QuestionWithAnswers
import com.example.slava.models.Task
import com.example.slava.models.User
import com.example.slava.models.UserChallenge
import com.example.slava.models.UserTask
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.minutes

class SupabaseClient : ViewModel() {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://ycybrdkzpztnwhobwwrg.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InljeWJyZGt6cHp0bndob2J3d3JnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzI3NzA2MzEsImV4cCI6MjA0ODM0NjYzMX0.EeDaj5f8z8q-jYphPDN2fd5QSDXTsS_E_qc__85-EPs"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
        httpEngine = HttpClient(OkHttp) {
            install(WebSockets)
        }.engine
        defaultSerializer = KotlinXSerializer(Json)
    }

    // Авторизация
    suspend fun login(
        context: Context,
        userEmail: String,
        userPassword: String
    ): Result<Boolean> {
        return try {
            supabase.auth.signInWith(Email) {
                email = userEmail
                password = userPassword
            }
            saveToken(context)
            Result.success(true)
        } catch (e: AuthRestException) {
            val errorMessage = translateError(e.errorCode?.value, e.message)
            Result.failure(Exception("Ошибка метод login: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод login: ${e.message}"))
        }
    }

    // Регистрация
    suspend fun signup(
        context: Context,
        userEmail: String,
        userPassword: String,
        userFullName: String,
        userPhone: String,
        userDate: String
    ): Result<Boolean> {
        return try {
            supabase.auth.signUpWith(Email) {
                email = userEmail
                password = userPassword
            }
            val user = User(
                uid = supabase.auth.currentUserOrNull()!!.id,
                name = userFullName,
                phone = userPhone,
                date_of_birth = userDate,
                role = "Пользователь",
                user_pts = 0
            )
            supabase.from("users").insert(user)
            saveToken(context)
            Result.success(true)
        } catch (e: AuthRestException) {
            val errorMessage = translateError(e.errorCode?.value, e.message)
            Result.failure(Exception("Ошибка метод signup: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод signup: ${e.message}"))
        }
    }

    // Отправка кода на почту
    suspend fun sendEmailOtp(
        userEmail: String
    ): Result<Boolean> {
        return try {
            supabase.auth.resetPasswordForEmail(userEmail)
            Result.success(true)
        } catch (e: AuthRestException) {
            val errorMessage = translateError(e.errorCode?.value, e.message)
            Result.failure(Exception("Ошибка метод sendEmailOtp: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод sendEmailOtp: ${e.message}"))
        }
    }

    // Проверка кода
    suspend fun checkOtp(
        code: String,
        userEmail: String
    ): Result<Boolean> {
        return try {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.RECOVERY,
                email = userEmail,
                token = code
            )
            Result.success(true)
        } catch (e: AuthRestException) {
            val errorMessage = translateError(e.errorCode?.value, e.message)
            Result.failure(Exception("Ошибка метод checkOtp: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод checkOtp: ${e.message}"))
        }
    }

    // Обновление пароля
    suspend fun updatePassword(userPassword: String): Result<Boolean> {
        return try {
            supabase.auth.updateUser {
                password = userPassword
            }
            Result.success(true)
        } catch (e: AuthRestException) {
            val errorMessage = translateError(e.errorCode?.value, e.message)
            Result.failure(Exception("Ошибка метод updatePassword: $errorMessage"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод updatePassword: ${e.message}"))
        }
    }

    // Сохранение токена
    suspend fun saveToken(context: Context) {
        try {
            val accessToken = supabase.auth.currentUserOrNull()?.id
            val sharedPref = SharedPreferenceHelper(context)
            val response = getUserById(accessToken.toString())
            response.onSuccess { user ->
                val userId = user.id_user
                sharedPref.saveStringData("userId", userId.toString())
            }
            sharedPref.saveStringData("accessToken", accessToken)
        } catch (e: Exception) {
            Log.e("Ошибка", "Ошибка метод saveToken: ${e.message}")
        }
    }

    // Получение токена
    fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    // Получение пользователя
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val response = supabase.from("users")
                .select {
                    filter {
                        eq("uid", userId)
                    }
                }
                .decodeSingle<User>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод saveToken: ${e.message}"))
        }
    }

    suspend fun getUserId(userId: Int): Result<User> {
        return try {
            val response = supabase.from("users")
                .select {
                    filter {
                        eq("id_user", userId)
                    }
                }
                .decodeSingle<User>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод getUserId: ${e.message}"))
        }
    }

    // Получение всех челленджей
    suspend fun fetchAllChallenges(challengeId: Int? = null): Result<List<Challenge>> {
        return try {
            val columns = Columns.raw("""
                id_challenge,
                category (
                    id_category,
                    name
                ),
                name,
                description,
                tasks,
                reward,
                challenge_start_date,
                challenge_end_date,
                status,
                id_user
            """.trimIndent())
            var response: List<Challenge> = if (challengeId != null) {
                supabase.from("challenge").select(columns) {
                    filter {
                        eq("id_challenge", challengeId)
                        eq("status", "Активный")
                    }
                }.decodeList<Challenge>()
            } else {
                supabase.from("challenge").select(columns) {
                    filter {
                        eq("status", "Активный")
                    }
                }.decodeList<Challenge>()
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchAllChallenges: ${e.message}"))
        }
    }

    // Получение деталей челленджа
    suspend fun fetchChallenge(challengeId: Int): Result<Challenge> {
        return try {
            val columns = Columns.raw("""
                id_challenge,
                category (
                    id_category,
                    name
                ),
                name,
                description,
                tasks,
                reward,
                challenge_start_date,
                challenge_end_date,
                status,
                id_user
            """.trimIndent())
            val response = supabase.from("challenge").select(columns) {
                filter {
                    eq("id_challenge", challengeId)
                    eq("status", "Активный")
                }
            }.decodeSingle<Challenge>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchChallenge: ${e.message}"))
        }
    }

    // Получение активных пользовательских челленджей
    suspend fun fetchAllUserAcceptedChallenges(userId: Int): Result<List<UserChallenge>> {
        return try {
            val columns = Columns.raw("""
                id_user_challenge,
                users (
                    id_user,
                    uid,
                    name,
                    phone,
                    date_of_birth,
                    password,
                    role,
                    user_pts
                ),
                challenge (
                    id_challenge,
                    category (
                        id_category,
                        name
                    ),
                    name,
                    description,
                    tasks,
                    reward,
                    challenge_start_date,
                    challenge_end_date
                ),
                user_start_date,
                step,
                progress,
                pts
            """.trimIndent())
            val response: List<UserChallenge> = supabase.from("user_challenge").select(columns) {
                filter {
                    eq("id_user", userId)
                }
            }.decodeList<UserChallenge>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchAllUserAcceptedChallenges: ${e.message}"))
        }
    }

    // Получение деталей активного пользовательского челленджа
    suspend fun fetchUserAcceptedChallenge(challengeId: Int, userId: Int): Result<UserChallenge> {
        return try {
            val columns = Columns.raw("""
                id_user_challenge,
                users (
                    id_user,
                    uid,
                    name,
                    phone,
                    date_of_birth,
                    password,
                    role,
                    user_pts
                ),
                challenge (
                    id_challenge,
                    category (
                        id_category,
                        name
                    ),
                    name,
                    description,
                    tasks,
                    reward,
                    challenge_start_date,
                    challenge_end_date
                ),
                user_start_date,
                step,
                progress,
                pts
            """.trimIndent())
            val response = supabase.from("user_challenge").select(columns) {
                filter {
                    eq("id_user", userId)
                    eq("id_challenge", challengeId)
                }
            }.decodeSingle<UserChallenge>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchUserAcceptedChallenge: ${e.message}"))
        }
    }

    // Принятие челленджа
    suspend fun insertUserChallenge(userId: Int, challengeId: Int, step: String, progress: Int): Result<Boolean> {
        return try {
            val data = UserChallenge(
                id_user = userId,
                id_challenge = challengeId,
                step = step,
                progress = progress,
                pts = 0
            )
            supabase.from("user_challenge").insert(data)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод insertUserChallenge: ${e.message}"))
        }
    }

    // Загрузка аватарки пользователя на сервер
    suspend fun uploadUserAvatar(imageUri: Uri, uid: String): Result<String> {
        return try {
            var isUploaded = false
            val bucket = supabase.storage.from("UsersPhoto")

            // Загружаем изображение в Supabase
            bucket.uploadAsFlow("${uid}.jpg", imageUri) {
                upsert = true  // Перезапись файла, если он уже есть
            }.collect {
                when (it) {
                    is UploadStatus.Progress -> Log.d("Upload", "Загрузка: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                    is UploadStatus.Success -> isUploaded = true
                }
            }

            if (isUploaded) {
                val signedUrl = bucket.createSignedUrl("${uid}.jpg", expiresIn = 525600.minutes)
                Result.success(signedUrl)
            } else {
                Result.failure(Exception("Ошибка метод uploadUserAvatar"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод uploadUserAvatar: ${e.message}"))
        }
    }

    suspend fun downloadUserAvatar(uid: String): Result<String> {
        return try {
            val bucket = supabase.storage.from("UsersPhoto")
            val url = bucket.createSignedUrl(path = "${uid}.jpg", expiresIn = 525600.minutes)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод downloadUserAvatar: ${e.message}"))
        }
    }

    // Обновление данных пользователя
    suspend fun updateUser(userId: Int, name: String, phone: String, date: String, userPassword: String? = null): Result<Boolean> {
        return try {
            supabase.from("users").update({
                set("name", name)
                set("phone", phone)
                set("date_of_birth", date)
            }) {
                filter {
                    eq("id_user", userId)
                }
            }
            if (!userPassword.isNullOrEmpty()) {
                supabase.auth.updateUser {
                    password = userPassword
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод updateUser: ${e.message}"))
        }
    }

    // Получение кол-ва пользователей, которые приняли челлендж
    suspend fun getChallengeUsers(challengeId: Int) : Result<Long> {
        return try {
            val count = supabase.from("user_challenge")
                .select {
                    count(Count.EXACT)
                    filter {
                        eq("id_challenge", challengeId)
                    }
                }.countOrNull()!!
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод getChallengeUsers: ${e.message}"))
        }
    }

    suspend fun fetchQuestionsByTestId(testId: Int): List<QuestionWithAnswers> {
        try {
            val questions = supabase.from("question")
                .select {
                    filter { eq("id_test", testId) }
                }
                .decodeList<Question>()

            val questionWithAnswers = questions.map { question ->
                val answers = supabase.from("answer")
                    .select {
                        filter { eq("id_questions", question.id) }
                    }
                    .decodeList<Answer>()

                QuestionWithAnswers(question.id, question.text, answers)
            }

            return questionWithAnswers
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Ошибка метод fetchQuestionsByTestId: ${e.message}")
        }
        return emptyList()
    }

    suspend fun fetchTasksByChallengeId(challengeId: Int): Result<List<Task>> {
        Log.d("123", challengeId.toString())
        return try {
            val response = supabase
                .from("task")
                .select {
                    filter {
                        eq("id_challenge", challengeId)
                    }
                }.decodeList<Task>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchTasksByChallengeId: ${e.message}"))
        }
    }

    suspend fun fetchTasksById(taskId: Int): Result<Task> {
        return try {
            val response = supabase
                .from("task")
                .select {
                    filter {
                        eq("id", taskId)
                    }
                }.decodeSingle<Task>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchTasksById: ${e.message}"))
        }
    }

    suspend fun fetchUserTaskCompleted(taskId: Int, userId: Int): Result<List<UserTask>> {
        return try {
            val response = supabase
                .from("user_task")
                .select {
                    filter {
                        eq("id_task", taskId)
                        eq("id_user", userId)
                    }
                }.decodeList<UserTask>()
            Log.d("fetchUserTaskCompleted", "Поиск записи: id_task = $taskId, id_user = $userId")
            Log.d("fetchUserTaskCompleted", "Результат: ${response.size}")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchUserTaskCompleted: ${e.message}"))
        }
    }

    suspend fun fetchNextPendingTask(userId: Int, challengeId: Int): Result<Task?> {
        return try {
            val allTasksResult = fetchTasksByChallengeId(challengeId)
            val allTasks = allTasksResult.getOrNull() ?: return Result.failure(Exception("Ошибка получения тестов"))
            val totalTasks = allTasks.size

            if (totalTasks == 0) return Result.failure(Exception("В этом челлендже нет задач"))

            var completedCount = 0

            for (task in allTasks) {
                Log.d("fetchNextPendingTask", "Проверяем задачу: ${task.name}")

                val completedResult = fetchUserTaskCompleted(task.id!!, userId)
                val completedTasks = completedResult.getOrNull()

                Log.d("fetchNextPendingTask", "Записи в user_task для задачи ${task.name}: ${completedTasks?.size ?: "нет данных"}")

                if (completedTasks.isNullOrEmpty()) {
                    // Обновим прогресс перед возвратом
                    val progress = (completedCount.toFloat() / totalTasks.toFloat()) * 100
                    updateChallengeProgressValue(userId, challengeId, progress)
                    return Result.success(task)
                } else {
                    completedCount++
                }
            }

            // Все задачи завершены
            val progress = 100f
            updateChallengeProgressValue(userId, challengeId, progress)
            return Result.success(null)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchNextPendingTask: ${e.message}"))
        }
    }

    suspend fun updateChallengeProgressValue(userId: Int, challengeId: Int, progress: Float) {
        try {
            supabase.from("user_challenge")
                .update({
                    set("progress", progress)
                }) {
                    filter {
                        eq("id_user", userId)
                        eq("id_challenge", challengeId)
                    }
                }
            Log.d("updateChallengeProgressValue", "Прогресс обновлён: $progress%")
        } catch (e: Exception) {
            Log.e("updateChallengeProgressValue", "Ошибка обновления прогресса: ${e.message}")
        }
    }


    suspend fun insertTestResult(userId: Int, taskId: Int, result: String, challengeId: Int): Result<Boolean> {
        return try {
            val data = UserTask(
                id_task = taskId,
                id_user = userId,
                result = result,
                status = "unsorted",
                id_challenge = challengeId
            )
            supabase.from("user_task")
                .insert(data)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод insertTestResult: ${e.message}"))
        }
    }

    suspend fun updateUserChallengePts(userId: Int, challengeId: Int, pts: Int): Result<Boolean> {
        return try {
            val user = supabase.from("user_challenge")
                .select{
                    filter {
                        eq("id_user", userId)
                        eq("id_challenge", challengeId)
                    }
                }.decodeSingle<UserChallenge>()
            val userPts = user.pts + pts
            supabase.from("user_challenge")
                .update({
                    set("pts", userPts)
                }) {
                    filter {
                        eq("id_user", userId)
                        eq("id_challenge", challengeId)
                    }
                }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод updateUserChallengePts: ${e.message}"))
        }
    }

    suspend fun updateUserPts(userId: Int, pts: Int): Result<Boolean> {
        return try {
            val response = getUserId(userId = userId)
            response.onSuccess { user ->
                val userPts = user.user_pts + pts
                supabase.from("users")
                    .update({
                        set("user_pts", userPts)
                    }) {
                        filter {
                            eq("id_user", userId)
                        }
                    }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод updateUserPts: ${e.message}"))
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun listenForNewMessages(chatId: Int, adapter: ChatAdapter) {
        val historyMessages = loadChatHistory(chatId)

        withContext(Dispatchers.Main) {
            adapter.updateMessages(historyMessages)
        }

        val channel = supabase.channel("public:messages")

        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter("chat_id", FilterOperator.EQ, chatId)
        }

        changeFlow.onEach { change ->
            try {
                val newMessage = Message(
                    id = change.record["id"]?.jsonPrimitive?.intOrNull,
                    chat_id = change.record["chat_id"]?.jsonPrimitive?.intOrNull ?: 0,
                    sender_id = change.record["sender_id"]?.jsonPrimitive?.intOrNull ?: 0,
                    text = change.record["text"]?.jsonPrimitive?.contentOrNull ?: "",
                    created_at = change.record["created_at"]?.jsonPrimitive?.contentOrNull ?: "",
                    type = change.record["type"]?.jsonPrimitive?.contentOrNull ?: "",
                    fileUri = change.record["file_uri"]?.jsonPrimitive?.contentOrNull ?: ""
                )

                val updatedMessages = adapter.getMessages().toMutableList()
                updatedMessages.add(newMessage)

                withContext(Dispatchers.Main) {
                    adapter.updateMessages(updatedMessages)
                }

                Log.d("Новое сообщение", "Данные: $newMessage")
            } catch (e: Exception) {
                Log.e("Ошибка парсинга", "Ошибка: ${e.message}")
            }
        }.launchIn(coroutineScope)

        channel.subscribe()
    }

    suspend fun loadChatHistory(chatId: Int): List<Message> {
        return try {
            val response: List<Message> = supabase
                .from("messages")
                .select {
                    filter {
                        eq("chat_id", chatId)
                    }
                }
                .decodeList()

            response
        } catch (e: Exception) {
            Log.e("Ошибка загрузки", "Ошибка метод loadChatHistory: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(message: Message) {
        try {
            supabase.from("messages").insert(message)
        } catch (e: Exception) {
            Log.e("Ошибка загрузки", "Ошибка метод sendMessage: ${e.message}")
        }
    }

    suspend fun getUserChats(userId: Int): List<Chat> {
        return try {
            val chats: List<Chat> = supabase.from("chats")
                .select {
                    filter {
                        or {
                            eq("user1_id", userId)
                            eq("user2_id", userId)
                        }
                    }
                }.decodeList()

            chats
        } catch (e: Exception) {
            Log.e("Ошибка загрузки", "Ошибка метод getUserChats: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserName(userId: Int): User? {
        return try {
            val response = supabase.from("users")
                .select {
                    filter {
                        eq("id_user", userId)
                    }
                }
                .decodeSingle<User>()

            response
        } catch (e: Exception) {
            Log.e("Ошибка загрузки", "Ошибка метод getUserName: ${e.message}")
            null
        }
    }

    suspend fun searchUsersByName(query: String): Result<List<User>> {
        Log.d("Search", "Поиск пользователей: $query")
        return try {
            val response = supabase.from("users")
                .select {
                    filter {
                        ilike("name", "%$query%")
                    }
                }
                .decodeList<User>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод searchUsersByName: ${e.message}"))
        }
    }

    suspend fun createChat(user1Id: Int, user2Id: Int): Chat? {
        return try {
            val data = Chat(user1_id = user1Id, user2_id = user2Id)
            val response = supabase
                .from("chats")
                .insert(data) {
                    select()
                }.decodeSingle<Chat>()

            val chatId = response.id
            Chat(id = chatId, user1_id = user1Id, user2_id = user2Id)

        } catch (e: Exception) {
            Log.e("123", "Ошибка метод getUserName: ${e.message}")
            null
        }
    }

    suspend fun uploadChatMedia(fileUri: Uri, fileName: String): Result<String> {
        return try {
            var result = false
            val bucket = supabase.storage.from("chat.media")
            bucket.uploadAsFlow(fileName, fileUri) {
                upsert = true
            }.collect {
                when(it) {
                    is UploadStatus.Progress -> println("Progress: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                    is UploadStatus.Success -> result = true
                }
            }
            if (result) {
                val bucket = supabase.storage.from("chat.media")
                val url = bucket.createSignedUrl(path = fileName, expiresIn = 525600.minutes)
                Result.success(url)
            } else {
                Result.failure(Exception("Ошибка метод uploadChatMedia"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод uploadChatMedia: ${e.message}"))
        }
    }

    suspend fun uploadUserTasksFiles(fileUri: Uri, fileName: String): Result<String> {
        return try {
            var result = false
            val bucket = supabase.storage.from("unsorted")
            bucket.uploadAsFlow(fileName, fileUri) {
                upsert = true
            }.collect {
                when(it) {
                    is UploadStatus.Progress -> println("Progress: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                    is UploadStatus.Success -> result = true
                }
            }
            if (result) {
                val bucket = supabase.storage.from("unsorted")
                val url = bucket.createSignedUrl(path = fileName, expiresIn = 525600.minutes)
                Result.success(url)
            } else {
                Result.failure(Exception("Ошибка метод uploadUserTasksFiles"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод uploadUserTasksFiles: ${e.message}"))
        }
    }

    suspend fun fetchRating(challengeId: Int): Result<List<UserChallenge>> {
        return try {
            val columns = Columns.raw("""
                id_user_challenge,
                users (
                    id_user,
                    uid,
                    name,
                    phone,
                    date_of_birth,
                    password,
                    role,
                    user_pts
                ),
                challenge (
                    id_challenge,
                    category (
                        id_category,
                        name
                    ),
                    name,
                    description,
                    tasks,
                    reward,
                    challenge_start_date,
                    challenge_end_date
                ),
                user_start_date,
                step,
                progress,
                pts
            """.trimIndent())
            val response: List<UserChallenge> = supabase.from("user_challenge").select(columns) {
                order("pts", Order.DESCENDING)
                filter {
                    eq("id_challenge", challengeId)
                }
            }.decodeList<UserChallenge>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchRating: ${e.message}"))
        }
    }

    suspend fun createChallenge(name: String, id_category: Int, description: String, tasks: List<String>, reward: String, challenge_start_date: String, challenge_end_date: String, userId: Int): Result<Boolean> {
        return try {
            val data = Challenge(
                name = name,
                id_category = id_category,
                description = description,
                tasks = tasks,
                reward = reward,
                challenge_start_date = challenge_start_date,
                challenge_end_date = challenge_end_date,
                status = "На модерации",
                id_user = userId
            )
            supabase.from("challenge").insert(data)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод createChallenge: ${e.message}"))
        }
    }

    suspend fun fetchUserCreatedChallenge(userId: Int): Result<List<Challenge>> {
        return try {
            val columns = Columns.raw("""
                id_challenge,
                category (
                    id_category,
                    name
                ),
                name,
                description,
                tasks,
                reward,
                challenge_start_date,
                challenge_end_date,
                status,
                comment,
                id_user
            """.trimIndent())
            val response = supabase.from("challenge").select(columns) {
                filter {
                    eq("id_user", userId)
                }
            }.decodeList<Challenge>()
            Log.d("fetchUserCreatedChallenge", response.size.toString())
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchUserCreatedChallenge: ${e.message}"))
        }
    }

    suspend fun fetchUserTasksResultsChallenge(userId: Int, challengeId: Int): Result<List<UserTask>> {
        return try {
            val columns = Columns.raw("""
                id,
                id_task,
                id_user,
                result,
                comment,
                status,
                id_challenge
            """.trimIndent())
            val response = supabase.from("user_task").select(columns) {
                filter {
                    eq("id_user", userId)
                    eq("id_challenge", challengeId)
                }
            }.decodeList<UserTask>()
            Log.d("fetchUserTasksResultsChallenge", response.size.toString())
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка метод fetchUserCreatedChallenge: ${e.message}"))
        }
    }
}