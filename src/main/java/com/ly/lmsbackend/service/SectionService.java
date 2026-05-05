package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.*;
import com.ly.lmsbackend.mapper.SectionMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final Quizzes quizzes;

    public SectionService(SectionRepository sectionRepository, SectionMapper sectionMapper, CourseRepository courseRepository, CategoryRepository categoryRepository, LessonRepository lessonRepository, QuizRepository quizRepository, Quizzes quizzes) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.sectionMapper = sectionMapper;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.quizzes = quizzes;
    }

    public List<SectionResponseDTO> getSections(){
        return sectionRepository.findAll()
                .stream()
                .map(sectionMapper::toDto)
                .toList();
    }

    public SectionResponseDTO addSection(SectionCreateDTO dto) {
        Sections section = new Sections();

        Courses courses = courseRepository.findById(dto.courseId())
                .orElseThrow(()->new RuntimeException("Course id "+ dto.courseId() + " is not found!!"));

        section = sectionMapper.toEntity(dto,courses);

        return sectionMapper.toDto(sectionRepository.save(section));
    }

    public SectionResponseDTO updateSection(Long id, SectionCreateDTO dto){
        Sections existingSection = sectionRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("s"));

        Courses courseId = courseRepository.findById(dto.courseId())
                .orElseThrow(()->new RuntimeException("Course id "+ dto.courseId() + " is not found!!"));


        existingSection.setTitle(dto.title());
        existingSection.setDuration(dto.duration());
        existingSection.setCourse(courseId);

        return sectionMapper.toDto(existingSection);
    }

    public void deleteSection(Long id){
        sectionRepository.deleteById(id);
    }



//    public SectionDetailDTO getSection(Long id){
//        Sections section = sectionRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Section id " + id + " not found"));
//
//        List<LessonDetailDTO> lessonDetailDTOS = lessonRepository.findBySections_SectionId(id)
//                .stream()
//                .map(lesson -> new LessonDetailDTO(
//                        lesson.getLessonId(),
//                        lesson.getTitle(),
//                        lesson.getVideoDir()
//                )).toList();
//
//        return new SectionDetailDTO(
//                section.getSectionId(),
//                section.getTitle(),
//                section.getDuration(),
//                (long) lessonDetailDTOS.size(),
//                lessonDetailDTOS
//        );
//    }
}
