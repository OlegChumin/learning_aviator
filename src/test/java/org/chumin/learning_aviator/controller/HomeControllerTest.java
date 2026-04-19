package org.chumin.learning_aviator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты для {@link HomeController}.
 * <p>
 * Тестовый класс проверяет поведение контроллера без запуска web-сервера:
 * методы вызываются напрямую, а данные для Thymeleaf проверяются через
 * {@link ExtendedModelMap}.
 * </p>
 * <p>
 * Покрываемые сценарии:
 * </p>
 * <ul>
 *     <li>главная страница возвращает шаблон {@code index} и версию приложения;</li>
 *     <li>GET-страницы примеров возвращают правильные Thymeleaf-шаблоны;</li>
 *     <li>POST {@code /arithmetic} вычисляет арифметическое выражение и добавляет историю;</li>
 *     <li>POST {@code /strings} вычисляет строковое выражение и добавляет историю;</li>
 *     <li>POST {@code /logic} вычисляет логическое выражение и добавляет историю;</li>
 *     <li>POST {@code /dates} вычисляет выражение с датами и добавляет историю;</li>
 *     <li>ошибки Aviator возвращаются пользователю и не попадают в историю;</li>
 *     <li>история не сохраняет дубликаты выражений;</li>
 *     <li>история ограничивается последними 100 уникальными записями.</li>
 * </ul>
 */
class HomeControllerTest {

    /**
     * Имя атрибута модели, в который контроллер кладет версию приложения.
     */
    private static final String APP_VERSION = "appVersion";

    /**
     * Имя атрибута модели, в который контроллер кладет результат вычисления.
     */
    private static final String APP_RESULT = "result";

    /**
     * Имя атрибута модели, в который контроллер кладет историю вычислений.
     */
    private static final String APP_HISTORY = "history";

    /**
     * Тестовое значение версии приложения, подставляемое в контроллер вручную.
     */
    private static final String TEST_VERSION = "test-version";

    /**
     * Экземпляр контроллера, который тестируется прямыми вызовами методов.
     */
    private HomeController controller;

    /**
     * Создает новый экземпляр контроллера перед каждым тестом и подставляет
     * значение поля {@code appVersion}, которое в приложении заполняется Spring.
     */
    @BeforeEach
    void setUp() {
        controller = new HomeController();
        ReflectionTestUtils.setField(controller, APP_VERSION, TEST_VERSION);
    }

    @Test
    @DisplayName("GET / возвращает главную страницу и версию приложения")
    void indexReturnsMainPageWithAppVersion() {
        Model model = new ExtendedModelMap();

        String viewName = controller.index(model);

        assertThat(viewName).isEqualTo("index");
        assertThat(model.asMap()).containsEntry(APP_VERSION, TEST_VERSION);
    }

    @Test
    @DisplayName("GET-страницы примеров возвращают свои шаблоны и версию приложения")
    void getPagesReturnExampleTemplatesWithAppVersion() {
        assertExamplePage(controller::showArithmeticPage, "examples/arithmetic");
        assertExamplePage(controller::showStringsPage, "examples/strings");
        assertExamplePage(controller::showLogicPage, "examples/logic");
        assertExamplePage(controller::showDatesPage, "examples/dates");
    }

    @Test
    @DisplayName("POST /arithmetic вычисляет выражение и добавляет запись в историю")
    void evaluateArithmeticReturnsResultAndAddsHistoryEntry() {
        Model model = new ExtendedModelMap();

        String viewName = controller.evaluateArithmetic("10 + (3 * 5)", model);

        assertThat(viewName).isEqualTo("examples/arithmetic");
        assertThat(model.asMap()).containsEntry(APP_VERSION, TEST_VERSION);
        assertThat(model.asMap()).containsEntry(APP_RESULT, 25L);
        assertHistory(model, "10 + (3 * 5) = 25");
    }

    @Test
    @DisplayName("POST /strings вычисляет выражение и добавляет запись в историю")
    void evaluateStringExpressionReturnsResultAndAddsHistoryEntry() {
        Model model = new ExtendedModelMap();

        String viewName = controller.evaluateStringExpression("\"Hello\" + \" World\"", model);

        assertThat(viewName).isEqualTo("examples/strings");
        assertThat(model.asMap()).containsEntry(APP_RESULT, "Hello World");
        assertHistory(model, "\"Hello\" + \" World\" = Hello World");
    }

    @Test
    @DisplayName("POST /logic вычисляет выражение и добавляет запись в историю")
    void evaluateLogicExpressionReturnsResultAndAddsHistoryEntry() {
        Model model = new ExtendedModelMap();

        String viewName = controller.evaluateLogicExpression("5 > 3 && true", model);

        assertThat(viewName).isEqualTo("examples/logic");
        assertThat(model.asMap()).containsEntry(APP_RESULT, true);
        assertHistory(model, "5 > 3 && true = true");
    }

    @Test
    @DisplayName("POST /dates вычисляет выражение и добавляет запись в историю")
    void evaluateDateExpressionReturnsResultAndAddsHistoryEntry() {
        Model model = new ExtendedModelMap();

        String viewName = controller.evaluateDateExpression("now() > 0", model);

        assertThat(viewName).isEqualTo("examples/dates");
        assertThat(model.asMap()).containsEntry(APP_RESULT, true);
        assertHistory(model, "now() > 0 = true");
    }

    @Test
    @DisplayName("Ошибки Aviator отображаются как результат и не добавляются в историю")
    void evaluateExpressionsReturnErrorAndDoNotAddHistoryEntryWhenAviatorFails() {
        assertAviatorError(controller::evaluateArithmetic, "examples/arithmetic");
        assertAviatorError(controller::evaluateStringExpression, "examples/strings");
        assertAviatorError(controller::evaluateLogicExpression, "examples/logic");
        assertAviatorError(controller::evaluateDateExpression, "examples/dates");
    }

    @Test
    @DisplayName("История не сохраняет одинаковые записи повторно")
    void historyDoesNotStoreDuplicateEntries() {
        controller.evaluateArithmetic("1 + 1", new ExtendedModelMap());
        Model model = new ExtendedModelMap();

        controller.evaluateArithmetic("1 + 1", model);

        assertThat(historyFrom(model)).containsExactly("1 + 1 = 2");
    }

    @Test
    @DisplayName("История хранит только последние 100 записей")
    void historyKeepsOnlyLastOneHundredEntries() {
        Model model = new ExtendedModelMap();

        for (int i = 0; i < 101; i++) {
            model = new ExtendedModelMap();
            controller.evaluateArithmetic(i + " + 1", model);
        }

        assertThat(historyFrom(model))
                .hasSize(100)
                .first()
                .isEqualTo("100 + 1 = 101");
        assertThat(historyFrom(model)).doesNotContain("0 + 1 = 1");
    }

    /**
     * Проверяет общий контракт GET-страниц с примерами.
     * <p>
     * Все такие страницы должны вернуть ожидаемое имя Thymeleaf-шаблона,
     * добавить версию приложения в модель и не добавлять результат вычисления,
     * потому что выражение еще не отправлялось.
     * </p>
     *
     * @param handler обработчик GET-страницы, который принимает {@link Model}
     * @param expectedViewName ожидаемое имя Thymeleaf-шаблона
     */
    private void assertExamplePage(ExamplePageHandler handler, String expectedViewName) {
        Model model = new ExtendedModelMap();
        String viewName = handler.handle(model);

        assertThat(viewName).isEqualTo(expectedViewName);
        assertThat(model.asMap()).containsEntry(APP_VERSION, TEST_VERSION);
        assertThat(model.asMap()).doesNotContainKey(APP_RESULT);
    }

    /**
     * Проверяет, что модель содержит историю ровно с одной ожидаемой записью.
     *
     * @param model MVC-модель, заполненная тестируемым методом контроллера
     * @param expectedEntry ожидаемая запись истории в формате {@code выражение = результат}
     */
    private void assertHistory(Model model, String expectedEntry) {
        assertThat(historyFrom(model)).containsExactly(expectedEntry);
    }

    /**
     * Проверяет единый сценарий обработки ошибки Aviator для POST-методов.
     * <p>
     * Контроллер должен вернуть исходный шаблон страницы, положить в модель
     * текст ошибки с префиксом {@code Ошибка: } и не добавлять неуспешное
     * вычисление в историю.
     * </p>
     *
     * @param handler обработчик POST-страницы, принимающий выражение и модель
     * @param expectedViewName ожидаемое имя Thymeleaf-шаблона
     */
    private void assertAviatorError(ExpressionHandler handler, String expectedViewName) {
        Model model = new ExtendedModelMap();

        String viewName = handler.handle("10 +", model);

        assertThat(viewName).isEqualTo(expectedViewName);
        assertThat(model.asMap().get(APP_RESULT)).asString().startsWith("Ошибка: ");
        assertThat(historyFrom(model)).isEmpty();
    }

    /**
     * Извлекает историю вычислений из модели.
     * <p>
     * Приведение типа безопасно для этих тестов, потому что контроллер кладет
     * в атрибут {@link #APP_HISTORY} поле типа {@code Deque<String>}.
     * </p>
     *
     * @param model MVC-модель, заполненная тестируемым методом контроллера
     * @return история вычислений из модели
     */
    @SuppressWarnings("unchecked")
    private Deque<String> historyFrom(Model model) {
        return (Deque<String>) model.asMap().get(APP_HISTORY);
    }

    /**
     * Функциональный интерфейс для унифицированной проверки GET-страниц.
     */
    @FunctionalInterface
    private interface ExamplePageHandler {

        /**
         * Вызывает метод контроллера, который отображает страницу с примером.
         *
         * @param model MVC-модель для передачи данных в шаблон
         * @return имя Thymeleaf-шаблона
         */
        String handle(Model model);
    }

    /**
     * Функциональный интерфейс для унифицированной проверки POST-обработчиков.
     */
    @FunctionalInterface
    private interface ExpressionHandler {

        /**
         * Вызывает метод контроллера, который вычисляет выражение Aviator.
         *
         * @param expression выражение Aviator из формы
         * @param model MVC-модель для передачи результата в шаблон
         * @return имя Thymeleaf-шаблона
         */
        String handle(String expression, Model model);
    }
}
