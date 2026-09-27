package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.model.Payments;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class PaymentMapper {

    public static final String ABA_PAYWAY_BASE_URL =
            "https://link.payway.com.kh/aba?id=E41BD42D5BCD&shortlink=b9jvxgkj&amount={AMOUNT}&source_caller=sdk&af_referrer_uid=1730301732203-6331293&pid=af_app_invites&dynamic=true&link_action=abaqr&c=abaqr&code=927227&created_from_app=true&af_referrer_customer_id=E41BD42D5BCD&af_dp=abamobilebank%3A%2F%2F&af_siteid=968860649&userid=E41BD42D5BCD&acc=004120485";

    public PaymentResponseDTO toDTO(Payments payment) {
        String paymentUrl = null;
        if (payment.getAmount() != null && payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            paymentUrl = buildAbaPayWayUrl(payment.getAmount());
        }

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
                payment.getUpdatedAt(),
                paymentUrl,
                payment.getOriginalAmount() != null ? payment.getOriginalAmount() : payment.getAmount(),
                payment.getDiscountAmount() != null ? payment.getDiscountAmount() : BigDecimal.ZERO,
                payment.getIsReEnrollmentDiscount() != null ? payment.getIsReEnrollmentDiscount() : false
        );
    }

    public static String buildAbaPayWayUrl(BigDecimal amount) {
        BigDecimal amt = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) ? amount : BigDecimal.valueOf(0.01);
        String formattedAmount = String.format(Locale.US, "%.2f", amt);
        return ABA_PAYWAY_BASE_URL.replace("{AMOUNT}", formattedAmount);
    }
}
