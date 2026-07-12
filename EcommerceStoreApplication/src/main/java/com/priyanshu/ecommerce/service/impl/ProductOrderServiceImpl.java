package com.priyanshu.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.priyanshu.ecommerce.entity.Cart;
import com.priyanshu.ecommerce.entity.OrderAddress;
import com.priyanshu.ecommerce.entity.ProductOrder;
import com.priyanshu.ecommerce.entity.ProductOrderRequest;
import com.priyanshu.ecommerce.repository.CartRepository;
import com.priyanshu.ecommerce.repository.ProductOrderRepository;
import com.priyanshu.ecommerce.service.ProductOrderService;

@Service
public class ProductOrderServiceImpl implements ProductOrderService {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Override
    public ProductOrder saveProductOrder(Long userId, ProductOrderRequest productOrderRequest) {

        // get all cart items for this user
        List<Cart> listOfCarts = cartRepository.findByUserId(userId);

        // ✅ collect all saved orders
        List<ProductOrder> savedOrders = new ArrayList<>();

        for (Cart cart : listOfCarts) {

            // ✅ Build the delivery address from the request
            OrderAddress orderAddress = new OrderAddress();
            orderAddress.setFirstName(productOrderRequest.getFirstName());
            orderAddress.setLastName(productOrderRequest.getLastName());
            orderAddress.setEmail(productOrderRequest.getEmail());
            orderAddress.setMobile(productOrderRequest.getMobile());
            orderAddress.setAddress(productOrderRequest.getAddress());
            orderAddress.setCity(productOrderRequest.getCity());
            orderAddress.setState(productOrderRequest.getState());
            orderAddress.setPinCode(productOrderRequest.getPinCode());

            // ✅ Build the order
            ProductOrder order = new ProductOrder();
            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(new Date());
            order.setProduct(cart.getProduct());
            order.setPrice(cart.getProduct().getDiscountPrice());
            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());
            order.setStatus("In Progress");
            order.setPaymentType(productOrderRequest.getPaymentType()); // ✅ was missing
            order.setOrderAddress(orderAddress);                        // ✅ was missing

            // ✅ Actually save the order — this was the main bug
            ProductOrder savedOrder = productOrderRepository.save(order);
            savedOrders.add(savedOrder);
        }

        // return the last saved order (or null if cart was empty)
        return savedOrders.isEmpty() ? null : savedOrders.get(savedOrders.size() - 1);
    }
}