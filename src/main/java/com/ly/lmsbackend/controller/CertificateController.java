package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.certificatedtos.CertificateResponseDTO;
import com.ly.lmsbackend.dto.certificatedtos.CertificateVerifyDTO;
import com.ly.lmsbackend.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @GetMapping("/api/certificates/me")
    public ResponseEntity<List<CertificateResponseDTO>> getMyCertificates(Authentication authentication) {
        return ResponseEntity.ok(certificateService.getMyCertificates(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @PostMapping("/api/certificates/claim/{courseId}")
    public ResponseEntity<CertificateResponseDTO> claimCertificate(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(certificateService.claimCertificate(courseId, authentication.getName()));
    }

    @GetMapping("/api/certificates/verify/{code}")
    public ResponseEntity<CertificateVerifyDTO> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }

    @GetMapping(value = "/api/certificates/{code}/view", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewCertificate(@PathVariable String code) {
        String html = certificateService.renderCertificateHtml(code);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(html);
    }

    @GetMapping(value = "/api/certificates/view/{code}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewCertificateAlias(@PathVariable String code) {
        return viewCertificate(code);
    }
}
