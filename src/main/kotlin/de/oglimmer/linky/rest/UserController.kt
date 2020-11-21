package de.oglimmer.linky.rest

import de.oglimmer.linky.logic.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/users")
class UserController(private var userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody userRequestParam: UserRequestParam): Mono<UserResponse> =
            userService.createUser(userRequestParam).map { UserResponse(it) }

    @PostMapping("/auth")
    fun auth(@RequestBody userRequestParam: UserRequestParam): Mono<ResponseEntity<UserResponse>> =
            userService.validateUserPassword(userRequestParam)
                    .map { ResponseEntity.ok(UserResponse(it)) }
                    .onErrorResume { Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()) }

}

data class UserRequestParam(
        val email: String,
        val password: String
)

data class UserResponse(
        val accessToken: String
)
