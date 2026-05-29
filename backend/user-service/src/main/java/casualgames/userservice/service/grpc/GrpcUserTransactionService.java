package casualgames.userservice.service.grpc;

import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.TransactionType;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.validator.TransactionValidator;
import com.casualgames.grpc.transaction.UpdateBalancesRequest;
import com.casualgames.grpc.transaction.UserTransaction;
import com.casualgames.grpc.transaction.UserTransactionServiceGrpc;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.NotFoundException;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static casualgames.userservice.config.ResourceMessageConstants.INSUFFICIENT_BALANCE;
import static casualgames.userservice.config.ResourceMessageConstants.ONE_OR_ANY_USERS_ARE_MISSING;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcUserTransactionService extends UserTransactionServiceGrpc.UserTransactionServiceImplBase {

    private final UserRepository userRepository;

    private final TransactionValidator transactionValidator;

    @Override
    @Transactional
    public void updateBalances(UpdateBalancesRequest request, StreamObserver<Empty> responseObserver) {
        transactionValidator.validateUpdateBalancesRequest(request.getTransactionsList());

        List<UUID> guids = request.getTransactionsList().stream()
                .map(UserTransaction::getUserGuid)
                .map(UUID::fromString)
                .distinct()
                .toList();

        List<User> users = userRepository.findAllByGuidWithLock(guids);

        if (users.size() != guids.size()) {
            log.error("Not all users found. Expected: {}, Found: {}", guids.size(), users.size());
            throw new NotFoundException(ONE_OR_ANY_USERS_ARE_MISSING);
        }

        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getGuid,
                        Function.identity()
                ));

        request.getTransactionsList().forEach(transaction -> {
            User user = userMap.get(UUID.fromString(transaction.getUserGuid()));
            BigDecimal newBalance;

            if (TransactionType.SUBTRACTION.name().equals(transaction.getType())) {
                newBalance = user.getBalance().subtract(new BigDecimal(transaction.getAmount()));

                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    log.error("Insufficient balance for user: {}. Current: {}, Required: {}", user.getGuid(), user.getBalance(), transaction.getAmount());
                    throw new BadRequestException(String.format(INSUFFICIENT_BALANCE, user.getGuid()));
                }

                user.setBalance(newBalance);
            } else if (TransactionType.ADDITION.name().equals(transaction.getType())) {
                newBalance = user.getBalance().add(new BigDecimal(transaction.getAmount()));
                user.setBalance(newBalance);
            }
        });

        userRepository.saveAll(users);

        log.info("Updated {} users with {} transactions", users.size(), request.getTransactionsList().size());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
