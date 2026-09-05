package com.clothmarket.user.dao;

import com.clothmarket.user.entity.UserEntity;

import java.util.Optional;

/**
 * Data Access Object interface for User entity operations.
 * Abstraction layer decoupling the Service layer from Spring Data JPA repository mechanisms.
 */
public interface UserDao {

    /**
     * Persists a new user or updates an existing user entity.
     *
     * @param user the UserEntity to save
     * @return the saved UserEntity instance
     */
    UserEntity save(UserEntity user);

    /**
     * Finds a user by their unique primary key ID.
     *
     * @param id the user identifier
     * @return an Optional containing the UserEntity if found
     */
    Optional<UserEntity> findById(Long id);

    /**
     * Finds a user by their unique email address.
     *
     * @param email user's email address
     * @return an Optional containing the UserEntity if found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Finds a user by their unique phone number.
     *
     * @param phone user's phone number
     * @return an Optional containing the UserEntity if found
     */
    Optional<UserEntity> findByPhone(String phone);

    /**
     * Checks if a user already exists with the given email.
     *
     * @param email email address to check
     * @return true if the user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user already exists with the given phone number.
     *
     * @param phone phone number to check
     * @return true if the user exists, false otherwise
     */
    boolean existsByPhone(String phone);
}
