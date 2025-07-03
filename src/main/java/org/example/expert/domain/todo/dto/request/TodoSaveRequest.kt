package org.example.expert.domain.todo.dto.request

import jakarta.validation.constraints.NotBlank

data class TodoSaveRequest(
    @field:NotBlank
    val title : String,

    @field:NotBlank
    val contents : String
)