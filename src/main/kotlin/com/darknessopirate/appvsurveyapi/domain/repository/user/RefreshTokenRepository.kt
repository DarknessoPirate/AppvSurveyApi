package com.darknessopirate.appvsurveyapi.domain.repository.user

import com.darknessopirate.appvsurveyapi.domain.entity.user.RefreshToken
import com.darknessopirate.appvsurveyapi.domain.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    fun deleteByUser(user: User)

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    fun deleteExpiredTokens(now: LocalDateTime)
}