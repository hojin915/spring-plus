package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserImageResponseDto {
    private String url;

    public UserImageResponseDto(String url) {
        this.url = url;
    }
}
