package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Inbound HTTP adapter for categories. Depends only on the inbound port. */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ManageCategoriesUseCase manageCategories;

    public CategoryController(ManageCategoriesUseCase manageCategories) {
        this.manageCategories = manageCategories;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return manageCategories.list().stream().map(CategoryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(
                manageCategories.create(request.name(), request.group(), request.pensionRelevant()));
    }

    // The name addresses the resource, and it travels in the query string rather
    // than in a path segment because a category name may contain a slash --
    // "Gesundheit / Arzt Dieter" is one the owner keeps. Percent-encoded into a
    // path segment that is a %2F, which Tomcat rejects with a 400 before the
    // request reaches this method. A query string has no segment structure to
    // violate (issue #82).
    @PutMapping
    public CategoryResponse update(@RequestParam String name, @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(manageCategories.update(
                name, request.name(), request.group(), request.pensionRelevant()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String name) {
        manageCategories.delete(name);
    }

    public record CategoryRequest(
            @NotBlank String name,
            @NotBlank String group,
            boolean pensionRelevant) {
    }
}
