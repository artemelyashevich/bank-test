package com.elyashevich.bank.entity;

import com.elyashevich.bank.exception.ResourceAlreadyExistsException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
@Data
@ToString(exclude = "password")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @Column(name = "PASSWORD", length = 500, nullable = false)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailData> emails;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhoneData> phones;

    public void addEmail(EmailData email) {
        if (emails == null) {
            emails = new ArrayList<>();
        }
        if (!emails.isEmpty()) {
            throw new ResourceAlreadyExistsException("User already has email");
        }
        emails.add(email);
        email.setUser(this);
    }

    public void addPhone(PhoneData phone) {
        if (phones == null) {
            phones = new ArrayList<>();
        }
        phones.add(phone);
        phone.setUser(this);
    }
}