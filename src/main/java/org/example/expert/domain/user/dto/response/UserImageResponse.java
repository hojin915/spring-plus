package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserImageResponse {
    private String key;
    private String url;

    public UserImageResponse(String key, String url) {
        this.key = key;
        this.url = url;
    }
}
