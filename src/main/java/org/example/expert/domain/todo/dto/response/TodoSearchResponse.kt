package org.example.expert.domain.todo.dto.response

data class TodoSearchResponse (
    val title : String,
    val managerCount : Int,
    val commentCount : Int
)