package com.clothmarket.user.dao.impl;

import com.clothmarket.user.dao.UserDao;
import com.clothmarket.user.entity.UserEntity;
import com.clothmarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Data Access Object implementation for User data persistence.
 * Delegates database interactions to {@link UserRepository}.
 */
@Component
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    /**
     * Persists a new user or updates an existing user entity.
     *
     * @param user the UserEntity to save
     * @return the saved UserEntity instance
     */
    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    /**
     * Finds a user by their unique primary key ID.
     *
     * @param id the user identifier
     * @return an Optional containing the UserEntity if found
     */
    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their unique email address.
     *
     * @param email user's email address
     * @return an Optional containing the UserEntity if found
     */
    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by their unique phone number.
     *
     * @param phone user's phone number
     * @return an Optional containing the UserEntity if found
     */
    @Override
    public Optional<UserEntity> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * Checks if a user already exists with the given email.
     *
     * @param email email address to check
     * @return true if the user exists, false otherwise
     */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Checks if a user already exists with the given phone number.
     *
     * @param phone phone number to check
     * @return true if the user exists, false otherwise
     */
    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}
