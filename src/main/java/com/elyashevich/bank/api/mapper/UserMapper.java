package com.elyashevich.bank.api.mapper;

import com.elyashevich.bank.api.dto.auth.LoginDto;
import com.elyashevich.bank.api.dto.auth.RegisterDto;
import com.elyashevich.bank.entity.EmailData;
import com.elyashevich.bank.entity.PhoneData;
import com.elyashevich.bank.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        imports = {List.class, EmailData.class, PhoneData.class}
)
public interface UserMapper {

    @Mapping(target = "emails", expression = "java(mapEmail(registerDTO.email()))")
    @Mapping(target = "phones", expression = "java(mapPhone(registerDTO.phone()))")
    User toEntity(RegisterDto registerDTO);

    @Mapping(target = "emails", expression = "java(mapEmail(loginDTO.email()))")
    User toEntity(LoginDto loginDTO);

    default List<EmailData> mapEmail(String email) {
        if (email == null) {
            return null;
        }
        return List.of(EmailData.builder()
                .email(email)
                .build());
    }

    default List<PhoneData> mapPhone(String phone) {
        if (phone == null) {
            return null;
        }
        return List.of(PhoneData.builder()
                .phone(phone)
                .build());
    }
}