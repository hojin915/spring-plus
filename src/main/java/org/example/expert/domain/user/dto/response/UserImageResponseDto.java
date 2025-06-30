package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserImageResponseDto {
    private String key;
    private String url;

    public UserImageResponseDto(String key, String url) {
        this.key = key;
        this.url = url;
    }
}
