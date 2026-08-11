package com.rentflow.icp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/icp")
public class IcpController {

    private final IcpService icpService;

    public IcpController(IcpService icpService) {
        this.icpService = icpService;
    }

    @GetMapping("/profile")
    public IcpProfile getProfile() {
        return icpService.getIcpProfile();
    }
}
