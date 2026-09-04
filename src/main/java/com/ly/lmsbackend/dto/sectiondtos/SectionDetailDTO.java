package com.ly.lmsbackend.dto.sectiondtos;

import com.ly.lmsbackend.dto.lessondtos.LessonDetailDTO;

import java.util.List;

public record SectionDetailDTO(
        Long sectionId,
        String title,
        String duration,
        Long lessonCount,
        List<LessonDetailDTO> lessons
) {
}
