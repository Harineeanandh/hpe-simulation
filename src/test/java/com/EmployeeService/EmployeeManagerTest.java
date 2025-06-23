package com.EmployeeService;

import com.EmployeeService.Exceptions.EmployeeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeManagerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeManager employeeManager;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleEmployee = new Employee("E123", "John", "Doe", "john.doe@example.com", "Developer");
    }

    @Test
    void testGetEmployees() {
        List<Employee> employees = List.of(sampleEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeManager.getEmployees();
        assertEquals(1, result.size());
        assertEquals("E123", result.get(0).getEmployeeId());
    }

    @Test
    void testAddEmployee() {
        employeeManager.addEmployee(sampleEmployee);
        verify(employeeRepository, times(1)).save(sampleEmployee);
    }

    @Test
    void testUpdateEmployee_Success() {
        when(employeeRepository.existsById("E123")).thenReturn(true);

        employeeManager.updateEmployee(sampleEmployee);
        verify(employeeRepository).save(sampleEmployee);
    }

    @Test
    void testUpdateEmployee_NotFound() {
        when(employeeRepository.existsById("E123")).thenReturn(false);

        assertThrows(EmployeeNotFoundException.class, () -> employeeManager.updateEmployee(sampleEmployee));
    }

    @Test
    void testPatchEmployee_Success() {
        when(employeeRepository.existsById("E123")).thenReturn(true);
        when(employeeRepository.findById("E123")).thenReturn(Optional.of(sampleEmployee));

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "Jane");

        employeeManager.patchEmployee("E123", updates);

        assertEquals("Jane", sampleEmployee.getFirstName());
        verify(employeeRepository).save(sampleEmployee);
    }

    @Test
    void testPatchEmployee_NotFoundById() {
        when(employeeRepository.existsById("E123")).thenReturn(false);

        assertThrows(EmployeeNotFoundException.class, () ->
                employeeManager.patchEmployee("E123", Map.of("firstName", "Jane")));
    }

    @Test
    void testPatchEmployee_EmptyOptional() {
        when(employeeRepository.existsById("E123")).thenReturn(true);
        when(employeeRepository.findById("E123")).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () ->
                employeeManager.patchEmployee("E123", Map.of("firstName", "Jane")));
    }

    @Test
    void testGetEmployeeById_Success() {
        when(employeeRepository.findById("E123")).thenReturn(Optional.of(sampleEmployee));

        Employee result = employeeManager.getEmployeeById("E123");

        assertEquals("John", result.getFirstName());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById("E123")).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeManager.getEmployeeById("E123"));
    }

    @Test
    void testDeleteEmployee_Success() {
        when(employeeRepository.existsById("E123")).thenReturn(true);

        employeeManager.deleteEmployee("E123");

        verify(employeeRepository).deleteById("E123");
    }

    @Test
    void testDeleteEmployee_NotFound() {
        when(employeeRepository.existsById("E123")).thenReturn(false);

        assertThrows(EmployeeNotFoundException.class, () -> employeeManager.deleteEmployee("E123"));
    }

    @Test
    void testBulkUpdateByTitle() {
        List<Employee> employees = List.of(sampleEmployee);
        Map<String, Object> updates = Map.of("firstName", "Jane");

        when(employeeRepository.findByTitle("Developer")).thenReturn(employees);

        int updated = employeeManager.bulkUpdateByTitle("Developer", updates);

        assertEquals(1, updated);
        assertEquals("Jane", sampleEmployee.getFirstName());
        verify(employeeRepository).saveAll(employees);
    }

    @Test
    void testBulkUpdateByTitle_EmptyList() {
        when(employeeRepository.findByTitle("Developer")).thenReturn(Collections.emptyList());

        int updated = employeeManager.bulkUpdateByTitle("Developer", Map.of("firstName", "Jane"));

        assertEquals(0, updated);
        verify(employeeRepository, never()).saveAll(any());
    }
}
