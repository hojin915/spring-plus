package org.example.expert.domain.todo.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.common.dto.CustomUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.QUser;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    private final JPAQueryFactory jpaQueryFactory;

    public TodoSaveResponse saveTodo(CustomUser customUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromCustomUser(customUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDateTime start, LocalDateTime end) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // start, end 없을 경우 임의 값 지정
        if(start == null){
            start = LocalDateTime.MIN;
        }
        if(end == null){
            end = LocalDateTime.MAX;
        }

        // 날씨 Param 이 없으면 기존 쿼리에 수정일 범위만 적용
        // 날씨 Param 있으면 필터링 적용된 쿼리 사용
        Page<Todo> todos;
        if(weather == null){
            todos = todoRepository.findAllByModifiedAtBetween(pageable, start, end);
        } else{
            todos = todoRepository.findAllByWeatherAndModifiedAtBetween(pageable, weather, start, end);
        }

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        // QTodo -> Todo 로 쿼리 결과 받기
         Todo resultTodo = jpaQueryFactory
                .selectFrom(todo)
                .join(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

         // 예외처리
        if(resultTodo == null) {
            throw new InvalidRequestException("Todo not found");
        }

        User resultUser = resultTodo.getUser();

        return new TodoResponse(
                resultTodo.getId(),
                resultTodo.getTitle(),
                resultTodo.getContents(),
                resultTodo.getWeather(),
                new UserResponse(resultUser.getId(), resultUser.getEmail()),
                resultTodo.getCreatedAt(),
                resultTodo.getModifiedAt()
        );
    }

    public Page<TodoSearchResponse> searchTodo(int page, int size, String searchTitle, String searchManager, LocalDateTime start, LocalDateTime end) {
        Pageable pageable = PageRequest.of(page - 1, size);

        QTodo todo = QTodo.todo;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;

        // start, end 없을 경우 임의 값 지정
        if(start == null){
            start = LocalDateTime.MIN;
        }
        if(end == null){
            end = LocalDateTime.MAX;
        }

        // BooleanBuilder 로 WHERE 조건 설정
        BooleanBuilder builder  = new BooleanBuilder();

        builder.and(todo.createdAt.between(start, end));

        if(searchTitle != null && !searchTitle.isBlank()){
            builder.and(todo.title.containsIgnoreCase(searchTitle));
        }

        if(searchManager != null && !searchManager.isBlank()){
            builder.and(todo.managers.any().user.nickname.containsIgnoreCase(searchManager));
        }

        // 조건에 맞는 Todos
        List<Todo> todos = jpaQueryFactory
                .select(todo)
                .from(todo)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Page 전환용 전체 개수
        Long total = Optional.ofNullable(
                jpaQueryFactory
                    .select(todo.count())
                    .from(todo)
                    .where(builder)
                    .fetchOne()
        ).orElse(0L);

        // Todos Id 추출
        List<Long> todoIds = todos.stream()
                .map(Todo::getId)
                .toList();

        // todoIds 기준 manager 수 카운트 및 매핑
        Map<Long, Long> managerCountMap = jpaQueryFactory
                .select(manager.todo.id, manager.count())
                .from(manager)
                .where(manager.todo.id.in(todoIds))
                .groupBy(manager.todo.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Long.class),
                        tuple -> Optional.ofNullable(tuple.get(1, Long.class)).orElse(0L)
                ));

        // todoIds 기준 comment 수 카운트 및 매핑
        Map<Long, Long> commentCountMap = jpaQueryFactory
                .select(comment.todo.id, comment.count())
                .from(comment)
                .where(comment.todo.id.in(todoIds))
                .groupBy(comment.todo.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Long.class),
                        tuple -> Optional.ofNullable(tuple.get(1, Long.class)).orElse(0L)
                ));

        // title, managerCount, commentCount -> ResponseDto 매핑
        List<TodoSearchResponse> responseList = todos.stream()
                .map(t -> new TodoSearchResponse(
                            t.getTitle(),
                            managerCountMap.getOrDefault(t.getId(), 0L).intValue(),
                            commentCountMap.getOrDefault(t.getId(), 0L).intValue()
                ))
                .toList();

        // 페이징 해서 리턴
        return new PageImpl<>(responseList, pageable, total);
    }
}
