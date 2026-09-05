package com.clothmarket.user.service;

import com.clothmarket.user.dto.request.AddressRequestDto;
import com.clothmarket.user.dto.request.AddressUpdateRequestDto;
import com.clothmarket.user.dto.response.AddressResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for customer shipping address lifecycle management.
 * Provides paginated lookups, address creation, partial modifications, and deletion.
 */
public interface AddressService {

    /**
     * Retrieves a paginated list of shipping addresses belonging to the specified user.
     *
     * @param userId   the authenticated user's ID
     * @param pageable pagination parameters
     * @return a page of AddressResponseDto objects
     */
    Page<AddressResponseDto> getUserAddresses(Long userId, Pageable pageable);

    /**
     * Retrieves all shipping addresses belonging to the specified user.
     *
     * @param userId the authenticated user's ID
     * @return list of AddressResponseDto objects
     */
    List<AddressResponseDto> getAllUserAddresses(Long userId);

    /**
     * Retrieves a single address by its ID, enforcing ownership by the user.
     *
     * @param id     the address ID
     * @param userId the authenticated user's ID
     * @return the AddressResponseDto if found and authorized
     */
    AddressResponseDto getAddressById(Long id, Long userId);

    /**
     * Creates and stores a new shipping address for the specified user.
     *
     * @param request address creation payload
     * @param userId  the authenticated user's ID
     * @return newly created AddressResponseDto
     */
    AddressResponseDto createAddress(AddressRequestDto request, Long userId);

    /**
     * Applies partial updates to an existing address owned by the user.
     *
     * @param id      the address ID
     * @param request partial update payload
     * @param userId  the authenticated user's ID
     * @return updated AddressResponseDto
     */
    AddressResponseDto updateAddress(Long id, AddressUpdateRequestDto request, Long userId);

    /**
     * Deletes a shipping address owned by the specified user.
     *
     * @param id     the address ID
     * @param userId the authenticated user's ID
     */
    void deleteAddress(Long id, Long userId);
}
