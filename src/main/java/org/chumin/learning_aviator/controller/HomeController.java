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
 * Главный MVC-контроллер учебного приложения Learning Aviator.
 * <p>
 * Контроллер отвечает за отображение страниц с примерами выражений Aviator,
 * выполнение введенных пользователем выражений и передачу результата выполнения
 * в Thymeleaf-шаблоны через {@link Model}.
 * </p>
 * <p>
 * Поддерживаемые endpoints:
 * </p>
 * <ul>
 *     <li>{@code GET /} - главная страница приложения;</li>
 *     <li>{@code GET /arithmetic} - страница арифметических примеров;</li>
 *     <li>{@code POST /arithmetic} - вычисление арифметического выражения;</li>
 *     <li>{@code GET /strings} - страница строковых примеров;</li>
 *     <li>{@code POST /strings} - вычисление строкового выражения;</li>
 *     <li>{@code GET /logic} - страница логических примеров;</li>
 *     <li>{@code POST /logic} - вычисление логического выражения;</li>
 *     <li>{@code GET /dates} - страница примеров работы с датами и временем;</li>
 *     <li>{@code POST /dates} - вычисление выражения для дат и времени.</li>
 * </ul>
 * <p>
 * История успешных вычислений хранится в памяти текущего экземпляра контроллера.
 * Она не сохраняется между перезапусками приложения и ограничена
 * {@link #MAX_HISTORY_SIZE} последними уникальными записями.
 * </p>
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
     * <p>
     * В модель добавляется текущая версия приложения, чтобы шаблон мог вывести
     * ее в интерфейсе.
     * </p>
     *
     * @param model MVC-модель, в которую добавляется атрибут версии приложения
     * @return имя Thymeleaf-шаблона главной страницы
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "index";
    }

    /**
     * Отображает страницу с примерами арифметических выражений.
     * <p>
     * Страница используется для ручного ввода и проверки выражений с числами,
     * арифметическими операторами и скобками.
     * </p>
     *
     * @param model MVC-модель, в которую добавляется атрибут версии приложения
     * @return имя Thymeleaf-шаблона страницы арифметических примеров
     */
    @GetMapping("/arithmetic")
    public String showArithmeticPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/arithmetic";
    }

    /**
     * Обрабатывает и вычисляет арифметическое выражение, отображает результат и историю.
     * <p>
     * Полученное из формы выражение передается в {@link AviatorEvaluator}.
     * При успешном выполнении результат добавляется в модель, а строка вида
     * {@code выражение = результат} сохраняется в истории. При ошибке выполнения
     * в модель добавляется текст ошибки с префиксом {@link #ERROR_PREFIX}.
     * </p>
     *
     * @param expression выражение Aviator из параметра формы {@code expression}
     * @param model MVC-модель для передачи версии приложения, результата и истории
     * @return имя Thymeleaf-шаблона страницы арифметических примеров
     */
    @PostMapping("/arithmetic")
    public String evaluateArithmetic(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

            String entry = expression + " = " + result;
            addToHistory(entry);
        } catch (Exception e) {
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/arithmetic";
    }

    /**
     * Отображает страницу с примерами строковых выражений.
     * <p>
     * Страница предназначена для демонстрации строковых функций Aviator,
     * например проверки вхождения, префикса, суффикса и изменения регистра.
     * </p>
     *
     * @param model MVC-модель, в которую добавляется атрибут версии приложения
     * @return имя Thymeleaf-шаблона страницы строковых примеров
     */
    @GetMapping("/strings")
    public String showStringsPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/strings";
    }

    /**
     * Обрабатывает и вычисляет строковое выражение, отображает результат и историю.
     * <p>
     * Выполняет пользовательское выражение через {@link AviatorEvaluator}.
     * Успешный результат записывается в модель и историю вычислений. Ошибки
     * синтаксиса или выполнения не пробрасываются наружу, а показываются
     * пользователю как значение атрибута результата.
     * </p>
     *
     * @param expression строковое выражение Aviator из параметра формы {@code expression}
     * @param model MVC-модель для передачи версии приложения, результата и истории
     * @return имя Thymeleaf-шаблона страницы строковых примеров
     */
    @PostMapping("/strings")
    public String evaluateStringExpression(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

            String entry = expression + " = " + result;
            addToHistory(entry);
        } catch (Exception e) {
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/strings";
    }

    /**
     * Отображает страницу с примерами логических выражений.
     * <p>
     * Страница используется для проверки операторов сравнения, логических
     * операторов и тернарных выражений Aviator.
     * </p>
     *
     * @param model MVC-модель, в которую добавляется атрибут версии приложения
     * @return имя Thymeleaf-шаблона страницы логических примеров
     */
    @GetMapping("/logic")
    public String showLogicPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/logic";
    }

    /**
     * Обрабатывает и вычисляет логическое выражение, отображает результат и историю.
     * <p>
     * Выполняет выражение через {@link AviatorEvaluator} и возвращает результат
     * на ту же страницу. При успешном вычислении запись добавляется в общую
     * историю выражений, при ошибке в модель записывается сообщение об ошибке.
     * </p>
     *
     * @param expression логическое выражение Aviator из параметра формы {@code expression}
     * @param model MVC-модель для передачи версии приложения, результата и истории
     * @return имя Thymeleaf-шаблона страницы логических примеров
     */
    @PostMapping("/logic")
    public String evaluateLogicExpression(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

            String entry = expression + " = " + result;
            addToHistory(entry);
        } catch (Exception e) {
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/logic";
    }

    /**
     * Отображает страницу с примерами работы с датами и временем.
     * <p>
     * Страница предназначена для демонстрации встроенных возможностей Aviator,
     * связанных с датами, временем и функцией {@code now()}.
     * </p>
     *
     * @param model MVC-модель, в которую добавляется атрибут версии приложения
     * @return имя Thymeleaf-шаблона страницы примеров с датами и временем
     */
    @GetMapping("/dates")
    public String showDatesPage(Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        return "examples/dates";
    }

    /**
     * Обрабатывает и вычисляет выражение, связанное с датами и временем, отображает результат и историю.
     * <p>
     * Метод выполняет выражение Aviator, переданное из формы страницы дат.
     * Результат успешного выполнения передается в шаблон и сохраняется в истории.
     * Если Aviator возвращает исключение, пользователь получает сообщение
     * об ошибке вместо результата.
     * </p>
     *
     * @param expression выражение Aviator для дат и времени из параметра формы {@code expression}
     * @param model MVC-модель для передачи версии приложения, результата и истории
     * @return имя Thymeleaf-шаблона страницы примеров с датами и временем
     */
    @PostMapping("/dates")
    public String evaluateDateExpression(@RequestParam(APP_EXPRESSION) String expression, Model model) {
        model.addAttribute(APP_VERSION, appVersion);
        try {
            Object result = AviatorEvaluator.execute(expression);
            model.addAttribute(APP_RESULT, result);

            String entry = expression + " = " + result;
            addToHistory(entry);
        } catch (Exception e) {
            model.addAttribute(APP_RESULT, ERROR_PREFIX + e.getMessage());
        }

        model.addAttribute(APP_HISTORY, expressionHistory);
        return "examples/dates";
    }

    /**
     * Добавляет успешное вычисление в историю выражений.
     * <p>
     * Метод синхронизируется по {@link #expressionHistory}, потому что контроллер
     * является Spring singleton bean и может обрабатывать несколько HTTP-запросов
     * одновременно. Запись добавляется только если такой строки еще нет в истории.
     * Новые записи помещаются в начало очереди, а самые старые удаляются при
     * превышении лимита {@link #MAX_HISTORY_SIZE}.
     * </p>
     *
     * @param entry строка истории в формате {@code выражение = результат}
     */
    private void addToHistory(String entry) {
        synchronized (expressionHistory) {
            if (!expressionHistory.contains(entry)) {
                expressionHistory.addFirst(entry);
                if (expressionHistory.size() > MAX_HISTORY_SIZE) {
                    expressionHistory.removeLast();
                }
            }
        }
    }
}
