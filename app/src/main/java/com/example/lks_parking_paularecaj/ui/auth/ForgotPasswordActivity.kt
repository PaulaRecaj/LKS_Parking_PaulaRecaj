package com.example.lks_parking_paularecaj.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lks_parking_paularecaj.ParkingApplication
import com.example.lks_parking_paularecaj.databinding.ActivityForgotPasswordBinding
import com.example.lks_parking_paularecaj.ui.ViewModelFactory

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(authRepository = (application as ParkingApplication).authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString()

            // Limpiar errores
            binding.tilEmail.error = null
            viewModel.clearErrors()

            // Solicitar recuperación
            viewModel.forgotPassword(email)
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.forgotPasswordSuccess.observe(this) { success ->
            if (success) {
                // Mostrar mensaje de éxito
                binding.cardSuccess.visibility = View.VISIBLE

                // Limpiar el campo de email
                binding.etEmail.text?.clear()

                // Opcional: cerrar la activity después de 3 segundos
                binding.root.postDelayed({
                    finish()
                }, 3000)
            }
        }

        viewModel.forgotPasswordError.observe(this) { error ->
            error?.let {
                binding.cardSuccess.visibility = View.GONE
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()

                if (it.contains("email", ignoreCase = true)) {
                    binding.tilEmail.error = it
                }
            }
        }

        viewModel.loading.observe(this) { loading ->
            binding.btnSubmit.isEnabled = !loading
            binding.btnSubmit.text = if (loading) "Enviando..." else "Enviar instrucciones"
        }
    }
}
