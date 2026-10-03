package com.kris007iron.modulithimpl.catalog.internal;

import com.kris007iron.modulithimpl.catalog.ProductReference;
import com.kris007iron.modulithimpl.catalog.internal.exception.ProductAlreadyExistsException;
import com.kris007iron.modulithimpl.catalog.internal.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CatalogService.class)
public class CatalogServiceTest {
    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() { productRepository.deleteAll(); }

    @Test
    void shouldAddProduct() {
        ProductDTO dto = productDto("SKU-001");
        ProductReference result = catalogService.addProduct(dto);

        assertThat(result).isNotNull();
        assertThat(result.sku()).isEqualTo("SKU-001");

        Optional<Product> saved = productRepository.findBySku("SKU-001");

        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Test product");
    }

    @Test
    void shouldRejectDuplicateSku() {
        ProductReference created = catalogService.addProduct(productDto("SKU-001"));

        assertThatThrownBy(() -> catalogService.addProduct(productDto("SKU-001"))).isInstanceOf(ProductAlreadyExistsException.class);
    }

    @Test
    void shouldFindProductById() {
        ProductReference created = catalogService.addProduct(productDto("SKU-001"));
        Optional<ProductReference> saved = catalogService.findProduct(created.productId());

        assertThat(saved).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist(){
        Optional<ProductReference> result = catalogService.findProduct(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindProductBySku() {
        ProductReference result = catalogService.addProduct(productDto("SKU-001"));
        Optional<ProductReference> productReference = catalogService.findProductBySku("SKU-001");

        assertThat(productReference).isPresent();
        assertThat(productReference.get()).isEqualTo(result);
    }

    @Test
    void shouldReturnEmptyWhenSkuDoesNotExist() {
        Optional<ProductReference> result = catalogService.findProductBySku("SKU-THAT-DOES-NOT-EXIST");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteProduct() {
        ProductReference created = catalogService.addProduct(productDto("SKU-001"));

        catalogService.deleteProduct(created.productId());
        assertThat(productRepository.findById(created.productId())).isEmpty();
    }

    @Test
    void shouldRejectDeletingMissingProduct() {
        UUID productId = UUID.randomUUID();

        assertThatThrownBy(() -> catalogService.deleteProduct(productId)).isInstanceOf(ProductNotFoundException.class);
    }

    private ProductDTO productDto(String sku) {
        return new ProductDTO(
                sku,
                "Test product",
                "Test description",
                BigDecimal.valueOf(99.99),
                Currency.getInstance("USD")
        );
    }
}
