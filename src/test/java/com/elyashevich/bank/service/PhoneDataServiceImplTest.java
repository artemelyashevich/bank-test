package com.elyashevich.bank.service;


import com.elyashevich.bank.domain.entity.PhoneData;
import com.elyashevich.bank.domain.entity.User;
import com.elyashevich.bank.exception.ResourceNotFoundException;
import com.elyashevich.bank.repository.PhoneDataRepository;
import com.elyashevich.bank.service.impl.PhoneDataServiceImpl;
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

@ExtendWith(MockitoExtension.class)
class PhoneDataServiceImplTest {

    @Mock
    private PhoneDataRepository phoneDataRepository;

    @InjectMocks
    private PhoneDataServiceImpl phoneDataService;

    static Stream<Arguments> providePhoneDataForExistsTests() {
        var user = new User(2L, "User2", null, null, null, null, null);

        return Stream.of(
                Arguments.of("79123456789", user, true),
                Arguments.of("79234567890", user, false)
        );
    }

    static Stream<Arguments> providePhoneDataForDeleteTests() {
        var user = new User(1L, "Test User", null, null, null, null, null);
        var phoneDataWithUser = new PhoneData(1L, user, "79123456789");

        return Stream.of(
                Arguments.of("79123456789", Optional.of(phoneDataWithUser), true),
                Arguments.of("79345678901", Optional.empty(), false)
        );
    }

    static Stream<Arguments> provideEdgeCasePhonesForExistsTests() {
        var user = new User(1L, "Test User", null, null, null, null, null);
        return Stream.of(
                Arguments.of("70000000000", user, true),  // All zeros
                Arguments.of("79999999999", user, true),  // All nines
                Arguments.of("7", user, false),           // Too short
                Arguments.of("791234567890", user, false) // Too long
        );
    }

    static Stream<Arguments> provideNullCasesForDeleteTests() {
        return Stream.of(
                Arguments.of(null, Optional.empty(), false),
                Arguments.of("", Optional.empty(), false)
        );
    }

    static Stream<Arguments> provideDuplicatePhonesForExistsTests() {
        var user1 = new User(1L, "User1", null, null, null, null, null);
        var user2 = new User(2L, "User2", null, null, null, null, null);
        return Stream.of(
                Arguments.of("79123456789", user1, false), // Same user
                Arguments.of("79123456789", user2, true)   // Different user
        );
    }

    @ParameterizedTest
    @MethodSource("providePhoneDataForExistsTests")
    void existsByPhoneAndAnotherUserShouldReturnExpectedResult(
            String phone, User user, boolean expectedResult) {
        when(phoneDataRepository.existsByPhoneAndAnotherUser(phone, user))
                .thenReturn(expectedResult);

        var result = phoneDataService.existsByPhoneAndAnotherUser(phone, user);

        assertAll(
                () -> assertEquals(expectedResult, result),
                () -> verify(phoneDataRepository).existsByPhoneAndAnotherUser(phone, user)
        );
    }


    @ParameterizedTest
    @MethodSource("providePhoneDataForDeleteTests")
    void deleteShouldHandleDifferentCasesCorrectly(
            String phone, Optional<PhoneData> phoneData, boolean shouldDelete) {
        when(phoneDataRepository.findByPhone(phone)).thenReturn(phoneData);

        if (phoneData.isEmpty()) {
            var exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> phoneDataService.delete(phone)
            );

            assertAll(
                    () -> assertTrue(exception.getMessage().contains(phone)),
                    () -> verify(phoneDataRepository, never()).delete(any())
            );
        } else {
            phoneDataService.delete(phone);

            assertAll(
                    () -> verify(phoneDataRepository).findByPhone(phone),
                    () -> {
                        if (shouldDelete) {
                            verify(phoneDataRepository).delete(phoneData.get());
                        } else {
                            verify(phoneDataRepository, never()).delete(any());
                        }
                    }
            );
        }
    }

    @ParameterizedTest
    @MethodSource("provideEdgeCasePhonesForExistsTests")
    void existsByPhoneShouldHandleEdgeCases(String phone, User user, boolean expected) {
        when(phoneDataRepository.existsByPhoneAndAnotherUser(phone, user))
                .thenReturn(expected);

        var result = phoneDataService.existsByPhoneAndAnotherUser(phone, user);

        assertAll(
                () -> assertEquals(expected, result),
                () -> verify(phoneDataRepository).existsByPhoneAndAnotherUser(phone, user)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNullCasesForDeleteTests")
    void deleteShouldHandleNullAndEmptyPhone(String phone, Optional<PhoneData> phoneData, boolean shouldDelete) {
        when(phoneDataRepository.findByPhone(phone)).thenReturn(phoneData);

        assertThrows(ResourceNotFoundException.class,
                () -> phoneDataService.delete(phone));

        verify(phoneDataRepository, never()).delete(any());
    }

    @ParameterizedTest
    @MethodSource("provideDuplicatePhonesForExistsTests")
    void existsByPhoneShouldHandleDuplicateCases(String phone, User user, boolean expected) {
        when(phoneDataRepository.existsByPhoneAndAnotherUser(phone, user))
                .thenReturn(expected);

        var result = phoneDataService.existsByPhoneAndAnotherUser(phone, user);

        assertAll(
                () -> assertEquals(expected, result),
                () -> verify(phoneDataRepository).existsByPhoneAndAnotherUser(phone, user)
        );
    }

    @Test
    void deleteShouldClearUserAssociation() {
        var phone = "79123456789";
        var user = new User(1L, "Test User", null, null, null, null, null);
        var phoneData = new PhoneData(1L, user, phone);

        when(phoneDataRepository.findByPhone(phone)).thenReturn(Optional.of(phoneData));

        phoneDataService.delete(phone);

        assertAll(
                () -> assertNull(phoneData.getUser()),
                () -> verify(phoneDataRepository).delete(phoneData)
        );
    }

    static Stream<Arguments> providePhoneFormatsForExistsTests() {
        var user = new User(1L, "Test User", null, null, null, null, null);
        return Stream.of(
                Arguments.of("7(912)345-67-89", user, false),  // Formatted number
                Arguments.of("+7 912 345 67 89", user, false), // International format
                Arguments.of("89123456789", user, false)       // Invalid Russian format
        );
    }

    @ParameterizedTest
    @MethodSource("providePhoneFormatsForExistsTests")
    void existsByPhoneShouldHandleDifferentFormats(String phone, User user, boolean expected) {
        when(phoneDataRepository.existsByPhoneAndAnotherUser(phone, user))
                .thenReturn(expected);

        var result = phoneDataService.existsByPhoneAndAnotherUser(phone, user);

        assertEquals(expected, result);
    }
}