package com.darknessopirate.appvsurveyapi.infrastructure.security

import com.darknessopirate.appvsurveyapi.infrastructure.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                if (!jwtService.isTokenExpired(token)) {
                    val username = jwtService.getUsernameFromToken(token)

                    if (SecurityContextHolder.getContext().authentication == null) {
                        val user = userDetailsService.getUserByUsername(username)

                        if (user != null && jwtService.isTokenValid(token, username)) {
                            val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
                            val authToken = UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                            )
                            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = authToken
                        }
                    }
                }
            } catch (e: Exception) {
                // Token is invalid, continue without authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}