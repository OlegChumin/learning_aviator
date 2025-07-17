package org.chumin.learning_aviator.controller;

import com.googlecode.aviator.AviatorEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayDeque;
import java.util.Deque;

@Controller
public class HomeController {

    private final Deque<String> expressionHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 100;
    private static final String APP_VERSION = "appVersion";
    private static final String APP_EXPRESSION = "expression";


    @Value("${spring.application.version}")
    private String appVersion;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "index";
    }

    @GetMapping("/arithmetic")
    public String showArithmeticPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/arithmetic";
    }

    @PostMapping("/arithmetic")
    public String evaluateArithmetic(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute("result", result);

            String entry = expression + " = " + result;

            synchronized (expressionHistory) {
                if (!expressionHistory.contains(entry)) {
                    expressionHistory.addFirst(entry);
                    if (expressionHistory.size() > MAX_HISTORY_SIZE) {
                        expressionHistory.removeLast();
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("result", "Ошибка: " + e.getMessage());
        }

        model.addAttribute("history", expressionHistory);
        return "examples/arithmetic";
    }

    @GetMapping("/strings")
    public String showStringsPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/strings";
    }

    @GetMapping("/logic")
    public String showLogicPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/logic";
    }

    @GetMapping("/dates")
    public String showDatesPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/dates";
    }
}
