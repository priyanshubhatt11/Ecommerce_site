package com.priyanshu.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.priyanshu.ecommerce.entity.Cart;
import com.priyanshu.ecommerce.entity.Product;
import com.priyanshu.ecommerce.entity.User;
import com.priyanshu.ecommerce.repository.CartRepository;
import com.priyanshu.ecommerce.repository.ProductRepository;
import com.priyanshu.ecommerce.repository.UserRepository;
import com.priyanshu.ecommerce.service.CartService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public Cart saveCart(Long productId, Long userId) {

        // ✅ FIXED: safe lookup with orElseThrow instead of .get() which crashes if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

        Cart cart;

        if (ObjectUtils.isEmpty(cartStatus)) {
            // product not in cart yet — add it fresh
            cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * product.getDiscountPrice());
        } else {
            // product already in cart — just increase quantity
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
        }

        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getCartsByUser(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updatedCartList = new ArrayList<>();

        for (Cart cart : carts) {
            Double totalPrice = cart.getProduct().getDiscountPrice() * cart.getQuantity();
            cart.setTotalPrice(totalPrice);

            totalOrderPrice = totalOrderPrice + totalPrice;
            cart.setTotalOrderPrice(totalOrderPrice);

            updatedCartList.add(cart);
        }

        return updatedCartList;
    }

    @Override
    public Long getCounterCart(Long userId) {
        return cartRepository.countByUserId(userId);
    }

    @Override
    public Boolean updateCartQuantity(String symbol, Long cartId) {

        Optional<Cart> cartOptional = cartRepository.findById(cartId);

        if (!cartOptional.isPresent()) {
            log.warn("Cart not found with id: {}", cartId);
            return false; // ✅ cart not found — clearly return false
        }

        Cart cart = cartOptional.get();
        int currentQty = cart.getQuantity();

        if (symbol.equalsIgnoreCase("decrease")) {
            int newQty = currentQty - 1;

            if (newQty <= 0) {
                // quantity hit zero — remove item from cart
                cartRepository.deleteById(cartId);
                log.info("Cart item removed, cartId: {}", cartId);
                return true; // ✅ successfully removed
            }

            cart.setQuantity(newQty);

        } else if (symbol.equalsIgnoreCase("increase")) {
            cart.setQuantity(currentQty + 1); // ✅ increase path

        } else {
            log.warn("Unknown symbol: {}", symbol);
            return false; // ✅ unknown symbol — return false
        }

        cartRepository.save(cart);
        return true; // ✅ FIXED: was returning false even on successful increase
    }

    @Override
    public void clearCartByUser(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(carts);
    }
}