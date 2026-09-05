package com.clothmarket.user.repository;

import com.clothmarket.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for user persistence operations.
 * Declares query methods for fetching users by email or phone.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email user's email address
     * @return an Optional containing the UserEntity if found, or empty
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Finds a user by their unique phone number.
     *
     * @param phone user's phone number
     * @return an Optional containing the UserEntity if found, or empty
     */
    Optional<UserEntity> findByPhone(String phone);

    /**
     * Checks if a user already exists with the given email address.
     *
     * @param email email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user already exists with the given phone number.
     *
     * @param phone phone number to check
     * @return true if a user with the phone exists, false otherwise
     */
    boolean existsByPhone(String phone);
}
