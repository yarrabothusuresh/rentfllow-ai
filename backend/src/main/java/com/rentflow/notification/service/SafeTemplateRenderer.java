package com.rentflow.notification.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SafeTemplateRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    public String render(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return sanitize(template);
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            Object rawValue = variables.get(key);
            String replacement = rawValue != null ? sanitize(String.valueOf(rawValue)) : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sanitize(sb.toString());
    }

    private String sanitize(String input) {
        if (input == null) return "";
        // Sanitize dangerous HTML script tags to prevent XSS injection
        return input.replace("<script>", "")
                    .replace("</script>", "")
                    .replace("javascript:", "")
                    .replace("onload=", "")
                    .replace("onerror=", "");
    }
}
