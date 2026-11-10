package com.ly.lmsbackend.service;

import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import com.ly.lmsbackend.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentReconciliationScheduler(
            PaymentRepository paymentRepository,
            PaymentService paymentService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${bakong.reconciliation.interval-ms:60000}")
    public void reconcilePendingBakongPayments() {
        List<Payments> pendingPayments = paymentRepository.findByProviderAndStatusAndKhqrMd5IsNotNull(
                PaymentService.PROVIDER_BAKONG_KHQR,
                PaymentStatus.PENDING
        );

        for (Payments pending : pendingPayments) {
            try {
                paymentService.reconcilePendingPaymentById(pending.getPaymentId());
            } catch (Exception ex) {
                log.warn("Error reconciling pending Bakong payment {}: {}", pending.getPaymentId(), ex.getMessage());
            }
        }
    }
}
