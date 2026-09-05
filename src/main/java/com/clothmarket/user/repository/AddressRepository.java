package com.clothmarket.user.repository;

import com.clothmarket.user.entity.AddressEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for user shipping addresses.
 * Declares query methods for fetching, checking ownership, and resetting default addresses.
 */
@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    /**
     * Finds all addresses belonging to a specific user as a paginated list.
     *
     * @param userId   the owner's user identifier
     * @param pageable pagination information
     * @return a Page of AddressEntity instances
     */
    Page<AddressEntity> findByUserId(Long userId, Pageable pageable);

    /**
     * Finds all addresses belonging to a specific user.
     *
     * @param userId the owner's user identifier
     * @return list of AddressEntity instances
     */
    List<AddressEntity> findByUserId(Long userId);

    /**
     * Finds an address by its ID and the owner's user ID to enforce ownership validation.
     *
     * @param id     the address identifier
     * @param userId the owner's user identifier
     * @return an Optional containing the AddressEntity if found and owned by userId
     */
    Optional<AddressEntity> findByIdAndUserId(Long id, Long userId);

    /**
     * Resets default flag to false for all addresses of a user except the specified address ID.
     *
     * @param userId    the owner's user identifier
     * @param addressId the address identifier to exclude from resetting
     */
    @Modifying
    @Query("UPDATE AddressEntity a SET a.isDefault = false WHERE a.userId = :userId AND a.id <> :addressId")
    void clearOtherDefaultAddresses(@Param("userId") Long userId, @Param("addressId") Long addressId);

    /**
     * Resets default flag to false for all addresses of a user.
     *
     * @param userId the owner's user identifier
     */
    @Modifying
    @Query("UPDATE AddressEntity a SET a.isDefault = false WHERE a.userId = :userId")
    void clearAllDefaultAddresses(@Param("userId") Long userId);
}
