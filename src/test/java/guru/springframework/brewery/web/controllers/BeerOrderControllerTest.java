package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.domain.*;
import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.*;
import guru.springframework.brewery.web.model.OrderStatusEnum;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerOrderDto validBeerOrderDto;

    @BeforeEach
    void setUp() {
        List<BeerOrderLineDto> orderLines1 = new ArrayList<>();
        orderLines1.add(BeerOrderLineDto.builder().beerId(UUID.randomUUID()).build());
        orderLines1.add(BeerOrderLineDto.builder().beerId(UUID.randomUUID()).build());

        validBeerOrderDto = BeerOrderDto.builder()
          .id(UUID.randomUUID())
          .orderStatus(OrderStatusEnum.NEW)
          .customerId(UUID.randomUUID())
          .customerRef("testOrder1")
          .orderStatusCallbackUrl("http://example.com/post")
          .beerOrderLines(orderLines1)
          .build();

        List<BeerOrderDto> beerOrderDtos = Lists.newArrayList(validBeerOrderDto);
        BeerOrderPagedList beerOrderPagedList = new BeerOrderPagedList(beerOrderDtos, PageRequest.of(1, 1), 2L);
        given(beerOrderService.listOrders(any(), any())).willReturn(beerOrderPagedList);

    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @Test
    void listOrders() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
          .get("/api/v1/customers/{customerId}/orders", validBeerOrderDto.getCustomerId()).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].id", is(validBeerOrderDto.getId().toString())));
    }

    @Test
    void getOrder() throws Exception {
        given(beerOrderService.getOrderById(any(), any())).willReturn(validBeerOrderDto);

        mockMvc.perform(MockMvcRequestBuilders
          .get("/api/v1/customers/{customerId}/orders/{orderId}", validBeerOrderDto.getCustomerId(), validBeerOrderDto.getId()).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
          .andExpect(jsonPath("$.id", is(validBeerOrderDto.getId().toString())));
    }
}