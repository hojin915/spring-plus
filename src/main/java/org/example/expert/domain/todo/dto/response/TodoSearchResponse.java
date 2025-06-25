package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchResponse {
    private final String title;
    private final Integer managerCount;
    private final Integer commentCount;

    public TodoSearchResponse(String title, Integer managerCount, Integer commentCount) {
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}
