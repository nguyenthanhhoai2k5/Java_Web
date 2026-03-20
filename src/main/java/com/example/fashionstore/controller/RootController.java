package com.example.fashionstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    @GetMapping("/")
    public String redirectToAdmin() {
        return "redirect:/admin/home";
    }
}