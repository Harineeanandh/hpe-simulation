package com.EmployeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestPropertySource(properties = "ADMIN_TOKEN=secret")
public class EmployeeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Value("${ADMIN_TOKEN}")
    private String adminToken;

    private String baseUrl;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port;
    }

    private String url(String path) {
        return baseUrl + path;
    }

    @Test
    void testAddAndGetEmployee() {
        Employee emp = new Employee("100", "Test", "User", "test@user.com", "Tester");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Employee> request = new HttpEntity<>(emp, headers);

        ResponseEntity<Void> postResponse = restTemplate.postForEntity(url("/employees"), request, Void.class);
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());

        ResponseEntity<Employee> getResponse = restTemplate.getForEntity(url("/employees/100"), Employee.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("Test", getResponse.getBody().getFirstName());
    }

    @Test
    void testPatchEmployee() {
        // Create employee first
        Employee emp = new Employee("101", "Patch", "Tester", "patch@test.com", "Developer");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Employee> createRequest = new HttpEntity<>(emp, headers);
        restTemplate.postForEntity(url("/employees"), createRequest, Void.class);

        Map<String, Object> patch = Map.of("title", "Lead Developer");
        HttpEntity<Map<String, Object>> patchRequest = new HttpEntity<>(patch, headers);
        ResponseEntity<Void> patchResponse = restTemplate.exchange(
                url("/employees/101"), HttpMethod.PATCH, patchRequest, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, patchResponse.getStatusCode());

        Employee updated = restTemplate.getForObject(url("/employees/101"), Employee.class);
        assertEquals("Lead Developer", updated.getTitle());
    }

    @Test
    void testDeleteEmployee() {
        // Create employee first
        Employee emp = new Employee("102", "Delete", "Tester", "delete@test.com", "Tester");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Employee> createRequest = new HttpEntity<>(emp, headers);
        restTemplate.postForEntity(url("/employees"), createRequest, Void.class);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                url("/employees/102"), HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.getForEntity(url("/employees/102"), String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testBulkUpdateByTitleAuthorized() {
        // Add multiple employees with the same title
        Employee emp1 = new Employee("201", "Bulk1", "Tester", "bulk1@test.com", "QA");
        Employee emp2 = new Employee("202", "Bulk2", "Tester", "bulk2@test.com", "QA");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url("/employees"), new HttpEntity<>(emp1, headers), Void.class);
        restTemplate.postForEntity(url("/employees"), new HttpEntity<>(emp2, headers), Void.class);

        Map<String, Object> updates = Map.of("title", "Senior QA");

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.set("ADMIN-TOKEN", adminToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updates, authHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/employees/title/QA"), HttpMethod.PATCH, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Updated"));

        // Verify employees updated
        Employee updated1 = restTemplate.getForObject(url("/employees/201"), Employee.class);
        Employee updated2 = restTemplate.getForObject(url("/employees/202"), Employee.class);
        assertEquals("Senior QA", updated1.getTitle());
        assertEquals("Senior QA", updated2.getTitle());
    }

    @Test
    void testBulkUpdateByTitleUnauthorized() {
        Map<String, Object> updates = Map.of("title", "Senior QA");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updates, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/employees/title/QA"), HttpMethod.PATCH, request, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("Unauthorized"));
    }

    @Test
    void testBulkUpdateByTitleForbiddenUpdateEmployeeId() {
        Map<String, Object> updates = Map.of("employeeId", "999");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ADMIN-TOKEN", adminToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updates, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/employees/title/QA"), HttpMethod.PATCH, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("not allowed"));
    }
}
