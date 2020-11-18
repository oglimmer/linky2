package de.oglimmer.linky.rest

import de.oglimmer.linky.dao.UserCrudRepository
import de.oglimmer.linky.entity.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/v1/users")
class UserController(private var repository: UserCrudRepository) {
    companion object {
        val SECRET_KEY = UUID.randomUUID().toString()
        const val TOKEN_LIFETIME = 1000 * 60 * 60
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody userRequestParam: UserRequestParam): Mono<UserResponse> = repository
            .save(User(UUID.randomUUID().toString(),
                    userRequestParam.email,
                    BCrypt.hashpw(userRequestParam.password, BCrypt.gensalt())))
            .map { createJwt(it) }

    @PostMapping("/auth")
    fun auth(@RequestBody userRequestParam: UserRequestParam): Mono<ResponseEntity<UserResponse>> = repository
            .findByEmail(userRequestParam.email)
            .filter { BCrypt.checkpw(userRequestParam.password, it.password) }
            .map { ResponseEntity.ok(createJwt(it)) }
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()))

    private fun createJwt(user: User) = UserResponse(
            Jwts.builder()
                    .setSubject(user.id)
                    .claim("email", user.email)
                    .setIssuedAt(Date())
                    .setExpiration(Date(System.currentTimeMillis() + TOKEN_LIFETIME))
                    .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                    .compact()
    )

}

data class UserRequestParam(
        val email: String,
        val password: String
)

data class UserResponse(
        val accessToken: String
)
