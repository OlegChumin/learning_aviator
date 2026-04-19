package org.chumin.learning_aviator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для {@link HomeController}.
 * <p>
 * Тестовый класс поднимает полноценный Spring Boot web-контекст на случайном
 * порту и обращается к приложению через HTTP с помощью {@link TestRestTemplate}.
 * Так проверяется связка маршрутизации Spring MVC, контроллера, Aviator,
 * Thymeleaf-шаблонов и обработки HTML-форм.
 * </p>
 * <p>
 * Покрываемые сценарии:
 * </p>
 * <ul>
 *     <li>{@code GET /} возвращает главную HTML-страницу;</li>
 *     <li>GET-страницы примеров доступны по HTTP и содержат названия разделов;</li>
 *     <li>{@code POST /arithmetic} принимает form-urlencoded запрос и выводит результат;</li>
 *     <li>{@code POST /strings} принимает form-urlencoded запрос и выводит результат;</li>
 *     <li>{@code POST /logic} принимает form-urlencoded запрос и выводит результат;</li>
 *     <li>{@code POST /dates} принимает form-urlencoded запрос и выводит результат;</li>
 *     <li>ошибка Aviator отображается на HTML-странице без HTTP 500.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HomeControllerIntegrationTest {

    /**
     * HTTP-клиент Spring Boot Test, привязанный к случайному порту поднятого
     * тестового web-сервера.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET / возвращает главную HTML-страницу")
    void indexReturnsMainHtmlPage() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Learning Aviator")
                .contains("Изучение работы с DSL языком AVIATOR");
    }

    @Test
    @DisplayName("GET-страницы примеров доступны по HTTP")
    void examplePagesAreAvailableOverHttp() {
        assertPageContains("/arithmetic", "Арифметические выражения в Aviator");
        assertPageContains("/strings", "Строковые выражения в Aviator");
        assertPageContains("/logic", "Логические выражения в Aviator");
        assertPageContains("/dates", "Примеры работы с датами и временем");
    }

    @Test
    @DisplayName("POST /arithmetic вычисляет выражение из HTML-формы")
    void arithmeticPostEvaluatesFormExpression() {
        ResponseEntity<String> response = postExpression("/arithmetic", "10 + (3 * 5)");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Результат:")
                .contains("25")
                .contains("10 + (3 * 5) = 25");
    }

    @Test
    @DisplayName("POST /strings вычисляет выражение из HTML-формы")
    void stringsPostEvaluatesFormExpression() {
        ResponseEntity<String> response = postExpression("/strings", "\"Hello\" + \" World\"");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Результат:")
                .contains("Hello World");
    }

    @Test
    @DisplayName("POST /logic вычисляет выражение из HTML-формы")
    void logicPostEvaluatesFormExpression() {
        ResponseEntity<String> response = postExpression("/logic", "5 > 3 && true");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Результат:")
                .contains("true")
                .contains("5 &gt; 3 &amp;&amp; true = true");
    }

    @Test
    @DisplayName("POST /dates вычисляет выражение из HTML-формы")
    void datesPostEvaluatesFormExpression() {
        ResponseEntity<String> response = postExpression("/dates", "now() > 0");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Результат:")
                .contains("true")
                .contains("now() &gt; 0 = true");
    }

    @Test
    @DisplayName("Ошибка Aviator отображается на странице без HTTP 500")
    void aviatorErrorIsRenderedOnPageWithoutServerError() {
        ResponseEntity<String> response = postExpression("/arithmetic", "10 +");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Результат:")
                .contains("Ошибка:");
    }

    /**
     * Проверяет, что GET-запрос к указанному URL возвращает HTML-страницу
     * со статусом {@link HttpStatus#OK} и ожидаемым текстом.
     *
     * @param url относительный URL внутри тестируемого приложения
     * @param expectedText текст, который должен присутствовать в HTML-ответе
     */
    private void assertPageContains(String url, String expectedText) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(expectedText);
    }

    /**
     * Отправляет выражение Aviator как HTML-форму на указанный POST endpoint.
     * <p>
     * Метод использует {@link MediaType#APPLICATION_FORM_URLENCODED}, чтобы
     * запрос соответствовал отправке формы из браузера. Параметр формы
     * называется {@code expression}, как ожидает {@link HomeController}.
     * </p>
     *
     * @param url относительный URL POST endpoint внутри тестируемого приложения
     * @param expression выражение Aviator, отправляемое в параметре формы
     * @return полный HTTP-ответ с отрендеренной HTML-страницей
     */
    private ResponseEntity<String> postExpression(String url, String expression) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("expression", expression);

        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }
}
