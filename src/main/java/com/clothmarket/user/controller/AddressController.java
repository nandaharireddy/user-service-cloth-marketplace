package com.clothmarket.user.controller;

import com.clothmarket.user.dto.request.AddressRequestDto;
import com.clothmarket.user.dto.request.AddressUpdateRequestDto;
import com.clothmarket.user.dto.response.AddressResponseDto;
import com.clothmarket.user.dto.response.ApiResponseDto;
import com.clothmarket.user.dto.response.ErrorResponseDto;
import com.clothmarket.user.security.UserPrincipal;
import com.clothmarket.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller managing customer shipping addresses.
 * Requires valid JWT Bearer token authentication for all operations.
 */
@RestController
@RequestMapping({"/addresses", "/api/v1/addresses"})
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Endpoints for managing saved user shipping addresses")
@SecurityRequirement(name = "BearerAuth")
public class AddressController {

    private final AddressService addressService;

    /**
     * Retrieves a paginated list of shipping addresses for the authenticated user.
     *
     * @param principal authenticated user principal
     * @param pageable  pagination and sort parameters
     * @return 200 OK with Page of AddressResponseDto
     */
    @GetMapping
    @Operation(summary = "List saved addresses", description = "Retrieves a paginated list of shipping addresses belonging to the authenticated customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Page<AddressResponseDto>> getAddresses(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        Page<AddressResponseDto> addresses = addressService.getUserAddresses(principal.getUserId(), pageable);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Retrieves a specific shipping address by ID for the authenticated user.
     *
     * @param id        the address identifier
     * @param principal authenticated user principal
     * @return 200 OK with AddressResponseDto
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Retrieves details of a specific address owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address details retrieved",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - address belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<AddressResponseDto> getAddressById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        AddressResponseDto address = addressService.getAddressById(id, principal.getUserId());
        return ResponseEntity.ok(address);
    }

    /**
     * Creates and adds a new shipping address for the authenticated user.
     *
     * @param request   address creation payload
     * @param principal authenticated user principal
     * @return 201 Created with AddressResponseDto
     */
    @PostMapping
    @Operation(summary = "Add a new address", description = "Saves a new shipping address for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid address payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<AddressResponseDto> createAddress(
            @Valid @RequestBody AddressRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AddressResponseDto created = addressService.createAddress(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Partially updates an existing address owned by the authenticated user.
     *
     * @param id        the address identifier
     * @param request   address partial update payload
     * @param principal authenticated user principal
     * @return 200 OK with updated AddressResponseDto
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Edit an existing address", description = "Applies partial updates to a shipping address owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - address belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<AddressResponseDto> updateAddress(
            @PathVariable("id") Long id,
            @Valid @RequestBody AddressUpdateRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AddressResponseDto updated = addressService.updateAddress(id, request, principal.getUserId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a shipping address owned by the authenticated user.
     *
     * @param id        the address identifier
     * @param principal authenticated user principal
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an address", description = "Removes a shipping address owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - address belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto> deleteAddress(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        addressService.deleteAddress(id, principal.getUserId());
        return ResponseEntity.ok(ApiResponseDto.ofSuccess("Address deleted successfully"));
    }
}
