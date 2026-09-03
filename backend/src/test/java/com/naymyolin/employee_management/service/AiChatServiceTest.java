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
    void shouldPrepareEmployeeUpdateWithoutChangingData()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectUpdateDecision(
                1L,
                "Senior Backend Developer"
        );

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
                .updateEmployee(
                        any(),
                        any(Employee.class)
                );

        mockServer.verify();
    }

    @Test
    void shouldUpdateEmployeeAfterConfirmation()
            throws Exception {
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

        expectUpdateDecision(
                1L,
                "Senior Backend Developer"
        );

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

        assertTrue(result.reply().contains(
                "updated successfully"
        ));

        ArgumentCaptor<Employee> employeeCaptor =
                ArgumentCaptor.forClass(Employee.class);

        verify(employeeService).updateEmployee(
                eq(1L),
                employeeCaptor.capture()
        );

        Employee updateDetails =
                employeeCaptor.getValue();

        assertTrue(
                updateDetails.getPosition()
                        .equals("Senior Backend Developer")
        );

        assertTrue(
                updateDetails.getName()
                        .equals("Nay Myo Lin")
        );

        assertTrue(
                updateDetails.getEmail()
                        .equals("naymyolin@example.com")
        );

        mockServer.verify();
    }

    @Test
    void shouldCancelEmployeeUpdateWithoutChangingData()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectUpdateDecision(
                1L,
                "Senior Backend Developer"
        );

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

        assertTrue(result.reply().contains(
                "cancelled"
        ));

        verify(employeeService, never())
                .updateEmployee(
                        any(),
                        any(Employee.class)
                );

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
                .updateEmployee(
                        any(),
                        any(Employee.class)
                );

        verify(employeeService, never())
                .deleteEmployee(any(Long.class));
    }

    @Test
    void shouldRejectUpdateWhenEmployeeDoesNotExist()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(9999L))
                .thenThrow(
                        new EmployeeNotFoundException(9999L)
                );

        expectUpdateDecision(
                9999L,
                "Manager"
        );

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
                .updateEmployee(
                        any(),
                        any(Employee.class)
                );

        mockServer.verify();
    }

    @Test
    void shouldPrepareEmployeeDeletionWithoutChangingData()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectDeleteDecision(1L);

        ChatResponse response = aiChatService.chat(
                "Delete employee 1",
                null
        );

        assertTrue(response.confirmationRequired());
        assertNotNull(response.confirmationToken());
        assertFalse(response.dataChanged());

        assertTrue(response.reply().contains(
                "permanently delete this employee"
        ));

        assertTrue(response.reply().contains(
                "Nay Myo Lin"
        ));

        verify(employeeService, never())
                .deleteEmployee(any(Long.class));

        mockServer.verify();
    }

    @Test
    void shouldDeleteEmployeeAfterConfirmation()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectDeleteDecision(1L);

        ChatResponse preview = aiChatService.chat(
                "Delete employee 1",
                null
        );

        ChatResponse result = aiChatService.chat(
                "confirm",
                preview.confirmationToken()
        );

        assertFalse(result.confirmationRequired());
        assertNull(result.confirmationToken());
        assertTrue(result.dataChanged());

        assertTrue(result.reply().contains(
                "deleted successfully"
        ));

        assertTrue(result.reply().contains(
                "Nay Myo Lin"
        ));

        verify(employeeService)
                .deleteEmployee(1L);

        mockServer.verify();
    }

    @Test
    void shouldCancelEmployeeDeletionWithoutChangingData()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectDeleteDecision(1L);

        ChatResponse preview = aiChatService.chat(
                "Delete employee 1",
                null
        );

        ChatResponse result = aiChatService.chat(
                "cancel",
                preview.confirmationToken()
        );

        assertFalse(result.confirmationRequired());
        assertNull(result.confirmationToken());
        assertFalse(result.dataChanged());

        assertTrue(result.reply().contains(
                "cancelled"
        ));

        verify(employeeService, never())
                .deleteEmployee(any(Long.class));

        mockServer.verify();
    }

    @Test
    void shouldKeepDeletionPendingForInvalidConfirmation()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(1L))
                .thenReturn(existing);

        expectDeleteDecision(1L);

        ChatResponse preview = aiChatService.chat(
                "Delete employee 1",
                null
        );

        ChatResponse result = aiChatService.chat(
                "maybe",
                preview.confirmationToken()
        );

        assertTrue(result.confirmationRequired());
        assertNotNull(result.confirmationToken());

        assertTrue(
                result.confirmationToken().equals(
                        preview.confirmationToken()
                )
        );

        assertFalse(result.dataChanged());

        assertTrue(result.reply().contains(
                "Reply with \"confirm\""
        ));

        verify(employeeService, never())
                .deleteEmployee(any(Long.class));

        mockServer.verify();
    }

    @Test
    void shouldRejectDeletionWhenEmployeeDoesNotExist()
            throws Exception {
        Employee existing = createEmployee();

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(existing));

        when(employeeService.getEmployeeById(9999L))
                .thenThrow(
                        new EmployeeNotFoundException(9999L)
                );

        expectDeleteDecision(9999L);

        ChatResponse response = aiChatService.chat(
                "Delete employee 9999",
                null
        );

        assertFalse(response.confirmationRequired());
        assertNull(response.confirmationToken());
        assertFalse(response.dataChanged());

        assertTrue(response.reply().contains(
                "could not safely identify"
        ));

        verify(employeeService, never())
                .deleteEmployee(any(Long.class));

        mockServer.verify();
    }

    @Test
    void shouldRejectDeletionWhenMultipleEmployeesHaveSameName()
            throws Exception {
        Employee firstEmployee = createEmployee();

        Employee secondEmployee = createEmployee();
        secondEmployee.setId(2L);
        secondEmployee.setEmail(
                "another.nay@example.com"
        );

        when(employeeService.getAllEmployees())
                .thenReturn(List.of(
                        firstEmployee,
                        secondEmployee
                ));

        expectDeleteDecisionByName(
                "Nay Myo Lin"
        );

        ChatResponse response = aiChatService.chat(
                "Delete employee Nay Myo Lin",
                null
        );

        assertFalse(response.confirmationRequired());
        assertNull(response.confirmationToken());
        assertFalse(response.dataChanged());

        assertTrue(response.reply().contains(
                "Multiple employees match"
        ));

        assertTrue(response.reply().contains(
                "naymyolin@example.com"
        ));

        assertTrue(response.reply().contains(
                "another.nay@example.com"
        ));

        verify(employeeService, never())
                .deleteEmployee(any(Long.class));

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

        expectDecision(decision);
    }

    private void expectDeleteDecision(
            Long employeeId
    ) throws Exception {
        String decision = objectMapper.writeValueAsString(
                Map.of(
                        "action", "DELETE",
                        "reply", "",
                        "employeeId", employeeId
                )
        );

        expectDecision(decision);
    }

    private void expectDeleteDecisionByName(
            String targetName
    ) throws Exception {
        String decision = objectMapper.writeValueAsString(
                Map.of(
                        "action", "DELETE",
                        "reply", "",
                        "targetName", targetName
                )
        );

        expectDecision(decision);
    }

    private void expectDecision(
            String decision
    ) throws Exception {
        String responseBody =
                objectMapper.writeValueAsString(
                        Map.of(
                                "choices",
                                List.of(
                                        Map.of(
                                                "message",
                                                Map.of(
                                                        "role",
                                                        "assistant",
                                                        "content",
                                                        decision
                                                )
                                        )
                                )
                        )
                );

        mockServer.expect(
                        once(),
                        requestTo(OPENROUTER_URL)
                )
                .andExpect(
                        method(HttpMethod.POST)
                )
                .andRespond(
                        withSuccess(
                                responseBody,
                                MediaType.APPLICATION_JSON
                        )
                );
    }

    private Employee createEmployee() {
        Employee employee = new Employee();

        employee.setId(1L);
        employee.setName("Nay Myo Lin");
        employee.setEmail(
                "naymyolin@example.com"
        );
        employee.setPhone("09712345678");
        employee.setDepartment(
                "Software Development"
        );
        employee.setPosition(
                "Junior Backend Developer"
        );
        employee.setSalary(
                new BigDecimal("800000.00")
        );
        employee.setHireDate(
                LocalDate.of(2026, 9, 1)
        );

        return employee;
    }
}