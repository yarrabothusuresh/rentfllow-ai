package com.rentflow.crm.dto;

import com.rentflow.crm.model.LeadStage;

import java.math.BigDecimal;
import java.util.List;

public class CrmPipelineDTO {
    private List<CrmPipelineColumnDTO> columns;
    private long totalLeads;
    private BigDecimal totalPipelineValue;

    public CrmPipelineDTO() {}

    public CrmPipelineDTO(List<CrmPipelineColumnDTO> columns, long totalLeads, BigDecimal totalPipelineValue) {
        this.columns = columns;
        this.totalLeads = totalLeads;
        this.totalPipelineValue = totalPipelineValue;
    }

    public List<CrmPipelineColumnDTO> getColumns() { return columns; }
    public void setColumns(List<CrmPipelineColumnDTO> columns) { this.columns = columns; }

    public long getTotalLeads() { return totalLeads; }
    public void setTotalLeads(long totalLeads) { this.totalLeads = totalLeads; }

    public BigDecimal getTotalPipelineValue() { return totalPipelineValue; }
    public void setTotalPipelineValue(BigDecimal totalPipelineValue) { this.totalPipelineValue = totalPipelineValue; }

    public static class CrmPipelineColumnDTO {
        private LeadStage stage;
        private String stageName;
        private long count;
        private BigDecimal totalValue;
        private List<LeadSummaryDTO> leads;

        public CrmPipelineColumnDTO() {}

        public CrmPipelineColumnDTO(LeadStage stage, String stageName, long count, BigDecimal totalValue, List<LeadSummaryDTO> leads) {
            this.stage = stage;
            this.stageName = stageName;
            this.count = count;
            this.totalValue = totalValue;
            this.leads = leads;
        }

        public LeadStage getStage() { return stage; }
        public void setStage(LeadStage stage) { this.stage = stage; }

        public String getStageName() { return stageName; }
        public void setStageName(String stageName) { this.stageName = stageName; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

        public List<LeadSummaryDTO> getLeads() { return leads; }
        public void setLeads(List<LeadSummaryDTO> leads) { this.leads = leads; }
    }
}
