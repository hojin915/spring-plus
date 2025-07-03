package org.example.expert.domain.todo.repository

import org.example.expert.domain.todo.entity.Todo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface TodoRepository : JpaRepository<Todo, Long>, CustomTodoRepository {

    // 변수명 짧게 변경
    // Page + Fetch Join -> EntityGraph
    @EntityGraph(attributePaths = ["user"])
    @Query(
        "SELECT t FROM Todo t " +
        "WHERE t.modifiedAt > :start AND t.modifiedAt < :end " +
        "ORDER BY t.modifiedAt DESC"
    )
    fun findAllByModifiedAtBetween(
        pageable : Pageable,
        @Param("start") start : LocalDateTime,
        @Param("end") end : LocalDateTime
    ) : Page<Todo>

    // Weather 필터링 쿼리 분리
    @EntityGraph(attributePaths = ["user"])
    @Query(
        "SELECT t FROM Todo t " +
        "WHERE t.weather = :weather AND t.modifiedAt > :start AND t.modifiedAt < :end " +
        "ORDER BY t.modifiedAt DESC"
    )
    fun findAllByWeatherAndModifiedAtBetween(
        pageable : Pageable,
        @Param("weather") weather: String,
        @Param("start") start : LocalDateTime,
        @Param("end") end : LocalDateTime
    ) : Page<Todo>
}