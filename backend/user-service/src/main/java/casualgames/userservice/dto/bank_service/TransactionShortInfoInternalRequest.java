package casualgames.userservice.dto.bank_service;

import casualgames.userservice.enums.TransactionStatus;
import casualgames.userservice.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TransactionShortInfoInternalRequest(
        Long id,

        UUID userGuid,

        BigDecimal amount,

        TransactionType type,

        TransactionStatus status
) {
}
