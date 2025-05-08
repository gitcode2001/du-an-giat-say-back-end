package com.example.duangiatsay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleName name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<Account> accounts;

    // Lấy tên của vai trò
    public String getRoleName() {
        return name.name();
    }

    public String toUpperCase() {
        return name.name().toUpperCase();
    }
}
