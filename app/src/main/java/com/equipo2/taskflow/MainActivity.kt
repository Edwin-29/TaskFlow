package com.equipo2.taskflow

// ==========================================
// BLOQUE 1: Importaciones Optimizadas (Nivel Superior)
// ==========================================
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo2.taskflow.ui.theme.TaskFlowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource

// ==========================================
// MOCK API: Modelo de Datos de AcademicTask
// ==========================================
data class AcademicTask(
    val id: String,
    val category: String, // Ej: "Simulacro", "Colegio", "Academia"
    val taskDescription: String,
    val dueDate: String,
    val isOverdue: Boolean,
    val isUrgent: Boolean = false,
    val status: String = "Pendiente"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                // 1. Estados de Navegación y Selección
                var currentScreen by remember { mutableStateOf("login") }
                var selectedTaskId by remember { mutableStateOf("") }
                var isNetworkLoading by remember { mutableStateOf(false) }

                // 2. Nuestra "Base de datos" local simulada (Mock API) actualizada
                var tasksList by remember { mutableStateOf(listOf<AcademicTask>()) }

                // 3. Herramientas para animaciones de carga y mensajes flotantes (Snackbar)
                val coroutineScope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = {
                                    coroutineScope.launch {
                                        isNetworkLoading = true
                                        delay(1500)
                                        // Lista adaptada al nuevo público objetivo
                                        tasksList = listOf(
                                            AcademicTask(id = "1", category = "Simulacro tipo Admisión", taskDescription = "Examen General de Ciencias", dueDate = "Hoy, 23:59", isOverdue = true),
                                            AcademicTask(id = "2", category = "Seminario de Letras", taskDescription = "Repaso de Habilidad Verbal", dueDate = "Mañana, 23:59", isOverdue = false, isUrgent = true),
                                            AcademicTask(id = "3", category = "Colegio - Matemáticas", taskDescription = "Práctica de Trigonometría", dueDate = "15 Nov, 23:59", isOverdue = false)
                                        )
                                        isNetworkLoading = false
                                        currentScreen = "dashboard"
                                    }
                                },
                                onNavigateToRegister = { currentScreen = "register" },
                                onForgotPassword = { currentScreen = "forgotPassword" }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackToLogin = { currentScreen = "login" },
                                onRegisterSuccess = {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Cuenta creada con éxito") }
                                    currentScreen = "login"
                                }
                            )
                        }
                        "forgotPassword" -> {
                            ForgotPasswordScreen(
                                modifier = Modifier.padding(innerPadding),
                                onBackToLogin = { currentScreen = "login" },
                                onSendResetLink = { email ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Enlace enviado a $email") }
                                    currentScreen = "login"
                                }
                            )
                        }
                        "dashboard" -> {
                            DashboardScreen(
                                modifier = Modifier.padding(innerPadding),
                                tasks = tasksList,
                                isLoading = isNetworkLoading,
                                onTaskClick = { taskId ->
                                    selectedTaskId = taskId
                                    currentScreen = "detail"
                                }
                            )
                        }
                        "detail" -> {
                            val taskDetails = tasksList.find { it.id == selectedTaskId }
                            if (taskDetails != null) {
                                TaskDetailScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    task = taskDetails,
                                    onBackClick = { currentScreen = "dashboard" },
                                    onSubmitTask = {
                                        coroutineScope.launch {
                                            delay(1500)
                                            tasksList = tasksList.map {
                                                if (it.id == selectedTaskId) it.copy(status = "Entregado") else it
                                            }
                                            currentScreen = "dashboard"
                                            snackbarHostState.showSnackbar("¡Tarea enviada con éxito!")
                                        }
                                    },
                                    showError = { mensaje ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(mensaje) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// BLOQUE 2: Sistema de Autenticación Preuniversitario (Login, Registro, Recuperación)
// ==========================================
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun validateAndLogin() {
        val allowedDomains = listOf("@gmail.com", "@outlook.com", "@hotmail.com")
        val isValidDomain = allowedDomains.any { email.trim().endsWith(it, ignoreCase = true) }

        if (!isValidDomain) {
            errorMessage = "Solo permitimos correos de Gmail, Outlook o Hotmail"
            return
        }
        if (password.length < 8) {
            errorMessage = "La contraseña requiere mínimo 8 caracteres"
            return
        }
        // Nota: La conexión real a Firebase se implementará aquí para rechazar correos no registrados
        errorMessage = ""
        onLoginSuccess()
    }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp).background(Color(0xFFF8F9FA)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "TaskFlow", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3949AB))
        Text(text = "Tu Organizador Preuniversitario", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = { Text("Correo Electrónico") },
            placeholder = { Text("ejemplo@gmail.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3949AB),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "🙈" else "👁️",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(8.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¿Olvidaste tu contraseña?",
            color = Color(0xFF3949AB),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.End).clickable { onForgotPassword() }.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { validateAndLogin() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A))
        ) {
            Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "¿No tienes cuenta? ", color = Color.Gray)
            Text(
                text = "Regístrate aquí",
                color = Color(0xFF3949AB),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, onBackToLogin: () -> Unit, onRegisterSuccess: () -> Unit) {
    val scrollState = rememberScrollState()

    // Datos personales
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    // Selector de Nivel
    var expandedLevel by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf("") }
    val nivelesAcademia = listOf("Ciclo Anual", "Ciclo Semestral", "Ciclo Repaso", "Ciclo Verano")

    // Credenciales
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val hasLength = password.length >= 8

    val strength = listOf(hasUppercase, hasNumber, hasSpecial, hasLength).count { it } / 4f
    val strengthColor = when {
        strength <= 0.25f -> Color(0xFFFF5252)
        strength <= 0.75f -> Color(0xFFFFA726)
        else -> Color(0xFF26A69A)
    }

    fun validateAndRegister() {
        if (nombres.isBlank() || apellidos.isBlank() || selectedLevel.isBlank()) {
            errorMessage = "Por favor, completa todos tus datos personales"
            return
        }
        val allowedDomains = listOf("@gmail.com", "@outlook.com", "@hotmail.com")
        if (!allowedDomains.any { email.trim().endsWith(it, ignoreCase = true) }) {
            errorMessage = "Solo se permite registro con Gmail, Outlook o Hotmail"
            return
        }
        if (strength < 1f) {
            errorMessage = "La contraseña no cumple los requisitos mínimos"
            return
        }
        errorMessage = ""
        onRegisterSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Crear Cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombres,
            onValueChange = { nombres = it; errorMessage = "" },
            label = { Text("Nombres completos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it; errorMessage = "" },
            label = { Text("Apellidos completos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Selector Desplegable de Academia
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecciona tu Ciclo de Preparación") },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                        contentDescription = "Desplegar",
                        modifier = Modifier.clickable { expandedLevel = !expandedLevel }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedLevel = true },
                shape = RoundedCornerShape(16.dp)
            )
            DropdownMenu(
                expanded = expandedLevel,
                onDismissRequest = { expandedLevel = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                nivelesAcademia.forEach { nivel ->
                    DropdownMenuItem(
                        text = { Text(nivel) },
                        onClick = {
                            selectedLevel = nivel
                            expandedLevel = false
                            errorMessage = ""
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = { Text("Correo Electrónico") },
            placeholder = { Text("ejemplo@gmail.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Crea una Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "🙈" else "👁️",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(8.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = strengthColor,
            trackColor = Color(0xFFE0E0E0),
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Requisitos de seguridad:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(text = "${if (hasLength) "✅" else "❌"} Mínimo 8 caracteres", style = MaterialTheme.typography.bodySmall)
            Text(text = "${if (hasUppercase) "✅" else "❌"} Al menos una mayúscula", style = MaterialTheme.typography.bodySmall)
            Text(text = "${if (hasNumber) "✅" else "❌"} Al menos un número", style = MaterialTheme.typography.bodySmall)
            Text(text = "${if (hasSpecial) "✅" else "❌"} Un carácter especial (!@#\$%)", style = MaterialTheme.typography.bodySmall)
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { validateAndRegister() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
        ) {
            Text("Registrarse", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Volver al Login", color = Color.Gray, modifier = Modifier.clickable { onBackToLogin() }.padding(16.dp))
    }
}

@Composable
fun ForgotPasswordScreen(modifier: Modifier = Modifier, onBackToLogin: () -> Unit, onSendResetLink: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    fun validateAndSend() {
        val allowedDomains = listOf("@gmail.com", "@outlook.com", "@hotmail.com")
        if (!allowedDomains.any { email.trim().endsWith(it, ignoreCase = true) }) {
            errorMessage = "Ingresa un correo válido (Gmail, Outlook o Hotmail)"
            return
        }
        errorMessage = ""
        onSendResetLink(email)
    }

    Column(modifier = modifier.fillMaxSize().padding(32.dp).background(Color(0xFFF8F9FA)), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Recuperar Acceso", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ingresa tu correo para enviarte un enlace de recuperación seguro.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = { Text("Correo Electrónico") },
            placeholder = { Text("ejemplo@gmail.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color(0xFFFF5252), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { validateAndSend() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A))
        ) {
            Text("Enviar Enlace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Volver al Login", color = Color.Gray, modifier = Modifier.clickable { onBackToLogin() }.padding(16.dp))
    }
}

// ==========================================
// BLOQUE 3: Pantalla Principal (Dashboard)
// ==========================================
@Composable
fun DashboardScreen(modifier: Modifier = Modifier, tasks: List<AcademicTask>, isLoading: Boolean, onTaskClick: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val tareasPendientes = tasks.filter { it.status == "Pendiente" }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Saludo
        Text(text = "Hola, Mateo 👋", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2C3E50))
        Text(text = "Modo Offline Activo - Sincronización Local", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        SummaryCard(title = "Entregas Pendientes", count = tareasPendientes.size.toString(), color = Color(0xFFFF6B6B))
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Tus próximas entregas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
        Spacer(modifier = Modifier.height(16.dp))

        // Lógica de Vistas (Cargando / Vacío / Lista)
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF5C6BC0))
            }
        } else if (tareasPendientes.isEmpty()) {
            // ESTADO VACÍO (Empty State)
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "¡Estás al día!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF26A69A))
                Text(text = "No tienes tareas pendientes.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        } else {
            // LISTA DE TAREAS
            tareasPendientes.forEach { task ->
                TaskCard(
                    courseName = task.category, // Actualizado: Pasa la categoría en lugar del curso universitario
                    taskDescription = task.taskDescription,
                    dueDate = task.dueDate,
                    isOverdue = task.isOverdue,
                    isUrgent = task.isUrgent,
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// BLOQUE 4: Pantalla de Detalle de Tarea (File Picker + Acciones)
// ==========================================
@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    task: AcademicTask, // Actualizado al nuevo modelo
    onBackClick: () -> Unit,
    onSubmitTask: () -> Unit,
    showError: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileName = uri.lastPathSegment?.substringAfterLast("/") ?: "documento_adjunto.pdf"
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "⬅ Volver",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF5C6BC0),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = task.taskDescription,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = task.category, // Actualizado a la nueva propiedad
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5C6BC0),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Estado:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(text = task.status, style = MaterialTheme.typography.bodyMedium, color = if (task.status == "Entregado") Color(0xFF26A69A) else Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Vence:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(text = task.dueDate, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recursos de la actividad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA)),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📄", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Material_de_Estudio.pdf",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF34495E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedFileName != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✅ Archivo: ", fontWeight = FontWeight.Bold, color = Color(0xFF26A69A))
                    Text(
                        text = selectedFileName!!,
                        color = Color.DarkGray,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Quitar",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { selectedFileName = null }
                            .padding(start = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (selectedFileName != null) {
                            onSubmitTask()
                        } else {
                            showError("⚠️ Debes cargar un archivo primero")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A))
                ) {
                    Text(
                        text = "Enviar Tarea",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
            }
        } else {
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0))
            ) {
                Text(
                    text = "Cargar un archivo",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==========================================
// BLOQUE 5: Componentes Auxiliares
// ==========================================
@Composable
fun SummaryCard(title: String, count: String, color: Color) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = count, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun TaskCard(
    courseName: String,
    taskDescription: String,
    dueDate: String,
    isOverdue: Boolean,
    isUrgent: Boolean = false,
    onClick: () -> Unit
) {
    val statusColor = when {
        isOverdue -> Color(0xFFFF5252)
        isUrgent -> Color(0xFFFFA726)
        else -> Color(0xFF26A69A)
    }

    val statusIndicator = when {
        isOverdue -> "🔴"
        isUrgent -> "🟠"
        else -> "🟢"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text(text = courseName, style = MaterialTheme.typography.titleSmall, color = Color(0xFF5C6BC0), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = taskDescription, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF34495E), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = statusIndicator, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Vence: $dueDate", style = MaterialTheme.typography.labelLarge, color = statusColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}