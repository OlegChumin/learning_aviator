package org.chumin.learning_aviator.controller;

import com.googlecode.aviator.AviatorEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    private final Deque<String> expressionHistory = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 100;
    private static final int MAX_EXPRESSION_LENGTH = 500;
    private static final String MODEL_ATTRIBUTE_APP_VERSION = "appVersion";
    private static final String MODEL_ATTRIBUTE_RESULT = "result";
    private static final String MODEL_ATTRIBUTE_HISTORY = "history";
    private static final String REQUEST_PARAM_EXPRESSION = "expression";
    private static final String ERROR_MESSAGE_PREFIX = "Ошибка: ";
    private static final String INDEX_VIEW = "index";
    private static final String ARITHMETIC_VIEW = "examples/arithmetic";
    private static final String STRINGS_VIEW = "examples/strings";
    private static final String LOGIC_VIEW = "examples/logic";
    private static final String DATES_VIEW = "examples/dates";
    private static final String ARITHMETIC_EXPRESSION_TYPE = "arithmetic";
    private static final String STRING_EXPRESSION_TYPE = "string";
    private static final String LOGIC_EXPRESSION_TYPE = "logic";
    private static final String DATE_EXPRESSION_TYPE = "date";

    private final String appVersion;

    public HomeController(@Value("${spring.application.version}") String appVersion) {
        this.appVersion = appVersion;
    }

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
        model.addAttribute(MODEL_ATTRIBUTE_APP_VERSION, appVersion);
        return INDEX_VIEW;
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
        model.addAttribute(MODEL_ATTRIBUTE_APP_VERSION, appVersion);
        return ARITHMETIC_VIEW;
    }

    /**
     * Обрабатывает и вычисляет арифметическое выражение, отображает результат и историю.
     * <p>
     * Полученное из формы выражение передается в {@link AviatorEvaluator}.
     * При успешном выполнении результат добавляется в модель, а строка вида
     * {@code выражение = результат} сохраняется в истории. При ошибке выполнения
     * в модель добавляется текст ошибки с префиксом {@link #ERROR_MESSAGE_PREFIX}.
     * </p>
     *
     * @param expression выражение Aviator из параметра формы {@code expression}
     * @param model MVC-модель для передачи версии приложения, результата и истории
     * @return имя Thymeleaf-шаблона страницы арифметических примеров
     */
    @PostMapping("/arithmetic")
    public String evaluateArithmetic(@RequestParam(REQUEST_PARAM_EXPRESSION) String expression, Model model) {
        return evaluateExpression(expression, model, ARITHMETIC_VIEW, ARITHMETIC_EXPRESSION_TYPE);
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
        model.addAttribute(MODEL_ATTRIBUTE_APP_VERSION, appVersion);
        return STRINGS_VIEW;
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
    public String evaluateStringExpression(@RequestParam(REQUEST_PARAM_EXPRESSION) String expression, Model model) {
        return evaluateExpression(expression, model, STRINGS_VIEW, STRING_EXPRESSION_TYPE);
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
        model.addAttribute(MODEL_ATTRIBUTE_APP_VERSION, appVersion);
        return LOGIC_VIEW;
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
    public String evaluateLogicExpression(@RequestParam(REQUEST_PARAM_EXPRESSION) String expression, Model model) {
        return evaluateExpression(expression, model, LOGIC_VIEW, LOGIC_EXPRESSION_TYPE);
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
        model.addAttribute(MODEL_ATTRIBUTE_APP_VERSION, appVersion);
        return DATES_VIEW;
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
    public String evaluateDateExpression(@RequestParam(REQUEST_PARAM_EXPRESSION) String expression, Model model) {
        return evaluateExpression(expression, model, DATES_VIEW, DATE_EXPRESSION_TYPE);
    }

    /**
     * Выполняет общий сценарий обработки POST-запроса с выражением Aviator.
     * <p>
     * Метод добавляет версию приложения в модель, проверяет ограничение длины,
     * выполняет выражение, сохраняет успешный результат в историю и возвращает
     * имя шаблона страницы, с которой пришел запрос.
     * </p>
     * <p>
     * Debug-логирование не выводит само выражение, чтобы не записывать
     * пользовательский ввод в лог. Вместо этого фиксируются тип выражения,
     * длина выражения, тип результата и техническая ошибка выполнения.
     * </p>
     *
     * @param expression выражение Aviator из формы
     * @param model MVC-модель для передачи версии приложения, результата и истории
     * @param viewName имя Thymeleaf-шаблона, который должен быть возвращен
     * @param expressionType тип раздела выражений для технического debug-лога
     * @return имя Thymeleaf-шаблона
     */
    private String evaluateExpression(String expression, Model model, String viewName, String expressionType) {
        LOGGER.debug("Received {} expression evaluation request. Expression length: {}",
                expressionType, expression.length());
        model.addAttribute(MODEL_ATTRIBUTE_APP_VERSION, appVersion);

        if (!isExpressionTooLong(expression, model)) {
            try {
                Object evaluationResult = AviatorEvaluator.execute(expression);
                model.addAttribute(MODEL_ATTRIBUTE_RESULT, evaluationResult);

                String historyEntry = expression + " = " + evaluationResult;
                addToHistory(historyEntry);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} expression evaluated successfully. Result type: {}",
                            expressionType, resultTypeName(evaluationResult));
                }
            } catch (Exception exception) {
                model.addAttribute(MODEL_ATTRIBUTE_RESULT, ERROR_MESSAGE_PREFIX + exception.getMessage());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} expression evaluation failed. Message: {}",
                            expressionType, exception.getMessage(), exception);
                }
            }
        }

        model.addAttribute(MODEL_ATTRIBUTE_HISTORY, expressionHistory);
        return viewName;
    }

    /**
     * Проверяет, не превышает ли пользовательское выражение допустимую длину.
     * <p>
     * Ограничение защищает приложение от слишком больших POST-запросов,
     * чрезмерного расхода памяти при вычислении выражения и раздувания истории.
     * Если лимит превышен, метод добавляет в модель сообщение об ошибке.
     * </p>
     *
     * @param expression выражение Aviator из формы
     * @param model MVC-модель для передачи сообщения об ошибке в шаблон
     * @return {@code true}, если выражение превышает допустимую длину
     */
    private boolean isExpressionTooLong(String expression, Model model) {
        if (expression.length() <= MAX_EXPRESSION_LENGTH) {
            return false;
        }

        model.addAttribute(MODEL_ATTRIBUTE_RESULT, ERROR_MESSAGE_PREFIX + "длина выражения не должна превышать "
                + MAX_EXPRESSION_LENGTH + " символов");
        LOGGER.debug("Expression rejected because length {} exceeds max length {}",
                expression.length(), MAX_EXPRESSION_LENGTH);
        return true;
    }

    /**
     * Возвращает имя типа результата для технического debug-лога.
     *
     * @param evaluationResult результат выполнения выражения Aviator
     * @return имя класса результата или {@code null}, если результат отсутствует
     */
    private String resultTypeName(Object evaluationResult) {
        return evaluationResult == null ? "null" : evaluationResult.getClass().getSimpleName();
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
     * @param historyEntry строка истории в формате {@code выражение = результат}
     */
    private void addToHistory(String historyEntry) {
        synchronized (expressionHistory) {
            if (!expressionHistory.contains(historyEntry)) {
                expressionHistory.addFirst(historyEntry);
                if (expressionHistory.size() > MAX_HISTORY_SIZE) {
                    expressionHistory.removeLast();
                    LOGGER.debug("Expression history size limit exceeded. Removed oldest history entry");
                }
                LOGGER.debug("Expression history entry added. History size: {}", expressionHistory.size());
            } else {
                LOGGER.debug("Expression history entry skipped because it already exists");
            }
        }
    }
}
