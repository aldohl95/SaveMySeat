package com.savemyseat.order;

import com.savemyseat.auth.CurrentUserProvider;
import com.savemyseat.hold.Hold;
import com.savemyseat.hold.HoldRepository;
import com.savemyseat.hold.HoldService;
import com.savemyseat.hold.HoldStatus;
import com.savemyseat.order.dto.CreateOrderRequest;
import com.savemyseat.order.dto.OrderResponse;
import com.savemyseat.user.User;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CurrentUserProvider currentUserProvider;
    private final HoldRepository holdRepository;
    private final HoldService holdService;
    private final MeterRegistry registry;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest dto){
        Timer.Sample sample = Timer.start(registry);
        try {
            Hold hold =
                    holdRepository.findById(dto.holdId()).orElseThrow(() -> new EntityNotFoundException("Hold not found: " + dto.holdId()));
            User currentUser = currentUserProvider.getCurrentUser();

            if (!Objects.equals(hold.getUser().getId(), currentUser.getId())) {
                throw new EntityNotFoundException("Hold not Found: " + hold.getId());
            }

            if (hold.getStatus() != HoldStatus.ACTIVE) {
                throw new IllegalStateException("Hold cannot be converted from " +
                        "status: " + hold.getStatus());
            }

            if (OffsetDateTime.now(ZoneOffset.UTC).isAfter(hold.getExpiresAt())) {
                throw new IllegalStateException("Hold has expired");
            }

            Optional<Order> existing = orderRepository.findByHoldId(hold.getId());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }

            long total =
                    (hold.getQuantity() * hold.getTicketTier().getPriceCents());
            Order order = new Order(
                    currentUser,
                    hold,
                    hold.getTicketTier(),
                    hold.getQuantity(),
                    total
            );
            registry.counter("order.created").increment();
            return toResponse(orderRepository.saveAndFlush(order));
        }finally {
            sample.stop(registry.timer("order.creation.time"));
        }
    }

    public OrderResponse getOrderById(Long orderId){
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        User currentUser = currentUserProvider.getCurrentUser();

        if(!Objects.equals(order.getUser().getId(), currentUser.getId())){
            throw new EntityNotFoundException("Order not found: " + orderId);
        }

        return toResponse(order);

    }

    @Transactional
    public OrderResponse markPaid(Long orderId, String stripeSessionId){
        Timer.Sample sample = Timer.start(registry);
        try {
            Order order =
                    orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
            User currentUser = currentUserProvider.getCurrentUser();

            if (!Objects.equals(order.getUser().getId(), currentUser.getId())) {
                throw new EntityNotFoundException("Order not found: " + orderId);
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("Order cannot be processed from " +
                        "status: " + order.getStatus());
            }

            order.setStatus(OrderStatus.PAID);
            order.setStripeSessionId(stripeSessionId);
            holdService.convertHold(order.getHold().getId());
            registry.counter("orders.completed");
            return toResponse(orderRepository.saveAndFlush(order));
        }finally {
            sample.stop(registry.timer("order.checkout.time"));
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId){
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        User currentUser = currentUserProvider.getCurrentUser();
        if(!Objects.equals(order.getUser().getId(), currentUser.getId())){
            throw new EntityNotFoundException("Order not found: " + orderId);
        }
        if(order.getStatus() != OrderStatus.PENDING){
            throw new IllegalStateException("Order cannot be cancelled with " +
                    "status " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        holdService.releaseHold(order.getHold().getId());

        return toResponse(orderRepository.saveAndFlush(order));

    }

    @Transactional
    public OrderResponse refundOrder(Long orderId){
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        User currentUser = currentUserProvider.getCurrentUser();

        if(!Objects.equals(order.getUser().getId(), currentUser.getId())){
            throw new EntityNotFoundException("Order not found: " + orderId);
        }

        if(order.getStatus() != OrderStatus.PAID){
            throw new IllegalStateException("Cannot refund a order with " +
                    "status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REFUNDED);
        // Todo: call stripe refund here
        //Todo: decide if this will restore inventory(likely not as refunds
        // will generally happen after the event is over

        return toResponse(orderRepository.saveAndFlush(order));
    }

    private OrderResponse toResponse(Order order){
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getHold().getId(),
                order.getTicketTier().getId(),
                order.getQuantity(),
                order.getTotalCents(),
                order.getStripeSessionId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

}
