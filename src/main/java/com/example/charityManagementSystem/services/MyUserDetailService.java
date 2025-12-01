package com.example.charityManagementSystem.services;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.charityManagementSystem.models.UserDonorPrincipal;
import com.example.charityManagementSystem.models.UserNeedyPrincipal;
import com.example.charityManagementSystem.repository.DonorRepo;
import com.example.charityManagementSystem.repository.NeedyRepo;

@Service(value = "firstUserService")
public class MyUserDetailService implements UserDetailsService {

    private final DonorRepo donorRepo;
    private final NeedyRepo needyRepo;

    public MyUserDetailService(DonorRepo donorRepo, NeedyRepo needyRepo) {
        this.donorRepo = donorRepo;
        this.needyRepo = needyRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        System.out.println("MYuserdetails function : " + username);
        if (username.substring(0, 5).equals("DONOR")) {
            UserDetails donorUserDetails = new UserDonorPrincipal(donorRepo.findByUsername(username.substring(5)));
            System.out.println(donorUserDetails.getUsername());
            System.out.println(donorUserDetails.getPassword());
            return donorUserDetails;
        } else if (username.substring(0, 5).equals("NEEDY")) {
            UserDetails needyUserDetails = new UserNeedyPrincipal(needyRepo.findByUsername(username.substring(5)));
            return needyUserDetails;

        } else {
            throw new UsernameNotFoundException("User Type Not Found !!!");
        }
    }
}
