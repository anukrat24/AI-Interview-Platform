package com.aiprep.interview.service;

import com.aiprep.interview.dto.CreateOrderResponseDTO;
import com.aiprep.interview.dto.VerifyPaymentDTO;
import com.aiprep.interview.entity.Payment;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.PaymentRepository;
import com.aiprep.interview.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @Value("${razorpay.premium-plan-amount-paise:49900}")
    private long premiumAmountPaise;

    @Override
    @Transactional
    public CreateOrderResponseDTO createPremiumOrder(String userEmail) {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new IllegalStateException(
                    "Payments are not configured on this server yet. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }

        User user = userService.getUserByEmail(userEmail);

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", premiumAmountPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "premium_" + user.getId() + "_" + System.currentTimeMillis());

            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id");

            Payment payment = new Payment();
            payment.setUser(user);
            payment.setRazorpayOrderId(orderId);
            payment.setAmountPaise(premiumAmountPaise);
            payment.setStatus("CREATED");
            paymentRepository.save(payment);

            return new CreateOrderResponseDTO(orderId, premiumAmountPaise, "INR", keyId);
        } catch (RazorpayException e) {
            throw new IllegalStateException("Could not create payment order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void verifyAndActivatePremium(String userEmail, VerifyPaymentDTO request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown order"));

        User user = userService.getUserByEmail(userEmail);
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("This order does not belong to you");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean valid = Utils.verifyPaymentSignature(options, keySecret);
            if (!valid) {
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
                throw new IllegalArgumentException("Payment signature verification failed");
            }
        } catch (RazorpayException e) {
            throw new IllegalStateException("Could not verify payment: " + e.getMessage(), e);
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus("PAID");
        paymentRepository.save(payment);

        user.setSubscriptionTier(User.SubscriptionTier.PREMIUM);
        user.setSubscriptionExpiry(LocalDateTime.now().plusDays(30));
        userRepository.save(user);

        emailService.sendSubscriptionConfirmationEmail(user.getEmail());
    }
}
