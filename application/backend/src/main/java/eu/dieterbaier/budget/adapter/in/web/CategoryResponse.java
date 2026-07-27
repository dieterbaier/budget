package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.domain.model.Category;

/** API representation of a category. */
public record CategoryResponse(String name, String group, boolean pensionRelevant) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.name(), category.group().name(), category.pensionRelevant());
    }
}
