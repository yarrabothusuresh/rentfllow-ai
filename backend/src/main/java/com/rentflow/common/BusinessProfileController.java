package com.rentflow.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BusinessProfileController {

    @GetMapping("/business/profile")
    public Map<String, Object> getProfile() {
        return Map.of(
            "name", "Evergreen Event Rentals",
            "city", "Dallas",
            "state", "Texas",
            "country", "USA",
            "businessType", "Event & Party Rental",
            "employees", 24,
            "locations", 2,
            "products", 1850
        );
    }
}
