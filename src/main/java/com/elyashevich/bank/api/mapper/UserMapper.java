package com.elyashevich.bank.api.mapper;

import com.elyashevich.bank.api.dto.auth.LoginDto;
import com.elyashevich.bank.api.dto.auth.RegisterDto;
import com.elyashevich.bank.api.dto.user.UserResponseDto;
import com.elyashevich.bank.entity.Account;
import com.elyashevich.bank.entity.EmailData;
import com.elyashevich.bank.entity.PhoneData;
import com.elyashevich.bank.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserMapper {

    @Mapping(target = "emails", expression = "java(mapSingleEmail(registerDTO.email()))")
    @Mapping(target = "phones", expression = "java(mapSinglePhone(registerDTO.phone()))")
    @Mapping(target = "account", expression = "java(mapSingleBalance(registerDTO.balance()))")
    User toEntity(RegisterDto registerDTO);

    @Mapping(target = "emails", expression = "java(mapSingleEmail(loginDTO.email()))")
    User toEntity(LoginDto loginDTO);

    @Mapping(target = "emails", expression = "java(mapEmailToDto(user.getEmails()))")
    @Mapping(target = "phones", expression = "java(mapPhonesToDto(user.getPhones()))")
    @Mapping(target = "balance", expression = "java(mapBalanceToDto(user.getAccount()))")
    UserResponseDto toDto(User user);

    default List<UserResponseDto> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .toList();
    }

    @Named("mapEmailToDto")
    default List<String> mapEmailToDto(List<EmailData> emailData) {
        return emailData.stream()
                .map(EmailData::getEmail)
                .toList();
    }

    @Named("mapPhonesToDto")
    default List<String> mapPhonesToDto(List<PhoneData> phones) {
        return phones == null ? List.of() : phones.stream()
                .map(PhoneData::getPhone)
                .toList();
    }

    @Named("mapBalanceToDto")
    default BigDecimal mapBalanceToDto(Account account) {
        return account == null ?
                BigDecimal.ZERO : account.getBalance();
    }

    default List<EmailData> mapSingleEmail(String email) {
        return List.of(email == null ? EmailData.builder().build() : EmailData.builder()
                .email(email)
                .build());
    }

    default List<PhoneData> mapSinglePhone(String phone) {
        return List.of(phone == null ? PhoneData.builder().build() : PhoneData.builder()
                .phone(phone)
                .build());
    }

    default Account mapSingleBalance(BigDecimal balance) {
        return Account.builder()
                .balance(balance == null ? BigDecimal.ZERO : balance)
                .build();
    }
}