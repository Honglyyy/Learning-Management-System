package com.ly.lmsbackend.dto.paymentdtos;

import java.math.BigDecimal;

public record CheckoutQuoteDTO(
        Long courseId,
        String courseTitle,
        BigDecimal originalPrice,
        BigDecimal finalPrice,
        BigDecimal discountAmount,
        Double discountPercentage,
        Boolean isReEnrollmentDiscount,
        Integer accessDurationDays,
        Boolean isExpired
) {
}
