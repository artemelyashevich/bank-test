package com.elyashevich.bank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PHONE_DATA", uniqueConstraints = {
    @UniqueConstraint(columnNames = "PHONE")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "PHONE", length = 13, nullable = false, unique = true)
    private String phone;
}