package com.quickpay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false, defaultValue = "ACC123456") String account,
            Model model) {
        model.addAttribute("accountNumber", account);
        return "dashboard";
    }

    @GetMapping("/transfer")
    public String transfer(
            @RequestParam(required = false, defaultValue = "ACC123456") String account,
            Model model) {
        model.addAttribute("accountNumber", account);
        return "transfer";
    }

    @GetMapping("/history")
    public String history(
            @RequestParam(required = false, defaultValue = "ACC123456") String account,
            Model model) {
        model.addAttribute("accountNumber", account);
        return "history";
    }
}