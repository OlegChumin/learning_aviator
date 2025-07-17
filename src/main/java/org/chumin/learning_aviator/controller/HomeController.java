package org.chumin.learning_aviator.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${spring.application.version}")
    private String appVersion;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "index";
    }

    @GetMapping("/arithmetic")
    public String showArithmeticPage(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "examples/arithmetic";
    }

    @GetMapping("/strings")
    public String showStringsPage(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "examples/strings";
    }

    @GetMapping("/logic")
    public String showLogicPage(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "examples/logic";
    }

    @GetMapping("/dates")
    public String showDatesPage(Model model) {
        model.addAttribute("appVersion", appVersion);
        return "examples/dates";
    }
}
