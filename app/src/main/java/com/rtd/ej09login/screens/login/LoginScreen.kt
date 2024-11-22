package com.rtd.ej09login.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rtd.ej09login.R
import com.rtd.ej09login.navigation.Screens
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LoginScreen(navController: NavController) {
    // Controla el formulario de inicio de sesión
    val token = "681602632257-31s9cgm6c7v53tbibmkql3tscft9aocf.apps.googleusercontent.com"
    val showLoginForm = rememberSaveable { mutableStateOf(true) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF23272A)),
        color = Color(0xFF23272A)// color de fondo similar a Discord
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Icono de Discord encima del texto
            Icon(
                painter = painterResource(id = R.drawable.ic_discord), // Aquí usas el ícono de Discord
                contentDescription = "Discord Icon",
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = Color.White // O el color que prefieras
            )

            Text(
                text = if (showLoginForm.value) "¡Bienvenido a Discord!" else "Crear una cuenta",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = if (showLoginForm.value) "¡Nos alegra verte de nuevo!" else "Únete a nosotros",
                color = Color(0xFFB9BBBE),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            UserForm(isCreateAccount = !showLoginForm.value) { email, password ->
                navController.navigate(Screens.HomeScreen.name)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text1 = if (showLoginForm.value) "¿No tienes cuenta?" else "¿Ya tienes cuenta?"
                val text2 = if (showLoginForm.value) "Regístrate" else "Iniciar sesión"
                Text(text = text1, color = Color.White)
                Text(
                    text = text2,
                    modifier = Modifier
                        .clickable { showLoginForm.value = !showLoginForm.value }
                        .padding(start = 5.dp),
                    color = Color(0xFF00AFF4) // Color azul para destacar el enlace
                )
            }
        }
    }
}

@Composable
fun UserForm(
    isCreateAccount: Boolean,
    onDone: (String, String) -> Unit = { _, _ -> }
) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }

    val isValid = remember(email.value, password.value) {
        email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }

    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailInput(emailState = email)
        PasswordInput(passwordState = password, passwordVisible = passwordVisible)
        SubmitButton(
            text = if (isCreateAccount) "Crear cuenta" else "Iniciar sesión",
            isEnabled = isValid
        ) {
            if (isCreateAccount) {
                createAccount(email.value.trim(), password.value.trim(), context, onDone) // Corregido
            } else {
                signIn(email.value.trim(), password.value.trim(), context, onDone)
            }
        }
    }
}

@Composable
fun SubmitButton(
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RectangleShape,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5865F2),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF4E5D94),
            disabledContentColor = Color(0xFFB9BBBE)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(5.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmailInput(emailState: MutableState<String>) {
    OutlinedTextField(
        value = emailState.value,
        onValueChange = { emailState.value = it },
        label = { Text("Email", color = Color(0xFFB9BBBE)) },
        singleLine = true,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
    )
}



fun createAccount(
    email: String,
    password: String,
    context: Context,
    onDone: (String, String) -> Unit // Agregado este parámetro
) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance() // Usar Firestore si prefieres
    val dbReference = database.reference.child("users") // Referencia en Realtime Database

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Obtener el usuario actual
                val user = auth.currentUser
                val userId = user?.uid

                if (userId != null) {
                    // Guardar datos del usuario en la base de datos
                    val userData = mapOf(
                        "email" to email,
                        "userId" to userId
                    )
                    dbReference.child(userId).setValue(userData)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(context, "Cuenta creada y datos guardados", Toast.LENGTH_LONG).show()
                                onDone(email, password) // Navegar automáticamente
                            } else {
                                Toast.makeText(context, "Error al guardar datos: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}


fun signIn(email: String, password: String, context: Context, onDone: (String, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_LONG).show()
                onDone(email, password) // Navega a la pantalla principal
            } else {
                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}


@Composable
fun PasswordInput(
    passwordState: MutableState<String>,
    passwordVisible: MutableState<Boolean>
) {
    OutlinedTextField(
        value = passwordState.value,
        onValueChange = { passwordState.value = it },
        label = { Text("Password", color = Color(0xFFB9BBBE)) },
        singleLine = true,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                Icon(
                    imageVector = if (passwordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color(0xFFB9BBBE)
                )
            }
        },
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
    )
}
