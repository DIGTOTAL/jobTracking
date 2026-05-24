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
class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createListGetUpdateDelete_flow() throws Exception {
        // create
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Alice\"," +
                                "\"skills\":[\"java\",\"sql\"]," +
                                "\"workExperience\":3" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice"));

        // list (не завязываемся на порядок)
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Alice')]").isNotEmpty());

        // get
        mockMvc.perform(get("/api/users/Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workExperience").value(3));

        // update (upsert)
        mockMvc.perform(put("/api/users/Alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Alice\"," +
                                "\"skills\":[\"java\"]," +
                                "\"workExperience\":4" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workExperience").value(4));

        // delete
        mockMvc.perform(delete("/api/users/Alice"))
                .andExpect(status().isNoContent());

        // get after delete
        mockMvc.perform(get("/api/users/Alice"))
                .andExpect(status().isNotFound());
    }
}
