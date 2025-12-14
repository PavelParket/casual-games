package com.bank_service.repository;

import com.bank_service.domain.entity.ProcessedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedRoomRepository extends JpaRepository<ProcessedRoom, Long> {

    boolean existsByRoomId(UUID roomId);
}
