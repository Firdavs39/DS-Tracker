package com.decoroomsteel.dstracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.decoroomsteel.dstracker.DSTrackerApplication
import com.decoroomsteel.dstracker.databinding.ActivityLoginBinding
import com.decoroomsteel.dstracker.ui.admin.AdminDashboardActivity
import com.decoroomsteel.dstracker.ui.worker.WorkerDashboardActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Экран входа в приложение
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    
    private val userRepository by lazy { 
        (application as DSTrackerApplication).userRepository 
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Инициализация Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Проверка, авторизован ли пользователь
        checkCurrentUser()
        
        // Обработчик кнопки входа
        binding.btnLogin.setOnClickListener {
            loginUser()
        }
        
        // Обработчик кнопки сброса пароля
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }
    
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // Пользователь уже авторизован, определяем роль и перенаправляем
            redirectBasedOnRole(currentUser.uid)
        }
    }
    
    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        // Валидация полей
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Пожалуйста, заполните все поля",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Показать индикатор загрузки
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        
        // Аутентификация пользователя
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                
                // Получение текущего пользователя
                val currentUser = auth.currentUser
                
                if (currentUser != null) {
                    // Определяем роль пользователя и перенаправляем
                    redirectBasedOnRole(currentUser.uid)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(
                            this@LoginActivity,
                            "Произошла ошибка при входе",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun redirectBasedOnRole(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получаем информацию о пользователе из локальной базы
                val user = userRepository.getUserById(userId)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    
                    if (user != null) {
                        // Проверяем роль и перенаправляем
                        if (user.isAdmin) {
                            // Если администратор, открываем панель администратора
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        } else {
                            // Если обычный сотрудник, открываем рабочую панель
                            startActivity(Intent(this@LoginActivity, WorkerDashboardActivity::class.java))
                        }
                        finish() // Закрываем экран входа
                    } else {
                        // Пользователь не найден в локальной базе
                        Toast.makeText(
                            this@LoginActivity,
                            "Ошибка: пользователь не найден в системе",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Выход из аккаунта, т.к. пользователь есть в Firebase, но нет в локальной базе
                        auth.signOut()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun showForgotPasswordDialog() {
        val dialogBinding = com.decoroomsteel.dstracker.databinding.DialogForgotPasswordBinding.inflate(layoutInflater)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Сброс пароля")
            .setView(dialogBinding.root)
            .setPositiveButton("Отправить") { _, _ ->
                val email = dialogBinding.etEmail.text.toString().trim()
                
                if (email.isNotEmpty()) {
                    resetPassword(email)
                } else {
                    Toast.makeText(
                        this,
                        "Введите email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun resetPassword(email: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@LoginActivity,
                        "Инструкции по сбросу пароля отправлены на $email",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}