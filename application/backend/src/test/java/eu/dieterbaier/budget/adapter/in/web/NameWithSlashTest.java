package eu.dieterbaier.budget.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Guards how a name reaches the two controllers, over a real servlet container.
 *
 * <p>The rest of the web adapter is tested with {@code MockMvc}, which builds the
 * request object directly and never runs a connector. That cannot catch this
 * class of defect: Tomcat rejects a percent-encoded slash in a path segment
 * before any handler is chosen, so a category named "Gesundheit / Arzt Dieter"
 * was unreachable for update and delete while every unit test stayed green
 * (issue #82). Only a request that travels over HTTP proves the name arrives.
 *
 * <p>The context is deliberately narrow — the two controllers and an actual
 * connector, no persistence — because what is under test is the URL, not the
 * domain behind it.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = NameWithSlashTest.WebAdapterOnly.class)
class NameWithSlashTest {

    private static final String CATEGORY = "Gesundheit / Arzt Dieter";
    private static final String GROUP = "Gesundheit / Arzt";

    @Configuration
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                FlywayAutoConfiguration.class
            })
    @Import({CategoryController.class, CategoryGroupController.class, ApiExceptionHandler.class})
    static class WebAdapterOnly {
    }

    @Autowired
    private TestRestTemplate rest;

    @LocalServerPort
    private int port;

    @MockBean
    private ManageCategoriesUseCase manageCategories;

    @MockBean
    private ManageCategoryGroupsUseCase manageGroups;

    @Test
    void movesACategoryWhoseNameContainsASlashIntoAGroup() {
        given(manageCategories.update(eq(CATEGORY), anyString(), anyString(), anyBoolean()))
                .willReturn(new Category(CATEGORY, new CategoryGroup(GROUP), true));

        ResponseEntity<String> response = put(
                "/api/categories?name=Gesundheit%20%2F%20Arzt%20Dieter",
                """
                {"name":"Gesundheit / Arzt Dieter","group":"Gesundheit / Arzt","pensionRelevant":true}
                """);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Gesundheit / Arzt Dieter");
    }

    @Test
    void renamesAGroupWhoseNameContainsASlash() {
        given(manageGroups.rename(eq(GROUP), anyString())).willReturn(new CategoryGroup("Gesundheit"));

        ResponseEntity<String> response = put(
                "/api/category-groups?name=Gesundheit%20%2F%20Arzt",
                """
                {"name":"Gesundheit"}
                """);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deletesACategoryWhoseNameContainsASlash() {
        ResponseEntity<String> response = rest.exchange(
                uri("/api/categories?name=Hausrat%2FGarten"), HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    /**
     * The constraint that shaped the endpoints, kept as a check rather than as a
     * claim in a comment. If this ever stops being true, the reason these names
     * live in the query string has gone away and can be reconsidered.
     */
    @Test
    void aSlashInAPathSegmentNeverReachesTheApplication() {
        ResponseEntity<String> response = put(
                "/api/categories/Gesundheit%20%2F%20Arzt%20Dieter",
                """
                {"name":"Gesundheit / Arzt Dieter","group":"Gesundheit / Arzt","pensionRelevant":true}
                """);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<String> put(String pathAndQuery, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(
                uri(pathAndQuery), HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
    }

    // java.net.URI rather than a template: the percent-encoding in the test is
    // the encoding the browser sends, and a template would encode it a second
    // time into %2520.
    private URI uri(String pathAndQuery) {
        return URI.create("http://localhost:" + port + pathAndQuery);
    }
}
