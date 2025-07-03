package org.example.expert.domain.todo.repository

import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.entity.Todo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface CustomTodoRepository {
    fun findTodoById(todoId : Long) : Todo?
    fun searchTodo(
        pageable : Pageable,
        searchTitle : String?,
        searchManager : String?,
        start : LocalDateTime?,
        end : LocalDateTime?
    ) : Page<TodoSearchResponse>
}