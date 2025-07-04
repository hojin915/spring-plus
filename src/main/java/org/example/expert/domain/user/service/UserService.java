package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.config.S3.S3Uploader;
import org.example.expert.domain.common.dto.CustomUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserImageResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    public UserImageResponse uploadImage(CustomUser customUser, MultipartFile image) {
        User user = User.fromCustomUser(customUser);
        String dirName = "profile-images";

        // PresignedUrl 리턴하기 위한 key
        String key = s3Uploader.getS3Key(dirName, user.getId(), image);

        // S3 업로드
        String publicUrl = s3Uploader.upload(image, dirName, user.getId());

        // DB 에 key + 확장자 형태로 저장한다
        userRepository.updateImage(user.getId(), key);

        String url = s3Uploader.generatePresignedUrl(key);

        return new UserImageResponse(key, url);
    }

    public UserImageResponse getImage(CustomUser customUser) {
        User user = userRepository.findById(customUser.getId()).
                orElseThrow(() -> new InvalidRequestException("User not found"));

        String key = user.getImageUrlKey();
        String url;
        if(key == null) {
            url = s3Uploader.generatePresignedUrl("profile-images/default.webp");
        } else {
            url = s3Uploader.generatePresignedUrl(user.getImageUrlKey());
        }

        return new UserImageResponse(key, url);
    }

    public Page<UserSearchResponse> searchUser(int page, int size, String nickname) {
        Pageable pageable = PageRequest.of(page - 1, size);

        return userRepository.findAllByNickname(nickname, pageable);
    }
}