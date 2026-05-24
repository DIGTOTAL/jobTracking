package ru.vk.education.job.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SuggestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void suggest_integration_happyPath_sortedAndLimited() throws Exception {
        String userName = "SuggestUser_Alice";

        // cleanup возможных конфликтующих данных (если БД не пустая)
        mockMvc.perform(delete("/api/vacancies/Suggest_V1"));
        mockMvc.perform(delete("/api/vacancies/Suggest_V2"));
        mockMvc.perform(delete("/api/vacancies/V1"));
        mockMvc.perform(delete("/api/users/" + userName));

        // user
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"" + userName + "\"," +
                                "\"skills\":[\"java\",\"sql\",\"spring\"]," +
                                "\"workExperience\":10" +
                                "}"))
                .andExpect(status().isCreated());

        // vacancies
        mockMvc.perform(post("/api/vacancies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"vacancyName\":\"Suggest_V1\"," +
                                "\"companyName\":\"C1\"," +
                                "\"tags\":[\"java\",\"sql\"]," +
                                "\"requiredWorkExperience\":0" +
                                "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/vacancies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"vacancyName\":\"Suggest_V2\"," +
                                "\"companyName\":\"C2\"," +
                                "\"tags\":[\"java\"]," +
                                "\"requiredWorkExperience\":0" +
                                "}"))
                .andExpect(status().isCreated());

        // suggest: проверяем, что обе наши вакансии есть в выдаче, и V1 ранжируется выше V2.
        mockMvc.perform(get("/api/suggest/{userName}", userName)
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.vacancyName=='Suggest_V1')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.vacancyName=='Suggest_V2')]").isNotEmpty())
                // В силу score=2 против score=1, V1 должен быть первым элементом.
                .andExpect(jsonPath("$[0].vacancyName").value("Suggest_V1"));
    }

    @Test
    void suggest_unknownUser_returns404() throws Exception {
        mockMvc.perform(get("/api/suggest/NoSuchUser"))
                .andExpect(status().isNotFound());
    }

    @Test
    void suggest_negativeLimit_returns400() throws Exception {
        mockMvc.perform(get("/api/suggest/Anyone").param("limit", "-1"))
                .andExpect(status().isBadRequest());
    }
}
