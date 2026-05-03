package com.example.lks_parking_paularecaj.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lks_parking_paularecaj.ParkingApplication
import com.example.lks_parking_paularecaj.databinding.ActivityRegisterBinding
import com.example.lks_parking_paularecaj.ui.ViewModelFactory
import com.example.lks_parking_paularecaj.ui.dashboard.DashboardActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(authRepository = (application as ParkingApplication).authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Limpiar errores previos
            clearErrors()

            // Registrar
            viewModel.register(name, email, password, confirmPassword)
        }

        binding.tvLogin.setOnClickListener {
            finish() // Volver a login
        }
    }

    private fun observeViewModel() {
        viewModel.registerSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                // Navegar al dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
                finishAffinity() // Cerrar todas las activities previas
            }
        }

        viewModel.registerError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()

                // Mostrar errores específicos en los campos
                when {
                    it.contains("nombre", ignoreCase = true) -> {
                        binding.tilName.error = it
                    }
                    it.contains("email", ignoreCase = true) -> {
                        binding.tilEmail.error = it
                    }
                    it.contains("contraseña", ignoreCase = true) -> {
                        binding.tilPassword.error = it
                    }
                    it.contains("coinciden", ignoreCase = true) -> {
                        binding.tilConfirmPassword.error = it
                    }
                }
            }
        }

        viewModel.loading.observe(this) { loading ->
            binding.btnRegister.isEnabled = !loading
            binding.btnRegister.text = if (loading) "Registrando..." else "Registrarse"
        }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        viewModel.clearErrors()
    }
}
