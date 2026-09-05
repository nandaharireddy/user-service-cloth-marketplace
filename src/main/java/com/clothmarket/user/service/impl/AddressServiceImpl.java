package com.clothmarket.user.service.impl;

import com.clothmarket.user.dao.AddressDao;
import com.clothmarket.user.dto.request.AddressRequestDto;
import com.clothmarket.user.dto.request.AddressUpdateRequestDto;
import com.clothmarket.user.dto.response.AddressResponseDto;
import com.clothmarket.user.entity.AddressEntity;
import com.clothmarket.user.exception.ResourceNotFoundException;
import com.clothmarket.user.exception.UnauthorizedAccessException;
import com.clothmarket.user.mapper.AddressMapper;
import com.clothmarket.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing customer shipping addresses and default selections.
 * Operates strictly through {@link AddressDao} fulfilling layering architectural rules.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressDao addressDao;
    private final AddressMapper addressMapper;

    /**
     * Retrieves a paginated list of shipping addresses belonging to the specified user.
     *
     * @param userId   the authenticated user's ID
     * @param pageable pagination parameters
     * @return a page of AddressResponseDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AddressResponseDto> getUserAddresses(Long userId, Pageable pageable) {
        log.debug("Fetching paginated addresses for user ID: {}, page: {}", userId, pageable.getPageNumber());
        return addressDao.findByUserId(userId, pageable)
                .map(addressMapper::toResponseDto);
    }

    /**
     * Retrieves all shipping addresses belonging to the specified user.
     *
     * @param userId the authenticated user's ID
     * @return list of AddressResponseDto objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDto> getAllUserAddresses(Long userId) {
        log.debug("Fetching all addresses for user ID: {}", userId);
        return addressDao.findByUserId(userId).stream()
                .map(addressMapper::toResponseDto)
                .toList();
    }

    /**
     * Retrieves a single address by its ID, enforcing ownership by the user.
     *
     * @param id     the address ID
     * @param userId the authenticated user's ID
     * @return the AddressResponseDto if found and authorized
     */
    @Override
    @Transactional(readOnly = true)
    public AddressResponseDto getAddressById(Long id, Long userId) {
        AddressEntity address = addressDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + id));

        if (!address.getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt: user ID {} tried to view address ID {} owned by user ID {}",
                    userId, id, address.getUserId());
            throw new UnauthorizedAccessException("You are not authorized to view this address");
        }

        return addressMapper.toResponseDto(address);
    }

    /**
     * Creates and stores a new shipping address for the specified user.
     *
     * @param request address creation payload
     * @param userId  the authenticated user's ID
     * @return newly created AddressResponseDto
     */
    @Override
    @Transactional
    public AddressResponseDto createAddress(AddressRequestDto request, Long userId) {
        List<AddressEntity> existingAddresses = addressDao.findByUserId(userId);

        AddressEntity entity = addressMapper.toEntity(request, userId);

        // If this is the user's first address, automatically mark it as default
        if (existingAddresses.isEmpty()) {
            entity.setIsDefault(true);
        } else if (Boolean.TRUE.equals(request.getIsDefault())) {
            // Reset existing default flags if this new address is explicitly set as default
            addressDao.clearAllDefaultAddresses(userId);
        }

        AddressEntity savedAddress = addressDao.save(entity);
        log.info("Created address ID: {} for user ID: {}", savedAddress.getId(), userId);

        return addressMapper.toResponseDto(savedAddress);
    }

    /**
     * Applies partial updates to an existing address owned by the user.
     *
     * @param id      the address ID
     * @param request partial update payload
     * @param userId  the authenticated user's ID
     * @return updated AddressResponseDto
     */
    @Override
    @Transactional
    public AddressResponseDto updateAddress(Long id, AddressUpdateRequestDto request, Long userId) {
        AddressEntity address = addressDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + id));

        if (!address.getUserId().equals(userId)) {
            log.warn("Unauthorized modification attempt: user ID {} tried to update address ID {} owned by user ID {}",
                    userId, id, address.getUserId());
            throw new UnauthorizedAccessException("You are not authorized to modify this address");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressDao.clearOtherDefaultAddresses(userId, id);
        }

        addressMapper.updateEntityFromDto(address, request);
        AddressEntity updatedAddress = addressDao.save(address);

        log.info("Updated address ID: {} for user ID: {}", updatedAddress.getId(), userId);
        return addressMapper.toResponseDto(updatedAddress);
    }

    /**
     * Deletes a shipping address owned by the specified user.
     *
     * @param id     the address ID
     * @param userId the authenticated user's ID
     */
    @Override
    @Transactional
    public void deleteAddress(Long id, Long userId) {
        AddressEntity address = addressDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + id));

        if (!address.getUserId().equals(userId)) {
            log.warn("Unauthorized deletion attempt: user ID {} tried to delete address ID {} owned by user ID {}",
                    userId, id, address.getUserId());
            throw new UnauthorizedAccessException("You are not authorized to delete this address");
        }

        addressDao.delete(address);
        log.info("Deleted address ID: {} for user ID: {}", id, userId);
    }
}
