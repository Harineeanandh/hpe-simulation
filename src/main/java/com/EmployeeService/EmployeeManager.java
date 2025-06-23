package com.EmployeeService;

import com.EmployeeService.Exceptions.EmployeeNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeManager {

    private final EmployeeRepository employeeRepository;

    public EmployeeManager(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    public void addEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    public boolean existsById(String id) {
        return employeeRepository.existsById(id);
    }

    public void updateEmployee(Employee employee) {
        if (!employeeRepository.existsById(employee.getEmployeeId())) {
            throw new EmployeeNotFoundException(employee.getEmployeeId());
        }
        employeeRepository.save(employee);
    }

    public void patchEmployee(String id, Map<String, Object> updates) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }

        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isEmpty()) {
            throw new EmployeeNotFoundException(id);
        }

        Employee employee = optionalEmployee.get();

        updates.forEach((key, value) -> {
            switch (key) {
                case "firstName":
                    employee.setFirstName((String) value);
                    break;
                case "lastName":
                    employee.setLastName((String) value);
                    break;
                case "email":
                    employee.setEmail((String) value);
                    break;
                case "title":
                    employee.setTitle((String) value);
                    break;
                case "employeeId":
                    employee.setEmployeeId((String) value);
                    break;
            }
        });

        employeeRepository.save(employee);
    }

    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public void deleteEmployee(String id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }

    public int bulkUpdateByTitle(String title, Map<String, Object> updates) {
        List<Employee> employees = employeeRepository.findByTitle(title);
        if (employees.isEmpty()) return 0;
        for (Employee employee : employees) {
            updates.forEach((key, value) -> {
                switch (key) {
                    case "firstName" -> employee.setFirstName((String) value);
                    case "lastName" -> employee.setLastName((String) value);
                    case "email" -> employee.setEmail((String) value);
                    case "title" -> employee.setTitle((String) value);
                }
            });
        }
        employeeRepository.saveAll(employees);
        return employees.size();
    }
}
