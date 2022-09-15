package com.example.demo.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(of = "id")
@Table(name = "users")
@EntityListeners({AuditingEntityListener.class})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long twitchId;
}
