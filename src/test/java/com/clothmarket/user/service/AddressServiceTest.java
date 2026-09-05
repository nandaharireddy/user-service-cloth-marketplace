package com.clothmarket.user.service;

import com.clothmarket.user.dao.AddressDao;
import com.clothmarket.user.dto.request.AddressRequestDto;
import com.clothmarket.user.dto.request.AddressUpdateRequestDto;
import com.clothmarket.user.dto.response.AddressResponseDto;
import com.clothmarket.user.entity.AddressEntity;
import com.clothmarket.user.exception.ResourceNotFoundException;
import com.clothmarket.user.exception.UnauthorizedAccessException;
import com.clothmarket.user.mapper.AddressMapper;
import com.clothmarket.user.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AddressServiceImpl} validating CRUD operations and ownership authorization.
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressDao addressDao;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    @DisplayName("createAddress: first address for user automatically becomes default")
    void createAddress_FirstAddress_BecomesDefault() {
        AddressRequestDto request = AddressRequestDto.builder()
                .line1("123 Main St")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560001")
                .phone("+919876543210")
                .isDefault(false)
                .build();

        AddressEntity entity = AddressEntity.builder()
                .userId(1L)
                .line1("123 Main St")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560001")
                .phone("+919876543210")
                .isDefault(false)
                .build();

        AddressResponseDto responseDto = AddressResponseDto.builder()
                .id(100L)
                .userId(1L)
                .isDefault(true)
                .build();

        when(addressDao.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(addressMapper.toEntity(request, 1L)).thenReturn(entity);
        when(addressDao.save(entity)).thenReturn(entity);
        when(addressMapper.toResponseDto(entity)).thenReturn(responseDto);

        AddressResponseDto result = addressService.createAddress(request, 1L);

        assertThat(result).isNotNull();
        assertThat(entity.getIsDefault()).isTrue();
        verify(addressDao).save(entity);
    }

    @Test
    @DisplayName("createAddress: clears other default addresses if new address is marked default")
    void createAddress_ExplicitDefault_ClearsOthers() {
        AddressRequestDto request = AddressRequestDto.builder()
                .line1("456 Second St")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560002")
                .phone("+919876543210")
                .isDefault(true)
                .build();

        AddressEntity existing = AddressEntity.builder().id(99L).userId(1L).isDefault(true).build();
        AddressEntity newEntity = AddressEntity.builder().userId(1L).isDefault(true).build();
        AddressResponseDto responseDto = AddressResponseDto.builder().id(101L).userId(1L).isDefault(true).build();

        when(addressDao.findByUserId(1L)).thenReturn(List.of(existing));
        when(addressMapper.toEntity(request, 1L)).thenReturn(newEntity);
        when(addressDao.save(newEntity)).thenReturn(newEntity);
        when(addressMapper.toResponseDto(newEntity)).thenReturn(responseDto);

        AddressResponseDto result = addressService.createAddress(request, 1L);

        assertThat(result).isNotNull();
        verify(addressDao).clearAllDefaultAddresses(1L);
        verify(addressDao).save(newEntity);
    }

    @Test
    @DisplayName("updateAddress: rejects update if address is owned by another user")
    void updateAddress_UnauthorizedUser_ThrowsException() {
        AddressUpdateRequestDto request = AddressUpdateRequestDto.builder()
                .city("Mumbai")
                .build();

        AddressEntity existing = AddressEntity.builder()
                .id(50L)
                .userId(999L) // Owned by different user
                .build();

        when(addressDao.findById(50L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> addressService.updateAddress(50L, request, 1L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("deleteAddress: throws ResourceNotFoundException if address does not exist")
    void deleteAddress_NotFound_ThrowsException() {
        when(addressDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deleteAddress: successfully deletes owned address")
    void deleteAddress_Success() {
        AddressEntity existing = AddressEntity.builder()
                .id(50L)
                .userId(1L)
                .build();

        when(addressDao.findById(50L)).thenReturn(Optional.of(existing));

        addressService.deleteAddress(50L, 1L);

        verify(addressDao).delete(existing);
    }
}
