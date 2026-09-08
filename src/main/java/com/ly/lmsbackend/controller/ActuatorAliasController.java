package com.ly.lmsbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller providing alias compatibility for requests sent to "/acuator/**"
 * (common misspelling of "/actuator/**") by forwarding to the standard Actuator endpoint.
 */
@Controller
public class ActuatorAliasController {

    @RequestMapping(value = "/health")
    public String forwardHealth() {
        return "forward:/actuator/health";
    }

    @RequestMapping(value = {"/acuator", "/acuator/**"})
    public String forwardToActuator(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        String target = uri.replaceFirst("^/acuator", "/actuator");
        return "forward:" + target;
    }
}
