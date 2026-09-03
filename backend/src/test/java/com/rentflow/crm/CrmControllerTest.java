package com.rentflow.crm;

import com.rentflow.security.SecurityUtils;
import com.rentflow.crm.controller.CrmController;
import com.rentflow.crm.controller.PublicStorefrontInquiryController;
import com.rentflow.crm.dto.CrmDashboardDTO;
import com.rentflow.crm.dto.CrmPipelineDTO;
import com.rentflow.crm.dto.LeadCreateRequest;
import com.rentflow.crm.dto.PublicInquiryRequest;
import com.rentflow.crm.model.LeadStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CrmControllerTest {

    @Autowired
    private CrmController crmController;

    @Autowired
    private PublicStorefrontInquiryController publicController;

    private final String tenantId = "tenant-crm-ctrl-test";

    @BeforeEach
    public void setup() {
        SecurityUtils.setTestTenantId(tenantId);
    }

    @Test
    public void testDashboardAndPipelineEndpoints() {
        LeadCreateRequest req = new LeadCreateRequest();
        req.setFirstName("Helen");
        req.setEmail("helen@test.com");
        req.setEstimatedValue(new BigDecimal("1500.00"));
        crmController.createLead(req);

        ResponseEntity<CrmDashboardDTO> dashResp = crmController.getDashboard();
        assertEquals(200, dashResp.getStatusCode().value());
        assertNotNull(dashResp.getBody());
        assertTrue(dashResp.getBody().getNewLeads() >= 1);

        ResponseEntity<CrmPipelineDTO> pipeResp = crmController.getPipeline();
        assertEquals(200, pipeResp.getStatusCode().value());
        assertNotNull(pipeResp.getBody());
        assertFalse(pipeResp.getBody().getColumns().isEmpty());
    }

    @Test
    public void testPublicStorefrontInquiry() {
        PublicInquiryRequest req = new PublicInquiryRequest();
        req.setName("Ian Malcolm");
        req.setEmail("ian.malcolm@jurassic.org");
        req.setMessage("Need tables for private science dinner");

        ResponseEntity<Map<String, Object>> resp = publicController.submitPublicInquiry("evergreen", req);
        assertEquals(200, resp.getStatusCode().value());
        assertTrue((Boolean) resp.getBody().get("success"));
    }
}
