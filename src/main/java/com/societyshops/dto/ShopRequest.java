package com.societyshops.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalTime;

@Data
public class ShopRequest {
    @NotBlank(message = "Shop name is required")
    private String name;

    private String description;
    private String category;
    private String phone;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String upiId;
}
