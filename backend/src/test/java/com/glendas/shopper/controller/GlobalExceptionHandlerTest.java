package com.glendas.shopper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Unit test: the handler turns validation failures and unexpected errors into the contract's
 * {@code Problem} shape. Uses a tiny stand-in controller. No Spring context and no database.
 */
class GlobalExceptionHandlerTest {

    record Widget(@NotBlank String name, @Positive int pricePence) {
    }

    @RestController
    static class WidgetController {
        @PostMapping("/widgets")
        String create(@Valid @RequestBody Widget widget) {
            return "ok";
        }

        @PostMapping("/boom")
        String boom() {
            throw new IllegalStateException("database password is hunter2");
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new WidgetController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void invalidBody_returns422ProblemWithOneErrorPerField() throws Exception {
        mvc.perform(post("/widgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"pricePence\": 0}"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'pricePence')].message").exists());
    }

    @Test
    void validBody_isNotAnError() throws Exception {
        mvc.perform(post("/widgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Flour\", \"pricePence\": 120}"))
            .andExpect(status().isOk());
    }

    @Test
    void unexpectedError_returns500ProblemWithoutLeakingDetails() throws Exception {
        mvc.perform(post("/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.title").value("Something went wrong"))
            .andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("hunter2"))));
    }
}
