package com.example.charityManagementSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charityManagementSystem.models.Donation;

@Repository
public interface DonationRepo extends JpaRepository<Donation, Long>{
    List<Donation> findByDonorId(Long donorId);
    
}
