package com.clothmarket.user.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom Spring Security UserDetails principal representing an authenticated user.
 * Encapsulates the user's primary key ID, email, assigned role, and vendor ID.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String role;
    private final Long vendorId;

    /**
     * Returns the user's granted security authorities.
     *
     * @return collection containing the role authority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return Collections.singletonList(new SimpleGrantedAuthority(formattedRole));
    }

    @Override
    public String getPassword() {
        return null; // JWT authentication does not expose passwords in principal
    }

    @Override
    public String getUsername() {
        return email != null ? email : String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
