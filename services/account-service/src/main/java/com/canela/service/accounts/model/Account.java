package com.canela.service.accounts.model;

import com.canela.service.accounts.util.AccountNumberGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @GeneratedValue(generator = "ACCOUNT_NUMBER")
    @GenericGenerator(name="ACCOUNT_NUMBER", type = AccountNumberGenerator.class)
    @Column(name = "number")
    private String number;

    @Column(
            name = "balance",
            nullable = false
    )
    private Double balance = 0.0;

    @Column(
            name = "holder_id",
            nullable = false
    )
    private String holderId;
}
