package com.kris007iron.modulithimpl.inventory.internal;

import com.kris007iron.modulithimpl.inventory.internal.exception.InsufficientStockException;
import com.kris007iron.modulithimpl.inventory.internal.exception.StockNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void shouldAddStock() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setLocationId(locationId);
        stock.setQuantity(10L);
        stock.setReservedQuantity(0L);

        when(inventoryService.addStock(productId, locationId, 10L))
                .thenReturn(stock);

        mockMvc.perform(post("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString())
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.locationId").value(locationId.toString()))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(0));
    }

    @Test
    void shouldRemoveStock() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setLocationId(locationId);
        stock.setQuantity(5L);
        stock.setReservedQuantity(0L);

        when(inventoryService.removeStock(productId, locationId, 5L))
                .thenReturn(stock);

        mockMvc.perform(patch("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString())
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void shouldFindStock() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setLocationId(locationId);
        stock.setQuantity(10L);
        stock.setReservedQuantity(2L);

        when(inventoryService.findStock(productId, locationId))
                .thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.locationId").value(locationId.toString()))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.reservedQuantity").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenStockDoesNotExist() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        when(inventoryService.findStock(productId, locationId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        when(inventoryService.removeStock(productId, locationId, 10L))
                .thenThrow(new InsufficientStockException(productId, locationId));

        mockMvc.perform(patch("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString())
                        .param("quantity", "10"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenStockIsMissing() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        when(inventoryService.removeStock(productId, locationId, 10L))
                .thenThrow(new StockNotFoundException(productId, locationId));

        mockMvc.perform(patch("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString())
                        .param("quantity", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenQuantityIsInvalid() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        when(inventoryService.addStock(productId, locationId, 0L))
                .thenThrow(new IllegalArgumentException("Quantity must be greater than zero"));

        mockMvc.perform(post("/api/stock")
                        .param("productId", productId.toString())
                        .param("locationId", locationId.toString())
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Quantity must be greater than zero"));
    }
}