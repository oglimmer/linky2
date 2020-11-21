package de.oglimmer.linky.conf

import de.oglimmer.linky.logic.UserService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
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
                        val jwt = Jwts.parser().setSigningKey(UserService.SECRET_KEY).parse(it)
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

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurityConfig(private val customPermissionEvaluator: CustomPermissionEvaluator)
    : GlobalMethodSecurityConfiguration() {

    override fun createExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator)
        return expressionHandler
    }

}
