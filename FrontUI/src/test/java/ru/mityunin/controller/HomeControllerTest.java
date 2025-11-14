package ru.mityunin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.UserDto;
import ru.mityunin.service.AccountsService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private AccountsService accountsService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    private UserDto testUser;
    private List<UserDto> testUsers;

    @BeforeEach
    void setUp() {
        testUser = new UserDto();
        testUser.setLogin("testuser");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        UserDto otherUser1 = new UserDto();
        otherUser1.setLogin("otheruser1");
        UserDto otherUser2 = new UserDto();
        otherUser2.setLogin("otheruser2");

        testUsers = Arrays.asList(otherUser1, otherUser2);
    }

    @Test
    void showMainPage_AuthenticatedUser_AddsAttributesToModel() {
        // Arrange
        String username = "testuser";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(accountsService.getUserByLogin(username)).thenReturn(testUser);
        when(accountsService.getAllUsersExcept(username))
                .thenReturn(new ApiResponse<>(true, "Success", testUsers));

        // Act
        String viewName = homeController.showMainPage(authentication, model);

        // Assert
        assertEquals("main", viewName);
        verify(accountsService).getUserByLogin(username);
        verify(accountsService).getAllUsersExcept(username);
        verify(model).addAttribute("userDto", testUser);
        verify(model).addAttribute("allUsers", testUsers);
    }

    @Test
    void showMainPage_NotAuthenticated_ReturnsViewWithoutAttributes() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        String viewName = homeController.showMainPage(authentication, model);

        // Assert
        assertEquals("main", viewName);
        verify(accountsService, never()).getUserByLogin(anyString());
        verify(accountsService, never()).getAllUsersExcept(anyString());
        verify(model, never()).addAttribute(eq("userDto"), any());
        verify(model, never()).addAttribute(eq("allUsers"), any());
    }

    @Test
    void showMainPage_NullAuthentication_ReturnsViewWithoutAttributes() {
        // Act
        String viewName = homeController.showMainPage(null, model);

        // Assert
        assertEquals("main", viewName);
        verify(accountsService, never()).getUserByLogin(anyString());
        verify(accountsService, never()).getAllUsersExcept(anyString());
        verify(model, never()).addAttribute(eq("userDto"), any());
        verify(model, never()).addAttribute(eq("allUsers"), any());
    }

    @Test
    void showMainPage_ServiceReturnsEmptyUserList_AddsEmptyListToModel() {
        // Arrange
        String username = "testuser";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(accountsService.getUserByLogin(username)).thenReturn(testUser);
        when(accountsService.getAllUsersExcept(username))
                .thenReturn(new ApiResponse<>(true, "Success", List.of()));

        // Act
        String viewName = homeController.showMainPage(authentication, model);

        // Assert
        assertEquals("main", viewName);
        verify(model).addAttribute("userDto", testUser);
        verify(model).addAttribute("allUsers", List.of());
    }

    @Test
    void showMainPage_ServiceReturnsNullUserList_AddsNullToModel() {
        // Arrange
        String username = "testuser";
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(accountsService.getUserByLogin(username)).thenReturn(testUser);
        when(accountsService.getAllUsersExcept(username))
                .thenReturn(new ApiResponse<>(true, "Success", null));

        // Act
        String viewName = homeController.showMainPage(authentication, model);

        // Assert
        assertEquals("main", viewName);
        verify(model).addAttribute("userDto", testUser);
        verify(model).addAttribute("allUsers", null);
    }
}