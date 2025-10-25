package com.msa.commerce.monolith.product.adapter.in.web.mapper;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.monolith.product.adapter.in.web.ProductSearchRequest;
import com.msa.commerce.monolith.product.adapter.in.web.ProductVerifyRequest;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyCommand;

@DisplayName("ProductMapper 테스트")
class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    @DisplayName("ProductSearchRequest를 ProductSearchCommand로 변환 - 기본 필드")
    void toSearchCommand_BasicFields() {
        // given
        ProductSearchRequest request = new ProductSearchRequest();

        // when
        ProductSearchCommand command = productMapper.toSearchCommand(request);

        // then
        assertThat(command).isNotNull();
    }

    @Test
    @DisplayName("ProductVerifyRequest를 ProductVerifyCommand로 변환")
    void toVerifyCommand() {
        // given
        ProductVerifyRequest request = new ProductVerifyRequest(
            List.of(
                new ProductVerifyRequest.ProductVerifyItem(1L, 5),
                new ProductVerifyRequest.ProductVerifyItem(2L, 10)
            )
        );

        // when
        ProductVerifyCommand command = productMapper.toVerifyCommand(request);

        // then
        assertThat(command).isNotNull();
        assertThat(command.getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("ProductVerifyRequest를 ProductVerifyCommand로 변환 - 빈 목록")
    void toVerifyCommand_EmptyList() {
        // given
        ProductVerifyRequest request = new ProductVerifyRequest(List.of());

        // when
        ProductVerifyCommand command = productMapper.toVerifyCommand(request);

        // then
        assertThat(command).isNotNull();
        assertThat(command.getItems()).isEmpty();
    }
}
