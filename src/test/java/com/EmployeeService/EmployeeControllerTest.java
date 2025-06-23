package com.EmployeeService;

import com.EmployeeService.Exceptions.EmployeeNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.*;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
@TestPropertySource(properties = "ADMIN_TOKEN=secret")
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeManager employeeManager;

    @Autowired
    private ObjectMapper objectMapper;

    private final Employee sampleEmployee = new Employee("E123", "John", "Doe", "john.doe@example.com", "Developer");

    @Test
    void testGetAllEmployees() throws Exception {
        when(employeeManager.getEmployees()).thenReturn(List.of(sampleEmployee));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value("E123"));
    }

    @Test
    void testGetEmployeeById_Success() throws Exception {
        when(employeeManager.getEmployeeById("E123")).thenReturn(sampleEmployee);

        mockMvc.perform(get("/employees/E123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testGetEmployeeById_NotFound() throws Exception {
        when(employeeManager.getEmployeeById("E999")).thenThrow(new EmployeeNotFoundException("E999"));

        mockMvc.perform(get("/employees/E999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddEmployee() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/employees/E123"));
    }

    @Test
    void testUpdateEmployee() throws Exception {
        mockMvc.perform(put("/employees/E123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEmployee)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPatchEmployee() throws Exception {
        Map<String, Object> updates = Map.of("firstName", "Jane");

        mockMvc.perform(patch("/employees/E123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testBulkUpdateByTitle_Success() throws Exception {
        // Send a valid update request
        Map<String, Object> updates = Map.of("firstName", "Jane");

        when(employeeManager.bulkUpdateByTitle("Developer", updates)).thenReturn(1);

        mockMvc.perform(patch("/employees/title/Developer")
                        .header("ADMIN-TOKEN", "secret") // Valid admin token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated 1 employees with title 'Developer'"));
    }
    @Test
    void testBulkUpdateByTitle_Forbidden() throws Exception {
        Map<String, Object> updates = Map.of("firstName", "Jane");

        mockMvc.perform(patch("/employees/title/Developer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testBulkUpdateByTitle_BadRequest() throws Exception {
        Map<String, Object> updates = Map.of("employeeId", "5");

        mockMvc.perform(patch("/employees/title/Developer")
                        .header("ADMIN-TOKEN", "secret") // Valid admin token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Updating employeeId is not allowed"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/employees/E123"))
                .andExpect(status().isNoContent());
    }
}
