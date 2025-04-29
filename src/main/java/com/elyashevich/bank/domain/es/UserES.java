package com.elyashevich.bank.domain.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(indexName = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserES {

    @Id
    private Long id;

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Date, name = "date_of_birth", format = DateFormat.date)
    private LocalDate dateOfBirth;

    @Field(type = FieldType.Keyword, name = "emails")
    private List<String> emails;

    @Field(type = FieldType.Keyword, name = "phones")
    private List<String> phones;

    @Field(type = FieldType.Double, name = "balance")
    private BigDecimal balance;
}