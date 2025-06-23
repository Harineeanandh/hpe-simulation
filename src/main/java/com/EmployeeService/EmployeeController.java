package com.EmployeeService;

import com.EmployeeService.Exceptions.EmployeeNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class EmployeeController {

    @Value("${ADMIN_TOKEN}")
    private String adminToken;
    private final EmployeeManager employeeManager;

    public EmployeeController(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    @GetMapping("/employees")
    public List<Employee> getEmployees() {
        return employeeManager.getEmployees();
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String id) {
        Employee employee = employeeManager.getEmployeeById(id);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Void> updateEmployee(@PathVariable("id") String id, @RequestBody Employee employee) {
        employee.setEmployeeId(id);
        employeeManager.updateEmployee(employee);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/employees")
    public ResponseEntity<Void> addEmployee(@RequestBody Employee employee) {
        employeeManager.addEmployee(employee);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/employees/" + employee.getEmployeeId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PatchMapping("/employees/{id}")
    public ResponseEntity<Void> patchEmployee(@PathVariable("id") String id, @RequestBody Map<String, Object> updates) {
        employeeManager.patchEmployee(id, updates);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/employees/title/{title}")
    public ResponseEntity<String> bulkUpdateByTitle(
            @PathVariable("title") String title,
            @RequestHeader(value = "ADMIN-TOKEN", required = false) String adminTokenHeader,
            @RequestBody Map<String, Object> updates) {

        if (adminTokenHeader == null || !adminTokenHeader.equals(adminToken)) {
            return new ResponseEntity<>("Unauthorized: Missing or invalid admin token", HttpStatus.FORBIDDEN);
        }

        if (updates.containsKey("employeeId")) {
            return new ResponseEntity<>("Updating employeeId is not allowed", HttpStatus.BAD_REQUEST);
        }

        int updatedCount = employeeManager.bulkUpdateByTitle(title, updates);
        return ResponseEntity.ok("Updated " + updatedCount + " employees with title '" + title + "'");
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") String id) {
        employeeManager.deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
