package com.clothmarket.user.dao.impl;

import com.clothmarket.user.dao.AddressDao;
import com.clothmarket.user.entity.AddressEntity;
import com.clothmarket.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object implementation for Address entity operations.
 * Delegates database interactions to {@link AddressRepository}.
 */
@Component
@RequiredArgsConstructor
public class AddressDaoImpl implements AddressDao {

    private final AddressRepository addressRepository;

    /**
     * Persists a new address or updates an existing address entity.
     *
     * @param address the AddressEntity to save
     * @return the saved AddressEntity instance
     */
    @Override
    public AddressEntity save(AddressEntity address) {
        return addressRepository.save(address);
    }

    /**
     * Finds an address by its primary key ID.
     *
     * @param id the address identifier
     * @return an Optional containing the AddressEntity if found
     */
    @Override
    public Optional<AddressEntity> findById(Long id) {
        return addressRepository.findById(id);
    }

    /**
     * Finds an address by ID and user ID ensuring ownership.
     *
     * @param id     the address identifier
     * @param userId the user identifier
     * @return an Optional containing the AddressEntity if found and owned by the user
     */
    @Override
    public Optional<AddressEntity> findByIdAndUserId(Long id, Long userId) {
        return addressRepository.findByIdAndUserId(id, userId);
    }

    /**
     * Retrieves a paginated list of addresses belonging to a user.
     *
     * @param userId   the user identifier
     * @param pageable pagination parameters
     * @return a Page of AddressEntity instances
     */
    @Override
    public Page<AddressEntity> findByUserId(Long userId, Pageable pageable) {
        return addressRepository.findByUserId(userId, pageable);
    }

    /**
     * Retrieves all addresses belonging to a user.
     *
     * @param userId the user identifier
     * @return list of AddressEntity instances
     */
    @Override
    public List<AddressEntity> findByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    /**
     * Deletes an address entity.
     *
     * @param address the address entity to delete
     */
    @Override
    public void delete(AddressEntity address) {
        addressRepository.delete(address);
    }

    /**
     * Clears the default flag for all other addresses of the user except the specified one.
     *
     * @param userId    the user identifier
     * @param addressId the address identifier to remain default
     */
    @Override
    public void clearOtherDefaultAddresses(Long userId, Long addressId) {
        addressRepository.clearOtherDefaultAddresses(userId, addressId);
    }

    /**
     * Clears the default flag for all addresses belonging to the user.
     *
     * @param userId the user identifier
     */
    @Override
    public void clearAllDefaultAddresses(Long userId) {
        addressRepository.clearAllDefaultAddresses(userId);
    }
}
