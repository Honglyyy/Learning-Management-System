package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.LessonCreateDTO;
import com.ly.lmsbackend.dto.LessonResponseDTO;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Sections;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonResponseDTO toDto(Lessons lesson){
        return new LessonResponseDTO(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getVideoUrl(),
                lesson.getVideoPublicId(),
                lesson.getSection().getSectionId(),
                lesson.getSection().getTitle()
        );
    }

    public Lessons toEntity(
            LessonCreateDTO dto,
            Sections section
    ){
        Lessons lesson = new Lessons();

        lesson.setTitle(dto.title());
        lesson.setVideoUrl(dto.videoUrl());
        lesson.setVideoPublicId(dto.videoPublicId());
        lesson.setSection(section);

        return lesson;
    }

}
