package org.example.expert.domain.user.entity

import jakarta.persistence.*
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.common.dto.CustomUser
import org.example.expert.domain.common.entity.Timestamped
import org.example.expert.domain.user.enums.UserRole

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var nickname: String,
    @Column(unique = true)
    var email: String,
    var password: String = "",
    @Enumerated(EnumType.STRING)
    var userRole: UserRole,

    var imageUrlKey: String? = null
) : Timestamped() {
    companion object {
        @JvmStatic
        fun create(email : String, nickname : String, password : String, userRole : UserRole) : User {
            val user = User(null, email, nickname, password, userRole, null)
            return user
        }

        @JvmStatic
        fun fromAuthUser(authUser: AuthUser): User =
            User(id = authUser.id, nickname = authUser.nickname, email = authUser.email, userRole = authUser.userRole)

        @JvmStatic
        fun fromCustomUser(customUser: CustomUser): User = User(
            id = customUser.id,
            nickname = customUser.nickname,
            email = customUser.email,
            userRole = customUser.userRole
        )
    }

    fun changePassword(password: String) {
        this.password = password
    }

    fun updateRole(userRole: UserRole) {
        this.userRole = userRole
    }
}