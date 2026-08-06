package com.savemyseat.order;


import com.savemyseat.order.dto.CreateOrderRequest;
import com.savemyseat.order.dto.MarkPaidRequest;
import com.savemyseat.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create an order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created " +
                    "successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Illegal status"),
            @ApiResponse(responseCode = "409", description = "Hold expired")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(dto));
    }

    @Operation(summary = "Find order by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Order marked paid")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order marked " +
                    "paid successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Illegal status")
    })
    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> markPaid(
            @PathVariable Long id,
            @Valid @RequestBody MarkPaidRequest dto) {
        return ResponseEntity.ok(orderService.markPaid(id, dto.stripeSessionId()));
    }

    @Operation(summary = "Orders canceled")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Illegal status")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @Operation(summary = "Order refunded")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order refunded"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Illegal status")
    })
    @PostMapping("/{id}/refund")
    public ResponseEntity<OrderResponse> refundOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.refundOrder(id));
    }
}