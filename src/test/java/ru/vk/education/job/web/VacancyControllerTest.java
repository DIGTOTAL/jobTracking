package ru.vk.education.job.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VacancyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createListGetDelete_flow() throws Exception {
        mockMvc.perform(post("/api/vacancies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"vacancyName\":\"JavaDev\"," +
                                "\"companyName\":\"ACME\"," +
                                "\"tags\":[\"java\"]," +
                                "\"requiredWorkExperience\":2" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vacancyName").value("JavaDev"));

        mockMvc.perform(get("/api/vacancies/JavaDev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("ACME"));

        mockMvc.perform(delete("/api/vacancies/JavaDev"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vacancies/JavaDev"))
                .andExpect(status().isNotFound());
    }
}
