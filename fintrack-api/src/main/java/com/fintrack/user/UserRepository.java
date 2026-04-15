package com.fintrack.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for user persistence operations.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Find a non-deleted user by email.
     */
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Find a non-deleted user by ID.
     */
    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Check if email is already taken by a non-deleted user.
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);
}
