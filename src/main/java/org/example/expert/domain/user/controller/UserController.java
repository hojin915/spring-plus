package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.CustomUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserImageResponse;
import org.example.expert.domain.user.dto.response.UserProfileResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal CustomUser user, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(user.getId(), userChangePasswordRequest);
    }

    // 토큰 nickname 삽입 확인용 프로필 조회
    @GetMapping("/users/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(
                new UserProfileResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getEmail(),
                        user.getUserRole()
                )
        );
    }

    @PostMapping("/users/image")
    public ResponseEntity<UserImageResponse> uploadImage(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(value = "image") MultipartFile image
    ){
        return ResponseEntity.ok(userService.uploadImage(user, image));
    }

    @GetMapping("/users/image")
    public ResponseEntity<UserImageResponse> getImage(
            @AuthenticationPrincipal CustomUser user
    ){
      return ResponseEntity.ok(userService.getImage(user));
    }

    @GetMapping("/users/search")
    public ResponseEntity<Page<UserSearchResponse>> searchUser(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String nickname
    ){
        LocalDateTime start = LocalDateTime.now();

        Page<UserSearchResponse> responsePage = userService.searchUser(page, size, nickname);

        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start, end);
        System.out.println(duration.toMillis());

        return ResponseEntity.ok(responsePage);
    }
}