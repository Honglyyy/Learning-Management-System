package com.ly.lmsbackend;

import com.ly.lmsbackend.controller.CourseController;
import com.ly.lmsbackend.dto.coursedtos.CourseCreateDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;
import com.ly.lmsbackend.service.CourseService;
import com.ly.lmsbackend.service.SectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseDeserializationTests {

    @Test
    @DisplayName("Should successfully deserialize payload with String instructor username and numeric instructorId")
    void testDeserializeWithUsernameAndInstructorId() throws Exception {
        CourseService courseService = Mockito.mock(CourseService.class);
        SectionService sectionService = Mockito.mock(SectionService.class);
        CourseController controller = new CourseController(courseService, sectionService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(courseService.updateCourse(eq(5L), any(CourseCreateDTO.class)))
                .thenReturn(new CourseResponseDTO(5L, "C#", "Desc", BigDecimal.valueOf(100), "2 Months",
                        "cover.png", "pubId", 16L, "adorablie_", List.of(1L), List.of("backend"), 5.0));

        String jsonPayload = """
                {
                    "title": "C#",
                    "description": "Beginner backend development with C# and the .NET framework.",
                    "price": 100,
                    "overallDuration": "2 Months",
                    "coverUrl": "LmpwZw.png",
                    "coverPublicId": "pub123",
                    "instructor": "adorablie_",
                    "instructorId": 16,
                    "categoryId": [1],
                    "level": "BEGINNER",
                    "status": "PUBLISHED"
                }
                """;

        mockMvc.perform(put("/api/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        ArgumentCaptor<CourseCreateDTO> captor = ArgumentCaptor.forClass(CourseCreateDTO.class);
        verify(courseService).updateCourse(eq(5L), captor.capture());

        CourseCreateDTO captured = captor.getValue();
        assertEquals("C#", captured.title());
        assertEquals(16L, captured.instructor());
        assertEquals("adorablie_", captured.instructorUsername());
        assertEquals(List.of(1L), captured.categoryId());
        assertEquals(CourseLevel.BEGINNER, captured.level());
        assertEquals(CourseStatus.PUBLISHED, captured.status());
    }

    @Test
    @DisplayName("Should successfully deserialize payload with only String instructor username")
    void testDeserializeWithOnlyUsername() throws Exception {
        CourseService courseService = Mockito.mock(CourseService.class);
        SectionService sectionService = Mockito.mock(SectionService.class);
        CourseController controller = new CourseController(courseService, sectionService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String jsonPayload = """
                {
                    "title": "C#",
                    "description": "Beginner backend development with C# and the .NET framework.",
                    "price": 100,
                    "instructor": "adorablie_",
                    "categoryIds": [2]
                }
                """;

        mockMvc.perform(put("/api/courses/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        ArgumentCaptor<CourseCreateDTO> captor = ArgumentCaptor.forClass(CourseCreateDTO.class);
        verify(courseService).updateCourse(eq(5L), captor.capture());

        CourseCreateDTO captured = captor.getValue();
        assertNull(captured.instructor());
        assertEquals("adorablie_", captured.instructorUsername());
        assertEquals(List.of(2L), captured.categoryId());
    }
}
