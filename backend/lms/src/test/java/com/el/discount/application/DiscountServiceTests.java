package com.el.discount.application;

import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.discount.application.dto.DiscountDTO;
import com.el.discount.application.impl.DiscountServiceImpl;
import com.el.discount.domain.Discount;
import com.el.discount.domain.DiscountRepository;
import com.el.discount.domain.Type;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTests {

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountServiceImpl discountService;

    private Discount discount;
    private DiscountDTO discountDTO;

    @BeforeEach
    void setUp() {
        var code = UUID.randomUUID().toString();

        discount = new Discount(code,
                Type.PERCENTAGE,
                10.0,
                null,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                100);
        discount = spy(discount); // mock :)

        discountDTO = new DiscountDTO(code,
                Type.PERCENTAGE,
                10.0,
                null,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                100);
    }

    @Test
    void testCreateDiscount_ShouldCreateAndSaveBehavior() {
        // mock
        when(discountRepository.save(any(Discount.class))).thenReturn(discount);

        // act
        Discount createdDiscount = discountService.createDiscount(discountDTO);

        // verify
        verify(discountRepository, times(1)).save(any(Discount.class));

        // assert
        assertNotNull(createdDiscount);
        assertEquals(createdDiscount.getCode(), discount.getCode());
        assertEquals(createdDiscount.getType(), discount.getType());
        assertEquals(createdDiscount.getMaxUsage(), discount.getMaxUsage());
        assertEquals(createdDiscount.getPercentage(), discount.getPercentage());
    }

    @Test
    void testCreateDiscount_ShouldThrowExceptionWhenCodeExists() {
        when(discountRepository.existsByCode(discountDTO.code())).thenReturn(true);
        Assertions.assertThrows(InputInvalidException.class, () -> discountService.createDiscount(discountDTO));
    }

    @Test
    void testUpdateDiscount_ShouldUpdateDiscount() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), false)).thenReturn(Optional.of(discount));
        when(discountRepository.save(any(Discount.class))).thenReturn(discount);

        // act
        Discount updatedDiscount = discountService.updateDiscount(discount.getId(), discountDTO);

        // verify
        verify(discountRepository, times(1)).findByIdAndDeleted(discount.getId(), false);
        verify(discountRepository, times(1)).save(any(Discount.class));
        verify(updatedDiscount, times(1)).updateInfo(any(Discount.class));

        // assert
        assertNotNull(updatedDiscount);
    }

    @Test
    void testUpdateDiscount_ShouldThrowExceptionWhenDiscountNotFound() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), false)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> discountService.updateDiscount(discount.getId(), discountDTO));
    }

    @Test
    void testUpdateDiscount_ShouldThrowExceptionWhenNewDiscountCodeNotExists() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), false)).thenReturn(Optional.of(discount));
        when(discountRepository.existsByCode(discount.getCode())).thenReturn(true);
        assertThrows(InputInvalidException.class, () -> discountService.updateDiscount(discount.getId(), discountDTO));
    }

    @Test
    void testDeleteDiscount_ShouldDeleteDiscount() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), false)).thenReturn(Optional.of(discount));

        discountService.deleteDiscount(discount.getId());

        verify(discountRepository, times(1)).findByIdAndDeleted(discount.getId(), false);
        verify(discountRepository, times(1)).save(any(Discount.class));
        verify(discount, times(1)).delete();
    }

    @Test
    void testDeleteDiscount_ShouldThrowExceptionWhenDiscountNotFound() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), false)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> discountService.deleteDiscount(discount.getId()));
    }

    @Test
    void testRestoreDiscount_ShouldRestoreDiscount() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), true)).thenReturn(Optional.of(discount));

        discountService.restoreDiscount(discount.getId());

        verify(discountRepository, times(1)).findByIdAndDeleted(discount.getId(), true);
        verify(discountRepository, times(1)).save(any(Discount.class));
        verify(discount, times(1)).restore();
    }

    @Test
    void testRestoreDiscount_ShouldThrowExceptionWhenDiscountNotFound() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), true)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> discountService.restoreDiscount(discount.getId()));
    }

    @Test
    void testForceDeleteDiscount_ShouldForceDeleteDiscount() {
        when(discount.getId()).thenReturn(1L);
        when(discountRepository.findByIdAndDeleted(discount.getId(), true)).thenReturn(Optional.of(discount));
        discountService.forceDeleteDiscount(discount.getId());

        verify(discountRepository, times(1)).findByIdAndDeleted(discount.getId(), true);
        verify(discountRepository, times(1)).delete(any(Discount.class));
    }

    @Test
    void testCalculateDiscount_ShouldCalculateDiscount() {
        when(discountRepository.findByCodeAndDeleted(discount.getCode(), false)).thenReturn(Optional.of(discount));

        var originalDiscount = Money.of(1000, Currencies.VND);
        MonetaryAmount discountedPrice = discountService.calculateDiscount(discountDTO.code(), originalDiscount);

        verify(discountRepository, times(1)).findByCodeAndDeleted(discount.getCode(), false);
        verify(discount).calculateDiscount(originalDiscount);

        assertNotNull(discountedPrice);
    }

    @Test
    void testIncreaseUsage_ShouldIncreaseUsage() {
        when(discountRepository.findByCodeAndDeleted(discount.getCode(), false)).thenReturn(Optional.of(discount));

        discountService.increaseUsage(discount.getCode());

        verify(discountRepository, times(1)).findByCodeAndDeleted(discount.getCode(), false);
        verify(discountRepository, times(1)).save(any(Discount.class));
        verify(discount).increaseUsage();
    }

}
