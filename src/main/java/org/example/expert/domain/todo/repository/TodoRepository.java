package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 변수명 짧게 변경
    // Page + Fetch Join -> EntityGraph
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Todo t " +
            "WHERE t.modifiedAt > :start AND t.modifiedAt < :end " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByModifiedAtBetween(
            Pageable pageable,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Weather 필터링 쿼리 분리
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Todo t " +
            "WHERE t.weather = :weather AND t.modifiedAt > :start AND t.modifiedAt < :end " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByWeatherAndModifiedAtBetween(
            Pageable pageable,
            @Param("weather") String weather,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
