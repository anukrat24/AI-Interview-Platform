package com.aiprep.interview.controller;

import com.aiprep.interview.dto.CreateOrderResponseDTO;
import com.aiprep.interview.dto.VerifyPaymentDTO;
import com.aiprep.interview.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponseDTO> createOrder(Authentication authentication) {
        return ResponseEntity.ok(paymentService.createPremiumOrder(authentication.getName()));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyPaymentDTO request, Authentication authentication) {
        paymentService.verifyAndActivatePremium(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }
}
