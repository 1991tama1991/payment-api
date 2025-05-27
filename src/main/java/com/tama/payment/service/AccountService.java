package com.tama.payment.service;


import com.tama.payment.persistence.entity.AccountEntity;
import com.tama.payment.persistence.repository.AccountRepository;
import com.tama.payment.web.model.request.AccountCreateRequestDto;
import com.tama.payment.web.model.response.AccountResponseDto;
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
public class AccountService {
    AccountRepository accountRepository;
    ModelMapper modelMapper;

    @Transactional
    public AccountResponseDto createAccount(AccountCreateRequestDto accountRequest) {

        AccountEntity accountEntity = AccountEntity.builder()
                .id(UUID.randomUUID())
                .balance(accountRequest.getBalance())
                .build();

        AccountEntity savedAccount = accountRepository.save(accountEntity);

        log.debug("Account: {} has been created.", savedAccount);

        return modelMapper.map(savedAccount, AccountResponseDto.class);
    }

    public List<AccountResponseDto> getAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(accountEntity -> modelMapper.map(accountEntity, AccountResponseDto.class))
                .toList();
    }

}
