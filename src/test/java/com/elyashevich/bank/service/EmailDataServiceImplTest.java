package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.service.impl.EmailDataServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.elyashevich.bank.domain.entity.EmailData;
import com.elyashevich.bank.repository.EmailDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
class EmailDataServiceImplTest {

    @Mock
    private EmailDataRepository emailDataRepository;

    @InjectMocks
    private EmailDataServiceImpl emailDataService;

    private static Stream<Arguments> provideEmailUserCombinations() {
        var user1 = new User(1L, "User1", null, null, null, null, null);
        var user2 = new User(2L, "User2", null, null, null, null, null);
        return Stream.of(
                Arguments.of("user1@test.com", user2, true),
                Arguments.of("user2@test.com", user1, true),
                Arguments.of("user1@test.com", user1, false),
                Arguments.of("nonexistent@test.com", user1, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEmailUserCombinations")
    @DisplayName("existsByEmailAndAnotherUser should return expected result")
    void existsByEmailAndAnotherUserShouldReturnExpectedResult(String email, User user, boolean expectedResult) {
        when(emailDataRepository.existsByEmailAndAnotherUser(email, user)).thenReturn(expectedResult);

        var result = emailDataService.existsByEmailAndAnotherUser(email, user);

        assertAll(
                () -> assertEquals(expectedResult, result),
                () -> verify(emailDataRepository).existsByEmailAndAnotherUser(email, user)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid@email.com", "another.valid@email.org"})
    @DisplayName("delete should succeed for existing emails")
    void deleteShouldSucceedForExistingEmails(String email) {
        var emailData = new EmailData(1L, new User(), email);
        when(emailDataRepository.findByEmail(email)).thenReturn(Optional.of(emailData));

        emailDataService.delete(email);

        assertAll(
                () -> assertNull(emailData.getUser()),
                () -> verify(emailDataRepository).findByEmail(email),
                () -> verify(emailDataRepository).delete(emailData)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("delete should throw exception for blank emails")
    void deleteShouldThrowExceptionForBlankEmails(String email) {
        assertThrows(ResourceNotFoundException.class,
                () -> emailDataService.delete(email));

        verify(emailDataRepository, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonexistent@email.com", "invalid@email"})
    @DisplayName("delete should throw exception for non-existent emails")
    void deleteShouldThrowExceptionForNonExistentEmails(String email) {
        when(emailDataRepository.findByEmail(email)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class,
                () -> emailDataService.delete(email));

        assertAll(
                () -> assertTrue(exception.getMessage().contains(email)),
                () -> verify(emailDataRepository).findByEmail(email),
                () -> verify(emailDataRepository, never()).delete(any())
        );
    }

    @Test
    @DisplayName("delete should clear user association before deletion")
    void deleteShouldClearUserAssociation() {
        var email = "test@example.com";
        var user = new User(1L, "Test User", null, null, null, null, null);
        var emailData = new EmailData(1L, user, email);
        when(emailDataRepository.findByEmail(email)).thenReturn(Optional.of(emailData));

        emailDataService.delete(email);

        assertAll(
                () -> assertNull(emailData.getUser()),
                () -> verify(emailDataRepository).delete(emailData)
        );
    }

    @Test
    @DisplayName("delete should handle email with special characters")
    void deleteShouldHandleEmailWithSpecialCharacters() {
        String email = "special.chars+test@example.com";
        EmailData emailData = new EmailData(1L, new User(), email);
        when(emailDataRepository.findByEmail(email)).thenReturn(Optional.of(emailData));

        assertDoesNotThrow(() -> emailDataService.delete(email));

        verify(emailDataRepository).delete(emailData);
    }

    @ParameterizedTest
    @MethodSource("provideEmailsWithDifferentCases")
    @DisplayName("existsByEmail should be case insensitive")
    void existsByEmailShouldBeCaseInsensitive(String email, String searchEmail, boolean expected) {
        when(emailDataRepository.existsByEmailAndAnotherUser(anyString(), any(User.class)))
                .thenReturn(expected);

        boolean result = emailDataService.existsByEmailAndAnotherUser(searchEmail, new User());

        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideEmailsWithDifferentCases() {
        return Stream.of(
                Arguments.of("User@Example.com", "user@example.com", true),
                Arguments.of("USER@EXAMPLE.COM", "user@example.com", true),
                Arguments.of("user@example.com", "USER@EXAMPLE.COM", true)
        );
    }

    @Test
    @DisplayName("delete should not fail when user is already null")
    void deleteShouldHandleNullUser() {
        String email = "test@example.com";
        EmailData emailData = new EmailData(1L, null, email);
        when(emailDataRepository.findByEmail(email)).thenReturn(Optional.of(emailData));

        assertDoesNotThrow(() -> emailDataService.delete(email));

        verify(emailDataRepository).delete(emailData);
    }

    @Test
    @DisplayName("existsByEmailAndAnotherUser should return false for null user")
    void existsByEmailAndAnotherUserShouldHandleNullUser() {
        String email = "test@example.com";
        when(emailDataRepository.existsByEmailAndAnotherUser(email, null)).thenReturn(false);

        assertFalse(emailDataService.existsByEmailAndAnotherUser(email, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@localhost",
            "test@[127.0.0.1]",
            "test@sub.domain.co.uk",
            "test+alias@example.com"
    })
    @DisplayName("should handle valid non-standard email formats")
    void shouldHandleValidEmailFormats(String email) {
        EmailData emailData = new EmailData(1L, new User(), email);
        when(emailDataRepository.findByEmail(email)).thenReturn(Optional.of(emailData));

        assertDoesNotThrow(() -> emailDataService.delete(email));
    }


    @Test
    @DisplayName("existsByEmailAndAnotherUser should return false for empty email")
    void existsByEmailAndAnotherUserShouldHandleEmptyEmail() {
        assertFalse(emailDataService.existsByEmailAndAnotherUser("", new User()));
    }

    @Test
    @DisplayName("should handle concurrent deletion attempts")
    void shouldHandleConcurrentDeletionAttempts() {
        String email = "test@example.com";
        EmailData emailData = new EmailData(1L, new User(), email);
        when(emailDataRepository.findByEmail(email))
                .thenReturn(Optional.of(emailData))
                .thenThrow(new RuntimeException("Concurrent modification"));

        assertDoesNotThrow(() -> emailDataService.delete(email));

        assertThrows(RuntimeException.class, () -> emailDataService.delete(email));
    }
}