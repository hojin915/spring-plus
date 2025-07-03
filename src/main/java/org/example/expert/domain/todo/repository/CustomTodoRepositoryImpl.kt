package org.example.expert.domain.todo.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.example.expert.domain.comment.entity.QComment
import org.example.expert.domain.manager.entity.QManager
import org.example.expert.domain.todo.dto.response.TodoSearchResponse
import org.example.expert.domain.todo.entity.QTodo
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.user.entity.QUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class CustomTodoRepositoryImpl(
    private val jpaQueryFactory : JPAQueryFactory
) : CustomTodoRepository{
    override fun findTodoById(todoId: Long): Todo? {
        val todo : QTodo = QTodo.todo
        val user : QUser = QUser.user

        return jpaQueryFactory
            .selectFrom(todo)
            .join(todo.user, user).fetchJoin()
            .where(todo.id.eq(todoId))
            .fetchOne()
    }

    override fun searchTodo(
        pageable: Pageable,
        searchTitle: String?,
        searchManager: String?,
        start: LocalDateTime?,
        end: LocalDateTime?
    ): Page<TodoSearchResponse> {
        val todo : QTodo = QTodo.todo
        val manager : QManager = QManager.manager
        val comment : QComment = QComment.comment

        // start, end 없을 경우 임의 값 지정
        val startTime = start ?: LocalDateTime.MIN
        val endTime = end ?: LocalDateTime.MAX

        val builder : BooleanBuilder = BooleanBuilder()

        builder.and(todo.createdAt.between(startTime, endTime))

        searchTitle?.let {
            builder.and(todo.title.containsIgnoreCase(searchTitle))
        }
        searchManager?.let {
            builder.and(todo.title.containsIgnoreCase(searchManager))
        }

        // 조건에 맞는 Todos
        val todos : List<Todo> = jpaQueryFactory
            .select(todo)
            .from(todo)
            .where(builder)
            .orderBy(todo.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        // Page 전환용 전체 개수
        val total : Long = jpaQueryFactory
            .select(todo.count())
            .from(todo)
            .where(builder)
            .fetchOne() ?: 0L

        // Todos Id 추출
        val todoIds : List<Long> =  todos.mapNotNull{it.id}

        // todoIds 기준 manager 수 카운트 및 매핑
        val managerCountMap : Map<Long, Long> = jpaQueryFactory
            .select(manager.todo.id, manager.count())
            .from(manager)
            .where(manager.todo.id.`in`(todoIds))
            .groupBy(manager.todo.id)
            .fetch()
            .associate {tuple ->
                val key = tuple.get(0, Long::class.java)!!
                val value = tuple.get(1, Long::class.java) ?: 0L
                key to value
            }

        // todoIds 기준 comment 수 카운트 및 매핑
        val commentCountMap : Map<Long, Long> = jpaQueryFactory
            .select(comment.todo.id, comment.count())
            .from(comment)
            .where(comment.todo.id.`in`(todoIds))
            .groupBy(comment.todo.id)
            .fetch()
            .associate {tuple ->
                val key = tuple.get(0, Long::class.java)!!
                val value = tuple.get(1, Long::class.java) ?: 0L
                key to value
            }

        // title, managerCount, commentCount -> responseDto 매핑
        val responseList : List<TodoSearchResponse> = todos.map{
            t -> TodoSearchResponse(
                t.title,
                managerCountMap.getOrDefault(t.id, 0L).toInt(),
                commentCountMap.getOrDefault(t.id, 0L).toInt()
            )}

        return PageImpl(responseList, pageable, total)
    }
}