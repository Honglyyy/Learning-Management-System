package com.ly.lmsbackend;

import com.ly.lmsbackend.controller.ActuatorAliasController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ActuatorEndpointTests {

    @Test
    @DisplayName("ActuatorAliasController forwards / to /actuator/health")
    void testRootForwarding() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ActuatorAliasController()).build();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/actuator/health"));
    }

    @Test
    @DisplayName("ActuatorAliasController forwards /health directly to /actuator/health")
    void testDirectHealthForwarding() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ActuatorAliasController()).build();

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/actuator/health"));
    }

    @Test
    @DisplayName("ActuatorAliasController forwards /acuator/health to /actuator/health")
    void testAcuatorHealthForwarding() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ActuatorAliasController()).build();

        mockMvc.perform(get("/acuator/health"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/actuator/health"));
    }

    @Test
    @DisplayName("ActuatorAliasController forwards /acuator root to /actuator")
    void testAcuatorRootForwarding() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ActuatorAliasController()).build();

        mockMvc.perform(get("/acuator"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/actuator"));
    }
}
