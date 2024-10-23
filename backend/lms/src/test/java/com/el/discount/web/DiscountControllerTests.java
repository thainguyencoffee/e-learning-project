package com.el.discount.web;

import com.el.common.config.SecurityConfig;
import com.el.common.config.jackson.JacksonCustomizations;
import com.el.common.exception.ResourceNotFoundException;
import com.el.discount.application.DiscountService;
import com.el.discount.application.dto.DiscountDTO;
import com.el.discount.domain.Discount;
import com.el.discount.domain.Type;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscountController.class)
@Import({SecurityConfig.class, JacksonCustomizations.class})
class DiscountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiscountService discountService;

    private Discount discount;
    private DiscountDTO discountDTO;

    @BeforeEach
    void setUp() {
        discount = new Discount(UUID.randomUUID().toString(),
                Type.PERCENTAGE,
                10.0,
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100);

        discountDTO = new DiscountDTO(
                UUID.randomUUID().toString(),
                Type.PERCENTAGE,
                10.0,
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100
        );
    }

    @Test
    void getAllDiscounts_shouldReturnPageOfDiscounts() throws Exception {
        Page<Discount> page = new PageImpl<>(Collections.singletonList(discount));
        when(discountService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/discounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getAllDiscounts_shouldReturn403_whenRoleIsTeacher() throws Exception {
        mockMvc.perform(get("/discounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDiscounts_shouldReturn403_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/discounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDiscountById_shouldReturnDiscount() throws Exception {
        when(discountService.findById(anyLong())).thenReturn(discount);

        mockMvc.perform(get("/discounts/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void getDiscountById_shouldReturn404_whenDiscountNotFound() throws Exception {
        when(discountService.findById(anyLong())).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get("/discounts/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDiscountById_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/discounts/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDiscountById_shouldReturn403_whenRoleIsTeacher() throws Exception {
        mockMvc.perform(get("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDiscountById_shouldReturn403_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDiscountByCode_shouldReturnDiscount() throws Exception {
        when(discountService.findByCode(any())).thenReturn(discount);

        mockMvc.perform(get("/discounts/code/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void getDiscountByCode_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/discounts/code/123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDeletedDiscounts_shouldReturnPageOfDeletedDiscounts() throws Exception {
        Page<Discount> page = new PageImpl<>(Collections.singletonList(discount));
        when(discountService.findTrashedDiscount(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/discounts/trash")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getDeletedDiscounts_shouldReturn403_whenRoleIsTeacher() throws Exception {
        mockMvc.perform(get("/discounts/trash")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_teacher")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDiscount_shouldReturnCreatedDiscount() throws Exception {
        Discount createdDiscount = Mockito.mock(Discount.class);
        when(createdDiscount.getId()).thenReturn(1L);
        when(createdDiscount.getCode()).thenReturn(UUID.randomUUID().toString());
        when(createdDiscount.getMaxUsage()).thenReturn(100);
        when(createdDiscount.getStartDate()).thenReturn(LocalDateTime.now().minusSeconds(3600));
        when(createdDiscount.getEndDate()).thenReturn(LocalDateTime.now().plusSeconds(3600));
        when(createdDiscount.getType()).thenReturn(Type.PERCENTAGE);
        when(createdDiscount.getPercentage()).thenReturn(10.0);

        when(discountService.createDiscount(any(DiscountDTO.class))).thenReturn(createdDiscount);

        mockMvc.perform(post("/discounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(createdDiscount.getId()))
                .andExpect(jsonPath("$.code").value(createdDiscount.getCode()))
                .andExpect(jsonPath("$.maxUsage").value(createdDiscount.getMaxUsage()))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").isNotEmpty())
                .andExpect(jsonPath("$.type").value(createdDiscount.getType().name()))
                .andExpect(jsonPath("$.percentage").value(createdDiscount.getPercentage()));
    }

    @Test
    void createDiscount_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/discounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDiscount_ShouldReturn400_WhenValueInvalid() throws Exception {
        discountDTO = new DiscountDTO(
                UUID.randomUUID().toString(),
                Type.PERCENTAGE,
                null, // null at same time
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100
        );

        mockMvc.perform(post("/discounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDiscount_shouldReturnUpdatedDiscount() throws Exception {
        Discount updatedDiscount = Mockito.mock(Discount.class);
        when(updatedDiscount.getId()).thenReturn(1L);
        when(updatedDiscount.getCode()).thenReturn(UUID.randomUUID().toString());
        when(updatedDiscount.getMaxUsage()).thenReturn(100);
        when(updatedDiscount.getStartDate()).thenReturn(LocalDateTime.now().minusSeconds(3600));
        when(updatedDiscount.getEndDate()).thenReturn(LocalDateTime.now().plusSeconds(3600));
        when(updatedDiscount.getType()).thenReturn(Type.PERCENTAGE);
        when(updatedDiscount.getPercentage()).thenReturn(10.0);

        when(discountService.updateDiscount(any(), any(DiscountDTO.class))).thenReturn(updatedDiscount);

        mockMvc.perform(put("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDiscount.getId()))
                .andExpect(jsonPath("$.code").value(updatedDiscount.getCode()))
                .andExpect(jsonPath("$.maxUsage").value(updatedDiscount.getMaxUsage()))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").isNotEmpty())
                .andExpect(jsonPath("$.type").value(updatedDiscount.getType().name()))
                .andExpect(jsonPath("$.percentage").value(updatedDiscount.getPercentage()));
    }

    @Test
    void updateDiscount_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(put("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDiscount_ShouldReturn400_WhenValueInvalid() throws Exception {
        discountDTO = new DiscountDTO(
                UUID.randomUUID().toString(),
                Type.PERCENTAGE,
                null, // null at same time
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100
        );

        mockMvc.perform(put("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDiscount_shouldReturn404_whenDiscountNotFound() throws Exception {
        when(discountService.updateDiscount(any(), any(DiscountDTO.class))).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(put("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDiscount_shouldReturn204_whenDiscountDeleted() throws Exception {
        // default: no mock == no throws

        mockMvc.perform(delete("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDiscount_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(delete("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDiscount_shouldReturn404_whenDiscountNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(discountService).deleteDiscount(any());

        mockMvc.perform(delete("/discounts/123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void restoreDiscount_shouldReturn204_whenDiscountDeleted() throws Exception {
        // default: no mock == no throws

        mockMvc.perform(post("/discounts/123/restore")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void restoreDiscount_shouldReturn403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/discounts/123/restore")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void restoreDiscount_shouldReturn404_whenDiscountNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(discountService).restoreDiscount(any());

        mockMvc.perform(post("/discounts/123/restore")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteForceDiscount_shouldReturn204_whenDiscountDeleted() throws Exception {
        // default: no mock == no throws

        mockMvc.perform(delete("/discounts/123?force=true")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


}
