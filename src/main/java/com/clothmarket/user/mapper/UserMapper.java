package com.clothmarket.user.mapper;

import com.clothmarket.user.dto.request.RegisterRequestDto;
import com.clothmarket.user.dto.response.UserResponseDto;
import com.clothmarket.user.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper component for bidirectional transformations between User JPA entities and DTOs.
 */
@Component
public class UserMapper {

    /**
     * Maps a {@link UserEntity} to a public {@link UserResponseDto}.
     *
     * @param entity the user entity to convert
     * @return the corresponding UserResponseDto, or null if entity is null
     */
    public UserResponseDto toResponseDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .isVendor(entity.getIsVendor())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a {@link RegisterRequestDto} into a fresh {@link UserEntity}.
     *
     * @param request      the incoming registration request
     * @param encodedPassword the BCrypt hashed password string
     * @return initialized UserEntity ready for persistence
     */
    public UserEntity toEntity(RegisterRequestDto request, String encodedPassword) {
        if (request == null) {
            return null;
        }
        return UserEntity.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null && !request.getPhone().isBlank() ? request.getPhone().trim() : null)
                .passwordHash(encodedPassword)
                .isVendor(Boolean.TRUE.equals(request.getIsVendor()))
                .build();
    }
}
