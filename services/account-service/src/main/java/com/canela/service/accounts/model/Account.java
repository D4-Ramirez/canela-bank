package com.canela.service.accounts.model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "number")
    private Long number;

    @Column(
            name = "balance",
            nullable = false
    )
    private Double balance;

    @Column(
            name = "holder_id",
            nullable = false
    )
    private String holderId;
}
