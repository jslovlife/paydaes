package com.paydaes.entities.model.tms;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public Company(String name, Client client) {
        this.name = name;
        this.client = client;
    }
}
