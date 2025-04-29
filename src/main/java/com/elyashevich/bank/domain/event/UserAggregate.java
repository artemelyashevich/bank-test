package com.elyashevich.bank.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAggregate {

    private Long id;

    private String name;

    private LocalDate dateOfBirth;

    private List<String> emails;

    private List<String> phones;

    private BigDecimal balance;
}
