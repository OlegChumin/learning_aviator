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
}
