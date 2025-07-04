package org.example.expert.domain.todo.entity

import jakarta.persistence.*
import org.example.expert.domain.comment.entity.Comment
import org.example.expert.domain.common.entity.Timestamped
import org.example.expert.domain.manager.entity.Manager
import org.example.expert.domain.user.entity.User

@Entity
@Table(name = "todo")
class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,
    var title : String,
    var contents : String,
    var weather : String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user : User,

    @OneToMany(mappedBy = "todo", cascade = [CascadeType.REMOVE])
    val comments : MutableList<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "todo", cascade = [CascadeType.PERSIST])
    val managers : MutableList<Manager> = mutableListOf()
) : Timestamped() {
    companion object {
        @JvmStatic
        fun create(title: String, contents: String, weather: String, user: User) : Todo{
            val todo = Todo(
                title = title,
                contents = contents,
                weather = weather,
                user = user
            )
            todo.managers.add(Manager(user, todo))
            return todo
        }
    }
}