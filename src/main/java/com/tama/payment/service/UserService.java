package com.tama.payment.service;


import com.tama.payment.persistence.entity.UserEntity;
import com.tama.payment.persistence.repository.UserRepository;
import com.tama.payment.web.model.UserCreateRequestDto;
import com.tama.payment.web.model.UserResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService {
    UserRepository userRepository;
    ModelMapper modelMapper;

    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto userRequest) {

        UserEntity userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .balance(userRequest.getBalance())
                .build();

        UserEntity savedUser = userRepository.save(userEntity);

        log.debug("User: {} has been created.", savedUser);

        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    public List<UserResponseDto> getUsers() {

        return userRepository.findAll()
                .stream()
                .map(userEntity -> modelMapper.map(userEntity, UserResponseDto.class))
                .toList();
    }

}
