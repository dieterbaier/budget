package eu.dieterbaier.budget.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
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

import eu.dieterbaier.budget.application.port.in.ManageCategoryGroupsUseCase;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.domain.model.CategoryGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryGroupController.class)
@Import(ApiExceptionHandler.class)
class CategoryGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManageCategoryGroupsUseCase manageGroups;

    @Test
    void listsGroups() throws Exception {
        given(manageGroups.list()).willReturn(List.of(new CategoryGroup("Car"), new CategoryGroup("House")));

        mockMvc.perform(get("/api/category-groups"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"name":"Car"},{"name":"House"}]
                        """));
    }

    @Test
    void createsGroupAndReturns201() throws Exception {
        given(manageGroups.create(anyString())).willReturn(new CategoryGroup("House"));

        mockMvc.perform(post("/api/category-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"House"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"name":"House"}
                        """));
    }

    @Test
    void rejectsABlankName() throws Exception {
        mockMvc.perform(post("/api/category-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renamesThroughTheOldName() throws Exception {
        given(manageGroups.rename(eq("Huose"), anyString())).willReturn(new CategoryGroup("House"));

        mockMvc.perform(put("/api/category-groups/Huose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"House"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"name":"House"}
                        """));
    }

    @Test
    void deletesAnEmptyGroup() throws Exception {
        mockMvc.perform(delete("/api/category-groups/Empty")).andExpect(status().isNoContent());
    }

    @Test
    void reportsAGroupThatStillHoldsCategoriesAsAConflict() throws Exception {
        willThrow(new NameInUseException("\"House\" still holds 3 categories"))
                .given(manageGroups).delete(any());

        mockMvc.perform(delete("/api/category-groups/House"))
                .andExpect(status().isConflict())
                .andExpect(content().json("""
                        {"error":"\\"House\\" still holds 3 categories"}
                        """));
    }
}
