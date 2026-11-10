package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "original_amount")
    private BigDecimal originalAmount;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "is_re_enrollment_discount")
    private Boolean isReEnrollmentDiscount = false;

    @Column(nullable = false)
    private String provider;

    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "khqr_payload", columnDefinition = "TEXT")
    private String khqrPayload;

    @Column(name = "khqr_md5", length = 64)
    private String khqrMd5;

    @Column(name = "bakong_deep_link", columnDefinition = "TEXT")
    private String bakongDeepLink;

    @Column(length = 10)
    private String currency = "USD";

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @Column(name = "paid_at")
    private Timestamp paidAt;

    @Column(name = "bakong_transaction_hash", length = 128)
    private String bakongTransactionHash;

    @Column(name = "payer_account_id", length = 128)
    private String payerAccountId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}
