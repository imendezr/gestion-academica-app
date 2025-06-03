package com.example.gestionacademicaapp.ui.common.state

sealed class ActionState {
    data object Success : ActionState()
    data class ValidationError(val message: String) : ActionState()
    data class DependencyError(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}
