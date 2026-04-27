package com.example.softwaremetric.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UseCaseModelControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersUseCaseModelPage() throws Exception {
        mockMvc.perform(get("/use-case-model"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("用例模型输入")))
                .andExpect(content().string(containsString("参与者")))
                .andExpect(content().string(containsString("用例")));
    }

    @Test
    void calculatesStructuredUseCaseModel() throws Exception {
        mockMvc.perform(post("/use-case-model")
                        .param("actorsText", "普通用户, 复杂\n支付平台, 一般")
                        .param("useCasesText", "提交订单, 一般\n支付订单, 复杂")
                        .param("technicalFactor", "1.0")
                        .param("environmentalFactor", "1.0")
                        .param("hoursPerUseCasePoint", "20.0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("用例点 UCP")))
                .andExpect(content().string(containsString("支付订单")))
                .andExpect(content().string(containsString("带入估算模型")));
    }
}
