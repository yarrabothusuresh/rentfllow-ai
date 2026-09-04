package com.rentflow.crm.model;

public enum LeadSource {
    STOREFRONT_REQUEST,
    AI_STOREFRONT,
    WEBSITE_INQUIRY,
    PHONE,
    EMAIL,
    WALK_IN,
    REFERRAL,
    SOCIAL,
    MANUAL,
    OTHER,
    // Aliases / legacy compatibility
    WEBSITE,
    SOCIAL_MEDIA,
    PARTNER
}
