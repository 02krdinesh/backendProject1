package com.example.charityManagementSystem.models;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserAdminPrincipal implements UserDetails {

    private Admin userDonor;

    public UserAdminPrincipal(Admin userDonor) {
        this.userDonor = userDonor;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ADMIN"));   }

    @Override
    public String getPassword() {
        return userDonor.getPassword();  
    }

    @Override
    public String getUsername() {
        return userDonor.getUsername();  
    }
}
