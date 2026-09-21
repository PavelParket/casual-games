package com.casualgames.bankservice.repository;

import com.casualgames.bankservice.domain.entity.ProcessedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedRoomRepository extends JpaRepository<ProcessedRoom, Long> {

    boolean existsByRoomId(UUID roomId);
}
