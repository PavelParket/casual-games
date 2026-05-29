package casualgames.userservice.validator;

import casualgames.userservice.domain.enums.TransactionStatus;
import com.casualgames.grpc.transaction.UserTransaction;
import com.common_utils.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static casualgames.userservice.config.ResourceMessageConstants.REQUIRED_PENDING_STATUS_FOR_TRANSACTIONS;
import static casualgames.userservice.config.ResourceMessageConstants.REQUIRED_POSITIVE_TRANSACTION_AMOUNTS;
import static casualgames.userservice.config.ResourceMessageConstants.REQUIRED_TRANSACTION_LIST;

@Component
@RequiredArgsConstructor
public class TransactionValidator {

    public void validateUpdateBalancesRequest(List<UserTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            throw new BadRequestException(REQUIRED_TRANSACTION_LIST);
        }

        boolean allPending = transactions.stream()
                .allMatch(transaction -> TransactionStatus.PENDING.name().equals(transaction.getStatus()));

        if (!allPending) {
            throw new BadRequestException(REQUIRED_PENDING_STATUS_FOR_TRANSACTIONS);
        }

        boolean allAmountsValid = transactions.stream()
                .allMatch(transaction -> {
                    try {
                        return !transaction.getAmount().isBlank()
                                && new BigDecimal(transaction.getAmount()).compareTo(BigDecimal.ZERO) > 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });

        if (!allAmountsValid) {
            throw new BadRequestException(REQUIRED_POSITIVE_TRANSACTION_AMOUNTS);
        }
    }
}
