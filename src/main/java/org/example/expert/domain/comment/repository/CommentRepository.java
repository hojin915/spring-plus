package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // EntityGraph 로 user 테이블까지 join 해서 한번에 조회
    // Lazy 로딩으로 인한 n+1 문제 해결가능
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Comment c WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
