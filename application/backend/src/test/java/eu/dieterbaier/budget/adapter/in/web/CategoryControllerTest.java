package eu.dieterbaier.budget.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.dieterbaier.budget.application.port.in.DuplicateNameException;
import eu.dieterbaier.budget.application.port.in.ManageCategoriesUseCase;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.in.UnknownNameException;
import eu.dieterbaier.budget.domain.model.Category;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@Import(ApiExceptionHandler.class)
class CategoryControllerTest {

    private static final CategoryGroup HOUSE = new CategoryGroup("House");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManageCategoriesUseCase manageCategories;

    @Test
    void listsCategoriesWithTheirGroup() throws Exception {
        given(manageCategories.list()).willReturn(List.of(Category.in(HOUSE, "Groceries")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"name":"Groceries","group":"House","pensionRelevant":true}]
                        """));
    }

    @Test
    void createsCategoryAndReturns201() throws Exception {
        given(manageCategories.create(anyString(), anyString(), anyBoolean()))
                .willReturn(Category.in(HOUSE, "Groceries"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Groceries","group":"House","pensionRelevant":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"name":"Groceries","group":"House"}
                        """));
    }

    @Test
    void rejectsABlankName() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  ","group":"House","pensionRelevant":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    // The name is the identity, so the old name addresses the resource and the
    // body carries the new one.
    @Test
    void renamesThroughTheOldName() throws Exception {
        given(manageCategories.update(eq("Grocries"), anyString(), anyString(), anyBoolean()))
                .willReturn(Category.in(HOUSE, "Groceries"));

        mockMvc.perform(put("/api/categories/Grocries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Groceries","group":"House","pensionRelevant":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"name":"Groceries"}
                        """));
    }

    @Test
    void deletesAnUnusedCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/Mistake")).andExpect(status().isNoContent());
    }

    // 409 rather than 400: the request is well formed and conflicts with data
    // that exists. The message has to say what is in the way.
    @Test
    void reportsADuplicateNameAsAConflict() throws Exception {
        given(manageCategories.create(anyString(), anyString(), anyBoolean()))
                .willThrow(new DuplicateNameException("category", "Groceries"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Groceries","group":"House","pensionRelevant":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {"error":"A category named \\"Groceries\\" already exists"}
                        """));
    }

    @Test
    void reportsACategoryStillInUseAsAConflict() throws Exception {
        willThrow(new NameInUseException("\"Groceries\" is still used by 42 transactions"))
                .given(manageCategories).delete(any());

        mockMvc.perform(delete("/api/categories/Groceries"))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {"error":"\\"Groceries\\" is still used by 42 transactions"}
                        """));
    }

    @Test
    void reportsAnUnknownCategoryAsNotFound() throws Exception {
        willThrow(new UnknownNameException("category", "Nope")).given(manageCategories).delete(any());

        mockMvc.perform(delete("/api/categories/Nope")).andExpect(status().isNotFound());
    }
}
