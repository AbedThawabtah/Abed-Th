package com.tourism.booking.payment;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.payment.dto.PaymentIntentRequest;
import com.tourism.booking.payment.dto.PaymentIntentResponse;
import com.tourism.booking.payment.dto.RefundRequest;
import com.tourism.booking.payment.entity.PaymentIntent;
import com.tourism.booking.payment.service.PaymentService;
import com.tourism.booking.payment.service.PaymentServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;

@Tag(name = "Payment", description = "Mock payment intents and simulation")
@RestController
@RequestMapping("/api/payment")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceInterface paymentService;

    @Operation(summary = "Create payment intent")
    @PostMapping("/intents")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            UriComponentsBuilder uriBuilder) {

        PaymentIntentResponse response = paymentService.createIntent(request);

        URI location = uriBuilder
                .path("/api/payment/intents/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Simulate payment success")
    @PostMapping("/intents/{id}/simulate-success")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<PaymentIntentResponse> simulateSuccess(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.simulateSuccess(id));
    }

    @Operation(summary = "Simulate payment failure")
    @PostMapping("/intents/{id}/simulate-failure")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<PaymentIntentResponse> simulateFailure(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.simulateFailure(id));
    }

    @Operation(summary = "Refund payment (mock)")
    @PostMapping("/refunds")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PaymentIntentResponse> refund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(request.getPaymentIntentId()));
    }

    // -----------------------------------
    // Browse Payments (Manager)
    // -----------------------------------

    @Operation(summary = "Browse payments with filters and pagination")
    @GetMapping("/intents")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PagedResponse<PaymentIntentResponse>> browsePayments(

            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) PaymentIntent.PaymentStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,

            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                paymentService.browsePayments(
                        bookingId,
                        status,
                        minAmount,
                        maxAmount,
                        fromDate,
                        toDate,
                        pageable
                )
        );
    }
}