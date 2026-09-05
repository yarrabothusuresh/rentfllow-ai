package com.rentflow.aisales.dto;

import java.util.List;
import java.util.Map;

public class CopilotDataBlockDTO {

    public enum BlockType {
        KPI,
        TABLE,
        CHART,
        ENTITY,
        ALERT,
        TIMELINE
    }

    private BlockType type;
    private String title;
    private String subtitle;
    private Map<String, Object> metrics; // e.g. value, previousValue, changePct, trend
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private List<Map<String, Object>> chartSeries;
    private String severity; // INFO, WARNING, SUCCESS, DANGER

    public CopilotDataBlockDTO() {}

    public static CopilotDataBlockDTO kpi(String title, Object value, String formattedValue, Double changePct, String trend, String period) {
        CopilotDataBlockDTO b = new CopilotDataBlockDTO();
        b.setType(BlockType.KPI);
        b.setTitle(title);
        b.setSubtitle(period);
        b.setMetrics(Map.of(
            "value", value != null ? value : 0,
            "formattedValue", formattedValue != null ? formattedValue : String.valueOf(value),
            "changePct", changePct != null ? changePct : 0.0,
            "trend", trend != null ? trend : "NEUTRAL"
        ));
        return b;
    }

    public static CopilotDataBlockDTO table(String title, List<String> columns, List<Map<String, Object>> rows) {
        CopilotDataBlockDTO b = new CopilotDataBlockDTO();
        b.setType(BlockType.TABLE);
        b.setTitle(title);
        b.setColumns(columns);
        b.setRows(rows);
        return b;
    }

    public static CopilotDataBlockDTO chart(String title, String subtitle, List<Map<String, Object>> chartSeries) {
        CopilotDataBlockDTO b = new CopilotDataBlockDTO();
        b.setType(BlockType.CHART);
        b.setTitle(title);
        b.setSubtitle(subtitle);
        b.setChartSeries(chartSeries);
        return b;
    }

    public static CopilotDataBlockDTO alert(String title, String message, String severity) {
        CopilotDataBlockDTO b = new CopilotDataBlockDTO();
        b.setType(BlockType.ALERT);
        b.setTitle(title);
        b.setSubtitle(message);
        b.setSeverity(severity);
        return b;
    }

    public BlockType getType() { return type; }
    public void setType(BlockType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }

    public List<Map<String, Object>> getChartSeries() { return chartSeries; }
    public void setChartSeries(List<Map<String, Object>> chartSeries) { this.chartSeries = chartSeries; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
