package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.domain.model.CategoryGroup;

/** API representation of a category group. */
public record CategoryGroupResponse(String name) {

    public static CategoryGroupResponse from(CategoryGroup group) {
        return new CategoryGroupResponse(group.name());
    }
}
