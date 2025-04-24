package com.example.duangiatsay.model;

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
@Table(name = "laundry_order")
public class LaundryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"account", "orders"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "shipper_id")
    @JsonIgnoreProperties({"user", "shippingOrders", "password", "role"})
    private Account shipper;
    @Column(name = "deleted_by_shipper")
    private Boolean deletedByShipper = false;


    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;


    private String note;
    private Double totalPrice;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("order")
    private List<LaundryItem> items;
}
