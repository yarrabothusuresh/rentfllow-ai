package com.rentflow.portal.ai;

import com.rentflow.portal.dto.PublicProductDTO;
import java.util.List;
import java.util.UUID;

public interface ProductRecommendationService {
    List<PublicProductDTO> getRecommendations(String tenantId, List<UUID> selectedProductIds);
}
