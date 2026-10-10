package com.kris007iron.modulithimpl.catalog.internal;

import com.kris007iron.modulithimpl.catalog.ProductReference;
import com.kris007iron.modulithimpl.catalog.internal.exception.ProductAlreadyExistsException;
import com.kris007iron.modulithimpl.catalog.internal.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @Test
    void shouldReturnProduct() throws Exception {
        UUID id = UUID.randomUUID();
        ProductReference productReference = new ProductReference(id, "SKU-001");

        when(catalogService.findProduct(id))
                .thenReturn(Optional.of(productReference));

        mockMvc.perform(get("/api/products/{productId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(id.toString()))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception
    {
        UUID id = UUID.randomUUID();

        when(catalogService.findProduct(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/{productId}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenProductAlreadyExists() throws Exception {
        when(catalogService.addProduct(any(ProductDTO.class)))
                .thenThrow(new ProductAlreadyExistsException("SKU-001"));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "sku": "SKU-001",
                            "name": "Test product",
                            "description": "Test description",
                            "price": 99.99,
                            "currency": "USD"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenDeletingMissingProduct() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new ProductNotFoundException(id))
                .when(catalogService)
                .deleteProduct(id);

        mockMvc.perform(delete("/api/products/{productId}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/{productId}", id))
                .andExpect(status().isNoContent());

        verify(catalogService).deleteProduct(id);
    }
}
