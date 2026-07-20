package com.aiprep.interview.service;

import com.aiprep.interview.dto.CreateOrderResponseDTO;
import com.aiprep.interview.dto.VerifyPaymentDTO;

public interface PaymentService {
    CreateOrderResponseDTO createPremiumOrder(String userEmail);
    void verifyAndActivatePremium(String userEmail, VerifyPaymentDTO request);
}
