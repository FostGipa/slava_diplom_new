package com.example.slava.exceptions

val errorTranslations: Map<String, String> = mapOf(
    "anonymous_provider_disabled" to "Анонимный вход отключен.",
    "bad_code_verifier" to "Ошибка проверки кода. Проверьте реализацию.",
    "bad_json" to "Некорректный JSON в запросе.",
    "bad_jwt" to "Некорректный JWT-токен.",
    "bad_oauth_callback" to "Ошибка OAuth-колбэка. Проверьте настройки OAuth-провайдера.",
    "bad_oauth_state" to "Некорректное состояние OAuth.",
    "captcha_failed" to "Ошибка капчи. Проверьте её интеграцию.",
    "conflict" to "Конфликт в базе данных. Попробуйте ещё раз.",
    "email_address_invalid" to "Некорректный адрес электронной почты.",
    "email_address_not_authorized" to "Отправка email запрещена для этого адреса.",
    "email_exists" to "Этот email уже используется.",
    "email_not_confirmed" to "Email не подтверждён.",
    "invalid_credentials" to "Неверный логин или пароль.",
    "network_request_failed" to "Ошибка сети. Проверьте подключение.",
    "otp_disabled" to "Авторизация через OTP отключена.",
    "otp_expired" to "OTP-код устарел. Запросите новый.",
    "over_request_rate_limit" to "Слишком много запросов. Подождите немного.",
    "phone_exists" to "Этот номер телефона уже зарегистрирован.",
    "phone_not_confirmed" to "Телефон не подтверждён.",
    "provider_disabled" to "OAuth-провайдер отключён.",
    "session_expired" to "Сессия истекла. Войдите снова.",
    "signup_disabled" to "Регистрация новых пользователей отключена.",
    "sms_send_failed" to "Ошибка отправки SMS.",
    "user_already_exists" to "Пользователь уже существует.",
    "user_banned" to "Этот пользователь заблокирован.",
    "user_not_found" to "Пользователь не найден.",
    "weak_password" to "Пароль слишком слабый. Выберите более сложный."
)

fun translateError(errorCode: String?, defaultMessage: String?): String {
    return errorTranslations[errorCode] ?: defaultMessage ?: "Неизвестная ошибка."
}