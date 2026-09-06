package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AutomationActionRegistry {

    private final Map<AutomationActionType, AutomationActionHandler> handlerMap = new EnumMap<>(AutomationActionType.class);

    @Autowired
    public AutomationActionRegistry(List<AutomationActionHandler> handlers) {
        for (AutomationActionHandler handler : handlers) {
            handlerMap.put(handler.getActionType(), handler);
        }
    }

    public Optional<AutomationActionHandler> getHandler(AutomationActionType type) {
        if (type == null) return Optional.empty();
        return Optional.ofNullable(handlerMap.get(type));
    }

    public boolean hasHandler(AutomationActionType type) {
        return type != null && handlerMap.containsKey(type);
    }
}
