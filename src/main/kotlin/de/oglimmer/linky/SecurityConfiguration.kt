package de.oglimmer.linky

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder

@EnableWebSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http {
            httpBasic {}
            authorizeRequests {
                authorize("/links/**")
                authorize("/**", permitAll)
            }
            oauth2ResourceServer {
                jwt {
                    jwtDecoder = JwtDecoder {
                        val jwt = Jwts.parser().setSigningKey(UserController.SECRET_KEY).parse(it)
                        val body = jwt.body
                        if (body !is Claims) {
                            throw RuntimeException("Failed to get claims from jwt")
                        }
                        Jwt(it, body.issuedAt.toInstant(), body.expiration.toInstant(), jwt.header, body)
                    }
                }
            }
            csrf {
                disable()
            }
        }
    }
}
