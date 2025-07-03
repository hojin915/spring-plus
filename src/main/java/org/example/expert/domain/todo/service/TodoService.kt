package org.example.expert.domain.todo.service

import org.example.expert.client.WeatherClient
import org.example.expert.domain.common.dto.CustomUser
import org.example.expert.domain.common.exception.InvalidRequestException
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.todo.repository.TodoRepository
import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class TodoService(
    private val todoRepository : TodoRepository,
    private val weatherClient : WeatherClient
) {
    fun saveTodo(
        customUser : CustomUser,
        todoSaveRequest : TodoSaveRequest
    ) : TodoSaveResponse {
        val user : User = User.fromCustomUser(customUser)

        val weather : String = weatherClient.todayWeather

        val newTodo : Todo = Todo.create(
            todoSaveRequest.title,
            todoSaveRequest.contents,
            weather,
            user
        )
        val savedTodo : Todo = todoRepository.save(newTodo)

        return TodoSaveResponse(
            savedTodo.id,
            savedTodo.title,
            savedTodo.contents,
            weather,
            UserResponse(user.id, user.email)
        )
    }

    fun getTodos(
        page : Int,
        size : Int,
        weather : String?,
        start : LocalDateTime?,
        end : LocalDateTime?
    ) : Page<TodoResponse> {
        val pageable : Pageable = PageRequest.of(page - 1, size)

        // start, end 없을 경우 임의 값 지정
        val startTime = start ?: LocalDateTime.of(2000, 1, 1, 0, 0)
        val endTime = end ?: LocalDateTime.of(2100, 1, 1, 0, 0)

        // 날씨 Param 이 없으면 기존 쿼리에 수정일 범위만 적용
        // 날씨 Param 있으면 필터링 적용된 쿼리 사용
        val todos : Page<Todo> = when(weather){
            null -> todoRepository.findAllByModifiedAtBetween(pageable, startTime, endTime)
            else -> todoRepository.findAllByWeatherAndModifiedAtBetween(pageable, weather, startTime, endTime)
        }

        return todos.map{todo -> TodoResponse(
            todo.id,
            todo.title,
            todo.contents,
            todo.weather,
            UserResponse(todo.user.id, todo.user.email),
            todo.createdAt,
            todo.modifiedAt
        )}
    }

    fun getTodo(
        todoId : Long
    ) : TodoResponse {
        val todo : Todo = todoRepository.findTodoById(todoId)
            ?: throw InvalidRequestException("Todo not found");

        val resultUser : User = todo.user

        return TodoResponse(
            todo.id,
            todo.title,
            todo.contents,
            todo.weather,
            UserResponse(resultUser.id, resultUser.email),
            todo.createdAt,
            todo.modifiedAt
        )
    }

    fun searchTodo(
        page : Int,
        size : Int,
        searchTitle : String?,
        searchManager : String?,
        start : LocalDateTime?,
        end : LocalDateTime?
    ) : Page<TodoSearchResponse> {
        val pageable : Pageable = PageRequest.of(page - 1, size)

        return todoRepository.searchTodo(pageable, searchTitle, searchManager, start, end)
    }
}