package com.example.bi4.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bi4.R
import com.example.bi4.data.Student
import com.example.bi4.viewmodel.ScreenState
import com.example.bi4.viewmodel.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApp(modifier: Modifier = Modifier, viewModel: StudentViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val students by viewModel.students.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    val showDeleteDialog by viewModel.showDeleteConfirmation.collectAsState()
    val showEditDialogOpen by viewModel.showEditConfirmation.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            ScreenState.LIST -> stringResource(R.string.student_list)
                            ScreenState.DETAIL -> stringResource(R.string.student_detail)
                            ScreenState.ADD_EDIT -> if (isEditing) stringResource(R.string.edit_student) else stringResource(R.string.add_student)
                        }
                    )
                },
                navigationIcon = {
                    if (currentScreen != ScreenState.LIST) {
                        IconButton(onClick = { viewModel.navigateToList() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (currentScreen == ScreenState.LIST) {
                FloatingActionButton(onClick = { viewModel.navigateToAdd() }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_student))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                ScreenState.LIST -> StudentListScreen(
                    students = students,
                    onItemClick = { student -> viewModel.navigateToDetail(student) },
                    onDeleteClick = { student -> viewModel.requestDelete(student.id) },
                    onEditClick = { student -> viewModel.navigateToEdit(student) }
                )
                ScreenState.DETAIL -> selectedStudent?.let { student ->
                    StudentDetailScreen(
                        student = student,
                        onEditClick = { viewModel.navigateToEdit(student) },
                        onDeleteClick = { viewModel.requestDelete(student.id) }
                    )
                }
                ScreenState.ADD_EDIT -> StudentFormScreen(
                    student = if (isEditing) selectedStudent else null,
                    isEditing = isEditing,
                    onSave = { student -> viewModel.saveStudent(student) },
                    onCancel = { viewModel.navigateToList() }
                )
            }
        }

        // Delete Confirmation Dialog: "Bạn có muốn xóa thông tin SV không?"
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteConfirmation() },
                title = { Text(stringResource(R.string.delete)) },
                text = { Text(stringResource(R.string.confirm_delete)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDelete() }) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
        }

        // Edit Confirmation Dialog: "Bạn có muốn sửa thông tin SV không?"
        if (showEditDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissEditConfirmation() },
                title = { Text(stringResource(R.string.edit)) },
                text = { Text(stringResource(R.string.confirm_edit)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmEdit() }) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissEditConfirmation() }) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
        }
    }
}

@Composable
fun StudentListScreen(
    students: List<Student>,
    onItemClick: (Student) -> Unit,
    onDeleteClick: (Student) -> Unit,
    onEditClick: (Student) -> Unit
) {
    if (students.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.empty_list),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(student) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        if (student.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = student.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = student.fullName, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${stringResource(R.string.student_id)}: ${student.mssv}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(onClick = { onEditClick(student) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { onDeleteClick(student) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDetailScreen(
    student: Student,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar
        if (student.avatarUrl.isNotBlank()) {
            AsyncImage(
                model = student.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = stringResource(R.string.full_name), value = student.fullName)
                HorizontalDivider()
                DetailRow(label = stringResource(R.string.student_id), value = student.mssv)
                HorizontalDivider()
                DetailRow(label = stringResource(R.string.email), value = student.email)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.edit))
            }

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.delete))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun StudentFormScreen(
    student: Student?,
    isEditing: Boolean,
    onSave: (Student) -> Unit,
    onCancel: () -> Unit
) {
    var mssv by remember { mutableStateOf(student?.mssv ?: "") }
    var fullName by remember { mutableStateOf(student?.fullName ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var avatarUrl by remember { mutableStateOf(student?.avatarUrl ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val errorEmptyFields = stringResource(R.string.error_empty_fields)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUrl = it.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = mssv,
            onValueChange = { mssv = it },
            label = { Text(stringResource(R.string.student_id)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(stringResource(R.string.full_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = avatarUrl,
            onValueChange = { avatarUrl = it },
            label = { Text(stringResource(R.string.avatar_url)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pick_image))
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel))
            }

            Button(
                onClick = {
                    if (mssv.isBlank() || fullName.isBlank() || email.isBlank()) {
                        errorMessage = errorEmptyFields
                    } else {
                        errorMessage = null
                        val updatedStudent = Student(
                            id = student?.id ?: 0,
                            mssv = mssv.trim(),
                            fullName = fullName.trim(),
                            email = email.trim(),
                            avatarUrl = avatarUrl.trim()
                        )
                        onSave(updatedStudent)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
