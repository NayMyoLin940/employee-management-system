package com.naymyolin.employee_management.controller;

import com.naymyolin.employee_management.exception.DuplicateEmailException;
import com.naymyolin.employee_management.exception.EmployeeNotFoundException;
import com.naymyolin.employee_management.model.Employee;
import com.naymyolin.employee_management.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    // 1. 创建员工
    @Test
    void shouldCreateEmployee() throws Exception {
        Employee requestEmployee = createEmployee(null);

        when(employeeService.createEmployee(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee employee = invocation.getArgument(0);
                    employee.setId(1L);
                    return employee;
                });

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nay Myo Lin"))
                .andExpect(jsonPath("$.email")
                        .value("naymyolin@example.com"))
                .andExpect(jsonPath("$.department").value("IT"))
                .andExpect(jsonPath("$.position")
                        .value("Software Developer"));

        verify(employeeService)
                .createEmployee(any(Employee.class));
    }

    // 2. 查询所有员工
    @Test
    void shouldGetAllEmployees() throws Exception {
        Employee firstEmployee = createEmployee(1L);

        Employee secondEmployee = createEmployee(2L);
        secondEmployee.setName("Aung Aung");
        secondEmployee.setEmail("aungaung@example.com");

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(firstEmployee, secondEmployee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Nay Myo Lin"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Aung Aung"));

        verify(employeeService).getAllEmployees();
    }

    // 3. 根据 ID 查询员工
    @Test
    void shouldGetEmployeeById() throws Exception {
        Employee employee = createEmployee(1L);

        when(employeeService.getEmployeeById(1L))
                .thenReturn(employee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nay Myo Lin"))
                .andExpect(jsonPath("$.email")
                        .value("naymyolin@example.com"));

        verify(employeeService).getEmployeeById(1L);
    }

    // 4. 修改员工
    @Test
    void shouldUpdateEmployee() throws Exception {
        Employee updateRequest = createEmployee(null);
        updateRequest.setName("Nay Myo Lin Updated");
        updateRequest.setPosition("Senior Software Developer");
        updateRequest.setSalary(new BigDecimal("2000000"));

        Employee updatedEmployee = createEmployee(1L);
        updatedEmployee.setName("Nay Myo Lin Updated");
        updatedEmployee.setPosition("Senior Software Developer");
        updatedEmployee.setSalary(new BigDecimal("2000000"));

        when(employeeService.updateEmployee(
                eq(1L),
                any(Employee.class)
        )).thenReturn(updatedEmployee);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Nay Myo Lin Updated"))
                .andExpect(jsonPath("$.position")
                        .value("Senior Software Developer"))
                .andExpect(jsonPath("$.salary").value(2000000));

        verify(employeeService)
                .updateEmployee(eq(1L), any(Employee.class));
    }

    // 5. 删除员工
    @Test
    void shouldDeleteEmployee() throws Exception {
        doNothing()
                .when(employeeService)
                .deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(1L);
    }

    // 6. 查询不存在的员工时返回 404
    @Test
    void shouldReturn404WhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new EmployeeNotFoundException(999L));

        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // 7. 邮箱重复时返回 409
    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        Employee employee = createEmployee(null);

        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(
                        new DuplicateEmailException(
                                "naymyolin@example.com"
                        )
                );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // 8. 姓名为空时返回 400
    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        Employee employee = createEmployee(null);
        employee.setName("");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // 9. 邮箱格式错误时返回 400
    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        Employee employee = createEmployee(null);
        employee.setEmail("invalid-email");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private Employee createEmployee(Long id) {
        Employee employee = new Employee();

        employee.setId(id);
        employee.setName("Nay Myo Lin");
        employee.setEmail("naymyolin@example.com");
        employee.setPhone("09123456789");
        employee.setDepartment("IT");
        employee.setPosition("Software Developer");
        employee.setSalary(new BigDecimal("1500000"));
        employee.setHireDate(LocalDate.of(2026, 9, 1));

        return employee;
    }
}