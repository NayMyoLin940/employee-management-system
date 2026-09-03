package com.naymyolin.employee_management.service;

import com.naymyolin.employee_management.dto.ChatResponse;
import com.naymyolin.employee_management.exception.EmployeeNotFoundException;
import com.naymyolin.employee_management.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiChatServiceTest {

    private static final String OPENROUTER_URL =
            "http://localhost/chat/completions";

    private EmployeeService employeeService;
    private ObjectMapper objectMapper;
    private MockRestServiceServer mockServer;
    private AiChatService aiChatService;

    @BeforeEach
    void setUp() {
        employeeService = mock(EmployeeService.class);
        objectMapper = new ObjectMapper();

        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://localhost");

        mockServer = MockRestServiceServer
                .bindTo(restClientBuilder)
                .build();

        aiChatService = new AiChatService(
                restClientBuilder.build(),
                "test-model",
                employeeService,
                objectMapper
        );
    }

    @Test
    void shouldPrepareEmployeeUpdateWithoutChangingData() throws Exception {
        Employee existing = createEmployee();
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));
        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectUpdateDecision(1L, "Senior Backend Developer");

        ChatResponse response = aiChatService.chat(
                "Update employee 1's position to Senior Backend Developer",
                null
        );

        assertTrue(response.confirmationRequired());
        assertNotNull(response.confirmationToken());
        assertFalse(response.dataChanged());
        assertTrue(response.reply().contains(
                "Junior Backend Developer -> Senior Backend Developer"
        ));

        verify(employeeService, never())
                .updateEmployee(any(), any(Employee.class));
        mockServer.verify();
    }

    @Test
    void shouldUpdateEmployeeAfterConfirmation() throws Exception {
        Employee existing = createEmployee();
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));
        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);
        when(employeeService.updateEmployee(
                eq(1L),
                any(Employee.class)
        )).thenAnswer(invocation -> {
            Employee updated = invocation.getArgument(1);
            updated.setId(1L);
            return updated;
        });

        expectUpdateDecision(1L, "Senior Backend Developer");

        ChatResponse preview = aiChatService.chat(
                "Update employee 1's position to Senior Backend Developer",
                null
        );

        ChatResponse result = aiChatService.chat(
                "confirm",
                preview.confirmationToken()
        );

        assertFalse(result.confirmationRequired());
        assertNull(result.confirmationToken());
        assertTrue(result.dataChanged());
        assertTrue(result.reply().contains("updated successfully"));

        ArgumentCaptor<Employee> employeeCaptor =
                ArgumentCaptor.forClass(Employee.class);

        verify(employeeService).updateEmployee(
                eq(1L),
                employeeCaptor.capture()
        );

        Employee updateDetails = employeeCaptor.getValue();
        assertTrue(updateDetails.getPosition()
                .equals("Senior Backend Developer"));
        assertTrue(updateDetails.getName().equals("Nay Myo Lin"));
        assertTrue(updateDetails.getEmail()
                .equals("naymyolin@example.com"));
        mockServer.verify();
    }

    @Test
    void shouldCancelEmployeeUpdateWithoutChangingData() throws Exception {
        Employee existing = createEmployee();
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));
        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectUpdateDecision(1L, "Senior Backend Developer");

        ChatResponse preview = aiChatService.chat(
                "Update employee 1's position to Senior Backend Developer",
                null
        );

        ChatResponse result = aiChatService.chat(
                "cancel",
                preview.confirmationToken()
        );

        assertFalse(result.confirmationRequired());
        assertFalse(result.dataChanged());
        assertTrue(result.reply().contains("cancelled"));

        verify(employeeService, never())
                .updateEmployee(any(), any(Employee.class));
        mockServer.verify();
    }

    @Test
    void shouldRejectInvalidConfirmationToken() {
        ChatResponse response = aiChatService.chat(
                "confirm",
                "invalid-token"
        );

        assertFalse(response.confirmationRequired());
        assertFalse(response.dataChanged());
        assertTrue(response.reply().contains(
                "does not exist or has expired"
        ));

        verify(employeeService, never())
                .updateEmployee(any(), any(Employee.class));
    }

    @Test
    void shouldRejectUpdateWhenEmployeeDoesNotExist() throws Exception {
        Employee existing = createEmployee();
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));
        when(employeeService.getEmployeeById(9999L))
                .thenThrow(new EmployeeNotFoundException(9999L));

        expectUpdateDecision(9999L, "Manager");

        ChatResponse response = aiChatService.chat(
                "Update employee 9999's position to Manager",
                null
        );

        assertFalse(response.confirmationRequired());
        assertFalse(response.dataChanged());
        assertTrue(response.reply().contains(
                "could not safely identify"
        ));

        verify(employeeService, never())
                .updateEmployee(any(), any(Employee.class));
        mockServer.verify();
    }

    private void expectUpdateDecision(
            Long employeeId,
            String position
    ) throws Exception {
        String decision = objectMapper.writeValueAsString(
                Map.of(
                        "action", "UPDATE",
                        "reply", "",
                        "employeeId", employeeId,
                        "position", position
                )
        );

        String responseBody = objectMapper.writeValueAsString(
                Map.of(
                        "choices", List.of(
                                Map.of(
                                        "message", Map.of(
                                                "role", "assistant",
                                                "content", decision
                                        )
                                )
                        )
                )
        );

        mockServer.expect(once(), requestTo(OPENROUTER_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        responseBody,
                        MediaType.APPLICATION_JSON
                ));
    }

    private Employee createEmployee() {
        Employee employee = new Employee();

        employee.setId(1L);
        employee.setName("Nay Myo Lin");
        employee.setEmail("naymyolin@example.com");
        employee.setPhone("09712345678");
        employee.setDepartment("Software Development");
        employee.setPosition("Junior Backend Developer");
        employee.setSalary(new BigDecimal("800000.00"));
        employee.setHireDate(LocalDate.of(2026, 9, 1));

        return employee;
    }
}
