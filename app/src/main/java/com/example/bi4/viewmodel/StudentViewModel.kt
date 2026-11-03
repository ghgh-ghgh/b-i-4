package com.example.bi4.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bi4.data.Student
import com.example.bi4.data.StudentDbHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenState {
    LIST,
    DETAIL,
    ADD_EDIT
}

class StudentViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = StudentDbHelper(application)

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenState.LIST)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    // Confirmation dialog states
    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    private val _showEditConfirmation = MutableStateFlow(false)
    val showEditConfirmation: StateFlow<Boolean> = _showEditConfirmation.asStateFlow()

    private var pendingStudent: Student? = null
    private var pendingDeleteId: Long? = null

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _students.value = dbHelper.getAllStudents()
        }
    }

    fun navigateToList() {
        _selectedStudent.value = null
        _currentScreen.value = ScreenState.LIST
    }

    fun navigateToDetail(student: Student) {
        _selectedStudent.value = student
        _currentScreen.value = ScreenState.DETAIL
    }

    fun navigateToAdd() {
        _selectedStudent.value = null
        _isEditing.value = false
        _currentScreen.value = ScreenState.ADD_EDIT
    }

    fun navigateToEdit(student: Student) {
        _selectedStudent.value = student
        _isEditing.value = true
        _currentScreen.value = ScreenState.ADD_EDIT
    }

    // Save student (Add or Edit)
    fun saveStudent(student: Student) {
        if (_isEditing.value) {
            // Request confirmation before editing: "Bạn có muốn sửa thông tin SV không?"
            pendingStudent = student
            _showEditConfirmation.value = true
        } else {
            // Adding new does not require confirmation dialog in requirements, or directly add
            viewModelScope.launch {
                dbHelper.insertStudent(student)
                loadStudents()
                navigateToList()
            }
        }
    }

    fun confirmEdit() {
        pendingStudent?.let { student ->
            viewModelScope.launch {
                dbHelper.updateStudent(student)
                loadStudents()
                // Update selected student if viewing detail
                _selectedStudent.value = student
                _currentScreen.value = ScreenState.DETAIL
            }
        }
        pendingStudent = null
        _showEditConfirmation.value = false
    }

    fun dismissEditConfirmation() {
        pendingStudent = null
        _showEditConfirmation.value = false
    }

    // Request delete with confirmation: "Bạn có muốn xóa thông tin SV không?"
    fun requestDelete(studentId: Long) {
        pendingDeleteId = studentId
        _showDeleteConfirmation.value = true
    }

    fun confirmDelete() {
        pendingDeleteId?.let { id ->
            viewModelScope.launch {
                dbHelper.deleteStudent(id)
                loadStudents()
                navigateToList()
            }
        }
        pendingDeleteId = null
        _showDeleteConfirmation.value = false
    }

    fun dismissDeleteConfirmation() {
        pendingDeleteId = null
        _showDeleteConfirmation.value = false
    }
}
