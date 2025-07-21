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

/**
 * Главный контроллер приложения для изучения и тестирования выражений Aviator.
 */
@Controller
public class HomeController {

    private final Deque<String> expressionHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 100;
    private static final String APP_VERSION = "appVersion";
    private static final String APP_EXPRESSION = "expression";
    private static final String APP_RESULT = "result";
    private static final String APP_HISTORY = "history";
    private static final String ERROR_PREFIX = "Ошибка: ";

    @Value("${spring.application.version}")
    private String appVersion;

    /**
     * Отображает главную страницу приложения.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "index";
    }

    /**
     * Отображает страницу с примерами арифметических выражений.
     */
    @GetMapping("/arithmetic")
    public String showArithmeticPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/arithmetic";
    }

    /**
     * Обрабатывает и вычисляет арифметическое выражение, отображает результат и историю.
     */
    @PostMapping("/arithmetic")
    public String evaluateArithmetic(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

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
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/arithmetic";
    }

    /**
     * Отображает страницу с примерами строковых выражений.
     */
    @GetMapping("/strings")
    public String showStringsPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/strings";
    }

    /**
     * Обрабатывает и вычисляет строковое выражение, отображает результат и историю.
     */
    @PostMapping("/strings")
    public String evaluateStringExpression(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

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
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/strings";
    }

    /**
     * Отображает страницу с примерами логических выражений.
     */
    @GetMapping("/logic")
    public String showLogicPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/logic";
    }

    /**
     * Обрабатывает и вычисляет логическое выражение, отображает результат и историю.
     */
    @PostMapping("/logic")
    public String evaluateLogicExpression(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

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
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/logic";
    }

    /**
     * Отображает страницу с примерами работы с датами и временем.
     */
    @GetMapping("/dates")
    public String showDatesPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/dates";
    }

    /**
     * Обрабатывает и вычисляет выражение, связанное с датами и временем, отображает результат и историю.
     */
    @PostMapping("/dates")
    public String evaluateDateExpression(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

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
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/dates";
    }
}
