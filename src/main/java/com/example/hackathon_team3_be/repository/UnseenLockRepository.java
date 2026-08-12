package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.entity.UnseenLockEntity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnseenLockRepository extends JpaRepository<UnseenLockEntity,Long> {
    List<UnseenLockEntity> findByUnseenId(String unseenId);
    void deleteByUnseenId(String unseenId);
}
