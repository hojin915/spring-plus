package org.example.expert.domain.todo.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.CustomUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.QUser;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        // 시작일은 현재시간 기준 하루 전
        if(start == null){
            start = LocalDateTime.now().minusDays(1);
        }
        // 종료일은 현재시간
        if(end == null){
            end = LocalDateTime.now();
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
}
