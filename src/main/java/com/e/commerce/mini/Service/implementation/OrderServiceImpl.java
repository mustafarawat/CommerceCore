
package com.e.commerce.mini.Service.implementation;

import com.e.commerce.mini.DTO.request.CreateOrderRequestDTO;
import com.e.commerce.mini.DTO.response.OrderResponseDTO;
import com.e.commerce.mini.Enums.OrderStatus;
import com.e.commerce.mini.Enums.PaymentMethod;
import com.e.commerce.mini.Enums.PaymentStatus;
import com.e.commerce.mini.Enums.Status;
import com.e.commerce.mini.Exception.OrderException;
import com.e.commerce.mini.Exception.OrderNotFoundException;
import com.e.commerce.mini.Mapper.OrderMapper;
import com.e.commerce.mini.Repository.AddressRepository;
import com.e.commerce.mini.Repository.CartItemRepository;
import com.e.commerce.mini.Repository.CartRepository;
import com.e.commerce.mini.Repository.OrderRepository;
import com.e.commerce.mini.Repository.PaymentRepository;
import com.e.commerce.mini.Repository.ProductRepository;
import com.e.commerce.mini.Repository.UserRepository;
import com.e.commerce.mini.Service.OrderService;
import com.e.commerce.mini.Service.PaymentService;
import com.e.commerce.mini.models.Address;
import com.e.commerce.mini.models.Cart;
import com.e.commerce.mini.models.CartItem;
import com.e.commerce.mini.models.Order;
import com.e.commerce.mini.models.OrderItem;
import com.e.commerce.mini.models.Payment;
import com.e.commerce.mini.models.Product;
import com.e.commerce.mini.models.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    private final AddressRepository addressRepository;

    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;


    private final PaymentService paymentService;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(
            String username,
            CreateOrderRequestDTO dto
    ) {

        User user = getUser(username);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new OrderException(
                                "Cart not found"
                        )
                );

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new OrderException(
                    "Cart is empty"
            );
        }

        Address address =
                addressRepository.findByIdAndUser(
                        dto.getAddressId(),
                        user
                ).orElseThrow(() ->
                        new OrderException(
                                "Address not found"
                        )
                );

        Order order = new Order();

        order.setOrderNumber(
                generateUniqueOrderNumber()
        );

        order.setUser(user);

        if (dto.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(
                    OrderStatus.CONFIRMED
            );
        } else {
            order.setStatus(
                    OrderStatus.PENDING
            );
        }

        copyShippingAddress(
                order,
                address
        );

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        List<OrderItem> orderItems =
                new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product =
                    productRepository
                            .findByIdForUpdate(
                                    cartItem
                                            .getProduct()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new OrderException(
                                            "Product not found"
                                    )
                            );

            validateProduct(product);

            validateStock(
                    product,
                    cartItem.getQuantity()
            );

            BigDecimal unitPrice =
                    product.getPrice();

            BigDecimal subtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(product);

            orderItem.setProductName(
                    product.getName()
            );

            orderItem.setSku(
                    product.getSku()
            );

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            orderItem.setUnitPrice(
                    unitPrice
            );

            orderItem.setSubtotal(
                    subtotal
            );

            orderItems.add(orderItem);

            totalAmount =
                    totalAmount.add(subtotal);

            if (dto.getPaymentMethod() == PaymentMethod.COD) {
                decreaseStock(
                        product,
                        cartItem.getQuantity()
                );
            }
        }

        order.setTotalAmount(
                totalAmount
        );

        order.setOrderItems(
                orderItems
        );

        Order savedOrder =
                orderRepository.save(order);

        if (dto.getPaymentMethod() == PaymentMethod.COD) {

            Payment payment = new Payment();

            payment.setOrder(savedOrder);
            payment.setPaymentMethod(PaymentMethod.COD);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setAmount(totalAmount);

            Payment savedPayment =
                    paymentRepository.save(payment);

            savedOrder.setPayment(savedPayment);
        }

        if (dto.getPaymentMethod() == PaymentMethod.COD) {
            cartItemRepository.deleteByCart(
                    cart
            );
        }

        return OrderMapper.toOrderDTO(
                savedOrder
        );
    }

    @Override
    public List<OrderResponseDTO> getMyOrders(
            String username
    ) {

        User user = getUser(username);

        return orderRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(OrderMapper::toOrderDTO)
                .toList();
    }

    @Override
    public OrderResponseDTO getMyOrderById(
            String username,
            Long orderId
    ) {

        User user = getUser(username);

        Order order =
                getUserOrder(
                        orderId,
                        user
                );

        return OrderMapper.toOrderDTO(
                order
        );
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(
            String username,
            Long orderId
    ) {

        User user = getUser(username);

        Order order =
                getUserOrder(
                        orderId,
                        user
                );

        validateCancellation(
                order
        );

        Payment payment = order.getPayment();

        if (
                payment != null
                        && payment.getPaymentMethod() == PaymentMethod.ONLINE
                        && payment.getPaymentStatus() == PaymentStatus.SUCCESS
        ) {

            paymentService.refundPayment(
                    username,
                    orderId
            );

            payment = order.getPayment();

            if (
                    payment == null
                            || payment.getPaymentStatus() != PaymentStatus.REFUNDED
            ) {

                throw new OrderException(
                        "Payment refund was not completed. Order was not cancelled."
                );
            }
        }

        if (
                payment == null
                        || payment.getPaymentMethod() == PaymentMethod.COD
                        || (
                        payment.getPaymentMethod() == PaymentMethod.ONLINE
                                && payment.getPaymentStatus() == PaymentStatus.FAILED
                )
        ) {

            restoreOrderStock(
                    order
            );
        }

        if (
                payment != null
                        && payment.getPaymentMethod() == PaymentMethod.ONLINE
                        && payment.getPaymentStatus() == PaymentStatus.PENDING
        ) {

            payment.setPaymentStatus(
                    PaymentStatus.FAILED
            );

            paymentRepository.save(payment);

            restoreOrderStock(
                    order
            );
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        Order savedOrder =
                orderRepository.save(order);

        return OrderMapper.toOrderDTO(
                savedOrder
        );
    }

    @Override
    public List<OrderResponseDTO> getAllOrdersForAdmin() {

        return orderRepository
                .findAll()
                .stream()
                .map(OrderMapper::toOrderDTO)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmOrderForAdmin(
            Long orderId
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );

        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new OrderException(
                    "This order cannot be confirmed in its current status"
            );
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderException(
                    "Only pending orders can be confirmed"
            );
        }

        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder =
                orderRepository.save(order);

        return OrderMapper.toOrderDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO markCodPaymentPaidForAdmin(
            Long orderId
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );

        Payment payment = order.getPayment();

        if (payment == null) {
            throw new OrderException(
                    "Payment not found for order: "
                            + order.getOrderNumber()
            );
        }

        if (payment.getPaymentMethod() != PaymentMethod.COD) {
            throw new OrderException(
                    "Only COD payments can be marked as paid from the admin dashboard"
            );
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return OrderMapper.toOrderDTO(order);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new OrderException(
                    "COD payment cannot be marked as paid from status: "
                            + payment.getPaymentStatus()
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        payment.setPaidAt(
                LocalDateTime.now()
        );

        paymentRepository.save(payment);

        return OrderMapper.toOrderDTO(order);
    }

    @Override
    public OrderResponseDTO getOrderByIdForAdmin(
            Long orderId
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with ID: "
                                                + orderId
                                )
                        );

        return OrderMapper.toOrderDTO(
                order
        );
    }

    private User getUser(
            String username
    ) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new OrderException(
                                "User not found"
                        )
                );
    }

    private Order getUserOrder(
            Long orderId,
            User user
    ) {

        return orderRepository
                .findByIdAndUser(
                        orderId,
                        user
                )
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: "
                                        + orderId
                        )
                );
    }

    private void copyShippingAddress(
            Order order,
            Address address
    ) {

        order.setShippingFullName(
                address.getFullName()
        );

        order.setShippingContactNo(
                address.getContactNo()
        );

        order.setShippingAddressLine(
                address.getAddressLine()
        );

        order.setShippingCity(
                address.getCity()
        );

        order.setShippingState(
                address.getState()
        );

        order.setShippingPincode(
                address.getPincode()
        );

        order.setShippingCountry(
                address.getCountry()
        );
    }

    private void validateProduct(
            Product product
    ) {

        if (product.getStatus() != Status.ACTIVE) {

            throw new OrderException(
                    "Product is not available: "
                            + product.getName()
            );
        }

        if (product.getPrice() == null) {

            throw new OrderException(
                    "Product price is not available: "
                            + product.getName()
            );
        }
    }

    private void validateStock(
            Product product,
            Integer requestedQuantity
    ) {

        if (
                requestedQuantity == null ||
                        requestedQuantity <= 0
        ) {

            throw new OrderException(
                    "Invalid quantity for product: "
                            + product.getName()
            );
        }

        if (
                product.getQuantity() == null ||
                        product.getQuantity() <
                                requestedQuantity
        ) {

            throw new OrderException(
                    "Insufficient stock for product: "
                            + product.getName()
            );
        }
    }

    private void decreaseStock(
            Product product,
            Integer quantity
    ) {

        product.setQuantity(
                product.getQuantity()
                        - quantity
        );

        productRepository.save(
                product
        );
    }

    private void restoreOrderStock(
            Order order
    ) {

        for (
                OrderItem orderItem :
                order.getOrderItems()
        ) {

            Product product =
                    productRepository
                            .findByIdForUpdate(
                                    orderItem
                                            .getProduct()
                                            .getId()
                            )
                            .orElseThrow(() ->
                                    new OrderException(
                                            "Product not found while restoring stock"
                                    )
                            );

            product.setQuantity(
                    product.getQuantity()
                            + orderItem.getQuantity()
            );

            productRepository.save(
                    product
            );
        }
    }

    private void validateCancellation(
            Order order
    ) {

        if (
                order.getStatus() !=
                        OrderStatus.PENDING
                        &&
                        order.getStatus() !=
                                OrderStatus.CONFIRMED
        ) {

            throw new OrderException(
                    "Order cannot be cancelled at status: "
                            + order.getStatus()
            );
        }
    }

    private String generateUniqueOrderNumber() {

        String orderNumber;

        do {

            orderNumber =
                    "ORD-" +
                            UUID.randomUUID()
                                    .toString()
                                    .substring(
                                            0,
                                            8
                                    )
                                    .toUpperCase();

        } while (
                orderRepository
                        .existsByOrderNumber(
                                orderNumber
                        )
        );

        return orderNumber;
    }
}
