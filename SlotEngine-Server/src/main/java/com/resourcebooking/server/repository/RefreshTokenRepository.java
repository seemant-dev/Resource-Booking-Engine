package com.resourcebooking.server.repository;

import com.resourcebooking.server.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("delete from RefreshToken refreshToken where refreshToken.token = :token")
    int deleteByToken(@Param("token") String token);

    void deleteByExpiresAtBefore(Instant now);
}
