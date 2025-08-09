package com.nimbleways.springboilerplate.common.infra.database.jparepositories;

import com.nimbleways.springboilerplate.common.infra.database.entities.UserSessionDbEntity;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserSessionRepository extends JpaRepository<UserSessionDbEntity, Integer> {
    @Query("SELECT u FROM UserSessionDbEntity u WHERE u.refreshToken = :refreshToken AND u.expirationDate >= :now")
    Optional<UserSessionDbEntity> findValidSessionByRefreshToken(String refreshToken, Instant now);

    @Transactional
    int deleteUserSessionByRefreshToken(String refreshToken);

    @Transactional
    void deleteUserSessionByExpirationDateBefore(Instant instant);
}
