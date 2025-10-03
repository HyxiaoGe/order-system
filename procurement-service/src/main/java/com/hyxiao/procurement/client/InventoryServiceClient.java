package com.hyxiao.procurement.client;

import com.hyxiao.procurement.dto.InventoryInfoDTO;
import com.hyxiao.procurement.dto.ApiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 库存服务客户端
 * 用于调用库存服务的API获取库存信息
 */
@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryServiceClient {
    
    /**
     * 获取所有产品的库存信息
     */
    @GetMapping("/all")
    ApiResponseDTO<List<InventoryInfoDTO>> getAllInventoryResponse();
    
    /**
     * 获取指定产品的库存信息
     */
    @GetMapping("/{productId}")
    ApiResponseDTO<InventoryInfoDTO> getInventoryByProductIdResponse(@PathVariable String productId);
    
    /**
     * 便利方法：直接获取数据部分
     */
    default List<InventoryInfoDTO> getAllInventory() {
        ApiResponseDTO<List<InventoryInfoDTO>> response = getAllInventoryResponse();
        return response.isSuccess() ? response.getData() : java.util.Collections.emptyList();
    }
    
    default InventoryInfoDTO getInventoryByProductId(String productId) {
        ApiResponseDTO<InventoryInfoDTO> response = getInventoryByProductIdResponse(productId);
        return response.isSuccess() ? response.getData() : null;
    }
}