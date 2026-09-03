package com.rentflow.crm.dto;

import java.util.List;

public class CrmDashboardDTO {
    private long newLeads;
    private long unassignedLeads;
    private long myActiveLeads;
    private long contactedLeads;
    private long qualifiedLeads;
    private long quotePreparedLeads;
    private long quoteSentLeads;
    private long followUpLeads;
    private long wonLeads;
    private long lostLeads;
    private long totalActiveLeads;

    private long followUpsDueToday;
    private long overdueFollowUps;
    private long upcomingFollowUps;

    private List<LeadSummaryDTO> unassignedQueue;
    private List<LeadFollowUpDTO> priorityFollowUps;
    private List<LeadSummaryDTO> recentLeads;

    public CrmDashboardDTO() {}

    public long getNewLeads() { return newLeads; }
    public void setNewLeads(long newLeads) { this.newLeads = newLeads; }

    public long getUnassignedLeads() { return unassignedLeads; }
    public void setUnassignedLeads(long unassignedLeads) { this.unassignedLeads = unassignedLeads; }

    public long getMyActiveLeads() { return myActiveLeads; }
    public void setMyActiveLeads(long myActiveLeads) { this.myActiveLeads = myActiveLeads; }

    public long getContactedLeads() { return contactedLeads; }
    public void setContactedLeads(long contactedLeads) { this.contactedLeads = contactedLeads; }

    public long getQualifiedLeads() { return qualifiedLeads; }
    public void setQualifiedLeads(long qualifiedLeads) { this.qualifiedLeads = qualifiedLeads; }

    public long getQuotePreparedLeads() { return quotePreparedLeads; }
    public void setQuotePreparedLeads(long quotePreparedLeads) { this.quotePreparedLeads = quotePreparedLeads; }

    public long getQuoteSentLeads() { return quoteSentLeads; }
    public void setQuoteSentLeads(long quoteSentLeads) { this.quoteSentLeads = quoteSentLeads; }

    public long getFollowUpLeads() { return followUpLeads; }
    public void setFollowUpLeads(long followUpLeads) { this.followUpLeads = followUpLeads; }

    public long getWonLeads() { return wonLeads; }
    public void setWonLeads(long wonLeads) { this.wonLeads = wonLeads; }

    public long getLostLeads() { return lostLeads; }
    public void setLostLeads(long lostLeads) { this.lostLeads = lostLeads; }

    public long getTotalActiveLeads() { return totalActiveLeads; }
    public void setTotalActiveLeads(long totalActiveLeads) { this.totalActiveLeads = totalActiveLeads; }

    public long getFollowUpsDueToday() { return followUpsDueToday; }
    public void setFollowUpsDueToday(long followUpsDueToday) { this.followUpsDueToday = followUpsDueToday; }

    public long getOverdueFollowUps() { return overdueFollowUps; }
    public void setOverdueFollowUps(long overdueFollowUps) { this.overdueFollowUps = overdueFollowUps; }

    public long getUpcomingFollowUps() { return upcomingFollowUps; }
    public void setUpcomingFollowUps(long upcomingFollowUps) { this.upcomingFollowUps = upcomingFollowUps; }

    public List<LeadSummaryDTO> getUnassignedQueue() { return unassignedQueue; }
    public void setUnassignedQueue(List<LeadSummaryDTO> unassignedQueue) { this.unassignedQueue = unassignedQueue; }

    public List<LeadFollowUpDTO> getPriorityFollowUps() { return priorityFollowUps; }
    public void setPriorityFollowUps(List<LeadFollowUpDTO> priorityFollowUps) { this.priorityFollowUps = priorityFollowUps; }

    public List<LeadSummaryDTO> getRecentLeads() { return recentLeads; }
    public void setRecentLeads(List<LeadSummaryDTO> recentLeads) { this.recentLeads = recentLeads; }
}
