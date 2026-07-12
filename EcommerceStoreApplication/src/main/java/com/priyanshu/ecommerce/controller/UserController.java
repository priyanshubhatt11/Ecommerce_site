package com.priyanshu.ecommerce.controller;

import java.security.Principal;
import java.util.List;

import com.priyanshu.ecommerce.entity.ProductOrder;
import com.priyanshu.ecommerce.entity.ProductOrderRequest;
import com.priyanshu.ecommerce.service.ProductOrderService;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.priyanshu.ecommerce.entity.Cart;
import com.priyanshu.ecommerce.entity.Category;
import com.priyanshu.ecommerce.entity.User;
import com.priyanshu.ecommerce.service.CartService;
import com.priyanshu.ecommerce.service.CategoryService;
import com.priyanshu.ecommerce.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    UserService userService;

    @Autowired
    CartService cartService;

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            User currentUserDetails = getLoggedUserDetails(principal);
            model.addAttribute("currentLoggedInUserDetails", currentUserDetails);

            Long countCartForUser = cartService.getCounterCart(currentUserDetails.getId());
            model.addAttribute("countCartForUser", countCartForUser);
        }

        List<Category> allActiveCategory = categoryService.findAllActiveCategory();
        model.addAttribute("allActiveCategory", allActiveCategory);
    }

    @GetMapping("/")
    public String home() {
        return "user/user-home";
    }

    // ✅ FIXED: userId now comes from Principal, not from the request
    @GetMapping("/add-to-cart")
    public String addToCart(@RequestParam Long productId, Principal principal, HttpSession session) {

        User loggedInUser = getLoggedUserDetails(principal);  // ✅ trusted source
        Cart saveCart = cartService.saveCart(productId, loggedInUser.getId());

        if (ObjectUtils.isEmpty(saveCart)) {
            session.setAttribute("errorMsg", "Failed to add product to cart");
        } else {
            session.setAttribute("successMsg", "Product added to cart successfully");
        }

        return "redirect:/product/" + productId;
    }

    @GetMapping("/cart")
    public String loadCartPage(Principal principal, Model model) {
        User user = getLoggedUserDetails(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);

        if (carts.size() > 0) {
            Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            model.addAttribute("totalOrderPrice", totalOrderPrice);
        }

        return "/user/cart";
    }

    @GetMapping("/cart-quantity-update")
    public String updateCartQuantity(@RequestParam("symbol") String symbol, @RequestParam("cartId") Long cartId) {
        cartService.updateCartQuantity(symbol, cartId);
        return "redirect:/user/cart";
    }

    // ✅ FIXED: shipping and tax are named constants, not magic numbers
    private static final double SHIPPING_CHARGE = 250.0;
    private static final double TAX_CHARGE = 100.0;

    @GetMapping("/orders")
    public String orderPage(Principal principal, Model model) {
        User user = getLoggedUserDetails(principal);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts", carts);

        if (carts.size() > 0) {
            Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            Double totalOrderPrice = orderPrice + SHIPPING_CHARGE + TAX_CHARGE;  // ✅ clear and named
            model.addAttribute("orderPrice", orderPrice);
            model.addAttribute("totalOrderPrice", totalOrderPrice);
        }

        return "/user/order";
    }

    @Autowired
    ProductOrderService productOrderService;

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute ProductOrderRequest productOrderRequest,
                            Principal principal, HttpSession session) {

        User user = getLoggedUserDetails(principal);

        ProductOrder savedOrder = productOrderService.saveProductOrder(
                user.getId(), productOrderRequest);

        if (savedOrder != null) {
            // ✅ clear the cart after successful order
            cartService.clearCartByUser(user.getId());
            session.setAttribute("successMsg", "Order Placed Successfully!");
        } else {
            session.setAttribute("errorMsg", "Something went wrong. Please try again.");
        }

        return "redirect:/user/orders";
    }

    // shared helper used across all methods
    private User getLoggedUserDetails(Principal principal) {
        String email = principal.getName();
        return userService.getUserByEmail(email);
    }
}