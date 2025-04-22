package com.example.duangiatsay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String username;

    @Column(name = "pass_word", nullable = false)
    private String password;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(columnDefinition = "TINYINT(1)")
    private Integer locked = 0;

    @ManyToOne
    @JoinColumn(name = "role_id")
    @JsonIgnoreProperties("accounts")
    private Role role;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "shipper")
    @JsonIgnore
    private List<LaundryOrder> shippingOrders;

    public boolean isLocked() {
        return this.locked != null && this.locked == 1;
    }

    public void setLocked(boolean locked) {
        this.locked = locked ? 1 : 0;
    }

    public void setLocked(Integer locked) {
        this.locked = (locked != null && locked == 1) ? 1 : 0;
    }

    public void setDateCreatePassWord(LocalDateTime now) {
        this.dateCreate = now;
    }
}