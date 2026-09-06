package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.lessondtos.LessonCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonResponseDTO;
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
                lesson.getSection() != null ? lesson.getSection().getSectionId() : null,
                lesson.getSection() != null ? lesson.getSection().getTitle() : null,
                lesson.getDescription(),
                lesson.getTextContent(),
                lesson.getOrderIndex(),
                lesson.getDuration(),
                lesson.getIsFree() != null ? lesson.getIsFree() : false
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
        lesson.setDescription(dto.description());
        lesson.setTextContent(dto.textContent());
        lesson.setOrderIndex(dto.orderIndex() != null ? dto.orderIndex() : 0);
        lesson.setDuration(dto.duration());
        lesson.setIsFree(dto.isFree() != null ? dto.isFree() : false);
        lesson.setSection(section);

        return lesson;
    }

}
