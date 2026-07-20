package com.aiprep.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponseDTO {
    private String orderId;
    private long amountPaise;
    private String currency;
    private String razorpayKeyId; // public key, safe to send to frontend
}
