package com.ly.lmsbackend;

import com.ly.lmsbackend.controller.UserController;
import com.ly.lmsbackend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserRegistrationEndpointTests {

    @Test
    @DisplayName("UserController /resend-otp succeeds with JSON body email")
    void testResendOtpSuccess() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code resent successfully"));

        verify(userService).resendVerificationOtp(eq("student@example.com"));
    }

    @Test
    @DisplayName("UserController /resend-otp rejects missing email with 400 Bad Request")
    void testResendOtpMissingEmail() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UserController /register rejects invalid email or missing fields")
    void testRegisterValidationFailure() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"not-an-email\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }
}
