package com.clothmarket.user.mapper;

import com.clothmarket.user.dto.request.AddressRequestDto;
import com.clothmarket.user.dto.request.AddressUpdateRequestDto;
import com.clothmarket.user.dto.response.AddressResponseDto;
import com.clothmarket.user.entity.AddressEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper component for transformations between Address JPA entities and DTOs.
 */
@Component
public class AddressMapper {

    /**
     * Maps an {@link AddressEntity} to an {@link AddressResponseDto}.
     *
     * @param entity the address entity to convert
     * @return the corresponding AddressResponseDto, or null if entity is null
     */
    public AddressResponseDto toResponseDto(AddressEntity entity) {
        if (entity == null) {
            return null;
        }
        return AddressResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .label(entity.getLabel())
                .line1(entity.getLine1())
                .line2(entity.getLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .phone(entity.getPhone())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Converts an {@link AddressRequestDto} into an {@link AddressEntity} bound to a specific user.
     *
     * @param request the incoming address creation request
     * @param userId  the authenticated user's ID
     * @return initialized AddressEntity ready for persistence
     */
    public AddressEntity toEntity(AddressRequestDto request, Long userId) {
        if (request == null) {
            return null;
        }
        return AddressEntity.builder()
                .userId(userId)
                .label(request.getLabel() != null ? request.getLabel().trim() : null)
                .line1(request.getLine1().trim())
                .line2(request.getLine2() != null ? request.getLine2().trim() : null)
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .postalCode(request.getPostalCode().trim())
                .country(request.getCountry() != null && !request.getCountry().isBlank() ? request.getCountry().trim() : "India")
                .phone(request.getPhone().trim())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();
    }

    /**
     * Applies partial updates from an {@link AddressUpdateRequestDto} to an existing {@link AddressEntity}.
     *
     * @param entity  the existing AddressEntity to update in-place
     * @param request the partial update DTO
     */
    public void updateEntityFromDto(AddressEntity entity, AddressUpdateRequestDto request) {
        if (entity == null || request == null) {
            return;
        }
        if (request.getLabel() != null) {
            entity.setLabel(request.getLabel().trim());
        }
        if (request.getLine1() != null && !request.getLine1().isBlank()) {
            entity.setLine1(request.getLine1().trim());
        }
        if (request.getLine2() != null) {
            entity.setLine2(request.getLine2().trim());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            entity.setCity(request.getCity().trim());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            entity.setState(request.getState().trim());
        }
        if (request.getPostalCode() != null && !request.getPostalCode().isBlank()) {
            entity.setPostalCode(request.getPostalCode().trim());
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            entity.setCountry(request.getCountry().trim());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            entity.setPhone(request.getPhone().trim());
        }
        if (request.getIsDefault() != null) {
            entity.setIsDefault(request.getIsDefault());
        }
    }
}
