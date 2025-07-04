package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.imageUrlKey = :imageUrl WHERE u.id = :userId")
    void updateImage(@Param("userId") Long userId, @Param("imageUrl") String imgUrl);

    @Query("SELECT new org.example.expert.domain.user.dto.response.UserSearchResponse(u.id, u.nickname, u.email) " +
            "FROM User u " +
            "WHERE u.nickname = :nickname")
    Page<UserSearchResponse> findAllByNickname(@Param("nickname")String nickname, Pageable pageable);
}
