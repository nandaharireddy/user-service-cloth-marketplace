package com.clothmarket.user.dao;

import com.clothmarket.user.entity.AddressEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Address entity operations.
 * Manages CRUD operations and default address resets for customer shipping addresses.
 */
public interface AddressDao {

    /**
     * Persists a new address or updates an existing address entity.
     *
     * @param address the AddressEntity to save
     * @return the saved AddressEntity instance
     */
    AddressEntity save(AddressEntity address);

    /**
     * Finds an address by its primary key ID.
     *
     * @param id the address identifier
     * @return an Optional containing the AddressEntity if found
     */
    Optional<AddressEntity> findById(Long id);

    /**
     * Finds an address by ID and user ID ensuring ownership.
     *
     * @param id     the address identifier
     * @param userId the user identifier
     * @return an Optional containing the AddressEntity if found and owned by the user
     */
    Optional<AddressEntity> findByIdAndUserId(Long id, Long userId);

    /**
     * Retrieves a paginated list of addresses belonging to a user.
     *
     * @param userId   the user identifier
     * @param pageable pagination parameters
     * @return a Page of AddressEntity instances
     */
    Page<AddressEntity> findByUserId(Long userId, Pageable pageable);

    /**
     * Retrieves all addresses belonging to a user.
     *
     * @param userId the user identifier
     * @return list of AddressEntity instances
     */
    List<AddressEntity> findByUserId(Long userId);

    /**
     * Deletes an address entity.
     *
     * @param address the address entity to delete
     */
    void delete(AddressEntity address);

    /**
     * Clears the default flag for all other addresses of the user except the specified one.
     *
     * @param userId    the user identifier
     * @param addressId the address identifier to remain default
     */
    void clearOtherDefaultAddresses(Long userId, Long addressId);

    /**
     * Clears the default flag for all addresses belonging to the user.
     *
     * @param userId the user identifier
     */
    void clearAllDefaultAddresses(Long userId);
}
