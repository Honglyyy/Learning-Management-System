package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.model.Payments;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponseDTO toDTO(Payments payment) {
        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getUser().getId(),
                payment.getUser().getUsername(),
                payment.getUser().getEmail(),
                payment.getCourse().getCourseId(),
                payment.getCourse().getTitle(),
                payment.getAmount(),
                payment.getProvider(),
                payment.getProviderReference(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
