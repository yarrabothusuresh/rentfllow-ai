package com.rentflow.inventory.service;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.inventory.dto.InventoryTransferDTO;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.*;
import com.rentflow.warehouse.model.Warehouse;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarehouseTransferService {

    private final InventoryTransferRepository transferRepository;
    private final InventoryTransferItemRepository transferItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public WarehouseTransferService(InventoryTransferRepository transferRepository,
                                  InventoryTransferItemRepository transferItemRepository,
                                  WarehouseRepository warehouseRepository,
                                  ProductRepository productRepository,
                                  InventoryItemRepository inventoryItemRepository,
                                  WarehouseStockRepository warehouseStockRepository,
                                  StockMovementRepository stockMovementRepository) {
        this.transferRepository = transferRepository;
        this.transferItemRepository = transferItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    // 1. CREATE TRANSFER REQUEST
    @Transactional
    public InventoryTransferDTO createTransfer(String tenantId, InventoryTransferDTO request, String username) {
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and target warehouses cannot be identical");
        }

        Warehouse fromWh = warehouseRepository.findByTenantIdAndId(tenantId, request.getFromWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source warehouse not found"));
        Warehouse toWh = warehouseRepository.findByTenantIdAndId(tenantId, request.getToWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target warehouse not found"));

        String transferNum = "TRF-" + System.currentTimeMillis() % 1000000;
        InventoryTransfer transfer = new InventoryTransfer(
                null, tenantId, transferNum, fromWh.getId(), toWh.getId(),
                TransferStatus.REQUESTED, username, request.getNotes()
        );
        transfer = transferRepository.save(transfer);

        List<InventoryTransferItem> savedItems = new ArrayList<>();
        if (request.getItems() != null) {
            for (InventoryTransferDTO.TransferItemDTO itemDto : request.getItems()) {
                Product product = productRepository.findByTenantIdAndId(tenantId, itemDto.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + itemDto.getProductId()));

                if (product.getTrackingType() == ProductTrackingType.SERIALIZED) {
                    if (itemDto.getInventoryItemId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serialized transfer item requires inventoryItemId");
                    }
                    InventoryItem item = inventoryItemRepository.findByTenantIdAndId(tenantId, itemDto.getInventoryItemId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serialized asset not found"));

                    if (!item.getWarehouseId().equals(fromWh.getId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset " + item.getAssetCode() + " belongs to another warehouse");
                    }
                    if (item.getStatus() != AssetStatus.AVAILABLE) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset " + item.getAssetCode() + " is not AVAILABLE for transfer");
                    }
                    item.setStatus(AssetStatus.TRANSFER_PENDING);
                    inventoryItemRepository.save(item);

                    InventoryTransferItem ti = new InventoryTransferItem(null, transfer.getId(), product.getId(), item.getId(), 1, 0, item.getCondition(), itemDto.getNotes());
                    savedItems.add(transferItemRepository.save(ti));

                } else {
                    InventoryTransferItem ti = new InventoryTransferItem(null, transfer.getId(), product.getId(), null, Math.max(1, itemDto.getQuantity()), 0, AssetCondition.GOOD, itemDto.getNotes());
                    savedItems.add(transferItemRepository.save(ti));
                }
            }
        }

        return mapToDTO(tenantId, transfer, savedItems);
    }

    // 2. APPROVE TRANSFER
    @Transactional
    public InventoryTransferDTO approveTransfer(String tenantId, UUID transferId, String username) {
        InventoryTransfer transfer = transferRepository.findByTenantIdAndId(tenantId, transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + transferId));

        if (transfer.getStatus() != TransferStatus.REQUESTED && transfer.getStatus() != TransferStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot approve transfer in status: " + transfer.getStatus());
        }

        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(username);
        transfer = transferRepository.save(transfer);

        List<InventoryTransferItem> items = transferItemRepository.findByTransferId(transfer.getId());
        return mapToDTO(tenantId, transfer, items);
    }

    // 3. SHIP / IN_TRANSIT
    @Transactional
    public InventoryTransferDTO shipTransfer(String tenantId, UUID transferId, String username) {
        InventoryTransfer transfer = transferRepository.findByTenantIdAndId(tenantId, transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + transferId));

        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer must be APPROVED before shipping");
        }

        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setShippedAt(LocalDateTime.now());
        transfer = transferRepository.save(transfer);

        List<InventoryTransferItem> items = transferItemRepository.findByTransferId(transfer.getId());
        for (InventoryTransferItem item : items) {
            Product product = productRepository.findByTenantIdAndId(tenantId, item.getProductId()).orElse(null);
            if (product != null && product.getTrackingType() == ProductTrackingType.QUANTITY) {
                WarehouseStock sourceStock = warehouseStockRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, product.getId(), transfer.getFromWarehouseId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient source warehouse stock"));
                sourceStock.setQuantityOnHand(Math.max(0, sourceStock.getQuantityOnHand() - item.getQuantity()));
                sourceStock.setQuantityAvailable(Math.max(0, sourceStock.getQuantityAvailable() - item.getQuantity()));
                warehouseStockRepository.save(sourceStock);
            }

            StockMovement sm = new StockMovement(
                    null, tenantId, item.getProductId(), item.getInventoryItemId(), transfer.getFromWarehouseId(),
                    MovementType.TRANSFER_OUT, item.getQuantity(), transfer.getFromWarehouseId(), transfer.getToWarehouseId(),
                    "TRANSFER", transfer.getId(), "Shipped transfer " + transfer.getTransferNumber(), username
            );
            stockMovementRepository.save(sm);
        }

        return mapToDTO(tenantId, transfer, items);
    }

    // 4. RECEIVE TRANSFER (HANDLES PARTIAL RECEIVING)
    @Transactional
    public InventoryTransferDTO receiveTransfer(String tenantId, UUID transferId, List<InventoryTransferDTO.TransferItemDTO> receivedItemsRequest, String username) {
        InventoryTransfer transfer = transferRepository.findByTenantIdAndId(tenantId, transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + transferId));

        if (transfer.getStatus() != TransferStatus.IN_TRANSIT && transfer.getStatus() != TransferStatus.PARTIALLY_RECEIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer is not in transit or partially received");
        }

        List<InventoryTransferItem> items = transferItemRepository.findByTransferId(transfer.getId());
        Map<UUID, InventoryTransferDTO.TransferItemDTO> reqMap = receivedItemsRequest != null ?
                receivedItemsRequest.stream().collect(Collectors.toMap(InventoryTransferDTO.TransferItemDTO::getId, i -> i, (a, b) -> a)) : Collections.emptyMap();

        boolean allComplete = true;

        for (InventoryTransferItem item : items) {
            int toReceive = item.getQuantity() - item.getReceivedQuantity();
            if (toReceive <= 0) continue;

            if (reqMap.containsKey(item.getId())) {
                toReceive = Math.min(toReceive, reqMap.get(item.getId()).getReceivedQuantity());
            }

            item.setReceivedQuantity(item.getReceivedQuantity() + toReceive);
            transferItemRepository.save(item);

            if (item.getReceivedQuantity() < item.getQuantity()) {
                allComplete = false;
            }

            Product product = productRepository.findByTenantIdAndId(tenantId, item.getProductId()).orElse(null);
            if (product != null) {
                if (product.getTrackingType() == ProductTrackingType.SERIALIZED) {
                    if (item.getInventoryItemId() != null) {
                        InventoryItem invItem = inventoryItemRepository.findByTenantIdAndId(tenantId, item.getInventoryItemId()).orElse(null);
                        if (invItem != null) {
                            invItem.setWarehouseId(transfer.getToWarehouseId());
                            invItem.setStatus(AssetStatus.AVAILABLE);
                            inventoryItemRepository.save(invItem);
                        }
                    }
                } else {
                    final UUID targetWhId = transfer.getToWarehouseId();
                    final UUID prodId = product.getId();
                    WarehouseStock destStock = warehouseStockRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, prodId, targetWhId)
                            .orElseGet(() -> new WarehouseStock(null, tenantId, prodId, targetWhId, 0, 0, 10));

                    destStock.setQuantityOnHand(destStock.getQuantityOnHand() + toReceive);
                    destStock.setQuantityAvailable(destStock.getQuantityAvailable() + toReceive);
                    warehouseStockRepository.save(destStock);
                }
            }

            StockMovement sm = new StockMovement(
                    null, tenantId, item.getProductId(), item.getInventoryItemId(), transfer.getToWarehouseId(),
                    MovementType.TRANSFER_IN, toReceive, transfer.getFromWarehouseId(), transfer.getToWarehouseId(),
                    "TRANSFER", transfer.getId(), "Received transfer " + transfer.getTransferNumber(), username
            );
            stockMovementRepository.save(sm);
        }

        if (allComplete) {
            transfer.setStatus(TransferStatus.RECEIVED);
            transfer.setReceivedAt(LocalDateTime.now());
        } else {
            transfer.setStatus(TransferStatus.PARTIALLY_RECEIVED);
        }
        transfer = transferRepository.save(transfer);

        return mapToDTO(tenantId, transfer, items);
    }

    // 5. GET ALL TRANSFERS
    public List<InventoryTransferDTO> getTransfers(String tenantId) {
        return transferRepository.findByTenantId(tenantId).stream()
                .map(t -> mapToDTO(tenantId, t, transferItemRepository.findByTransferId(t.getId())))
                .collect(Collectors.toList());
    }

    // 6. GET SINGLE TRANSFER BY ID
    public InventoryTransferDTO getTransferById(String tenantId, UUID id) {
        InventoryTransfer t = transferRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + id));
        return mapToDTO(tenantId, t, transferItemRepository.findByTransferId(t.getId()));
    }

    private InventoryTransferDTO mapToDTO(String tenantId, InventoryTransfer t, List<InventoryTransferItem> items) {
        InventoryTransferDTO dto = new InventoryTransferDTO();
        dto.setId(t.getId());
        dto.setTransferNumber(t.getTransferNumber());
        dto.setFromWarehouseId(t.getFromWarehouseId());
        dto.setToWarehouseId(t.getToWarehouseId());
        dto.setStatus(t.getStatus());
        dto.setRequestedBy(t.getRequestedBy());
        dto.setApprovedBy(t.getApprovedBy());
        dto.setShippedAt(t.getShippedAt());
        dto.setReceivedAt(t.getReceivedAt());
        dto.setNotes(t.getNotes());
        dto.setCreatedAt(t.getCreatedAt());

        warehouseRepository.findByTenantIdAndId(tenantId, t.getFromWarehouseId()).ifPresent(w -> dto.setFromWarehouseName(w.getName()));
        warehouseRepository.findByTenantIdAndId(tenantId, t.getToWarehouseId()).ifPresent(w -> dto.setToWarehouseName(w.getName()));

        List<InventoryTransferDTO.TransferItemDTO> itemDtos = items.stream().map(i -> {
            InventoryTransferDTO.TransferItemDTO idto = new InventoryTransferDTO.TransferItemDTO();
            idto.setId(i.getId());
            idto.setProductId(i.getProductId());
            idto.setInventoryItemId(i.getInventoryItemId());
            idto.setQuantity(i.getQuantity());
            idto.setReceivedQuantity(i.getReceivedQuantity());
            idto.setNotes(i.getNotes());

            productRepository.findByTenantIdAndId(tenantId, i.getProductId()).ifPresent(p -> {
                idto.setProductName(p.getName());
                idto.setProductSku(p.getSku());
            });

            if (i.getInventoryItemId() != null) {
                inventoryItemRepository.findByTenantIdAndId(tenantId, i.getInventoryItemId()).ifPresent(inv -> idto.setAssetCode(inv.getAssetCode()));
            }
            return idto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
