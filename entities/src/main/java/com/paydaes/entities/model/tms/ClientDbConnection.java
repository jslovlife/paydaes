package com.paydaes.entities.model.tms;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_db_connections")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ClientDbConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(name = "database_name", nullable = false)
    private String databaseName;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
}
