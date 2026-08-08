package com.websocket_hub.service;

import com.common_utils.exception.NotFoundException;
import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.websocket_hub.domain.entity.User;
import com.websocket_hub.domain.repository.UserRepository;
import com.websocket_hub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.websocket_hub.config.ResourceMessageConstants.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Transactional
    public void synchronizeUpdatedUser(SynchronizedUser synchronizedUser) {
        User user = userRepository.findById(synchronizedUser.getId())
                .orElseGet(() ->
                        User.builder()
                                .id(synchronizedUser.getId())
                                .build()
                );

        userMapper.updateEntity(user, synchronizedUser);
        userRepository.save(user);
    }

    public User getByGuid(UUID guid) {
        return userRepository.findByGuid(guid)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }
}
