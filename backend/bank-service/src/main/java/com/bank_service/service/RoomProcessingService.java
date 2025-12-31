package com.bank_service.service;

import com.bank_service.domain.entity.ProcessedRoom;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.repository.ProcessedRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomProcessingService {

    private final ProcessedRoomRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markRoomAsProcessed(UUID roomId, RoomType roomType, int transactionCount) {
        if (repository.existsByRoomId(roomId)) {
            log.info("Room {} already processed", roomId);

            return false;
        }

        try {
            ProcessedRoom processedRoom = ProcessedRoom.builder()
                    .roomId(roomId)
                    .roomType(roomType)
                    .transactionCount(transactionCount)
                    .build();

            repository.save(processedRoom);

            log.info("Room {} marked as processed with {} transactions", roomId, transactionCount);

            return true;
        } catch (DataIntegrityViolationException e) {
            log.info("Room {} was marked as processed by another request", roomId);

            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean isRoomProcessed(UUID roomId) {
        return repository.existsByRoomId(roomId);
    }
}
