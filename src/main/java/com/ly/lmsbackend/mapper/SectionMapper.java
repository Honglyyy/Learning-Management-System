package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.sectiondtos.SectionCreateDTO;
import com.ly.lmsbackend.dto.sectiondtos.SectionResponseDTO;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Sections;
import org.springframework.stereotype.Component;

@Component
public class SectionMapper {
    public SectionResponseDTO toDto(
            Sections section
    ){
        return new SectionResponseDTO(
                section.getSectionId(),
                section.getTitle(),
                section.getDuration(),
                section.getCourse().getCourseId(),
                section.getCourse().getTitle()
        );
    }

    public Sections toEntity(
            SectionCreateDTO dto,
            Courses course
    ){
        Sections section = new Sections();
        section.setTitle(dto.title());
        section.setDuration(dto.duration());
        section.setCourse(course);

        return section;
    }
}
