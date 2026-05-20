package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.PaymentResponseDTO;
import com.ly.lmsbackend.dto.PaymentStatusUpdateDTO;
import com.ly.lmsbackend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/payments/checkout")
    public ResponseEntity<PaymentResponseDTO> createCheckout(
            @Valid @RequestBody PaymentCheckoutRequestDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                paymentService.createCheckout(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/api/payments/{id}/confirm")
    public ResponseEntity<PaymentResponseDTO> confirmPayment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(paymentService.confirmPayment(id, authentication.getName()));
    }

    @GetMapping("/api/payments/me")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getMyPayments(authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/payments/{id}/status")
    public ResponseEntity<PaymentResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PaymentStatusUpdateDTO dto
    ) {
        return ResponseEntity.ok(paymentService.updateStatus(id, dto.status(), dto.providerReference()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/payments/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
