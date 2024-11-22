package com.rtd.ej09login.screens.splash

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.rtd.ej09login.R
import com.rtd.ej09login.navigation.Screens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Animación de escala
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Configura la animación con un rebote
        scale.animateTo(
            targetValue = 0.8f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = { OvershootInterpolator(8f).getInterpolation(it) }
            )
        )

        // Espera 2 segundos antes de la navegación
        delay(2000)

        // Navega dependiendo del estado de autenticación
        if (FirebaseAuth.getInstance().currentUser?.email.isNullOrEmpty()) {
            navController.navigate(Screens.LoginScreen.name) {
                popUpTo(Screens.SplashScreen.name) { inclusive = true }
            }
        } else {
            navController.navigate(Screens.HomeScreen.name) {
                popUpTo(Screens.SplashScreen.name) { inclusive = true }
            }
        }
    }

    // Fondo oscuro completo y el icono de Discord
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .scale(scale.value)
            .background(Color(0xFF23272A)) // Fondo oscuro de Discord
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_discord), // Icono de Discord en PNG
            contentDescription = "Discord Logo",
            modifier = Modifier.size(120.dp) // Tamaño del icono
        )
    }
}
