package com.elyashevich.bank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EMAIL_DATA", uniqueConstraints = {
    @UniqueConstraint(columnNames = "EMAIL")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "EMAIL", length = 200, nullable = false, unique = true)
    private String email;
}