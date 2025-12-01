package com.example.charityManagementSystem.models;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserNeedyPrincipal implements UserDetails{


    private Needy user_needy;

    public UserNeedyPrincipal(Needy user_needy){
        this.user_needy = user_needy;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));        
    }

    @Override
    public String getPassword() {
        return user_needy.getPassword();
    }

    @Override
    public String getUsername() {
        return user_needy.getUsername();
    }
    
}

