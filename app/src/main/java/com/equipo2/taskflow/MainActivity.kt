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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.equipo2.taskflow.ui.theme.TaskFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                // Navegación principal: Inicia en el Login
                var currentScreen by remember { mutableStateOf("login") }
                var selectedCourseName by remember { mutableStateOf("") }
                var selectedTaskTitle by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = { currentScreen = "dashboard" }
                            )
                        }
                        "dashboard" -> {
                            DashboardScreen(
                                modifier = Modifier.padding(innerPadding),
                                onTaskClick = { courseName, taskTitle ->
                                    selectedCourseName = courseName
                                    selectedTaskTitle = taskTitle
                                    currentScreen = "detail"
                                }
                            )
                        }
                        "detail" -> {
                            TaskDetailScreen(
                                modifier = Modifier.padding(innerPadding),
                                courseName = selectedCourseName,
                                taskTitle = selectedTaskTitle,
                                onBackClick = { currentScreen = "dashboard" }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// BLOQUE 2: Pantalla de Login (Validación de Seguridad)
// ==========================================
@Composable
fun LoginScreen(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados para validación
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Función de validación (Estándares de seguridad básica)
    fun validateAndLogin() {
        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}\$".toRegex()

        usernameError = !username.matches(emailPattern)
        passwordError = !password.matches(passwordPattern)

        if (usernameError) {
            errorMessage = "Ingrese un formato de correo válido (ej. usuario@ucv.edu.pe)"
            return
        }
        if (passwordError) {
            errorMessage = "La contraseña debe tener mín. 8 caracteres, letras y números"
            return
        }

        errorMessage = ""
        // Aquí se ejecutará la petición REST a Blackboard para obtener el Token
        onLoginSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TaskFlow",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5C6BC0)
        )
        Text(
            text = "Sincronizado con Blackboard",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = false
            },
            label = { Text("Correo Institucional") },
            isError = usernameError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = { Text("Contraseña") },
            isError = passwordError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val symbol = if (passwordVisible) "🙈" else "👁️"
                Text(
                    text = symbol,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(8.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Mensaje de error dinámico
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { validateAndLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
        ) {
            Text(
                text = "Iniciar Sesión",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
        }
    }
}

// ==========================================
// BLOQUE 3: Pantalla Principal (Dashboard)
// ==========================================
@Composable
fun DashboardScreen(modifier: Modifier = Modifier, onTaskClick: (String, String) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hola, Mateo 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2C3E50)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Sincronizado con Blackboard UCV",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SummaryCard(title = "Entregas Pendientes", count = "4", color = Color(0xFFFF6B6B))
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Tus próximas entregas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(16.dp))

        TaskCard(
            courseName = "COMPUTACIÓN III-148T1",
            taskDescription = "Cuestionario CAS Intro to Cybersecurity",
            dueDate = "Hoy, 23:59",
            isOverdue = true,
            onClick = { onTaskClick("COMPUTACIÓN III-148T1", "Cuestionario CAS Intro to Cybersecurity") }
        )
        TaskCard(
            courseName = "SISTEMAS INTELIGENTES-C1T1",
            taskDescription = "Actividades No Lectivas",
            dueDate = "Mañana, 23:59",
            isOverdue = false,
            isUrgent = true,
            onClick = { onTaskClick("SISTEMAS INTELIGENTES-C1T1", "Actividades No Lectivas") }
        )
        TaskCard(
            courseName = "CALIDAD DE SOFTWARE-C1T1",
            taskDescription = "Informe de Proyecto",
            dueDate = "15 Nov, 23:59",
            isOverdue = false,
            onClick = { onTaskClick("CALIDAD DE SOFTWARE-C1T1", "Informe de Proyecto") }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// BLOQUE 4: Pantalla de Detalle de Tarea (File Picker + Acciones)
// ==========================================
@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    courseName: String,
    taskTitle: String = "Evaluación Pendiente",
    taskPath: String = "Unidad X • Sesión Y",
    onBackClick: () -> Unit
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
            text = taskTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$courseName • $taskPath",
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
                    Text(text = "Pendiente", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF26A69A), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Puntuación máxima:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(text = "20 puntos", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, fontWeight = FontWeight.Bold)
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
                        text = "Guia_de_Laboratorio.docx",
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
                    onClick = { /* Lógica futura de envío */ },
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