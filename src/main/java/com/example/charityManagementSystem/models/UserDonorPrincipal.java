package com.example.charityManagementSystem.models;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDonorPrincipal implements UserDetails {

    private Donor userDonor;

    public UserDonorPrincipal(Donor userDonor) {
        this.userDonor = userDonor;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("DONOR"));   }

    @Override
    public String getPassword() {
        return userDonor.getPassword();  
    }

    @Override
    public String getUsername() {
        return userDonor.getUsername();  
    }
}
