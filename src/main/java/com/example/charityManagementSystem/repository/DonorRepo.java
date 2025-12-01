package com.example.charityManagementSystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charityManagementSystem.models.Donor;

@Repository
public interface DonorRepo extends JpaRepository<Donor, Long>{

    Donor findByUsername(String username);

    Optional<Donor> findByEmail(String email);
}


/*
 * 
 * 
 * 
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUserEmailAndUserPassword(String userEmail, String userPassword);

    User findByUserName(String userName);

    void save(UserPost user);
}
 */