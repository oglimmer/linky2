package de.oglimmer.linky.logic

import de.oglimmer.linky.dao.UserCrudRepository
import de.oglimmer.linky.entity.User
import de.oglimmer.linky.rest.dto.UserRequestParam
import de.oglimmer.linky.util.IdGen
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.*

@Service
@Transactional
class UserService(private val repository: UserCrudRepository) {

    companion object {
        val SECRET_KEY = IdGen.generateId()
        const val TOKEN_LIFETIME = 1000 * 60 * 60
    }

    fun createUser(userRequestParam: UserRequestParam): Mono<String> = repository
            .save(User(IdGen.generateId(),
                    userRequestParam.email,
                    BCrypt.hashpw(userRequestParam.password, BCrypt.gensalt())))
            .map { createJwt(it) }

    fun validateUserPassword(userRequestParam: UserRequestParam): Mono<String> = repository
            .findByEmail(userRequestParam.email)
            .filter { BCrypt.checkpw(userRequestParam.password, it.password) }
            .switchIfEmpty(Mono.error(UserOrPasswordWrongException()))
            .map { createJwt(it) }

    private fun createJwt(user: User) =
            Jwts.builder()
                    .setSubject(user.id)
                    .claim("email", user.email)
                    .setIssuedAt(Date())
                    .setExpiration(Date(System.currentTimeMillis() + TOKEN_LIFETIME))
                    .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                    .compact()

}

class UserOrPasswordWrongException : RuntimeException() {

}
