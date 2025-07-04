package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserSearchResponse {
    private Long id;
    private String nickname;
    private String email;

    public UserSearchResponse(Long id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}
