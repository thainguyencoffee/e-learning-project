package com.el.discount.domain;

import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DiscountTests {

    private Discount discount;

    @BeforeEach
    void setUp() {
        // Invalid discount
        discount = new Discount(
                "DISCOUNT10",
                Type.PERCENTAGE,
                10.0,
                Money.of(5, Currencies.VND),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                100
        );
    }

    @Test
    void isExpired_shouldReturnFalse_whenEndDateIsInFuture() {
        assertFalse(discount.isExpired());
    }

    @Test
    void isExpired_shouldReturnTrue_whenEndDateIsInPast() {
        discount = new Discount(
                "DISCOUNT10",
                Type.PERCENTAGE,
                10.0,
                Money.of(5, Currencies.VND),
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600),
                100
        );
        assertTrue(discount.isExpired());
    }

    @Test
    void isActive_shouldReturnTrue_whenCurrentDateIsWithinStartAndEndDate() {
        assertTrue(discount.isActive());
    }

    @Test
    void isActive_shouldReturnFalse_whenCurrentDateIsOutsideStartAndEndDate() {
        discount = new Discount(
                "DISCOUNT10",
                Type.PERCENTAGE,
                10.0,
                Money.of(5, Currencies.VND),
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(7200),
                100
        );
        assertFalse(discount.isActive());
    }

    @Test
    void calculateDiscount_shouldReturnDiscountedPrice_whenDiscountIsValid() {
        MonetaryAmount originalPrice = Money.of(100, Currencies.VND);
        MonetaryAmount discountedPrice = discount.calculateDiscount(originalPrice);
        assertEquals(Money.of(10, Currencies.VND), discountedPrice);
    }

    @Test
    void calculateDiscount_shouldThrowException_whenDiscountIsInvalid() {
        discount = new Discount(
                "DISCOUNT10",
                Type.PERCENTAGE,
                10.0,
                Money.of(5, Currencies.VND),
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600),
                100
        );
        MonetaryAmount originalPrice = Money.of(100, Currencies.VND);
        assertThrows(InputInvalidException.class, () -> discount.calculateDiscount(originalPrice));
    }

    @Test
    void increaseUsage_shouldIncreaseCurrentUsage_whenUsageIsBelowMax() {
        discount.increaseUsage();
        assertEquals(1, discount.getCurrentUsage());
    }


    @Test
    void increaseUsage_shouldThrowException_whenUsageIsAtMax() {
        discount = new Discount(
                "DISCOUNT10",
                Type.PERCENTAGE,
                10.0,
                Money.of(5, Currencies.VND),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                1
        );
        discount.increaseUsage();
        assertThrows(InputInvalidException.class, discount::increaseUsage);
    }

    @Test
    void updateInfo_shouldUpdateDiscount_whenCurrentUsageIsZero() {
        Discount newDiscount = new Discount(
                "DISCOUNT20",
                Type.FIXED,
                null,
                Money.of(20, Currencies.VND),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                100
        );
        discount.updateInfo(newDiscount);
        assertEquals("DISCOUNT20", discount.getCode());
        assertEquals(Type.FIXED, discount.getType());
        assertEquals(Money.of(20, Currencies.VND), discount.getFixedPrice());
    }

    @Test
    void updateInfo_shouldThrowException_whenCurrentUsageIsGreaterThanZero() {
        discount.increaseUsage();
        Discount newDiscount = new Discount(
                "DISCOUNT20",
                Type.FIXED,
                null,
                Money.of(20, Currencies.VND),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                100
        );
        assertThrows(InputInvalidException.class, () -> discount.updateInfo(newDiscount));
    }

    @Test
    void delete_shouldMarkDiscountAsDeleted_whenCurrentUsageIsZero() {
        discount.delete();
        assertTrue(discount.isDeleted());
    }

    @Test
    void delete_shouldThrowException_whenCurrentUsageIsGreaterThanZero() {
        discount.increaseUsage();
        assertThrows(InputInvalidException.class, discount::delete);
    }

    @Test
    void restore_shouldUnmarkDiscountAsDeleted() {
        discount.delete();
        discount.restore();
        assertFalse(discount.isDeleted());
    }

}
