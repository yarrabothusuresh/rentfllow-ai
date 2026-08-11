package com.rentflow.icp;

import org.springframework.stereotype.Service;

@Service
public class IcpService {

    public IcpProfile getIcpProfile() {
        return new IcpProfile(
            "US Event & Party Rental Company",
            "$500K - $10M",
            "10 - 50",
            "1 - 3",
            "500 - 10,000+",
            "United States",
            "INITIAL_ASSUMPTION"
        );
    }
}
