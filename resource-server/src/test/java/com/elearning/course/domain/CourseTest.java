package com.elearning.course.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.util.Set;

class CourseTest {

    private Course course;

    @BeforeEach
    void setUp() {
        Audience audience = new Audience(false, Set.of("email1@example.com"));
        course = new Course("Java Basics", Money.of(100, "USD"), "A basic Java course", audience);
    }

    @Test
    void testCourseCreation() {
        // Assert that the course is created correctly
        assertEquals("Java Basics", course.getTitle());
        assertEquals(Money.of(100, "USD"), course.getPrice());
        assertEquals("A basic Java course", course.getDescription());
        assertNotNull(course.getAudience());
    }

    @Test
    void testApplyValidDiscount() {
        // Act
        MonetaryAmount discount = Money.of(20, "USD");
        course.applyDiscount(discount);

        // Assert
        assertEquals(Money.of(80, "USD"), course.getDiscountedPrice());
    }

    @Test
    void testApplyDiscountZeroOrNegativeFinalPrice() {
        // Apply a discount larger than the price to ensure it is capped at zero
        MonetaryAmount discountAmount = Money.of(120, "USD");
        course.applyDiscount(discountAmount);

        // Assert that the final price is zero
        assertEquals(Money.zero(course.getPrice().getCurrency()), course.getDiscountedPrice());
    }

    @Test
    void testApplyNullDiscountThrowsException() {
        // Expect an IllegalArgumentException when applying a null discount
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            course.applyDiscount(null);
        });
        assertEquals("Discount amount must not be null", exception.getMessage());
    }

    @Test
    void testAddSection() {
        // Create a new section and add it to the course
        CourseSection section = new CourseSection("Section 1");
        course.addSection(section);

        // Assert that the section is added
        assertTrue(course.getSections().contains(section));
    }

    @Test
    void testAddNullSectionThrowsException() {
        // Expect an IllegalArgumentException when trying to add a null section
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            course.addSection(null);
        });
        assertEquals("Section can't be null", exception.getMessage());
    }

    @Test
    void testRemoveSectionsOrphan() {
        // Add sections to the course
        CourseSection section1 = new CourseSection("Section 1");
        section1.setId(1L);
        CourseSection section2 = new CourseSection("Section 2");
        section2.setId(2L);
        course.addSection(section1);
        course.addSection(section2);

        // Remove sections not in the given valid section IDs
        Set<Long> validSectionIds = Set.of(section1.getId());
        course.removeSectionsOrphan(validSectionIds);

        // Assert that only section1 remains
        assertTrue(course.getSections().contains(section1));
        assertFalse(course.getSections().contains(section2));
    }

    @Test
    void testUpdateCourseInfo() {
        // Update the course information
        Audience newAudience = new Audience(false, Set.of("email2@example.com"));
        MonetaryAmount newPrice = Money.of(150, "USD");
        course.updateInfo("Advanced Java", newPrice, "Advanced Java course", newAudience);

        // Assert the course information is updated
        assertEquals("Advanced Java", course.getTitle());
        assertEquals(newPrice, course.getPrice());
        assertEquals("Advanced Java course", course.getDescription());
        assertEquals(newAudience, course.getAudience());
    }

    @Test
    void testUpdateCourseWithInvalidValues() {
        // Invalid title
        Exception titleException = assertThrows(IllegalArgumentException.class, () -> {
            course.updateInfo("", Money.of(150, "USD"), "New description", new Audience(false, Set.of("email@example.com")));
        });
        assertEquals("Title must not be empty", titleException.getMessage());

        // Invalid price
        Exception priceException = assertThrows(IllegalArgumentException.class, () -> {
            course.updateInfo("Valid Title", null, "New description", new Audience(false, Set.of("email@example.com")));
        });
        assertEquals("Price must not be null", priceException.getMessage());
    }
}
