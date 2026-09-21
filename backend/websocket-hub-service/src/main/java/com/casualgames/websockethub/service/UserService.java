package com.casualgames.websockethub.service;

import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.kafkastarter.dto.event.sync.SynchronizedUser;
import com.casualgames.websockethub.domain.entity.User;
import com.casualgames.websockethub.domain.repository.UserRepository;
import com.casualgames.websockethub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.casualgames.websockethub.config.ResourceMessageConstants.USER_NOT_FOUND;

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
