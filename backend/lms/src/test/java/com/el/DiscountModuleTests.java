package com.el;

import com.el.common.Currencies;
import com.el.discount.web.dto.DiscountDTO;
import com.el.discount.domain.Type;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountModuleTests extends AbstractLmsApplicationTests {

    @Test
    void testCreateDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();
        performCreateDiscountTest(discountDTO);
    }

    @Test
    void testUpdateDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();

        String discountId = performCreateDiscountTest(discountDTO);

        DiscountDTO updateDiscountDTO = new DiscountDTO("DISCOUNT_30",
                Type.PERCENTAGE,
                30.0,
                Money.of(20000, Currencies.VND),
                null,
                LocalDateTime.now().minusSeconds(360),
                LocalDateTime.now().plusSeconds(360),
                100);

        webTestClient.put().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateDiscountDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(discountId)
                .jsonPath("$.code").isEqualTo(updateDiscountDTO.code())
                .jsonPath("$.percentage").isEqualTo(updateDiscountDTO.percentage())
                .jsonPath("$.maxUsage").isEqualTo(updateDiscountDTO.maxUsage())
                .jsonPath("$.startDate").isNotEmpty()
                .jsonPath("$.endDate").isNotEmpty()
                .jsonPath("$.type").isEqualTo(updateDiscountDTO.type().name())
                .jsonPath("$.createdBy").isEqualTo(extractClaimFromToken(bossToken.accessToken, "preferred_username"))
                .jsonPath("$.lastModifiedBy").isEqualTo(extractClaimFromToken(bossToken.accessToken, "preferred_username"))
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.lastModifiedDate").isNotEmpty();
    }

    @Test
    void testDeleteDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();
        String discountId = performCreateDiscountTest(discountDTO);

        webTestClient.delete().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isEmpty();
    }

    @Test
    void testRestoreDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();

        String discountId = performCreateDiscountTest(discountDTO);

        webTestClient.delete().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isEmpty();

        webTestClient.post().uri("/discounts/{id}/restore", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isOk();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isNotEmpty();
    }

    @Test
    void testDeleteForceDiscount_Successful() {
        DiscountDTO discountDTO = TestFactory.createDefaultDiscountDTO();

        String discountId = performCreateDiscountTest(discountDTO);

        webTestClient.delete().uri("/discounts/{id}", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findByIdAndDeleted(Long.valueOf(discountId), false)).isEmpty();

        webTestClient.delete().uri("/discounts/{id}?force=true", discountId)
                .headers(header -> header.setBearerAuth(bossToken.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        assertThat(discountRepository.findById(Long.valueOf(discountId))).isEmpty();
    }

}
