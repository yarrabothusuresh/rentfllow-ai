-- ====================================================================
-- RentFlow AI - V1__initial_schema.sql
-- Authoritative Initial Baseline Schema (114 Core Business Tables)
-- ====================================================================

-- Section 1: Tables
CREATE TABLE ai_copilot_action_proposals (id uuid not null, action_type varchar(255) not null check (action_type in ('CREATE_LEAD_FOLLOW_UP','ADD_INTERNAL_LEAD_NOTE','ADD_INTERNAL_BOOKING_NOTE','ASSIGN_LEAD','ASSIGN_DRIVER','ASSIGN_WAREHOUSE_OPERATOR','SEND_NOTIFICATION_TEMPLATE')), confirmed_at timestamp(6), conversation_id uuid not null, created_at timestamp(6) not null, error_message varchar(1000), executed_at timestamp(6), expires_at timestamp(6) not null, risk_level varchar(255) not null check (risk_level in ('READ_ONLY','LOW','MEDIUM','HIGH','PROHIBITED')), safe_payload_json varchar(4000), status varchar(255) not null check (status in ('PROPOSED','CONFIRMED','EXECUTING','EXECUTED','CANCELLED','EXPIRED','FAILED')), summary varchar(500), target_id varchar(255) not null, target_type varchar(255) not null, tenant_id varchar(255) not null, user_id varchar(255), primary key (id));

CREATE TABLE ai_escalations (id uuid not null, assigned_to varchar(255), conversation_id uuid not null, created_at timestamp(6) not null, priority varchar(255) not null check (priority in ('LOW','MEDIUM','HIGH','URGENT')), reason varchar(255) not null check (reason in ('CUSTOMER_REQUEST','AI_UNCERTAIN','PRICING_EXCEPTION','LOW_MARGIN','UNAVAILABLE_INVENTORY','COMPLEX_EVENT','CUSTOM_PRODUCT','DISPUTE','COMPLAINT','OTHER')), resolution_notes varchar(2000), resolved_at timestamp(6), status varchar(255) not null check (status in ('OPEN','ASSIGNED','RESOLVED','CLOSED')), summary varchar(2000) not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE ai_feedback (id uuid not null, comments varchar(2000), conversation_id uuid not null, created_at timestamp(6) not null, helpful boolean not null, message_id uuid, reason varchar(255), submitted_by varchar(255), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE ai_prompt_templates (id uuid not null, active boolean not null, content varchar(8000) not null, created_at timestamp(6) not null, created_by varchar(255), name varchar(255) not null, tenant_id varchar(255), type varchar(255) not null check (type in ('SALES_SYSTEM','INQUIRY_EXTRACTION','QUOTE_ASSISTANCE','RESPONSE_GUARDRAIL')), updated_at timestamp(6), version varchar(255) not null, primary key (id));

CREATE TABLE ai_recommendations (id uuid not null, category varchar(255) not null check (category in ('SALES','CRM','PROFITABILITY','INVENTORY','WAREHOUSE','DELIVERY','RETURNS','MAINTENANCE','DAMAGE','FINANCE','CONTRACT','CUSTOMER','INTEGRATION','OPERATIONS')), created_at timestamp(6) not null, detailed_explanation varchar(4000), dismiss_reason varchar(255), evidence_json varchar(4000), evidence_strength varchar(255) not null check (evidence_strength in ('LOW','MEDIUM','HIGH')), expires_at timestamp(6), generated_by varchar(255) not null check (generated_by in ('RULE','RULE_PLUS_AI')), priority varchar(255) not null check (priority in ('LOW','MEDIUM','HIGH','CRITICAL')), recommendation_number varchar(255) not null, resolved_at timestamp(6), reviewed_at timestamp(6), reviewed_by varchar(255), rule_id uuid, signal_id uuid, signal_type varchar(255) not null check (signal_type in ('LEAD_FOLLOW_UP_OVERDUE','CRM_LEAD_OVERDUE_FOLLOW_UP','LEAD_UNASSIGNED','CRM_HIGH_VALUE_LEAD_UNTOUCHED','QUOTE_EXPIRING','QUOTE_EXPIRING_SOON','QUOTE_EXPIRED_PENDING_ACTION','QUOTE_AWAITING_RESPONSE','QUOTE_AWAITING_CUSTOMER_RESPONSE','QUOTE_LOW_MARGIN','QUOTE_MARGIN_LOW','BOOKING_LOW_MARGIN','BOOKING_MARGIN_LOW','BOOKING_NEGATIVE_MARGIN','INVENTORY_HIGH_UTILIZATION','INVENTORY_REPEATED_CONFLICT','INVENTORY_LOW_AVAILABILITY','WAREHOUSE_ORDER_BLOCKED','WAREHOUSE_STAGING_BLOCKED','WAREHOUSE_ORDER_UNASSIGNED','WAREHOUSE_SHORTAGE','WAREHOUSE_SHORTAGE_DETECTED','WAREHOUSE_PACKING_DELAY','DELIVERY_UNASSIGNED','DELIVERY_MISSING_DRIVER','DELIVERY_AT_RISK','DELIVERY_AT_RISK_TOMORROW','RETURN_OVERDUE','INSPECTION_BACKLOG','DAMAGE_CLAIM_REVIEW_REQUIRED','DAMAGE_CLAIM_PENDING_ESTIMATE','HIGH_VALUE_DAMAGE_CLAIM','DAMAGE_CLAIM_OPEN_OVER_THRESHOLD','MAINTENANCE_BACKLOG','MAINTENANCE_TICKET_OVERDUE','CONTRACT_UNSIGNED','DEPOSIT_UNPAID','INVOICE_OVERDUE','AR_THRESHOLD_EXCEEDED','PAYMENT_FAILED','CUSTOMER_REQUEST_WAITING','INTEGRATION_EVENT_FAILED','INTEGRATION_DEAD_LETTER','INTEGRATION_DEAD_LETTER_EVENT','WEBHOOK_REPEATED_FAILURE','INTEGRATION_WEBHOOK_FAILED_MULTIPLE')), source_entity_id varchar(255) not null, source_entity_number varchar(255), source_entity_type varchar(255) not null, status varchar(255) not null check (status in ('NEW','REVIEWED','APPROVED','REJECTED','DISMISSED','EXECUTING','EXECUTED','FAILED','RESOLVED','EXPIRED')), suggested_action_payload_json varchar(4000), suggested_action_type varchar(255) check (suggested_action_type in ('CREATE_INTERNAL_TASK','CREATE_LEAD_FOLLOW_UP','SEND_QUOTE_REMINDER','SEND_CONTRACT_REMINDER','SEND_DEPOSIT_REMINDER','SEND_INVOICE_REMINDER','ASSIGN_DRIVER','ASSIGN_WAREHOUSE_OPERATOR','RETRY_WEBHOOK_DELIVERY','RETRY_INTEGRATION_EVENT','CREATE_MAINTENANCE_REVIEW_TASK','CREATE_DAMAGE_REVIEW_TASK')), summary varchar(1000) not null, tenant_id varchar(255) not null, title varchar(255) not null, updated_at timestamp(6) not null, why_important varchar(2000), primary key (id));

CREATE TABLE ai_rental_inquiries (id uuid not null, availability_checked boolean not null, budget numeric(10,2), chair_preference varchar(255), city varchar(255), company_name varchar(255), complete boolean not null, conversation_id uuid not null, created_at timestamp(6) not null, customer_id uuid, customer_name varchar(255), delivery_address varchar(255), delivery_city varchar(255), delivery_required boolean not null, delivery_time varchar(255), email varchar(255), estimate_generated boolean not null, event_date date, event_name varchar(255), event_type varchar(255), guest_count integer, lead_created boolean not null, lead_id uuid, missing_fields varchar(1000), notes varchar(2000), phone varchar(255), pickup_required boolean not null, product_preferences varchar(255), product_requirements varchar(255), quote_draft_created boolean not null, quote_id uuid, rental_end timestamp(6), rental_request_created boolean not null, rental_request_id uuid, rental_start timestamp(6), requested_items_json varchar(2000), setup_required boolean not null, style_preferences varchar(255), table_preference varchar(255), tenant_id varchar(255) not null, updated_at timestamp(6), venue_address varchar(255), venue_name varchar(255), primary key (id));

CREATE TABLE ai_sales_conversations (id uuid not null, assigned_sales_user_id varchar(255), channel varchar(255) not null check (channel in ('INTERNAL','INTERNAL_SALES','CUSTOMER_PORTAL','STOREFRONT','WEB_CHAT','EMAIL','SMS','PHONE','WHATSAPP')), closed_at timestamp(6), completed_at timestamp(6), conversation_type varchar(255) not null check (conversation_type in ('SALES_AGENT','COPILOT')), created_at timestamp(6) not null, customer_email varchar(255), customer_id uuid, customer_name varchar(255), detected_intent varchar(255) check (detected_intent in ('PRODUCT_SEARCH','AVAILABILITY_CHECK','RENTAL_RECOMMENDATION','PRICE_ESTIMATE','QUOTE_REQUEST','QUOTE_STATUS','BOOKING_QUESTION','GENERAL_RENTAL_QUESTION','HUMAN_ASSISTANCE','UNKNOWN')), employee_role varchar(255), employee_user_id varchar(255), internal_notes varchar(2000), last_message_at timestamp(6), lead_id uuid, locale varchar(255), model varchar(255), page_context_id varchar(255), page_context_type varchar(255), prompt_version varchar(255), provider varchar(255), public_id varchar(255) not null, quote_id uuid, rental_request_id uuid, started_at timestamp(6) not null, status varchar(255) not null check (status in ('ACTIVE','WAITING_FOR_CUSTOMER','WAITING_FOR_AGENT','WAITING_FOR_HUMAN','HUMAN_ACTIVE','QUOTE_DRAFTED','COMPLETED','ABANDONED','CLOSED','ESCALATED')), storefront_session_id varchar(255), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE ai_sales_messages (id uuid not null, content varchar(4000) not null, conversation_id uuid not null, created_at timestamp(6) not null, customer_visible boolean not null, latency_ms bigint, message_type varchar(255) not null check (message_type in ('TEXT','TOOL_CALL','TOOL_RESULT','SYSTEM_EVENT')), role varchar(255) check (role in ('USER','ASSISTANT','SYSTEM','TOOL')), sender_type varchar(255) not null check (sender_type in ('CUSTOMER','AI','SALES_USER','EMPLOYEE','SYSTEM')), structured_data varchar(8000), tenant_id varchar(255) not null, token_count integer, tool_call_reference varchar(255), tool_name varchar(255), primary key (id));

CREATE TABLE ai_tenant_settings (id uuid not null, ai_enabled boolean not null, ai_model varchar(255) not null, ai_provider varchar(255) not null, api_key_masked varchar(255), created_at timestamp(6) not null, customer_ai_enabled boolean not null, daily_request_limit integer not null, human_quote_approval_required boolean not null, internal_sales_assistant_enabled boolean not null, low_margin_threshold_pct float(53) not null, max_conversation_messages integer not null, target_gross_margin_pct float(53) not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE ai_usage_records (id uuid not null, conversation_id uuid, created_at timestamp(6) not null, estimated_cost numeric(10,4), input_tokens integer, latency_ms bigint, model varchar(255) not null, operation varchar(255) not null, output_tokens integer, provider varchar(255) not null, success boolean not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE app_user (id uuid not null, active boolean not null, created_at timestamp(6), email varchar(255) not null, name varchar(255), password_hash varchar(255) not null, updated_at timestamp(6), tenant_id uuid, primary key (id));

CREATE TABLE automation_approvals (id uuid not null, action_payload_json varchar(4000), action_type varchar(255) not null check (action_type in ('CREATE_INTERNAL_TASK','CREATE_LEAD_FOLLOW_UP','SEND_QUOTE_REMINDER','SEND_CONTRACT_REMINDER','SEND_DEPOSIT_REMINDER','SEND_INVOICE_REMINDER','ASSIGN_DRIVER','ASSIGN_WAREHOUSE_OPERATOR','RETRY_WEBHOOK_DELIVERY','RETRY_INTEGRATION_EVENT','CREATE_MAINTENANCE_REVIEW_TASK','CREATE_DAMAGE_REVIEW_TASK')), approval_reason varchar(255), approved_by varchar(255), created_at timestamp(6) not null, decided_at timestamp(6), expires_at timestamp(6), recommendation_id uuid not null, rejection_reason varchar(255), requested_by varchar(255) not null, status varchar(255) not null check (status in ('PENDING','APPROVED','REJECTED','EXPIRED')), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE automation_audits (id uuid not null, action varchar(255) not null, created_at timestamp(6) not null, details_json varchar(4000), entity_id varchar(255) not null, entity_type varchar(255) not null, performed_by varchar(255) not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE automation_executions (id uuid not null, action_payload_json varchar(4000), action_type varchar(255) not null check (action_type in ('CREATE_INTERNAL_TASK','CREATE_LEAD_FOLLOW_UP','SEND_QUOTE_REMINDER','SEND_CONTRACT_REMINDER','SEND_DEPOSIT_REMINDER','SEND_INVOICE_REMINDER','ASSIGN_DRIVER','ASSIGN_WAREHOUSE_OPERATOR','RETRY_WEBHOOK_DELIVERY','RETRY_INTEGRATION_EVENT','CREATE_MAINTENANCE_REVIEW_TASK','CREATE_DAMAGE_REVIEW_TASK')), approval_id uuid, attempt_count integer not null, completed_at timestamp(6), created_at timestamp(6) not null, error_details varchar(4000), executed_by varchar(255) not null, execution_status varchar(255) not null check (execution_status in ('PROPOSED','WAITING_APPROVAL','APPROVED','EXECUTING','EXECUTED','REJECTED','CANCELLED','FAILED','EXPIRED','SKIPPED','STALE')), idempotency_key varchar(255) not null, recommendation_id uuid, result_summary varchar(2000), rule_id uuid, started_at timestamp(6), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE automation_rules (id uuid not null, action_type varchar(255) check (action_type in ('CREATE_INTERNAL_TASK','CREATE_LEAD_FOLLOW_UP','SEND_QUOTE_REMINDER','SEND_CONTRACT_REMINDER','SEND_DEPOSIT_REMINDER','SEND_INVOICE_REMINDER','ASSIGN_DRIVER','ASSIGN_WAREHOUSE_OPERATOR','RETRY_WEBHOOK_DELIVERY','RETRY_INTEGRATION_EVENT','CREATE_MAINTENANCE_REVIEW_TASK','CREATE_DAMAGE_REVIEW_TASK')), allowed_roles varchar(255), conditions_json varchar(2000), cooldown_minutes integer not null, created_at timestamp(6) not null, description varchar(1000), enabled boolean not null, max_executions_per_day integer not null, mode varchar(255) not null check (mode in ('RECOMMEND_ONLY','APPROVAL_REQUIRED','AUTO_EXECUTE_LOW_RISK')), name varchar(255) not null, rule_code varchar(255) not null, signal_type varchar(255) not null check (signal_type in ('LEAD_FOLLOW_UP_OVERDUE','CRM_LEAD_OVERDUE_FOLLOW_UP','LEAD_UNASSIGNED','CRM_HIGH_VALUE_LEAD_UNTOUCHED','QUOTE_EXPIRING','QUOTE_EXPIRING_SOON','QUOTE_EXPIRED_PENDING_ACTION','QUOTE_AWAITING_RESPONSE','QUOTE_AWAITING_CUSTOMER_RESPONSE','QUOTE_LOW_MARGIN','QUOTE_MARGIN_LOW','BOOKING_LOW_MARGIN','BOOKING_MARGIN_LOW','BOOKING_NEGATIVE_MARGIN','INVENTORY_HIGH_UTILIZATION','INVENTORY_REPEATED_CONFLICT','INVENTORY_LOW_AVAILABILITY','WAREHOUSE_ORDER_BLOCKED','WAREHOUSE_STAGING_BLOCKED','WAREHOUSE_ORDER_UNASSIGNED','WAREHOUSE_SHORTAGE','WAREHOUSE_SHORTAGE_DETECTED','WAREHOUSE_PACKING_DELAY','DELIVERY_UNASSIGNED','DELIVERY_MISSING_DRIVER','DELIVERY_AT_RISK','DELIVERY_AT_RISK_TOMORROW','RETURN_OVERDUE','INSPECTION_BACKLOG','DAMAGE_CLAIM_REVIEW_REQUIRED','DAMAGE_CLAIM_PENDING_ESTIMATE','HIGH_VALUE_DAMAGE_CLAIM','DAMAGE_CLAIM_OPEN_OVER_THRESHOLD','MAINTENANCE_BACKLOG','MAINTENANCE_TICKET_OVERDUE','CONTRACT_UNSIGNED','DEPOSIT_UNPAID','INVOICE_OVERDUE','AR_THRESHOLD_EXCEEDED','PAYMENT_FAILED','CUSTOMER_REQUEST_WAITING','INTEGRATION_EVENT_FAILED','INTEGRATION_DEAD_LETTER','INTEGRATION_DEAD_LETTER_EVENT','WEBHOOK_REPEATED_FAILURE','INTEGRATION_WEBHOOK_FAILED_MULTIPLE')), tenant_id varchar(255) not null, trigger_type varchar(255) not null check (trigger_type in ('EVENT','SCHEDULED','MANUAL','SIGNAL_CREATED')), updated_at timestamp(6) not null, primary key (id));

CREATE TABLE booking (id uuid not null, balance_due numeric(10,2), booking_date date not null, booking_number varchar(255) not null, breakdown_fee numeric(10,2), contract_signed boolean, created_at timestamp(6), created_by varchar(255), customer_id uuid not null, delivery_fee numeric(10,2), deposit_paid numeric(10,2), deposit_required numeric(10,2), discount_amount numeric(10,2), event_id uuid not null, internal_notes varchar(2000), notes varchar(2000), pickup_fee numeric(10,2), quote_id uuid not null, rental_end_date_time timestamp(6) not null, rental_start_date_time timestamp(6) not null, service_fee numeric(10,2), setup_fee numeric(10,2), status varchar(255) not null check (status in ('PENDING','CONFIRMED','DEPOSIT_PENDING','PARTIALLY_PAID','PAID','READY_FOR_FULFILLMENT','IN_PROGRESS','DELIVERED','READY_FOR_PICKUP','PICKED_UP','INSPECTING','RETURNED','COMPLETED','CANCELLED','NO_SHOW')), subtotal numeric(10,2), tax_amount numeric(10,2), tenant_id varchar(255) not null, total_amount numeric(10,2), updated_at timestamp(6), primary key (id));

CREATE TABLE booking_item (id uuid not null, booking_id uuid not null, created_at timestamp(6), description varchar(255) not null, line_subtotal numeric(10,2), product_id uuid not null, quantity integer not null, rental_end_date_time timestamp(6) not null, rental_start_date_time timestamp(6) not null, unit_price numeric(10,2) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE business_signals (id uuid not null, category varchar(255) not null check (category in ('SALES','CRM','PROFITABILITY','INVENTORY','WAREHOUSE','DELIVERY','RETURNS','MAINTENANCE','DAMAGE','FINANCE','CONTRACT','CUSTOMER','INTEGRATION','OPERATIONS')), created_at timestamp(6) not null, dedupe_key varchar(255) not null, detected_at timestamp(6) not null, evidence_json varchar(4000), expires_at timestamp(6), last_detected_at timestamp(6) not null, resolved_at timestamp(6), severity varchar(255) not null check (severity in ('LOW','MEDIUM','HIGH','CRITICAL')), signal_type varchar(255) not null check (signal_type in ('LEAD_FOLLOW_UP_OVERDUE','CRM_LEAD_OVERDUE_FOLLOW_UP','LEAD_UNASSIGNED','CRM_HIGH_VALUE_LEAD_UNTOUCHED','QUOTE_EXPIRING','QUOTE_EXPIRING_SOON','QUOTE_EXPIRED_PENDING_ACTION','QUOTE_AWAITING_RESPONSE','QUOTE_AWAITING_CUSTOMER_RESPONSE','QUOTE_LOW_MARGIN','QUOTE_MARGIN_LOW','BOOKING_LOW_MARGIN','BOOKING_MARGIN_LOW','BOOKING_NEGATIVE_MARGIN','INVENTORY_HIGH_UTILIZATION','INVENTORY_REPEATED_CONFLICT','INVENTORY_LOW_AVAILABILITY','WAREHOUSE_ORDER_BLOCKED','WAREHOUSE_STAGING_BLOCKED','WAREHOUSE_ORDER_UNASSIGNED','WAREHOUSE_SHORTAGE','WAREHOUSE_SHORTAGE_DETECTED','WAREHOUSE_PACKING_DELAY','DELIVERY_UNASSIGNED','DELIVERY_MISSING_DRIVER','DELIVERY_AT_RISK','DELIVERY_AT_RISK_TOMORROW','RETURN_OVERDUE','INSPECTION_BACKLOG','DAMAGE_CLAIM_REVIEW_REQUIRED','DAMAGE_CLAIM_PENDING_ESTIMATE','HIGH_VALUE_DAMAGE_CLAIM','DAMAGE_CLAIM_OPEN_OVER_THRESHOLD','MAINTENANCE_BACKLOG','MAINTENANCE_TICKET_OVERDUE','CONTRACT_UNSIGNED','DEPOSIT_UNPAID','INVOICE_OVERDUE','AR_THRESHOLD_EXCEEDED','PAYMENT_FAILED','CUSTOMER_REQUEST_WAITING','INTEGRATION_EVENT_FAILED','INTEGRATION_DEAD_LETTER','INTEGRATION_DEAD_LETTER_EVENT','WEBHOOK_REPEATED_FAILURE','INTEGRATION_WEBHOOK_FAILED_MULTIPLE')), source_entity_id varchar(255) not null, source_entity_number varchar(255), source_entity_type varchar(255) not null, status varchar(255) not null check (status in ('ACTIVE','RESOLVED','IGNORED','EXPIRED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE claim_audits (id uuid not null, action varchar(255) not null, claim_id uuid, details varchar(2000), performed_by varchar(255) not null, tenant_id varchar(255) not null, timestamp timestamp(6) not null, primary key (id));

CREATE TABLE claim_estimates (id uuid not null, claim_id uuid not null, created_at timestamp(6), created_by varchar(255), currency varchar(255) not null, discount numeric(10,2), labor_cost numeric(10,2), notes varchar(2000), other_cost numeric(10,2), repair_cost numeric(10,2), replacement_cost numeric(10,2), subtotal numeric(10,2), tax numeric(10,2), tenant_id varchar(255) not null, total numeric(10,2), transport_cost numeric(10,2), version integer not null, primary key (id));

CREATE TABLE crm_leads (id uuid not null, assigned_sales_user_id varchar(255), assigned_sales_user_name varchar(255), booking_id uuid, booking_number varchar(255), company_name varchar(255), contact_name varchar(255), converted_at timestamp(6), created_at timestamp(6), created_by varchar(255), customer_id uuid, customer_name varchar(255), customer_notes varchar(4000), email varchar(255) not null, estimated_budget numeric(38,2), estimated_value numeric(38,2), event_date date, event_name varchar(255), event_type varchar(255) check (event_type in ('WEDDING','BIRTHDAY','CORPORATE','CONFERENCE','FESTIVAL','GRADUATION','BABY_SHOWER','PRIVATE_PARTY','OTHER')), first_name varchar(255) not null, guest_count integer, internal_notes varchar(4000), last_contacted_at timestamp(6), last_name varchar(255), lead_number varchar(255) not null, lost_at timestamp(6), lost_reason varchar(255) check (lost_reason in ('PRICE','AVAILABILITY','DATE_CHANGED','COMPETITOR','NO_RESPONSE','EVENT_CANCELLED','NOT_A_FIT','DUPLICATE','CUSTOMER_CANCELLED','OTHER')), lost_reason_notes varchar(2000), next_follow_up_at timestamp(6), phone varchar(255), preferred_contact_method varchar(255), priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), qualified_at timestamp(6), quote_id uuid, quote_number varchar(255), rental_end_date date, rental_request_id uuid, rental_start_date date, reopen_reason varchar(2000), reopened_at timestamp(6), source varchar(255) not null check (source in ('STOREFRONT_REQUEST','AI_STOREFRONT','WEBSITE_INQUIRY','PHONE','EMAIL','WALK_IN','REFERRAL','SOCIAL','MANUAL','OTHER','WEBSITE','SOCIAL_MEDIA','PARTNER')), stage varchar(255) not null check (stage in ('NEW','CONTACTED','NEEDS_DISCOVERY','QUALIFIED','QUOTE_PREPARED','QUOTE_SENT','FOLLOW_UP','NEGOTIATION','WON','LOST','DISQUALIFIED')), tenant_id varchar(255) not null, updated_at timestamp(6), updated_by varchar(255), venue_address_snapshot varchar(1000), venue_name varchar(255), won_at timestamp(6), primary key (id));

CREATE TABLE customer_activities (id uuid not null, activity_type varchar(255) not null check (activity_type in ('CATALOG_VIEW','PRODUCT_VIEW','QUOTE_REQUESTED','QUOTE_APPROVED','QUOTE_DECLINED','BOOKING_CREATED','PAYMENT_COMPLETED','MESSAGE_SENT','CLAIM_APPROVED','CLAIM_DISPUTED')), created_at timestamp(6), customer_id uuid not null, description varchar(2000), reference_id uuid, reference_type varchar(255), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE customer_addresses (id uuid not null, address_line1 varchar(255) not null, address_line2 varchar(255), address_type varchar(255) not null check (address_type in ('HOME','OFFICE','VENUE','OTHER')), city varchar(255) not null, contact_person varchar(255), country varchar(255), created_at timestamp(6), customer_id uuid not null, delivery_instructions varchar(1000), is_default boolean not null, phone varchar(255), state varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6), zip_code varchar(255) not null, primary key (id));

CREATE TABLE customer_conversations (id uuid not null, booking_id uuid, created_at timestamp(6), customer_id uuid not null, quote_id uuid, status varchar(255) not null check (status in ('OPEN','WAITING_FOR_CUSTOMER','WAITING_FOR_STAFF','CLOSED')), subject varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE customer_messages (id uuid not null, created_at timestamp(6), message varchar(4000) not null, read_at timestamp(6), sender_id varchar(255), sender_type varchar(255) not null, conversation_id uuid not null, primary key (id));

CREATE TABLE customer_requests (id uuid not null, booking_id uuid, created_at timestamp(6), customer_id uuid not null, message varchar(2000) not null, quote_id uuid, request_type varchar(255) not null check (request_type in ('QUOTE_CHANGE','DELIVERY_QUESTION','BOOKING_QUESTION','BILLING_QUESTION','GENERAL')), status varchar(255) not null check (status in ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')), subject varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE customer_users (id uuid not null, active boolean not null, created_at timestamp(6), customer_id uuid not null, email varchar(255) not null, password_hash varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6), user_id uuid not null, primary key (id));

CREATE TABLE customers (id uuid not null, alternate_phone varchar(255), billing_address varchar(255), city varchar(255), company_name varchar(255), country varchar(255), created_at timestamp(6), customer_number varchar(255) not null, customer_type varchar(255) not null check (customer_type in ('INDIVIDUAL','BUSINESS','VENUE','EVENT_PLANNER','CORPORATE','NONPROFIT','OTHER')), email varchar(255) not null, first_name varchar(255) not null, last_name varchar(255), notes varchar(2000), phone varchar(255), shipping_address varchar(255), state varchar(255), status varchar(255) not null check (status in ('ACTIVE','INACTIVE','BLOCKED')), tenant_id varchar(255) not null, updated_at timestamp(6), zip_code varchar(255), primary key (id));

CREATE TABLE damage_claim_items (id uuid not null, approved_cost numeric(10,2), claim_id uuid not null, claim_type varchar(255) not null check (claim_type in ('DAMAGE','MISSING','LOST','REPAIR','REPLACEMENT','MIXED')), condition varchar(255) not null check (condition in ('GOOD','MINOR_DAMAGE','MAJOR_DAMAGE','UNUSABLE')), created_at timestamp(6), damage_category varchar(255) check (damage_category in ('BROKEN','STAINED','SCRATCHED','MISSING_PART','WATER_DAMAGE','OTHER')), description varchar(2000), estimated_cost numeric(10,2), final_cost numeric(10,2), inspection_id uuid, notes varchar(2000), product_id uuid not null, product_name_snapshot varchar(255), quantity integer not null, resolution varchar(255) check (resolution in ('REPAIRED','REPLACED','CUSTOMER_CHARGED','WAIVED','WRITTEN_OFF','NO_ACTION')), return_item_id uuid, severity varchar(255) check (severity in ('MINOR','MAJOR','CRITICAL')), sku_snapshot varchar(255), tenant_id varchar(255) not null, unit_repair_cost numeric(10,2), unit_replacement_cost numeric(10,2), updated_at timestamp(6), primary key (id));

CREATE TABLE damage_claims (id uuid not null, approved_total_cost numeric(10,2), assessed_at timestamp(6), assessed_by varchar(255), booking_id uuid not null, claim_number varchar(255) not null, claim_type varchar(255) not null check (claim_type in ('DAMAGE','MISSING','LOST','REPAIR','REPLACEMENT','MIXED')), created_at timestamp(6), currency varchar(255) not null, customer_id uuid not null, customer_notes varchar(2000), customer_visible boolean not null, description varchar(2000), dispute_reason varchar(2000), disputed_at timestamp(6), disputed_by varchar(255), estimated_total_cost numeric(10,2), final_total_cost numeric(10,2), internal_notes varchar(2000), priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), reported_at timestamp(6), reported_by varchar(255), resolution varchar(255) check (resolution in ('REPAIRED','REPLACED','CUSTOMER_CHARGED','WAIVED','WRITTEN_OFF','NO_ACTION')), resolution_notes varchar(2000), resolved_at timestamp(6), resolved_by varchar(255), return_order_id uuid not null, status varchar(255) not null check (status in ('OPEN','UNDER_REVIEW','ESTIMATE_CREATED','CUSTOMER_REVIEW','APPROVED','DISPUTED','REPAIR_IN_PROGRESS','REPLACEMENT_REQUIRED','RESOLVED','WAIVED','CANCELLED')), tenant_id varchar(255) not null, updated_at timestamp(6), waive_reason varchar(2000), waived_at timestamp(6), waived_by varchar(255), primary key (id));

CREATE TABLE damage_records (id uuid not null, category varchar(255) not null check (category in ('BROKEN','STAINED','SCRATCHED','MISSING_PART','WATER_DAMAGE','OTHER')), created_at timestamp(6), description varchar(2000), estimated_repair_cost numeric(10,2), estimated_replacement_cost numeric(10,2), inspection_id uuid, product_id uuid not null, quantity integer not null, return_item_id uuid, severity varchar(255) not null check (severity in ('MINOR','MAJOR','CRITICAL')), status varchar(255) not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE deliveries (id uuid not null, actual_arrival_time timestamp(6), actual_completion_time timestamp(6), actual_start_time timestamp(6), booking_id uuid not null, created_at timestamp(6), customer_id uuid not null, customer_notes varchar(2000), delivery_address_snapshot varchar(1000), delivery_number varchar(255) not null, delivery_type varchar(255) not null check (delivery_type in ('DELIVERY','PICKUP')), driver_id uuid, estimated_distance float(53), estimated_duration integer, event_id uuid not null, failure_notes varchar(2000), failure_reason varchar(255), latitude float(53), longitude float(53), notes varchar(2000), priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), scheduled_date date, scheduled_end_time varchar(255), scheduled_start_time varchar(255), sequence_number integer, setup_duration_minutes integer, setup_required boolean, status varchar(255) not null check (status in ('PENDING','SCHEDULED','ASSIGNED','READY','OUT_FOR_DELIVERY','ARRIVED','SETUP_IN_PROGRESS','DELIVERED','CANCELLED','FAILED')), tenant_id varchar(255) not null, updated_at timestamp(6), vehicle_id uuid, warehouse_order_id uuid not null, primary key (id));

CREATE TABLE delivery_audits (id uuid not null, action varchar(255) not null, delivery_id uuid, details varchar(2000), performed_by varchar(255), route_id uuid, tenant_id varchar(255) not null, timestamp timestamp(6), primary key (id));

CREATE TABLE delivery_route_stops (id uuid not null, actual_arrival timestamp(6), delivery_id uuid not null, estimated_arrival timestamp(6), route_id uuid not null, sequence_number integer not null, status varchar(255) not null check (status in ('PENDING','SCHEDULED','ASSIGNED','READY','OUT_FOR_DELIVERY','ARRIVED','SETUP_IN_PROGRESS','DELIVERED','CANCELLED','FAILED')), primary key (id));

CREATE TABLE delivery_routes (id uuid not null, created_at timestamp(6), driver_id uuid, estimated_distance float(53), estimated_duration integer, route_date date not null, route_number varchar(255) not null, status varchar(255) not null check (status in ('PLANNED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED')), tenant_id varchar(255) not null, total_deliveries integer, updated_at timestamp(6), vehicle_id uuid, primary key (id));

CREATE TABLE drivers (id uuid not null, active boolean not null, created_at timestamp(6), license_number varchar(255), name varchar(255) not null, phone varchar(255), status varchar(255) not null check (status in ('AVAILABLE','ASSIGNED','ON_DELIVERY','OFF_DUTY')), tenant_id varchar(255) not null, updated_at timestamp(6), user_id uuid, primary key (id));

CREATE TABLE event_requirements (id uuid not null, created_at timestamp(6), description varchar(255) not null, event_id uuid not null, notes varchar(255), quantity integer not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE events (id uuid not null, city varchar(255), created_at timestamp(6), customer_id uuid not null, end_time varchar(255), event_date date not null, event_name varchar(255) not null, event_type varchar(255) not null check (event_type in ('WEDDING','BIRTHDAY','CORPORATE','CONFERENCE','FESTIVAL','GRADUATION','BABY_SHOWER','PRIVATE_PARTY','OTHER')), guest_count integer not null, special_instructions varchar(2000), start_time varchar(255), state varchar(255), status varchar(255) not null check (status in ('PLANNING','QUOTED','BOOKED','PREPARING','IN_PROGRESS','COMPLETED','CANCELLED')), tenant_id varchar(255) not null, updated_at timestamp(6), venue_address varchar(255), venue_name varchar(255), zip_code varchar(255), primary key (id));

CREATE TABLE external_api_key (id uuid not null, created_at timestamp(6) not null, created_by varchar(255), expires_at timestamp(6), key_hash varchar(255) not null, key_prefix varchar(255) not null, last_used_at timestamp(6), name varchar(255) not null, rate_limit_per_minute integer, revoked_at timestamp(6), scopes varchar(2000) not null, status varchar(255) not null check (status in ('ACTIVE','REVOKED','EXPIRED')), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE external_entity_mapping (id uuid not null, created_at timestamp(6) not null, entity_type varchar(255) not null check (entity_type in ('CUSTOMER','INVOICE','PAYMENT','PRODUCT','BOOKING','QUOTE')), external_id varchar(255) not null, internal_id varchar(255) not null, last_error varchar(2000), last_synced_at timestamp(6), provider varchar(255) not null check (provider in ('QUICKBOOKS','XERO','SHOPIFY','HUBSPOT','SALESFORCE','STRIPE','ZAPIER','MAKE','N8N','CUSTOM_WEBHOOK','CUSTOM_API','MOCK_ACCOUNTING','MOCK_CRM','MOCK_COMMERCE')), sync_status varchar(255) not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE inbound_integration_event (id uuid not null, error_message varchar(2000), event_type varchar(255) not null, external_event_id varchar(255) not null, payload varchar(8000) not null, processed_at timestamp(6), provider varchar(255) not null check (provider in ('QUICKBOOKS','XERO','SHOPIFY','HUBSPOT','SALESFORCE','STRIPE','ZAPIER','MAKE','N8N','CUSTOM_WEBHOOK','CUSTOM_API','MOCK_ACCOUNTING','MOCK_CRM','MOCK_COMMERCE')), received_at timestamp(6) not null, status varchar(255) not null check (status in ('RECEIVED','PROCESSING','PROCESSED','IGNORED','FAILED')), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE inspections (id uuid not null, condition varchar(255) not null check (condition in ('GOOD','MINOR_DAMAGE','MAJOR_DAMAGE','UNUSABLE')), damaged_quantity integer not null, good_quantity integer not null, inspected_at timestamp(6), inspected_by varchar(255), inspected_quantity integer not null, missing_quantity integer not null, notes varchar(2000), product_id uuid not null, return_order_id uuid not null, return_order_item_id uuid not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE integration_connection (id uuid not null, configuration varchar(4000), created_at timestamp(6) not null, created_by varchar(255), credential_reference varchar(255), last_connected_at timestamp(6), last_error varchar(2000), last_sync_at timestamp(6), name varchar(255) not null, provider varchar(255) not null check (provider in ('QUICKBOOKS','XERO','SHOPIFY','HUBSPOT','SALESFORCE','STRIPE','ZAPIER','MAKE','N8N','CUSTOM_WEBHOOK','CUSTOM_API','MOCK_ACCOUNTING','MOCK_CRM','MOCK_COMMERCE')), status varchar(255) not null check (status in ('DISCONNECTED','CONNECTING','CONNECTED','ERROR','DISABLED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE integration_event (id uuid not null, aggregate_id varchar(255), aggregate_type varchar(255), created_at timestamp(6) not null, event_id varchar(255) not null, event_type varchar(255) not null, occurred_at timestamp(6) not null, payload varchar(8000) not null, status varchar(255) not null check (status in ('PENDING','PROCESSING','COMPLETED','PARTIAL','FAILED','DEAD_LETTER')), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE integration_outbox (id uuid not null, aggregate_id varchar(255), aggregate_type varchar(255), attempts integer not null, created_at timestamp(6) not null, event_id varchar(255) not null, event_type varchar(255) not null, last_error varchar(2000), next_attempt_at timestamp(6), payload varchar(8000) not null, processed_at timestamp(6), status varchar(255) not null check (status in ('PENDING','PROCESSING','COMPLETED','PARTIAL','FAILED','DEAD_LETTER')), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE integration_sync_job (id uuid not null, completed_at timestamp(6), connection_id uuid not null, created_at timestamp(6) not null, error_summary varchar(4000), provider varchar(255) not null check (provider in ('QUICKBOOKS','XERO','SHOPIFY','HUBSPOT','SALESFORCE','STRIPE','ZAPIER','MAKE','N8N','CUSTOM_WEBHOOK','CUSTOM_API','MOCK_ACCOUNTING','MOCK_CRM','MOCK_COMMERCE')), records_failed integer, records_processed integer, started_at timestamp(6), status varchar(255) not null check (status in ('QUEUED','RUNNING','COMPLETED','PARTIAL','FAILED')), sync_type varchar(255) not null check (sync_type in ('INITIAL_SYNC','INCREMENTAL_SYNC','MANUAL_SYNC','EVENT_DRIVEN')), tenant_id varchar(255) not null, primary key (id));

CREATE TABLE inventory_cycle_count_items (id uuid not null, counted_quantity integer not null, cycle_count_id uuid not null, expected_quantity integer not null, inventory_item_id uuid, notes varchar(255), product_id uuid not null, reason varchar(255), variance integer not null, primary key (id));

CREATE TABLE inventory_cycle_counts (id uuid not null, approved_by varchar(255), assigned_to varchar(255), category_id uuid, count_date timestamp(6) not null, count_number varchar(255) not null, created_at timestamp(6), notes varchar(255), status varchar(255) not null check (status in ('PLANNED','IN_PROGRESS','COMPLETED','APPROVED','CANCELLED')), tenant_id varchar(255) not null, updated_at timestamp(6), warehouse_id uuid not null, primary key (id));

CREATE TABLE inventory_items (id uuid not null, acquisition_date timestamp(6), asset_code varchar(255) not null, barcode varchar(255), condition varchar(255) not null check (condition in ('NEW','EXCELLENT','GOOD','FAIR','DAMAGED','UNUSABLE')), created_at timestamp(6), current_booking_id uuid, current_location varchar(255), last_checked_at timestamp(6), product_id uuid not null, purchase_cost numeric(10,2), qr_code varchar(255), serial_number varchar(255), status varchar(255) not null check (status in ('AVAILABLE','RESERVED','PICKED','OUT_ON_RENT','RETURNED','INSPECTION','DAMAGED','MAINTENANCE','LOST','RETIRED','TRANSFER_PENDING')), tenant_id varchar(255) not null, updated_at timestamp(6), version bigint, warehouse_id uuid not null, primary key (id));

CREATE TABLE inventory_reservations (id uuid not null, booking_id uuid, created_at timestamp(6), created_by varchar(255), end_date_time timestamp(6) not null, event_id uuid, expires_at timestamp(6), inventory_item_id uuid, product_id uuid not null, quantity integer not null, reservation_type varchar(255) not null check (reservation_type in ('BOOKING','HOLD','MAINTENANCE','OTHER')), start_date_time timestamp(6) not null, status varchar(255) not null check (status in ('PENDING','CONFIRMED','RESERVED','HOLD','RELEASED','CANCELLED','EXPIRED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE inventory_transactions (id uuid not null, created_at timestamp(6) not null, created_by varchar(255), notes varchar(255), product_id uuid not null, quantity integer not null, reference_id uuid, reference_type varchar(255), tenant_id varchar(255) not null, transaction_type varchar(255) not null check (transaction_type in ('PURCHASE','ADJUSTMENT','RESERVATION','RELEASE','ALLOCATE','CHECKOUT','RETURN','DAMAGE','LOSS','MAINTENANCE','RESTORED')), primary key (id));

CREATE TABLE inventory_transfer_items (id uuid not null, condition varchar(255) check (condition in ('NEW','EXCELLENT','GOOD','FAIR','DAMAGED','UNUSABLE')), inventory_item_id uuid, notes varchar(255), product_id uuid not null, quantity integer not null, received_quantity integer not null, transfer_id uuid not null, primary key (id));

CREATE TABLE inventory_transfers (id uuid not null, approved_by varchar(255), created_at timestamp(6), from_warehouse_id uuid not null, notes varchar(255), received_at timestamp(6), requested_by varchar(255), shipped_at timestamp(6), status varchar(255) not null check (status in ('DRAFT','REQUESTED','APPROVED','IN_TRANSIT','PARTIALLY_RECEIVED','RECEIVED','CANCELLED')), tenant_id varchar(255) not null, to_warehouse_id uuid not null, transfer_number varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE invoice_audits (id uuid not null, action varchar(255) not null, booking_id uuid, details varchar(2000), invoice_id uuid, performed_by varchar(255), tenant_id varchar(255) not null, timestamp timestamp(6) not null, primary key (id));

CREATE TABLE invoice_items (id uuid not null, created_at timestamp(6) not null, description varchar(255) not null, discount numeric(10,2), invoice_id uuid not null, line_total numeric(10,2) not null, product_id uuid, quantity integer not null, tax numeric(10,2), unit_price numeric(10,2) not null, primary key (id));

CREATE TABLE invoices (id uuid not null, amount_paid numeric(10,2) not null, balance_due numeric(10,2) not null, billing_address varchar(255), booking_id uuid not null, city varchar(255), company_name varchar(255), country varchar(255), created_at timestamp(6) not null, created_by varchar(255), customer_id uuid not null, customer_name varchar(255), discount numeric(10,2), due_date date not null, email varchar(255), fees numeric(10,2), invoice_number varchar(255) not null, issue_date date not null, notes varchar(2000), phone varchar(255), state varchar(255), status varchar(255) not null check (status in ('DRAFT','SENT','PARTIALLY_PAID','PAID','OVERDUE','VOID')), subtotal numeric(10,2) not null, tax numeric(10,2), tenant_id varchar(255) not null, total_amount numeric(10,2) not null, updated_at timestamp(6) not null, zip_code varchar(255), primary key (id));

CREATE TABLE kit_components (id uuid not null, component_name varchar(255) not null, component_product_id uuid not null, component_sku varchar(255), created_at timestamp(6), kit_definition_id uuid not null, quantity_per_kit integer not null, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE kit_definitions (id uuid not null, created_at timestamp(6), description varchar(255), name varchar(255) not null, product_id uuid not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE lead_activities (id uuid not null, call_outcome varchar(255) check (call_outcome in ('CONNECTED','NO_ANSWER','VOICEMAIL','FOLLOW_UP_REQUIRED')), created_at timestamp(6), created_by varchar(255), direction varchar(255) check (direction in ('INBOUND','OUTBOUND','INTERNAL')), lead_id uuid not null, notes varchar(4000), occurred_at timestamp(6) not null, reference_id varchar(255), reference_type varchar(255), subject varchar(255), summary varchar(2000), tenant_id varchar(255) not null, type varchar(255) not null check (type in ('NOTE','CALL','EMAIL','SMS','MEETING','STATUS_CHANGE','ASSIGNMENT','FOLLOW_UP_CREATED','FOLLOW_UP_COMPLETED','RENTAL_REQUEST_LINKED','CUSTOMER_LINKED','CUSTOMER_CREATED','QUOTE_CREATED','QUOTE_SENT','WON','LOST','REOPENED','AI_CONVERSATION_STARTED','AI_REQUIREMENTS_CAPTURED','AI_RENTAL_REQUEST_CREATED','AI_QUOTE_DRAFT_CREATED','AI_HUMAN_HANDOFF')), primary key (id));

CREATE TABLE lead_follow_ups (id uuid not null, assigned_to varchar(255), assigned_to_name varchar(255), completed_at timestamp(6), completed_by varchar(255), created_at timestamp(6), created_by varchar(255), due_at timestamp(6) not null, lead_id uuid not null, notes varchar(2000), status varchar(255) not null check (status in ('OPEN','COMPLETED','CANCELLED','OVERDUE')), tenant_id varchar(255) not null, title varchar(255) not null, type varchar(255) not null check (type in ('CALL','EMAIL','SMS','MEETING','QUOTE','GENERAL','OTHER')), updated_at timestamp(6), primary key (id));

CREATE TABLE leads (id uuid not null, assigned_to varchar(255), company_name varchar(255), created_at timestamp(6), email varchar(255) not null, event_date date, event_type varchar(255) check (event_type in ('WEDDING','BIRTHDAY','CORPORATE','CONFERENCE','FESTIVAL','GRADUATION','BABY_SHOWER','PRIVATE_PARTY','OTHER')), first_name varchar(255) not null, guest_count integer, last_name varchar(255), notes varchar(2000), phone varchar(255), source varchar(255) check (source in ('WEBSITE','PHONE','EMAIL','REFERRAL','SOCIAL_MEDIA','WALK_IN','PARTNER','OTHER')), status varchar(255) not null check (status in ('NEW','CONTACTED','QUALIFIED','QUOTE_REQUESTED','QUOTE_SENT','NEGOTIATION','CONVERTED','LOST')), tenant_id varchar(255) not null, updated_at timestamp(6), venue_name varchar(255), primary key (id));

CREATE TABLE load_list_items (id uuid not null, container_code_snapshot varchar(255), container_id uuid, created_at timestamp(6), inventory_item_id uuid, load_list_id uuid not null, loaded_quantity integer not null, notes varchar(1000), product_id uuid, product_name_snapshot varchar(255), required_quantity integer not null, status varchar(255) not null check (status in ('PENDING','LOADED','BLOCKED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE load_lists (id uuid not null, assigned_to varchar(255), completed_at timestamp(6), created_at timestamp(6), delivery_id uuid, driver_id uuid, driver_name_snapshot varchar(255), driver_notes varchar(255), handed_off_at timestamp(6), handed_off_to_driver_name varchar(255), load_list_number varchar(255) not null, started_at timestamp(6), status varchar(255) not null check (status in ('PENDING','LOADING','LOADED','VERIFIED','HANDED_OFF','BLOCKED')), tenant_id varchar(255) not null, updated_at timestamp(6), vehicle_code_snapshot varchar(255), vehicle_id uuid, verified_at timestamp(6), verified_by varchar(255), warehouse_order_id uuid not null, primary key (id));

CREATE TABLE notification_audit (id uuid not null, action varchar(255) not null, details varchar(2000), notification_id uuid, performed_by varchar(255), tenant_id varchar(255) not null, timestamp timestamp(6) not null, primary key (id));

CREATE TABLE notification_preferences (id uuid not null, created_at timestamp(6) not null, customer_id uuid, email_enabled boolean not null, in_app_enabled boolean not null, notification_type varchar(255) not null check (notification_type in ('QUOTE_SENT','QUOTE_ACCEPTED','QUOTE_CHANGE_REQUESTED','BOOKING_CONFIRMED','BOOKING_CANCELLED','PAYMENT_RECEIVED','PAYMENT_DUE','PAYMENT_FAILED','INVOICE_CREATED','INVOICE_SENT','INVOICE_OVERDUE','DELIVERY_SCHEDULED','DELIVERY_ASSIGNED','DELIVERY_STARTED','DELIVERY_ARRIVED','DELIVERY_COMPLETED','DELIVERY_FAILED','CUSTOMER_REQUEST_CREATED','CUSTOMER_REQUEST_UPDATED','SYSTEM','WAREHOUSE_ORDER_CREATED','WAREHOUSE_ITEM_SHORT','WAREHOUSE_ORDER_READY','PICK_LIST_ASSIGNED','PICK_SHORTAGE','DAMAGED_ITEM_FOUND','SUBSTITUTION_REQUIRED','PICK_COMPLETE','PACK_COMPLETE','LOAD_READY','LOAD_EXCEPTION','DRIVER_HANDOFF_COMPLETE','RETURN_SCHEDULED','RETURN_ASSIGNED','RETURN_PICKUP_STARTED','RETURN_PICKED_UP','RETURN_READY_FOR_INSPECTION','RETURN_COMPLETED','RETURN_ITEM_MISSING','RETURN_ITEM_DAMAGED','DAMAGE_CLAIM_CREATED','DAMAGE_CLAIM_ESTIMATE_CREATED','DAMAGE_CLAIM_SENT_TO_CUSTOMER','DAMAGE_CLAIM_APPROVED','DAMAGE_CLAIM_DISPUTED','DAMAGE_CLAIM_WAIVED','DAMAGE_CLAIM_RESOLVED','REPAIR_STARTED','REPAIR_COMPLETED','REPLACEMENT_REQUIRED')), sms_enabled boolean not null, tenant_id varchar(255) not null, updated_at timestamp(6) not null, user_id uuid, primary key (id));

CREATE TABLE notification_templates (id uuid not null, active boolean not null, body varchar(4000) not null, channel varchar(255) not null check (channel in ('IN_APP','EMAIL','SMS')), created_at timestamp(6) not null, name varchar(255) not null, notification_type varchar(255) not null check (notification_type in ('QUOTE_SENT','QUOTE_ACCEPTED','QUOTE_CHANGE_REQUESTED','BOOKING_CONFIRMED','BOOKING_CANCELLED','PAYMENT_RECEIVED','PAYMENT_DUE','PAYMENT_FAILED','INVOICE_CREATED','INVOICE_SENT','INVOICE_OVERDUE','DELIVERY_SCHEDULED','DELIVERY_ASSIGNED','DELIVERY_STARTED','DELIVERY_ARRIVED','DELIVERY_COMPLETED','DELIVERY_FAILED','CUSTOMER_REQUEST_CREATED','CUSTOMER_REQUEST_UPDATED','SYSTEM','WAREHOUSE_ORDER_CREATED','WAREHOUSE_ITEM_SHORT','WAREHOUSE_ORDER_READY','PICK_LIST_ASSIGNED','PICK_SHORTAGE','DAMAGED_ITEM_FOUND','SUBSTITUTION_REQUIRED','PICK_COMPLETE','PACK_COMPLETE','LOAD_READY','LOAD_EXCEPTION','DRIVER_HANDOFF_COMPLETE','RETURN_SCHEDULED','RETURN_ASSIGNED','RETURN_PICKUP_STARTED','RETURN_PICKED_UP','RETURN_READY_FOR_INSPECTION','RETURN_COMPLETED','RETURN_ITEM_MISSING','RETURN_ITEM_DAMAGED','DAMAGE_CLAIM_CREATED','DAMAGE_CLAIM_ESTIMATE_CREATED','DAMAGE_CLAIM_SENT_TO_CUSTOMER','DAMAGE_CLAIM_APPROVED','DAMAGE_CLAIM_DISPUTED','DAMAGE_CLAIM_WAIVED','DAMAGE_CLAIM_RESOLVED','REPAIR_STARTED','REPAIR_COMPLETED','REPLACEMENT_REQUIRED')), subject varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6) not null, primary key (id));

CREATE TABLE notifications (id uuid not null, channel varchar(255) not null check (channel in ('IN_APP','EMAIL','SMS')), created_at timestamp(6) not null, failed_at timestamp(6), failure_reason varchar(1000), message varchar(4000) not null, priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), read_at timestamp(6), recipient_customer_id uuid, recipient_user_id uuid, reference_id varchar(255), reference_type varchar(255), retry_count integer not null, sent_at timestamp(6), status varchar(255) not null check (status in ('PENDING','PROCESSING','SENT','DELIVERED','FAILED','READ','CANCELLED')), tenant_id varchar(255) not null, title varchar(255) not null, type varchar(255) not null check (type in ('QUOTE_SENT','QUOTE_ACCEPTED','QUOTE_CHANGE_REQUESTED','BOOKING_CONFIRMED','BOOKING_CANCELLED','PAYMENT_RECEIVED','PAYMENT_DUE','PAYMENT_FAILED','INVOICE_CREATED','INVOICE_SENT','INVOICE_OVERDUE','DELIVERY_SCHEDULED','DELIVERY_ASSIGNED','DELIVERY_STARTED','DELIVERY_ARRIVED','DELIVERY_COMPLETED','DELIVERY_FAILED','CUSTOMER_REQUEST_CREATED','CUSTOMER_REQUEST_UPDATED','SYSTEM','WAREHOUSE_ORDER_CREATED','WAREHOUSE_ITEM_SHORT','WAREHOUSE_ORDER_READY','PICK_LIST_ASSIGNED','PICK_SHORTAGE','DAMAGED_ITEM_FOUND','SUBSTITUTION_REQUIRED','PICK_COMPLETE','PACK_COMPLETE','LOAD_READY','LOAD_EXCEPTION','DRIVER_HANDOFF_COMPLETE','RETURN_SCHEDULED','RETURN_ASSIGNED','RETURN_PICKUP_STARTED','RETURN_PICKED_UP','RETURN_READY_FOR_INSPECTION','RETURN_COMPLETED','RETURN_ITEM_MISSING','RETURN_ITEM_DAMAGED','DAMAGE_CLAIM_CREATED','DAMAGE_CLAIM_ESTIMATE_CREATED','DAMAGE_CLAIM_SENT_TO_CUSTOMER','DAMAGE_CLAIM_APPROVED','DAMAGE_CLAIM_DISPUTED','DAMAGE_CLAIM_WAIVED','DAMAGE_CLAIM_RESOLVED','REPAIR_STARTED','REPAIR_COMPLETED','REPLACEMENT_REQUIRED')), updated_at timestamp(6) not null, primary key (id));

CREATE TABLE operational_conflicts (id uuid not null, conflict_type varchar(255) not null check (conflict_type in ('INVENTORY','DRIVER','VEHICLE','WAREHOUSE','EVENT','TURNAROUND','DATE_RANGE')), created_at timestamp(6), message varchar(2000), overridden_at timestamp(6), overridden_by varchar(255), override_reason varchar(2000), reference_id varchar(255), reference_type varchar(255), resource_id varchar(255), resource_name varchar(255), severity varchar(255) not null check (severity in ('HARD_CONFLICT','WARNING','INFO')), status varchar(255) not null, suggested_action varchar(2000), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE pack_list_items (id uuid not null, container_code_snapshot varchar(255), container_id uuid, created_at timestamp(6), inventory_item_id uuid, notes varchar(1000), pack_list_id uuid not null, packed_quantity integer not null, product_id uuid, product_name_snapshot varchar(255), required_quantity integer not null, sku_snapshot varchar(255), status varchar(255) not null check (status in ('PENDING','PACKING','PACKED','PARTIAL','BLOCKED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE pack_lists (id uuid not null, assigned_to varchar(255), completed_at timestamp(6), created_at timestamp(6), pack_list_number varchar(255) not null, started_at timestamp(6), status varchar(255) not null check (status in ('PENDING','IN_PROGRESS','PARTIAL','COMPLETED','BLOCKED')), tenant_id varchar(255) not null, updated_at timestamp(6), warehouse_order_id uuid not null, primary key (id));

CREATE TABLE packing_containers (id uuid not null, container_code varchar(255) not null, created_at timestamp(6), description varchar(255), status varchar(255) not null check (status in ('AVAILABLE','IN_USE','PACKED','LOADED')), tenant_id varchar(255) not null, type varchar(255) not null check (type in ('CASE','CART','PALLET','BAG','OTHER')), updated_at timestamp(6), warehouse_id uuid, primary key (id));

CREATE TABLE payment (id uuid not null, amount numeric(10,2) not null, booking_id uuid not null, created_at timestamp(6) not null, created_by varchar(255), customer_id uuid not null, invoice_id uuid, notes varchar(2000), payment_date date not null, payment_method varchar(255) not null check (payment_method in ('CASH','BANK_TRANSFER','CREDIT_CARD','DEBIT_CARD','CHECK','OTHER')), payment_status varchar(255) not null check (payment_status in ('PENDING','COMPLETED','FAILED','REFUNDED','VOID')), tenant_id varchar(255) not null, transaction_reference varchar(100) not null, updated_at timestamp(6) not null, primary key (id));

CREATE TABLE payment_audit (id uuid not null, action varchar(255) not null, booking_id uuid not null, details varchar(2000), payment_id uuid, performed_by varchar(255), tenant_id varchar(255) not null, timestamp timestamp(6) not null, primary key (id));

CREATE TABLE permission (id uuid not null, code varchar(255) check (code in ('DASHBOARD_VIEW','AI_COPILOT_USE','CUSTOMER_VIEW','CUSTOMER_CREATE','CUSTOMER_UPDATE','LEAD_VIEW','LEAD_CREATE','LEAD_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','PRODUCT_UPDATE','PRODUCT_DELETE','INVENTORY_VIEW','INVENTORY_UPDATE','INVENTORY_RESERVE','QUOTE_VIEW','QUOTE_CREATE','QUOTE_UPDATE','QUOTE_SEND','BOOKING_VIEW','BOOKING_CREATE','BOOKING_UPDATE','BOOKING_CANCEL','PAYMENT_VIEW','PAYMENT_CREATE','PAYMENT_REFUND','WAREHOUSE_VIEW','WAREHOUSE_UPDATE','DELIVERY_VIEW','DELIVERY_UPDATE','ANALYTICS_VIEW','STOREFRONT_VIEW','STOREFRONT_UPDATE','USER_VIEW','USER_CREATE','USER_UPDATE','USER_DISABLE','COMPANY_SETTINGS_VIEW','COMPANY_SETTINGS_UPDATE','INTEGRATION_VIEW','INTEGRATION_MANAGE','API_KEY_MANAGE','WEBHOOK_MANAGE','AI_SALES_VIEW','AI_SALES_MANAGE','AI_SALES_USE','AI_QUOTE_APPROVE','AI_SETTINGS_MANAGE','CRM_VIEW','CRM_MANAGE','CRM_ASSIGN','CRM_QUALIFY','CRM_QUOTE')), description varchar(255), primary key (id));

CREATE TABLE phone_call_sessions (id uuid not null, ai_conversation_id uuid, answered_at timestamp(6) with time zone, assigned_user_id varchar(255), caller_number_masked varchar(255) not null, consent_status varchar(255) not null check (consent_status in ('NOT_REQUESTED','PENDING','GRANTED','DECLINED')), created_at timestamp(6) with time zone not null, customer_id uuid, direction varchar(255) not null check (direction in ('INBOUND','OUTBOUND')), ended_at timestamp(6) with time zone, handoff_at timestamp(6) with time zone, handoff_reason varchar(255) check (handoff_reason in ('CUSTOMER_REQUEST','AI_UNCERTAIN','COMPLEX_EVENT','CUSTOM_PRODUCT','PRICING_EXCEPTION','AVAILABILITY_CONFLICT','COMPLAINT','PAYMENT_QUESTION','CONTRACT_QUESTION','OTHER')), lead_id uuid, provider varchar(255) not null, provider_call_id varchar(255), public_id varchar(255) not null, recording_status varchar(255) not null check (recording_status in ('DISABLED','RECORDING','COMPLETED')), started_at timestamp(6) with time zone not null, status varchar(255) not null check (status in ('RINGING','ANSWERED','AI_ACTIVE','WAITING_FOR_HUMAN','HUMAN_ACTIVE','COMPLETED','FAILED','ABANDONED')), summary varchar(2000), tenant_id varchar(255) not null, updated_at timestamp(6) with time zone not null, primary key (id));

CREATE TABLE phone_tenant_settings (id uuid not null, created_at timestamp(6) with time zone not null, disclosure_text varchar(1000), greeting varchar(1000), human_handoff_number varchar(255), inbound_enabled boolean not null, phone_ai_enabled boolean not null, provider varchar(255) not null, recording_enabled boolean not null, tenant_id varchar(255) not null, transcript_retention_days integer not null, transcription_enabled boolean not null, updated_at timestamp(6) with time zone not null, primary key (id));

CREATE TABLE phone_transcript_segments (id uuid not null, call_session_id uuid not null, confidence float(53), created_at timestamp(6) with time zone not null, speaker varchar(255) not null check (speaker in ('CUSTOMER','AI','HUMAN_AGENT','SYSTEM')), tenant_id varchar(255) not null, text varchar(2000) not null, timestamp timestamp(6) with time zone not null, primary key (id));

CREATE TABLE pick_list_items (id uuid not null, booking_item_id uuid, created_at timestamp(6), damaged_quantity integer not null, inventory_item_id uuid, location_code_snapshot varchar(255), notes varchar(1000), pick_list_id uuid not null, picked_quantity integer not null, product_id uuid, product_name_snapshot varchar(255), required_quantity integer not null, sequence_number integer not null, short_quantity integer not null, sku_snapshot varchar(255), status varchar(255) not null check (status in ('PENDING','PICKING','PARTIAL','PICKED','SHORT','DAMAGED','SUBSTITUTED','SKIPPED')), substituted_quantity integer not null, tenant_id varchar(255) not null, updated_at timestamp(6), warehouse_location_id uuid, primary key (id));

CREATE TABLE pick_lists (id uuid not null, assigned_to varchar(255), completed_at timestamp(6), created_at timestamp(6), pick_list_number varchar(255) not null, priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), started_at timestamp(6), status varchar(255) not null check (status in ('PENDING','IN_PROGRESS','PARTIAL','COMPLETED','BLOCKED','CANCELLED')), tenant_id varchar(255) not null, updated_at timestamp(6), warehouse_id uuid, warehouse_order_id uuid not null, primary key (id));

CREATE TABLE pick_verifications (id uuid not null, all_verified boolean not null, exception_acknowledgements varchar(2000), notes varchar(1000), pick_list_id uuid not null, tenant_id varchar(255) not null, verified_at timestamp(6) not null, verified_by varchar(255) not null, warehouse_order_id uuid not null, primary key (id));

CREATE TABLE product_categories (id uuid not null, active boolean not null, created_at timestamp(6), description varchar(255), name varchar(255) not null, parent_category_id uuid, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE products (id uuid not null, category_id uuid, created_at timestamp(6), default_turnaround_minutes integer not null, description varchar(2000), image_url varchar(255), name varchar(255) not null, product_type varchar(255) not null check (product_type in ('RENTAL_ITEM','PACKAGE','SERVICE','CONSUMABLE')), quantity_damaged integer not null, quantity_in_maintenance integer not null, quantity_lost integer not null, quantity_owned integer not null, rental_price numeric(10,2), replacement_cost numeric(10,2), sku varchar(255) not null, status varchar(255) not null check (status in ('ACTIVE','INACTIVE','DRAFT','DISCONTINUED')), tenant_id varchar(255) not null, tracking_type varchar(255) not null check (tracking_type in ('QUANTITY','SERIALIZED')), updated_at timestamp(6), primary key (id));

CREATE TABLE quote_discounts (id uuid not null, amount numeric(10,2) not null, approved_by varchar(255), created_at timestamp(6), quote_id uuid not null, reason varchar(255), type varchar(255) not null check (type in ('PERCENTAGE','FIXED')), discount_value numeric(10,2) not null, primary key (id));

CREATE TABLE quote_fees (id uuid not null, amount numeric(10,2) not null, created_at timestamp(6), description varchar(255), fee_type varchar(255) not null check (fee_type in ('DELIVERY','PICKUP','SETUP','BREAKDOWN','SERVICE','OTHER')), quote_id uuid not null, primary key (id));

CREATE TABLE quote_items (id uuid not null, created_at timestamp(6), description varchar(255) not null, discount_amount numeric(10,2), line_subtotal numeric(10,2), line_total numeric(10,2), notes varchar(255), pricing_strategy varchar(255) not null check (pricing_strategy in ('PER_EVENT','PER_DAY','PER_WEEK','FLAT_RATE')), product_id uuid not null, quantity integer not null, quote_id uuid not null, rental_days integer not null, standard_unit_price numeric(10,2), tax_amount numeric(10,2), unit_price numeric(10,2) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE quotes (id uuid not null, breakdown_fee numeric(10,2), created_at timestamp(6), created_by varchar(255), customer_id uuid not null, delivery_fee numeric(10,2), deposit_amount numeric(10,2), deposit_percentage numeric(10,2), discount_amount numeric(10,2), event_id uuid not null, internal_notes varchar(2000), notes varchar(2000), pickup_fee numeric(10,2), quote_date date not null, quote_number varchar(255) not null, rental_end_date_time timestamp(6) not null, rental_start_date_time timestamp(6) not null, service_fee numeric(10,2), setup_fee numeric(10,2), status varchar(255) not null check (status in ('DRAFT','PENDING_REVIEW','SENT','VIEWED','ACCEPTED','CHANGE_REQUESTED','DECLINED','REJECTED','EXPIRED','CANCELLED')), subtotal numeric(10,2), tax_amount numeric(10,2), tax_rate numeric(10,2), tenant_id varchar(255) not null, total_amount numeric(10,2), updated_at timestamp(6), valid_until date not null, primary key (id));

CREATE TABLE rental_cart_items (id uuid not null, created_at timestamp(6), end_date_time timestamp(6), product_id uuid not null, product_name varchar(255), quantity integer not null, sku varchar(255), start_date_time timestamp(6), unit_price numeric(38,2), cart_id uuid not null, primary key (id));

CREATE TABLE rental_carts (id uuid not null, cart_token varchar(255) not null, created_at timestamp(6), customer_id uuid, expires_at timestamp(6), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE rental_event (id uuid not null, created_at timestamp(6) not null, customer_id uuid, end_time time(6), event_date date, event_name varchar(255) not null, event_type varchar(255), guest_count integer, special_instructions varchar(2000), start_time time(6), status varchar(255) not null check (status in ('PLANNING','QUOTED','BOOKED','PREPARING','IN_PROGRESS','COMPLETED','CANCELLED')), tenant_id uuid not null, updated_at timestamp(6), venue_address varchar(255), venue_name varchar(255), primary key (id));

CREATE TABLE rental_request_items (id uuid not null, line_total numeric(10,2), product_id uuid not null, product_name varchar(255), quantity integer not null, sku varchar(255), unit_price numeric(10,2), rental_request_id uuid, primary key (id));

CREATE TABLE rental_requests (id uuid not null, conversation_id uuid, created_at timestamp(6) not null, customer_email varchar(255) not null, customer_id uuid, customer_name varchar(255) not null, customer_phone varchar(255), delivery_address varchar(255), delivery_city varchar(255), delivery_required boolean not null, estimated_budget numeric(10,2), estimated_total numeric(10,2), event_date date, event_name varchar(255), event_type varchar(255), guest_count integer, lead_id uuid, notes varchar(4000), quote_id uuid, rental_end_date timestamp(6), rental_start_date timestamp(6), request_number varchar(255) not null, status varchar(255) not null check (status in ('SUBMITTED','UNDER_REVIEW','ACCEPTED','CONVERTED_TO_QUOTE','REJECTED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE repair_orders (id uuid not null, actual_cost numeric(10,2), assigned_to varchar(255), claim_id uuid not null, completed_at timestamp(6), created_at timestamp(6), description varchar(2000), estimated_cost numeric(10,2), notes varchar(2000), product_id uuid not null, quantity integer not null, repair_number varchar(255) not null, repair_type varchar(255), started_at timestamp(6), status varchar(255) not null check (status in ('PENDING','ASSIGNED','IN_PROGRESS','COMPLETED','FAILED','CANCELLED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE replacement_orders (id uuid not null, claim_id uuid not null, created_at timestamp(6), product_id uuid not null, quantity integer not null, reason varchar(2000), replacement_number varchar(255) not null, status varchar(255) not null check (status in ('PENDING','ORDERED','RECEIVED','ALLOCATED','COMPLETED','CANCELLED')), tenant_id varchar(255) not null, total_cost numeric(10,2), unit_cost numeric(10,2), updated_at timestamp(6), primary key (id));

CREATE TABLE return_audits (id uuid not null, action varchar(255) not null, created_at timestamp(6) not null, details varchar(2000), performed_by varchar(255) not null, return_order_id uuid, tenant_id varchar(255) not null, primary key (id));

CREATE TABLE return_order_items (id uuid not null, booking_item_id uuid, created_at timestamp(6), notes varchar(2000), product_id uuid not null, product_name_snapshot varchar(255), quantity_damaged integer not null, quantity_expected integer not null, quantity_good integer not null, quantity_missing integer not null, quantity_received integer not null, return_order_id uuid not null, sku_snapshot varchar(255), status varchar(255) not null check (status in ('PENDING','PARTIAL','RECEIVED','MISSING','DAMAGED','INSPECTED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE return_orders (id uuid not null, actual_arrival_time timestamp(6), actual_check_in_time timestamp(6), actual_inspection_time timestamp(6), actual_pickup_start_time timestamp(6), actual_pickup_time timestamp(6), booking_id uuid not null, completed_at timestamp(6), created_at timestamp(6), customer_id uuid not null, delivery_id uuid, driver_id uuid, event_id uuid, notes varchar(2000), pickup_address_snapshot varchar(2000), priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), return_number varchar(255) not null, scheduled_date date, scheduled_end_time varchar(255), scheduled_start_time varchar(255), status varchar(255) not null check (status in ('PENDING','SCHEDULED','ASSIGNED','READY_FOR_PICKUP','OUT_FOR_PICKUP','ARRIVED','PICKED_UP','CHECK_IN','INSPECTION','COMPLETED','CANCELLED','FAILED')), tenant_id varchar(255) not null, updated_at timestamp(6), vehicle_id uuid, primary key (id));

CREATE TABLE role (id uuid not null, role_type varchar(255) check (role_type in ('OWNER','ADMIN','SALES','WAREHOUSE','WAREHOUSE_MANAGER','WAREHOUSE_OPERATOR','FINANCE','DRIVER','CUSTOMER')), primary key (id));

CREATE TABLE role_permission (role_id uuid not null, permission_id uuid not null, primary key (role_id, permission_id));

CREATE TABLE stock_movements (id uuid not null, created_at timestamp(6) not null, from_warehouse_id uuid, inventory_item_id uuid, movement_type varchar(255) not null check (movement_type in ('RECEIPT','ADJUSTMENT_IN','ADJUSTMENT_OUT','TRANSFER_OUT','TRANSFER_IN','RESERVATION','RELEASE','CHECKOUT','RETURN','DAMAGE','MAINTENANCE','MAINTENANCE_RETURN','LOSS','RECOVERY','RETIREMENT')), performed_by varchar(255), product_id uuid not null, quantity integer not null, reason varchar(255), reference_id uuid, reference_type varchar(255), tenant_id varchar(255) not null, to_warehouse_id uuid, warehouse_id uuid not null, primary key (id));

CREATE TABLE tenant (id uuid not null, name varchar(255), primary key (id));

CREATE TABLE tenant_storefront_config (id uuid not null, company_name varchar(255) not null, created_at timestamp(6), currency varchar(255), logo_url varchar(255), primary_email varchar(255), primary_phone varchar(255), tenant_id varchar(255) not null, tenant_slug varchar(255) not null, terms_and_conditions varchar(4000), timezone varchar(255), updated_at timestamp(6), website_url varchar(255), primary key (id));

CREATE TABLE user_role (user_id uuid not null, role_id uuid not null, primary key (user_id, role_id));

CREATE TABLE vehicles (id uuid not null, active boolean not null, capacity integer, created_at timestamp(6), name varchar(255) not null, status varchar(255) not null check (status in ('AVAILABLE','ASSIGNED','IN_USE','MAINTENANCE','INACTIVE')), tenant_id varchar(255) not null, type varchar(255), updated_at timestamp(6), vehicle_number varchar(255) not null, primary key (id));

CREATE TABLE warehouse_audits (id uuid not null, action varchar(255) not null, booking_id uuid, details varchar(2000), performed_by varchar(255) not null, tenant_id varchar(255) not null, timestamp timestamp(6) not null, warehouse_order_id uuid, primary key (id));

CREATE TABLE warehouse_exceptions (id uuid not null, asset_code_snapshot varchar(255), assigned_to varchar(255), created_at timestamp(6), description varchar(2000) not null, inventory_item_id uuid, load_list_id uuid, pack_list_id uuid, pick_list_id uuid, product_id uuid, product_name_snapshot varchar(255), quantity integer not null, reported_by varchar(255), resolution varchar(255) check (resolution in ('FOUND_ITEM','SUBSTITUTION','QUANTITY_REDUCED','REPAIRED','REPACKED','VEHICLE_CHANGED','MANAGER_OVERRIDE','OTHER')), resolution_notes varchar(2000), resolved_at timestamp(6), resolved_by varchar(255), severity varchar(255) not null check (severity in ('INFO','WARNING','BLOCKING')), status varchar(255) not null check (status in ('OPEN','ACKNOWLEDGED','RESOLVED','WAIVED','CANCELLED')), tenant_id varchar(255) not null, type varchar(255) not null check (type in ('SHORTAGE','DAMAGE','WRONG_LOCATION','MISSING_ASSET','KIT_INCOMPLETE','PACKING_ISSUE','LOAD_ISSUE','VEHICLE_CAPACITY','OTHER')), warehouse_order_id uuid not null, primary key (id));

CREATE TABLE warehouse_locations (id uuid not null, active boolean not null, code varchar(255) not null, created_at timestamp(6), description varchar(255), name varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE warehouse_order_checklists (id uuid not null, completed boolean not null, completed_at timestamp(6), completed_by varchar(255), mandatory boolean not null, notes varchar(255), stage varchar(255) not null check (stage in ('PICK','PACK','LOAD')), task_description varchar(255) not null, tenant_id varchar(255) not null, warehouse_order_id uuid not null, primary key (id));

CREATE TABLE warehouse_order_items (id uuid not null, booking_item_id uuid, created_at timestamp(6), location_snapshot varchar(255), notes varchar(1000), product_id uuid, product_name_snapshot varchar(255) not null, quantity_packed integer not null, quantity_picked integer not null, quantity_required integer not null, sku_snapshot varchar(255), status varchar(255) not null check (status in ('PENDING','PICKED','PARTIALLY_PICKED','PACKED','SHORT','DAMAGED')), updated_at timestamp(6), warehouse_order_id uuid not null, primary key (id));

CREATE TABLE warehouse_orders (id uuid not null, assigned_to varchar(255), booking_id uuid not null, completed_at timestamp(6), created_at timestamp(6), created_by varchar(255), customer_id uuid not null, event_id uuid not null, notes varchar(2000), order_number varchar(255) not null, priority varchar(255) not null check (priority in ('LOW','NORMAL','HIGH','URGENT')), scheduled_date timestamp(6), status varchar(255) not null check (status in ('PENDING','READY_TO_PICK','PICKING','PICKED','VERIFYING','PACKING','PACKED','LOADING','LOADED','READY_FOR_DELIVERY','HANDED_TO_DRIVER','SHORT','BLOCKED','CANCELLED')), tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE warehouse_stock (id uuid not null, created_at timestamp(6), minimum_stock_level integer not null, product_id uuid not null, quantity_available integer not null, quantity_damaged integer not null, quantity_in_maintenance integer not null, quantity_lost integer not null, quantity_on_hand integer not null, quantity_reserved integer not null, tenant_id varchar(255) not null, updated_at timestamp(6), version bigint, warehouse_id uuid not null, primary key (id));

CREATE TABLE warehouse_substitutions (id uuid not null, approved_by varchar(255), created_at timestamp(6), original_product_id uuid not null, original_product_name_snapshot varchar(255), original_quantity integer not null, proposed_by varchar(255), reason varchar(1000), replacement_product_id uuid not null, replacement_product_name_snapshot varchar(255), replacement_quantity integer not null, status varchar(255) not null check (status in ('PROPOSED','APPROVED','REJECTED','APPLIED')), tenant_id varchar(255) not null, updated_at timestamp(6), warehouse_order_id uuid not null, primary key (id));

CREATE TABLE warehouses (id uuid not null, active boolean not null, address varchar(255), code varchar(255) not null, created_at timestamp(6), daily_checkin_capacity integer not null, daily_pack_capacity integer not null, daily_pick_capacity integer not null, name varchar(255) not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

CREATE TABLE webhook_delivery (id uuid not null, attempt_number integer not null, completed_at timestamp(6), created_at timestamp(6) not null, duration_ms bigint, event_type varchar(255) not null, http_status integer, integration_event_id varchar(255) not null, next_retry_at timestamp(6), request_headers varchar(2000), request_payload varchar(8000), response_summary varchar(4000), started_at timestamp(6), status varchar(255) not null check (status in ('PENDING','SUCCESS','FAILED','RETRYING','DEAD_LETTER')), tenant_id varchar(255) not null, webhook_endpoint_id uuid not null, primary key (id));

CREATE TABLE webhook_endpoint (id uuid not null, created_at timestamp(6) not null, created_by varchar(255), description varchar(255), endpoint_url varchar(1000) not null, name varchar(255) not null, secret_reference varchar(255) not null, status varchar(255) not null check (status in ('ACTIVE','DISABLED','ERROR')), subscribed_events varchar(2000) not null, tenant_id varchar(255) not null, updated_at timestamp(6), primary key (id));

-- Section 2: Constraints & Foreign Keys
ALTER TABLE ai_sales_conversations ADD CONSTRAINT UK_905t9es1b4wkxfthlikjsqpkq unique (public_id);
ALTER TABLE ai_tenant_settings ADD CONSTRAINT idx_aisettings_tenant unique (tenant_id);
ALTER TABLE app_user ADD CONSTRAINT idx_app_user_email unique (email);
ALTER TABLE automation_executions ADD CONSTRAINT idx_ae_idempotency unique (tenant_id, idempotency_key);
ALTER TABLE automation_rules ADD CONSTRAINT idx_ar_tenant_code unique (tenant_id, rule_code);
ALTER TABLE booking ADD CONSTRAINT UK_6j74n7w8mp19sixr5272028mk unique (booking_number);
ALTER TABLE crm_leads ADD CONSTRAINT idx_crm_lead_tenant_number unique (tenant_id, lead_number);
ALTER TABLE customers ADD CONSTRAINT UK_t74y58jagthxqxysuw9l0jx6y unique (customer_number);
ALTER TABLE damage_claims ADD CONSTRAINT UK_2frr7vsrgrm6jemqrcgnyy05 unique (claim_number);
ALTER TABLE deliveries ADD CONSTRAINT UK_667lysacjsrsb1r6ppswku1rx unique (delivery_number);
ALTER TABLE delivery_routes ADD CONSTRAINT UK_an419bwcaikjvqvold7hdcoki unique (route_number);
ALTER TABLE external_api_key ADD CONSTRAINT UK_c4y8h3ad9td58ukw89k49w6wq unique (key_hash);
ALTER TABLE external_entity_mapping ADD CONSTRAINT UK2doi1qc43do2piiigetk1hxdu unique (tenant_id, provider, entity_type, internal_id);
ALTER TABLE inbound_integration_event ADD CONSTRAINT UKhs0shuw50p8lmhji9mrfj0ua9 unique (tenant_id, provider, external_event_id);
ALTER TABLE integration_event ADD CONSTRAINT UK_69vffc46tkng8mp3g87r28itm unique (event_id);
ALTER TABLE load_lists ADD CONSTRAINT UK_mk6w547gtvm3x41qv9n5bew6v unique (load_list_number);
ALTER TABLE pack_lists ADD CONSTRAINT UK_jjkojputuq15l0d9loxsjfkir unique (pack_list_number);
ALTER TABLE permission ADD CONSTRAINT UK_a7ujv987la0i7a0o91ueevchc unique (code);
ALTER TABLE phone_call_sessions ADD CONSTRAINT UK_5px7c5h090h7frj0kr430lk1f unique (public_id);
ALTER TABLE phone_tenant_settings ADD CONSTRAINT UK_cenur2disgyi4b7dagaqe394 unique (tenant_id);
ALTER TABLE pick_lists ADD CONSTRAINT UK_7ipth6a44n677nqe4oyq8ik5a unique (pick_list_number);
ALTER TABLE quotes ADD CONSTRAINT UK_bkbvhxprsi25u20qvjyt5aoay unique (quote_number);
ALTER TABLE rental_carts ADD CONSTRAINT idx_cart_session unique (cart_token);
ALTER TABLE rental_requests ADD CONSTRAINT idx_rental_req_number unique (tenant_id, request_number);
ALTER TABLE repair_orders ADD CONSTRAINT UK_mf3x4l61qwj6y4aradqbgqvyj unique (repair_number);
ALTER TABLE replacement_orders ADD CONSTRAINT UK_jnvm2w3vspshn9cl3a17lwdoa unique (replacement_number);
ALTER TABLE return_orders ADD CONSTRAINT UK_5g0dsl0dlw8ynte8dogx354f5 unique (return_number);
ALTER TABLE role ADD CONSTRAINT UK_8nhufvk7ufr23s4xoqglqtbdx unique (role_type);
ALTER TABLE tenant_storefront_config ADD CONSTRAINT idx_storefront_slug unique (tenant_slug);
ALTER TABLE tenant_storefront_config ADD CONSTRAINT UK_36b894hj7hno6kvehlet1dbn9 unique (tenant_id);
ALTER TABLE warehouse_orders ADD CONSTRAINT UK_i5oe5l78xt3uxm7ff64l2vpfg unique (order_number);
ALTER TABLE app_user ADD CONSTRAINT FKsr53t6kfhi9d1k42hv4nlfkkd foreign key (tenant_id) references tenant;
ALTER TABLE customer_messages ADD CONSTRAINT FKfw1ij0smur559hbyj3g2qbwuh foreign key (conversation_id) references customer_conversations;
ALTER TABLE rental_cart_items ADD CONSTRAINT FKeavxolsrywimtvkx2rqv4xdoj foreign key (cart_id) references rental_carts;
ALTER TABLE rental_request_items ADD CONSTRAINT FKsrlw587v6pxc7pif7gcdm0g0t foreign key (rental_request_id) references rental_requests;
ALTER TABLE role_permission ADD CONSTRAINT FKf8yllw1ecvwqy3ehyxawqa1qp foreign key (permission_id) references permission;
ALTER TABLE role_permission ADD CONSTRAINT FKa6jx8n8xkesmjmv6jqug6bg68 foreign key (role_id) references role;
ALTER TABLE user_role ADD CONSTRAINT FKa68196081fvovjhkek5m97n3y foreign key (role_id) references role;
ALTER TABLE user_role ADD CONSTRAINT FKg7fr1r7o0fkk41nfhnjdyqn7b foreign key (user_id) references app_user;

-- Section 3: Baseline Indexes
create index idx_copilot_proposal_tenant on ai_copilot_action_proposals (tenant_id);
create index idx_copilot_proposal_conv on ai_copilot_action_proposals (conversation_id);
create index idx_copilot_proposal_status on ai_copilot_action_proposals (status);
create index idx_escalation_tenant on ai_escalations (tenant_id);
create index idx_escalation_conv on ai_escalations (conversation_id);
create index idx_escalation_status on ai_escalations (status);
create index idx_aifeedback_tenant on ai_feedback (tenant_id);
create index idx_aifeedback_conv on ai_feedback (conversation_id);
create index idx_prompt_tenant on ai_prompt_templates (tenant_id);
create index idx_prompt_type on ai_prompt_templates (type);
create index idx_rec_tenant_status on ai_recommendations (tenant_id, status);
create index idx_rec_tenant_priority on ai_recommendations (tenant_id, priority);
create index idx_rec_number on ai_recommendations (tenant_id, recommendation_number);
create index idx_rec_signal on ai_recommendations (signal_id);
create index idx_rec_created_at on ai_recommendations (created_at);
create index idx_inquiry_tenant on ai_rental_inquiries (tenant_id);
create index idx_inquiry_conv on ai_rental_inquiries (conversation_id);
create index idx_inquiry_cust on ai_rental_inquiries (customer_id);
create index idx_aisales_tenant on ai_sales_conversations (tenant_id);
create index idx_aisales_customer on ai_sales_conversations (customer_id);
create index idx_aisales_status on ai_sales_conversations (status);
create index idx_aisales_public_id on ai_sales_conversations (public_id);
create index idx_aism_tenant on ai_sales_messages (tenant_id);
create index idx_aism_conversation on ai_sales_messages (conversation_id);
create index idx_aism_created on ai_sales_messages (created_at);
create index idx_aiusage_tenant on ai_usage_records (tenant_id);
create index idx_aiusage_conv on ai_usage_records (conversation_id);
create index idx_aiusage_created on ai_usage_records (created_at);
create index idx_aa_tenant_status on automation_approvals (tenant_id, status);
create index idx_aa_rec_id on automation_approvals (recommendation_id);
create index idx_aa_created_at on automation_approvals (created_at);
create index idx_audit_tenant_action on automation_audits (tenant_id, action);
create index idx_audit_entity on automation_audits (tenant_id, entity_type, entity_id);
create index idx_audit_created_at on automation_audits (created_at);
create index idx_ae_tenant_status on automation_executions (tenant_id, execution_status);
create index idx_ae_rec_id on automation_executions (recommendation_id);
create index idx_ae_created_at on automation_executions (created_at);
create index idx_ar_tenant_signal on automation_rules (tenant_id, signal_type);
create index idx_ar_tenant_enabled on automation_rules (tenant_id, enabled);
create index idx_booking_tenant on booking (tenant_id);
create index idx_booking_quote on booking (quote_id);
create index idx_booking_customer on booking (customer_id);
create index idx_booking_event on booking (event_id);
create index idx_booking_status on booking (status);
create index idx_booking_item_booking on booking_item (booking_id);
create index idx_booking_item_product on booking_item (product_id);
create index idx_bs_tenant_status_type on business_signals (tenant_id, status, signal_type);
create index idx_bs_dedupe_key on business_signals (tenant_id, dedupe_key);
create index idx_bs_source_entity on business_signals (tenant_id, source_entity_type, source_entity_id);
create index idx_bs_detected_at on business_signals (detected_at);
create index idx_claim_audit_tenant on claim_audits (tenant_id);
create index idx_claim_audit_claim on claim_audits (claim_id);
create index idx_estimate_tenant on claim_estimates (tenant_id);
create index idx_estimate_claim on claim_estimates (claim_id);
create index idx_crm_lead_tenant on crm_leads (tenant_id);
create index idx_crm_lead_stage on crm_leads (stage);
create index idx_crm_lead_assigned on crm_leads (assigned_sales_user_id);
create index idx_crm_lead_req on crm_leads (rental_request_id);
create index idx_crm_lead_cust on crm_leads (customer_id);
create index idx_crm_lead_quote on crm_leads (quote_id);
create index idx_crm_lead_event_date on crm_leads (event_date);
create index idx_crm_lead_next_fu on crm_leads (next_follow_up_at);
create index idx_activity_tenant on customer_activities (tenant_id);
create index idx_activity_customer on customer_activities (customer_id);
create index idx_cust_addr_tenant on customer_addresses (tenant_id);
create index idx_cust_addr_customer on customer_addresses (customer_id);
create index idx_conv_tenant on customer_conversations (tenant_id);
create index idx_conv_customer on customer_conversations (customer_id);
create index idx_conv_booking on customer_conversations (booking_id);
create index idx_msg_conversation on customer_messages (conversation_id);
create index idx_customer_req_tenant on customer_requests (tenant_id);
create index idx_customer_req_customer on customer_requests (customer_id);
create index idx_customer_req_status on customer_requests (status);
create index idx_customer_user_tenant on customer_users (tenant_id);
create index idx_customer_user_customer on customer_users (customer_id);
create index idx_customer_user_email on customer_users (tenant_id, email);
create index idx_customer_tenant on customers (tenant_id);
create index idx_customer_email on customers (email);
create index idx_customer_phone on customers (phone);
create index idx_customer_number on customers (customer_number);
create index idx_claim_item_tenant on damage_claim_items (tenant_id);
create index idx_claim_item_claim on damage_claim_items (claim_id);
create index idx_claim_item_product on damage_claim_items (product_id);
create index idx_claim_tenant on damage_claims (tenant_id);
create index idx_claim_number on damage_claims (claim_number);
create index idx_claim_booking on damage_claims (booking_id);
create index idx_claim_return on damage_claims (return_order_id);
create index idx_claim_customer on damage_claims (customer_id);
create index idx_claim_status on damage_claims (status);
create index idx_damage_tenant on damage_records (tenant_id);
create index idx_damage_inspection on damage_records (inspection_id);
create index idx_damage_product on damage_records (product_id);
create index idx_damage_return_item on damage_records (return_item_id);
create index idx_del_tenant on deliveries (tenant_id);
create index idx_del_wh_order on deliveries (warehouse_order_id);
create index idx_del_booking on deliveries (booking_id);
create index idx_del_customer on deliveries (customer_id);
create index idx_del_driver on deliveries (driver_id);
create index idx_del_vehicle on deliveries (vehicle_id);
create index idx_del_date on deliveries (scheduled_date);
create index idx_del_status on deliveries (status);
create index idx_del_audit_tenant on delivery_audits (tenant_id);
create index idx_del_audit_delivery on delivery_audits (delivery_id);
create index idx_stop_route on delivery_route_stops (route_id);
create index idx_stop_delivery on delivery_route_stops (delivery_id);
create index idx_route_tenant on delivery_routes (tenant_id);
create index idx_route_date on delivery_routes (route_date);
create index idx_route_driver on delivery_routes (driver_id);
create index idx_route_vehicle on delivery_routes (vehicle_id);
create index idx_driver_tenant on drivers (tenant_id);
create index idx_driver_user on drivers (user_id);
create index idx_driver_status on drivers (status);
create index idx_event_req_tenant on event_requirements (tenant_id);
create index idx_event_req_event on event_requirements (event_id);
create index idx_event_tenant on events (tenant_id);
create index idx_event_customer on events (customer_id);
create index idx_event_date on events (event_date);
create index idx_inspection_tenant on inspections (tenant_id);
create index idx_inspection_order on inspections (return_order_id);
create index idx_inspection_item on inspections (return_order_item_id);
create index idx_inspection_product on inspections (product_id);
create index idx_cci_count on inventory_cycle_count_items (cycle_count_id);
create index idx_cci_product on inventory_cycle_count_items (product_id);
create index idx_cci_item on inventory_cycle_count_items (inventory_item_id);
create index idx_cc_tenant on inventory_cycle_counts (tenant_id);
create index idx_cc_warehouse on inventory_cycle_counts (warehouse_id);
create index idx_cc_status on inventory_cycle_counts (status);
create index idx_item_tenant on inventory_items (tenant_id);
create index idx_item_product on inventory_items (product_id);
create index idx_item_warehouse on inventory_items (warehouse_id);
create index idx_item_asset_code on inventory_items (asset_code);
create index idx_item_serial on inventory_items (serial_number);
create index idx_item_barcode on inventory_items (barcode);
create index idx_item_status on inventory_items (status);
create index idx_inv_res_tenant on inventory_reservations (tenant_id);
create index idx_inv_res_product on inventory_reservations (product_id);
create index idx_inv_res_dates on inventory_reservations (start_date_time, end_date_time);
create index idx_inv_res_status on inventory_reservations (status);
create index idx_inv_tx_tenant on inventory_transactions (tenant_id);
create index idx_inv_tx_product on inventory_transactions (product_id);
create index idx_inv_tx_created on inventory_transactions (created_at);
create index idx_tr_item_transfer on inventory_transfer_items (transfer_id);
create index idx_tr_item_product on inventory_transfer_items (product_id);
create index idx_tr_item_inventory on inventory_transfer_items (inventory_item_id);
create index idx_transfer_tenant on inventory_transfers (tenant_id);
create index idx_transfer_number on inventory_transfers (transfer_number);
create index idx_transfer_status on inventory_transfers (status);
create index idx_invoice_audit_tenant on invoice_audits (tenant_id);
create index idx_invoice_audit_invoice on invoice_audits (invoice_id);
create index idx_invoice_audit_booking on invoice_audits (booking_id);
create index idx_invoice_item_invoice on invoice_items (invoice_id);
create index idx_invoice_item_product on invoice_items (product_id);
create index idx_invoice_tenant on invoices (tenant_id);
create index idx_invoice_number on invoices (tenant_id, invoice_number);
create index idx_invoice_booking on invoices (booking_id);
create index idx_invoice_customer on invoices (customer_id);
create index idx_invoice_status on invoices (status);
create index idx_kit_comp_tenant on kit_components (tenant_id);
create index idx_kit_comp_def on kit_components (kit_definition_id);
create index idx_kit_comp_prod on kit_components (component_product_id);
create index idx_kit_def_tenant on kit_definitions (tenant_id);
create index idx_kit_def_product on kit_definitions (product_id);
create index idx_lead_act_tenant_lead on lead_activities (tenant_id, lead_id);
create index idx_lead_act_occurred on lead_activities (occurred_at);
create index idx_lead_fu_tenant_lead on lead_follow_ups (tenant_id, lead_id);
create index idx_lead_fu_tenant_assigned on lead_follow_ups (tenant_id, assigned_to);
create index idx_lead_fu_due on lead_follow_ups (due_at);
create index idx_lead_tenant on leads (tenant_id);
create index idx_lead_status on leads (status);
create index idx_lead_event_date on leads (event_date);
create index idx_load_item_tenant on load_list_items (tenant_id);
create index idx_load_item_list on load_list_items (load_list_id);
create index idx_load_item_product on load_list_items (product_id);
create index idx_load_item_container on load_list_items (container_id);
create index idx_load_item_status on load_list_items (status);
create index idx_load_list_tenant on load_lists (tenant_id);
create index idx_load_list_order on load_lists (warehouse_order_id);
create index idx_load_list_delivery on load_lists (delivery_id);
create index idx_load_list_vehicle on load_lists (vehicle_id);
create index idx_load_list_driver on load_lists (driver_id);
create index idx_load_list_number on load_lists (load_list_number);
create index idx_load_list_status on load_lists (status);
create index idx_notifaudit_tenant on notification_audit (tenant_id);
create index idx_notifaudit_notif on notification_audit (notification_id);
create index idx_notifpref_tenant on notification_preferences (tenant_id);
create index idx_notifpref_cust on notification_preferences (tenant_id, customer_id);
create index idx_notifpref_user on notification_preferences (tenant_id, user_id);
create index idx_notiftpl_tenant on notification_templates (tenant_id);
create index idx_notiftpl_type_chan on notification_templates (tenant_id, notification_type, channel);
create index idx_notif_tenant on notifications (tenant_id);
create index idx_notif_user on notifications (recipient_user_id);
create index idx_notif_cust on notifications (recipient_customer_id);
create index idx_notif_status on notifications (status);
create index idx_notif_ref on notifications (reference_type, reference_id);
create index idx_op_conf_tenant on operational_conflicts (tenant_id);
create index idx_op_conf_status on operational_conflicts (status);
create index idx_op_conf_type on operational_conflicts (conflict_type);
create index idx_pack_item_tenant on pack_list_items (tenant_id);
create index idx_pack_item_list on pack_list_items (pack_list_id);
create index idx_pack_item_product on pack_list_items (product_id);
create index idx_pack_item_container on pack_list_items (container_id);
create index idx_pack_item_status on pack_list_items (status);
create index idx_pack_list_tenant on pack_lists (tenant_id);
create index idx_pack_list_order on pack_lists (warehouse_order_id);
create index idx_pack_list_number on pack_lists (pack_list_number);
create index idx_pack_list_status on pack_lists (status);
create index idx_pack_list_assigned on pack_lists (assigned_to);
create index idx_container_tenant on packing_containers (tenant_id);
create index idx_container_code on packing_containers (container_code);
create index idx_container_status on packing_containers (status);
create index idx_container_warehouse on packing_containers (warehouse_id);
create index idx_payment_tenant on payment (tenant_id);
create index idx_payment_booking on payment (booking_id);
create index idx_payment_customer on payment (customer_id);
create index idx_payment_status on payment (payment_status);
create index idx_payaudit_tenant on payment_audit (tenant_id);
create index idx_payaudit_booking on payment_audit (booking_id);
create index idx_payaudit_payment on payment_audit (payment_id);
create index idx_pick_item_tenant on pick_list_items (tenant_id);
create index idx_pick_item_list on pick_list_items (pick_list_id);
create index idx_pick_item_product on pick_list_items (product_id);
create index idx_pick_item_asset on pick_list_items (inventory_item_id);
create index idx_pick_item_status on pick_list_items (status);
create index idx_pick_item_sequence on pick_list_items (sequence_number);
create index idx_pick_list_tenant on pick_lists (tenant_id);
create index idx_pick_list_order on pick_lists (warehouse_order_id);
create index idx_pick_list_number on pick_lists (pick_list_number);
create index idx_pick_list_status on pick_lists (status);
create index idx_pick_list_assigned on pick_lists (assigned_to);
create index idx_pick_list_priority on pick_lists (priority);
create index idx_pick_verif_tenant on pick_verifications (tenant_id);
create index idx_pick_verif_list on pick_verifications (pick_list_id);
create index idx_pick_verif_order on pick_verifications (warehouse_order_id);
create index idx_category_tenant on product_categories (tenant_id);
create index idx_category_parent on product_categories (parent_category_id);
create index idx_product_tenant on products (tenant_id);
create index idx_product_sku on products (sku);
create index idx_product_category on products (category_id);
create index idx_product_status on products (status);
create index idx_quotes_tenant on quotes (tenant_id);
create index idx_cart_item_cart on rental_cart_items (cart_id);
create index idx_cart_item_product on rental_cart_items (product_id);
create index idx_cart_tenant on rental_carts (tenant_id);
create index idx_cart_customer on rental_carts (customer_id);
create index idx_rri_request on rental_request_items (rental_request_id);
create index idx_rri_product on rental_request_items (product_id);
create index idx_rental_req_tenant on rental_requests (tenant_id);
create index idx_rental_req_status on rental_requests (status);
create index idx_rental_req_lead on rental_requests (lead_id);
create index idx_rental_req_quote on rental_requests (quote_id);
create index idx_repair_tenant on repair_orders (tenant_id);
create index idx_repair_number on repair_orders (repair_number);
create index idx_repair_claim on repair_orders (claim_id);
create index idx_repair_product on repair_orders (product_id);
create index idx_repair_status on repair_orders (status);
create index idx_replacement_tenant on replacement_orders (tenant_id);
create index idx_replacement_number on replacement_orders (replacement_number);
create index idx_replacement_claim on replacement_orders (claim_id);
create index idx_replacement_product on replacement_orders (product_id);
create index idx_replacement_status on replacement_orders (status);
create index idx_return_audit_tenant on return_audits (tenant_id);
create index idx_return_audit_order on return_audits (return_order_id);
create index idx_return_item_tenant on return_order_items (tenant_id);
create index idx_return_item_order on return_order_items (return_order_id);
create index idx_return_item_product on return_order_items (product_id);
create index idx_return_order_tenant on return_orders (tenant_id);
create index idx_return_order_booking on return_orders (booking_id);
create index idx_return_order_status on return_orders (status);
create index idx_return_order_driver on return_orders (driver_id);
create index idx_return_order_vehicle on return_orders (vehicle_id);
create index idx_return_order_date on return_orders (scheduled_date);
create index idx_sm_tenant on stock_movements (tenant_id);
create index idx_sm_product on stock_movements (product_id);
create index idx_sm_item on stock_movements (inventory_item_id);
create index idx_sm_warehouse on stock_movements (warehouse_id);
create index idx_sm_type on stock_movements (movement_type);
create index idx_sm_created on stock_movements (created_at);
create index idx_storefront_tenant on tenant_storefront_config (tenant_id);
create index idx_veh_tenant on vehicles (tenant_id);
create index idx_veh_status on vehicles (status);
create index idx_wh_audit_tenant on warehouse_audits (tenant_id);
create index idx_wh_audit_order on warehouse_audits (warehouse_order_id);
create index idx_wh_audit_booking on warehouse_audits (booking_id);
create index idx_wh_exc_tenant on warehouse_exceptions (tenant_id);
create index idx_wh_exc_order on warehouse_exceptions (warehouse_order_id);
create index idx_wh_exc_pick on warehouse_exceptions (pick_list_id);
create index idx_wh_exc_pack on warehouse_exceptions (pack_list_id);
create index idx_wh_exc_load on warehouse_exceptions (load_list_id);
create index idx_wh_exc_status on warehouse_exceptions (status);
create index idx_wh_exc_severity on warehouse_exceptions (severity);
create index idx_wh_exc_type on warehouse_exceptions (type);
create index idx_wh_loc_tenant on warehouse_locations (tenant_id);
create index idx_wh_loc_code on warehouse_locations (code);
create index idx_checklist_tenant on warehouse_order_checklists (tenant_id);
create index idx_checklist_order on warehouse_order_checklists (warehouse_order_id);
create index idx_checklist_stage on warehouse_order_checklists (stage);
create index idx_wh_item_order on warehouse_order_items (warehouse_order_id);
create index idx_wh_item_product on warehouse_order_items (product_id);
create index idx_wh_order_tenant on warehouse_orders (tenant_id);
create index idx_wh_order_booking on warehouse_orders (booking_id);
create index idx_wh_order_event on warehouse_orders (event_id);
create index idx_wh_order_customer on warehouse_orders (customer_id);
create index idx_wh_order_status on warehouse_orders (status);
create index idx_wh_order_priority on warehouse_orders (priority);
create index idx_wh_stock_tenant on warehouse_stock (tenant_id);
create index idx_wh_stock_product on warehouse_stock (product_id);
create index idx_wh_stock_warehouse on warehouse_stock (warehouse_id);
create index idx_wh_sub_tenant on warehouse_substitutions (tenant_id);
create index idx_wh_sub_order on warehouse_substitutions (warehouse_order_id);
create index idx_wh_sub_status on warehouse_substitutions (status);
create index idx_wh_tenant on warehouses (tenant_id);
create index idx_wh_code on warehouses (code);

