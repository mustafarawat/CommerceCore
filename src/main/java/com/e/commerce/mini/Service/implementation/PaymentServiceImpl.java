package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.RazorpayVerifyRequestDTO;
import com.e.commerce.mini.DTO.response.PaymentResponseDTO;
import com.e.commerce.mini.DTO.response.RazorpayCheckoutResponseDTO;
import com.e.commerce.mini.Enums.OrderStatus;
import com.e.commerce.mini.Enums.PaymentMethod;
import com.e.commerce.mini.Enums.PaymentStatus;
import com.e.commerce.mini.Exception.OrderNotFoundException;
import com.e.commerce.mini.Exception.PaymentException;
import com.e.commerce.mini.Exception.userNotFoundException;
import com.e.commerce.mini.Mapper.PaymentMapper;
import com.e.commerce.mini.Repository.CartItemRepository;
import com.e.commerce.mini.Repository.CartRepository;
import com.e.commerce.mini.Repository.OrderRepository;
import com.e.commerce.mini.Repository.PaymentRepository;
import com.e.commerce.mini.Repository.ProductRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.PaymentService;
import com.e.commerce.mini.models.Cart;
import com.e.commerce.mini.models.CartItem;
import com.e.commerce.mini.models.Order;
import com.e.commerce.mini.models.OrderItem;
import com.e.commerce.mini.models.Payment;
import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.User;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private final RazorpayClient razorpayClient;

    private final PaymentMapper paymentMapper;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(
            String username,
            Long orderId
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found"
                        )
                );

        Order order = orderRepository
                .findByIdAndUser(orderId, user)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: " + orderId
                        )
                );

        if (order.getStatus() != OrderStatus.PENDING) {

            throw new PaymentException(
                    "Payment cannot be created for order with status: "
                            + order.getStatus()
            );
        }

        Payment existingPayment = paymentRepository
                .findByOrder(order)
                .orElse(null);

        if (existingPayment != null) {

            if (existingPayment.getPaymentMethod() != PaymentMethod.ONLINE) {

                throw new PaymentException(
                        "Online payment cannot be created for this order"
                );
            }

            if (existingPayment.getPaymentStatus() == PaymentStatus.SUCCESS) {

                throw new PaymentException(
                        "Payment is already successful for this order"
                );
            }

            if (
                    existingPayment.getPaymentStatus() == PaymentStatus.PENDING
                            && existingPayment.getRazorpayOrderId() != null
                            && !existingPayment.getRazorpayOrderId().isBlank()
            ) {

                return paymentMapper.toResponseDTO(
                        existingPayment
                );
            }

            if (existingPayment.getPaymentStatus() == PaymentStatus.FAILED) {
                existingPayment.setPaymentStatus(PaymentStatus.PENDING);
            }

            Payment savedPayment = paymentRepository.save(existingPayment);

            if (
                    savedPayment.getRazorpayOrderId() != null
                            && !savedPayment.getRazorpayOrderId().isBlank()
            ) {

                return paymentMapper.toResponseDTO(
                        savedPayment
                );
            }
        }

        BigDecimal amount = order.getTotalAmount();

        if (
                amount == null
                        || amount.compareTo(BigDecimal.ZERO) <= 0
        ) {

            throw new PaymentException(
                    "Invalid order amount"
            );
        }

        validateCurrentStockForPayment(order);

        long amountInPaise;

        try {

            amountInPaise = amount
                    .setScale(2, RoundingMode.HALF_UP)
                    .movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException exception) {

            throw new PaymentException(
                    "Order amount is too large or has invalid precision"
            );
        }

        JSONObject razorpayOrderRequest =
                new JSONObject();

        razorpayOrderRequest.put(
                "amount",
                amountInPaise
        );

        razorpayOrderRequest.put(
                "currency",
                "INR"
        );

        razorpayOrderRequest.put(
                "receipt",
                order.getOrderNumber()
        );

        try {

            com.razorpay.Order razorpayOrder =
                    razorpayClient.orders.create(
                            razorpayOrderRequest
                    );

            String razorpayOrderId =
                    razorpayOrder.get("id");

            if (
                    razorpayOrderId == null
                            || razorpayOrderId.isBlank()
            ) {

                throw new PaymentException(
                        "Razorpay did not return an order ID"
                );
            }

            Payment payment = existingPayment;

            if (payment == null) {
                payment = new Payment();
            }

            payment.setOrder(order);
            payment.setPaymentMethod(PaymentMethod.ONLINE);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
            payment.setRazorpayOrderId(razorpayOrderId);

            Payment savedPayment =
                    paymentRepository.save(payment);

            return paymentMapper.toResponseDTO(
                    savedPayment
            );

        } catch (RazorpayException exception) {

            throw new PaymentException(
                    "Unable to create Razorpay order"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByOrder(
            String username,
            Long orderId
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found"
                        )
                );

        Order order = orderRepository
                .findByIdAndUser(orderId, user)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: " + orderId
                        )
                );

        Payment payment = paymentRepository
                .findByOrder(order)
                .orElseThrow(() ->
                        new PaymentException(
                                "Payment not found for order: " + orderId
                        )
                );

        return paymentMapper.toResponseDTO(payment);
    }

    @Override
    @Transactional
    public PaymentResponseDTO refundPayment(
            String username,
            Long orderId
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found"
                        )
                );

        Order order = orderRepository
                .findByIdAndUser(
                        orderId,
                        user
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: " + orderId
                        )
                );

        Payment payment = paymentRepository
                .findByOrder(order)
                .orElseThrow(() ->
                        new PaymentException(
                                "Payment not found for order: " + orderId
                        )
                );

        if (payment.getPaymentMethod() != PaymentMethod.ONLINE) {

            throw new PaymentException(
                    "Refund is available only for online payments"
            );
        }

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {

            return paymentMapper.toResponseDTO(
                    payment
            );
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {

            throw new PaymentException(
                    "Only successful online payments can be refunded"
            );
        }

        if (
                payment.getRazorpayPaymentId() == null
                        || payment.getRazorpayPaymentId().isBlank()
        ) {

            throw new PaymentException(
                    "Razorpay payment ID is missing for this payment"
            );
        }

        BigDecimal amount = payment.getAmount();

        if (
                amount == null
                        || amount.compareTo(BigDecimal.ZERO) <= 0
        ) {

            throw new PaymentException(
                    "Invalid refund amount"
            );
        }

        long amountInPaise;

        try {

            amountInPaise = amount
                    .setScale(2, RoundingMode.HALF_UP)
                    .movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException exception) {

            throw new PaymentException(
                    "Refund amount is too large or has invalid precision"
            );
        }

        JSONObject refundRequest =
                new JSONObject();

        refundRequest.put(
                "amount",
                amountInPaise
        );

        refundRequest.put(
                "speed",
                "optimum"
        );

        JSONObject notes =
                new JSONObject();

        notes.put(
                "reason",
                "Order Cancellation"
        );

        refundRequest.put(
                "notes",
                notes
        );

        try {

            com.razorpay.Refund refund =
                    razorpayClient.payments.refund(
                            payment.getRazorpayPaymentId(),
                            refundRequest
                    );

            if (refund == null) {

                throw new PaymentException(
                        "Razorpay did not return a refund response"
                );
            }

            payment.setPaymentStatus(
                    PaymentStatus.REFUNDED
            );

            paymentRepository.save(payment);

            return paymentMapper.toResponseDTO(
                    payment
            );

        } catch (RazorpayException exception) {

            throw new PaymentException(
                    "Unable to process Razorpay refund: "
                            + exception.getMessage()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RazorpayCheckoutResponseDTO getCheckoutDetails(
            String username,
            Long orderId
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found"
                        )
                );

        Order order = orderRepository
                .findByIdAndUser(orderId, user)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: " + orderId
                        )
                );

        if (order.getStatus() != OrderStatus.PENDING) {

            throw new PaymentException(
                    "Checkout is not available for order with status: "
                            + order.getStatus()
            );
        }

        Payment payment = paymentRepository
                .findByOrder(order)
                .orElseThrow(() ->
                        new PaymentException(
                                "Payment not found for order: " + orderId
                        )
                );

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {

            throw new PaymentException(
                    "Checkout is not available for payment with status: "
                            + payment.getPaymentStatus()
            );
        }

        if (
                payment.getRazorpayOrderId() == null
                        || payment.getRazorpayOrderId().isBlank()
        ) {

            throw new PaymentException(
                    "Razorpay order ID is missing"
            );
        }

        if (
                payment.getAmount() == null
                        || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0
        ) {

            throw new PaymentException(
                    "Invalid payment amount"
            );
        }

        return new RazorpayCheckoutResponseDTO(
                razorpayKeyId,
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                "INR",
                order.getId(),
                order.getOrderNumber()
        );
    }

    @Override
    @Transactional
    public PaymentResponseDTO verifyRazorpayPayment(
            String username,
            RazorpayVerifyRequestDTO request
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new userNotFoundException(
                                "User not found"
                        )
                );

        if (
                request.getRazorpayPaymentId() == null
                        || request.getRazorpayPaymentId().isBlank()
        ) {

            throw new PaymentException(
                    "Razorpay payment ID is required"
            );
        }

        if (
                request.getRazorpayOrderId() == null
                        || request.getRazorpayOrderId().isBlank()
        ) {

            throw new PaymentException(
                    "Razorpay order ID is required"
            );
        }

        if (
                request.getRazorpaySignature() == null
                        || request.getRazorpaySignature().isBlank()
        ) {

            throw new PaymentException(
                    "Razorpay signature is required"
            );
        }

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                request.getRazorpayOrderId()
                        )
                        .orElseThrow(() ->
                                new PaymentException(
                                        "Payment not found for Razorpay order: "
                                                + request.getRazorpayOrderId()
                                )
                        );

        Order order = payment.getOrder();

        if (order == null) {

            throw new PaymentException(
                    "Order not found for payment"
            );
        }

        if (!order.getUser().getId().equals(user.getId())) {

            throw new PaymentException(
                    "You are not authorized to verify this payment"
            );
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {

            return paymentMapper.toResponseDTO(
                    payment
            );
        }

        if (order.getStatus() != OrderStatus.PENDING) {

            throw new PaymentException(
                    "Payment verification is not available for order with status: "
                            + order.getStatus()
            );
        }

        if (
                !request.getRazorpayOrderId()
                        .equals(payment.getRazorpayOrderId())
        ) {

            throw new PaymentException(
                    "Razorpay order ID does not match"
            );
        }

        JSONObject verificationData =
                new JSONObject();

        verificationData.put(
                "razorpay_order_id",
                payment.getRazorpayOrderId()
        );

        verificationData.put(
                "razorpay_payment_id",
                request.getRazorpayPaymentId()
        );

        verificationData.put(
                "razorpay_signature",
                request.getRazorpaySignature()
        );

        boolean signatureValid;

        try {

            signatureValid =
                    Utils.verifyPaymentSignature(
                            verificationData,
                            razorpayKeySecret
                    );

        } catch (Exception exception) {

            throw new PaymentException(
                    "Unable to verify Razorpay payment signature"
            );
        }

        if (!signatureValid) {

            payment.setPaymentStatus(
                    PaymentStatus.FAILED
            );

            paymentRepository.save(payment);

            throw new PaymentException(
                    "Invalid Razorpay payment signature"
            );
        }

        if (
                paymentRepository
                        .findByRazorpayPaymentId(
                                request.getRazorpayPaymentId()
                        )
                        .filter(existingPayment ->
                                !existingPayment.getId().equals(payment.getId())
                        )
                        .isPresent()
        ) {

            throw new PaymentException(
                    "Razorpay payment ID is already used"
            );
        }

        try {

            com.razorpay.Payment razorpayPayment =
                    razorpayClient.payments.fetch(
                            request.getRazorpayPaymentId()
                    );

            String fetchedOrderId =
                    razorpayPayment.get("order_id");

            if (
                    fetchedOrderId == null
                            || !fetchedOrderId.equals(
                            payment.getRazorpayOrderId()
                    )
            ) {

                payment.setPaymentStatus(
                        PaymentStatus.FAILED
                );

                paymentRepository.save(payment);

                throw new PaymentException(
                        "Razorpay payment does not belong to this order"
                );
            }

            String paymentStatus =
                    razorpayPayment.get("status");

            if (!"captured".equalsIgnoreCase(paymentStatus)) {

                throw new PaymentException(
                        "Razorpay payment is not captured. Current status: "
                                + paymentStatus
                );
            }

            finalizeSuccessfulOnlineOrder(
                    order
            );

            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId()
            );

            payment.setRazorpaySignature(
                    request.getRazorpaySignature()
            );

            payment.setPaymentStatus(
                    PaymentStatus.SUCCESS
            );

            payment.setPaidAt(
                    LocalDateTime.now()
            );

            paymentRepository.save(payment);

            order.setStatus(
                    OrderStatus.CONFIRMED
            );

            orderRepository.save(order);

            return paymentMapper.toResponseDTO(
                    payment
            );

        } catch (RazorpayException exception) {

            throw new PaymentException(
                    "Unable to verify Razorpay payment status"
            );
        }
    }

    private void validateCurrentStockForPayment(
            Order order
    ) {

        for (OrderItem orderItem : order.getOrderItems()) {

            Product product =
                    productRepository
                            .findByIdForUpdate(
                                    orderItem
                                            .getProduct()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new PaymentException(
                                            "Product not found for order item"
                                    )
                            );

            if (
                    product.getQuantity() == null
                            || product.getQuantity() < orderItem.getQuantity()
            ) {

                throw new PaymentException(
                        "Insufficient stock for product: "
                                + orderItem.getProductName()
                );
            }
        }
    }

    private void finalizeSuccessfulOnlineOrder(
            Order order
    ) {

        Cart cart = cartRepository
                .findByUser(order.getUser())
                .orElseThrow(() ->
                        new PaymentException(
                                "Cart not found while completing payment"
                        )
                );

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        for (OrderItem orderItem : order.getOrderItems()) {

            Product product =
                    productRepository
                            .findByIdForUpdate(
                                    orderItem
                                            .getProduct()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new PaymentException(
                                            "Product not found while completing order"
                                    )
                            );

            if (
                    product.getQuantity() == null
                            || product.getQuantity() < orderItem.getQuantity()
            ) {

                throw new PaymentException(
                        "Insufficient stock for product: "
                                + orderItem.getProductName()
                );
            }

            product.setQuantity(
                    product.getQuantity()
                            - orderItem.getQuantity()
            );

            productRepository.save(product);

            CartItem matchingCartItem = null;

            for (CartItem cartItem : cartItems) {

                if (
                        cartItem.getProduct() != null
                                && cartItem.getProduct().getId()
                                .equals(orderItem.getProduct().getId())
                ) {
                    matchingCartItem = cartItem;
                    break;
                }
            }

            if (matchingCartItem != null) {

                int remainingQuantity =
                        matchingCartItem.getQuantity()
                                - orderItem.getQuantity();

                if (remainingQuantity <= 0) {
                    cartItemRepository.delete(matchingCartItem);
                } else {
                    matchingCartItem.setQuantity(remainingQuantity);
                    cartItemRepository.save(matchingCartItem);
                }
            }
        }
    }
}
