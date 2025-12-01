package com.example.charityManagementSystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charityManagementSystem.models.Needy;


@Repository
public interface NeedyRepo extends JpaRepository<Needy, Long> {

    Needy findByUsername(String username);

    Optional<Needy> findByEmail(String email);

    Needy findByStripeId(String stripeId);
}
