package com.lksnext.ParkingPRecaj.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lksnext.ParkingPRecaj.ParkingApplication
import com.lksnext.ParkingPRecaj.databinding.ActivityLoginBinding
import com.lksnext.ParkingPRecaj.ui.ViewModelFactory
import com.lksnext.ParkingPRecaj.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(authRepository = (application as ParkingApplication).authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }

        viewModel.loginError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loading.observe(this) { loading ->
            binding.btnLogin.isEnabled = !loading
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "El email es requerido"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña es requerida"
            return false
        }
        return true
    }
}
