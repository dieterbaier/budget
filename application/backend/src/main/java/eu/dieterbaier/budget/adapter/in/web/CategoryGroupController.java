package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
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

/** Inbound HTTP adapter for category groups. Depends only on the inbound port. */
@RestController
@RequestMapping("/api/category-groups")
public class CategoryGroupController {

    private final ManageCategoryGroupsUseCase manageGroups;

    public CategoryGroupController(ManageCategoryGroupsUseCase manageGroups) {
        this.manageGroups = manageGroups;
    }

    @GetMapping
    public List<CategoryGroupResponse> list() {
        return manageGroups.list().stream().map(CategoryGroupResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryGroupResponse create(@Valid @RequestBody NameRequest request) {
        return CategoryGroupResponse.from(manageGroups.create(request.name()));
    }

    // The name is the identity (ADR-009), so it is what addresses the resource --
    // and a rename is a PUT to the old name carrying the new one. It travels in
    // the query string rather than in a path segment because a group name may
    // contain a slash, and a percent-encoded slash in a path segment is rejected
    // by Tomcat before the request arrives here (issue #82).
    @PutMapping
    public CategoryGroupResponse rename(@RequestParam String name, @Valid @RequestBody NameRequest request) {
        return CategoryGroupResponse.from(manageGroups.rename(name, request.name()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String name) {
        manageGroups.delete(name);
    }

    public record NameRequest(@NotBlank String name) {
    }
}
