package com.elyashevich.bank.service;

import com.elyashevich.bank.domain.entity.*;
import com.elyashevich.bank.exception.*;
import com.elyashevich.bank.repository.UserRepository;
import com.elyashevich.bank.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PhoneDataService phoneDataService;

    @Mock
    private EmailDataService emailDataService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account(1L,  null, BigDecimal.valueOf(1000));
        
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("encodedPassword")
                .emails(new ArrayList<>(List.of(
                        new EmailData(1L, null, "test@example.com")
                )))
                .phones(new ArrayList<>(List.of(
                        new PhoneData(1L, null, "79123456789")
                )))
                .account(testAccount)
                .build();
        
        testAccount.setUser(testUser);
        testUser.getEmails().forEach(email -> email.setUser(testUser));
        testUser.getPhones().forEach(phone -> phone.setUser(testUser));
    }

    static Stream<Arguments> provideUsersForFindTests() {
        return Stream.of(
            Arguments.of(1L, true),
            Arguments.of(2L, false)
        );
    }

    static Stream<Arguments> provideEmailsForFindTests() {
        return Stream.of(
            Arguments.of("test@example.com", true),
            Arguments.of("nonexistent@example.com", false)
        );
    }

    @Test
    void findByIdShouldReturnUserWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        var result = userService.findById(1L);

        assertEquals(testUser, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(2L));
        verify(userRepository).findById(2L);
    }

    @Test
    void findByEmailShouldReturnUserWhenExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var result = userService.findByEmail("test@example.com");

        assertEquals(testUser, result);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmailShouldThrowWhenNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> userService.findByEmail("nonexistent@example.com"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void createShouldSaveUserWithProperAssociations() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        var result = userService.create(testUser);

        assertAll(
            () -> assertEquals(testUser, result),
            () -> verify(userRepository).save(testUser),
            () -> assertTrue(testUser.getEmails().stream().allMatch(e -> e.getUser() == testUser)),
            () -> assertTrue(testUser.getPhones().stream().allMatch(p -> p.getUser() == testUser)),
            () -> assertEquals(testUser, testUser.getAccount().getUser())
        );
    }

    @Test
    void findAllShouldReturnAllUsers() {
        var users = List.of(testUser, testUser);
        when(userRepository.findAll()).thenReturn(users);

        var result = userService.findAll();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void updateShouldValidateAndUpdateContacts() {
        var candidate = User.builder()
                .emails(List.of(new EmailData(null, null, "new@example.com")))
                .phones(List.of(new PhoneData(null, null, "79234567890")))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emailDataService.existsByEmailAndAnotherUser(anyString(), any())).thenReturn(false);
        when(phoneDataService.existsByPhoneAndAnotherUser(anyString(), any())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(testUser);

        var result = userService.update(1L, candidate);

        assertAll(
            () -> assertEquals(testUser, result),
            () -> assertTrue(testUser.getEmails().stream().anyMatch(e -> e.getEmail().equals("new@example.com"))),
            () -> assertTrue(testUser.getPhones().stream().anyMatch(p -> p.getPhone().equals("79234567890"))),
            () -> verify(userRepository).save(testUser)
        );
    }

    @Test
    void updateShouldThrowWhenEmailExists() {
        var candidate = User.builder()
                .emails(List.of(new EmailData(null, null, "existing@example.com")))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emailDataService.existsByEmailAndAnotherUser("existing@example.com", testUser)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.update(1L, candidate));
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetails() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertAll(
            () -> assertEquals(testUser.getId().toString(), userDetails.getUsername()),
            () -> assertEquals(testUser.getPassword(), userDetails.getPassword())
        );
    }

    @Test
    void loadUserByUsernameShouldThrowWhenNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> userService.loadUserByUsername("nonexistent@example.com"));
    }
}