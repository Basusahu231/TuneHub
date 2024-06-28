package com.example.demo.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entities.Users;
import com.example.demo.services.UsersService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.servlet.http.HttpSession;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {
    @Autowired
    UsersService service;
    
    @PostMapping("/createOrder")
    @ResponseBody
    public String createOrder() {
        Order order = null;
        try {
            RazorpayClient razorpay = new RazorpayClient("rzp_test_sjobK4oIafIABH", "ZdjOxVof6I2OgMOHrabN0Vnj");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", 50000);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt#1");
            JSONObject notes = new JSONObject();
            notes.put("notes_key_1", "Tea, Earl Grey, Hot");
            orderRequest.put("notes", notes);

            order = razorpay.orders.create(orderRequest);

            // Log the order ID for debugging purposes
            System.out.println("Order created successfully. Order ID: " + order.get("id"));
        } catch (Exception e) {
            System.out.println("Exception while creating order: " + e.getMessage());
            e.printStackTrace();
        }
        return order != null ? order.toString() : "Error in creating order";
    }
    
    @PostMapping("/verify")
    @ResponseBody
    public boolean verifyPayment(@RequestParam String orderId, @RequestParam String paymentId, @RequestParam String signature) {
        try {
            RazorpayClient razorpayClient = new RazorpayClient("rzp_test_sjobK4oIafIABH", "ZdjOxVof6I2OgMOHrabN0Vnj");

            String verificationData = orderId + "|" + paymentId;

            boolean isValidSignature = Utils.verifySignature(verificationData, signature, "ZdjOxVof6I2OgMOHrabN0Vnj");

            // Log the verification result
            System.out.println("Verification result for order ID " + orderId + ": " + isValidSignature);

            return isValidSignature;
        } catch (RazorpayException e) {
            System.out.println("Exception while verifying payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("payment-success")
    public String paymentSuccess(HttpSession session) {
        String email = (String) session.getAttribute("email");
        Users user = service.getUser(email);
        if (user != null) {
            user.setPremium(true);
            service.updateUser(user);
        } else {
            System.out.println("User not found for email: " + email);
        }
        return "login";
    }

    @GetMapping("payment-failure")
    public String paymentFailure() {
        return "login";
    }
}

