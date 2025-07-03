package org.example.expert.domain.todo.controller

import jakarta.validation.Valid
import org.example.expert.domain.common.dto.CustomUser
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.service.TodoService
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
class TodoController(
    private val todoService: TodoService
) {
    @PostMapping("/todos")
    fun saveTodo(
        @AuthenticationPrincipal user : CustomUser,
        @Valid @RequestBody todoSaveRequest: TodoSaveRequest
    ) : ResponseEntity<TodoSaveResponse> {
        return ResponseEntity.ok(todoService.saveTodo(user, todoSaveRequest))
    }

    @GetMapping("/todos")
    fun getTodos(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) weather : String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime?
    ) : ResponseEntity<Page<TodoResponse>>{
        return ResponseEntity.ok(todoService.getTodos(page, size, weather, start, end))
    }

    @GetMapping("/todos/{todoId}")
    fun getTodo(
        @PathVariable todoId : Long
    ) : ResponseEntity<TodoResponse> {
        return ResponseEntity.ok(todoService.getTodo(todoId))
    }

    @GetMapping("/todos/search")
    fun searchTodo(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) searchTitle : String?,
        @RequestParam(required = false) searchManager : String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime?
    ) : ResponseEntity<Page<TodoSearchResponse>> {
        return ResponseEntity.ok(todoService.searchTodo(page, size, searchTitle, searchManager, start, end))
    }
}