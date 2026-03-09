package com.banking.core.service;

import com.banking.core.domain.Account;
import com.banking.core.domain.AccountStatus;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Role;
import com.banking.core.domain.User;
import com.banking.core.domain.UserStatus;
import com.banking.core.dto.UserAdminResponse;
import com.banking.core.exception.UserNotFoundException;
import com.banking.core.repository.AccountRepository;
import com.banking.core.repository.UserRepository;
import com.banking.core.repository.UserAuditLogRepository;
import com.banking.core.dto.UserUpdateRequest;
import com.banking.core.dto.UserUpdateResponse;
import com.banking.core.domain.UserAuditLog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserAuditLogRepository userAuditLogRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .passwordHash("hashedpw")
                .status(UserStatus.ACTIVE)
                .transactionLimit(new BigDecimal("1000.00"))
                .role(Role.ROLE_USER)
                .accounts(new ArrayList<>())
                .build();

        // Add an account with balance
        Account account = Account.builder()
                .id(1L)
                .accountNumber("100000000001")
                .balance(new BigDecimal("5000.0000"))
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .user(testUser)
                .build();
        testUser.getAccounts().add(account);

        testAdmin = User.builder()
                .id(2L)
                .username("adminuser")
                .email("admin@example.com")
                .fullName("Admin User")
                .passwordHash("hashedpw")
                .status(UserStatus.ACTIVE)
                .transactionLimit(new BigDecimal("50000.00"))
                .role(Role.ROLE_ADMIN)
                .accounts(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("Task 1: DTO Mapping via getUserById")
    class DtoMappingTests {

        @Test
        @DisplayName("Subtask 1.1/1.2: getUserById maps all required DTO fields correctly")
        void getUserById_shouldMapAllFields() {
            when(userRepository.findByIdWithAccounts(1L)).thenReturn(Optional.of(testUser));
            UserAdminResponse response = userService.getUserById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("5000.0000"));
            assertThat(response.getTransactionLimit()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getRole()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("Subtask 1.3: User with no accounts gets zero balance")
        void getUserById_noAccounts_shouldReturnZeroBalance() {
            when(userRepository.findByIdWithAccounts(2L)).thenReturn(Optional.of(testAdmin));

            UserAdminResponse response = userService.getUserById(2L);

            assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Subtask 1.3: User with multiple accounts gets aggregated balance")
        void getAllUsers_multipleAccounts_shouldSumBalances() {
            Account acct1 = Account.builder().balance(new BigDecimal("1000.0000")).build();
            Account acct2 = Account.builder().balance(new BigDecimal("2500.0000")).build();
            testAdmin.setAccounts(List.of(acct1, acct2));

            when(userRepository.findAllWithAccounts()).thenReturn(List.of(testAdmin));

            List<UserAdminResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBalance()).isEqualByComparingTo(new BigDecimal("3500.0000"));
        }
    }

    @Nested
    @DisplayName("Task 2: Service Layer Methods")
    class ServiceLayerTests {

        @Test
        @DisplayName("Subtask 2.2: getAllUsers returns list of UserAdminResponse DTOs")
        void getAllUsers_shouldReturnMappedDtos() {
            when(userRepository.findAllWithAccounts()).thenReturn(List.of(testUser, testAdmin));

            List<UserAdminResponse> result = userService.getAllUsers();
            System.out.println(result);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUsername()).isEqualTo("testuser");
            assertThat(result.get(1).getUsername()).isEqualTo("adminuser");
        }

        @Test
        @DisplayName("Subtask 2.2: getAllUsers returns empty list when no users exist")
        void getAllUsers_noUsers_shouldReturnEmptyList() {
            when(userRepository.findAllWithAccounts()).thenReturn(List.of());

            List<UserAdminResponse> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Subtask 2.3: getUserById returns UserAdminResponse for existing user")
        void getUserById_existingUser_shouldReturnDto() {
            when(userRepository.findByIdWithAccounts(1L)).thenReturn(Optional.of(testUser));

            UserAdminResponse result = userService.getUserById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("5000.0000"));
        }

        @Test
        @DisplayName("Subtask 2.3: getUserById throws UserNotFoundException for missing user")
        void getUserById_nonExistentUser_shouldThrowException() {
            when(userRepository.findByIdWithAccounts(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Task 3: Admin User Updates (US1 & US2)")
    class UpdateUserTests {

        @BeforeEach
        void setupSecurityContext() {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("adminuser");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);
        }

        @Test
        @DisplayName("US1: Should update user status to SUSPENDED and create audit log")
        void updateUser_shouldUpdateStatusAndLog() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserUpdateRequest request = UserUpdateRequest.builder().status(UserStatus.SUSPENDED).build();
            UserUpdateResponse response = userService.updateUser(1L, request);

            assertThat(response.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            verify(userAuditLogRepository).save(any(UserAuditLog.class));
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("US2: Should update kycVerified flag and log")
        void updateUser_shouldUpdateKycAndLog() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            testUser.setKycVerified(false);

            UserUpdateRequest request = UserUpdateRequest.builder().kycVerified(true).build();
            UserUpdateResponse response = userService.updateUser(1L, request);

            assertThat(response.getKycVerified()).isTrue();
            assertThat(testUser.getKycVerified()).isTrue();
            verify(userAuditLogRepository).save(any(UserAuditLog.class));
        }

        @Test
        @DisplayName("US2: Should update transaction limits and log")
        void updateUser_shouldUpdateLimitsAndLog() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserUpdateRequest request = UserUpdateRequest.builder()
                    .transactionLimit(new BigDecimal("2500.00")).build();
            UserUpdateResponse response = userService.updateUser(1L, request);

            assertThat(response.getTransactionLimit()).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(testUser.getTransactionLimit()).isEqualByComparingTo(new BigDecimal("2500.00"));
            verify(userAuditLogRepository).save(any(UserAuditLog.class));
        }

        @Test
        @DisplayName("US2: Should reject 0 or negative transaction limits")
        void updateUser_invalidLimit_shouldThrowException() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserUpdateRequest request = UserUpdateRequest.builder()
                    .transactionLimit(new BigDecimal("-50.00")).build();

            assertThatThrownBy(() -> userService.updateUser(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Transaction limit must be strictly > 0");
        }
    }
}
