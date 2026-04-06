package com.societyshops.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.societyshops.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "shops")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private User owner;

    @Column(nullable = false)
    private String name;

    private String description;
    private String category;
    private String phone;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String upiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopStatus status = ShopStatus.CLOSED;

    @Column(nullable = false)
    private Boolean isApproved = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Inventory> inventoryItems;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Favorite> favorites;
}
