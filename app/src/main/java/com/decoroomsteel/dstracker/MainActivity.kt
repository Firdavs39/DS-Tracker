package com.decoroomsteel.dstracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.decoroomsteel.dstracker.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Главный экран приложения, который проверяет состояние авторизации
 * и направляет пользователя либо на экран входа, либо на соответствующую панель управления
 */
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Проверяем, авторизован ли пользователь
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Если пользователь уже авторизован, ему не нужно повторно входить
            // Перенаправляем на экран LoginActivity, который дальше определит роль
            // и направит на соответствующую панель (админ или работник)
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // Если пользователь не авторизован, направляем на экран входа
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Закрываем MainActivity, чтобы пользователь не мог вернуться назад кнопкой "Назад"
        finish()
    }
}