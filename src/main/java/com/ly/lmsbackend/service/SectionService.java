package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.sectiondtos.SectionCreateDTO;
import com.ly.lmsbackend.dto.sectiondtos.SectionResponseDTO;
import com.ly.lmsbackend.mapper.SectionMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public SectionService(SectionRepository sectionRepository, SectionMapper sectionMapper, CourseRepository courseRepository, CategoryRepository categoryRepository, LessonRepository lessonRepository, QuizRepository quizRepository, UserRepository userRepository, QuizAttemptRepository quizAttemptRepository) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.sectionMapper = sectionMapper;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.quizAttemptRepository = quizAttemptRepository;
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

        return sectionMapper.toDto(sectionRepository.save(existingSection));
    }

    @Transactional
    public void deleteSection(Long id){
        if (!sectionRepository.existsById(id)) {
            throw new RuntimeException("Section not found");
        }

        quizAttemptRepository.deleteAllBySectionId(id);
        sectionRepository.deleteById(id);
    }

    public List<SectionResponseDTO> getSectionByInstructor(String instructorEmail){
        return sectionRepository.findByInstructor_Email(instructorEmail)
                .stream().map(sectionMapper::toDto).toList();
    }

    public List<SectionResponseDTO> getSectionByInstructorAndCourse(String instructorEmail, Long courseId) {
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return sectionRepository.findByInstructor_EmailAndCourse_CourseId(instructorEmail, courseId)
                .stream()
                .map(sectionMapper::toDto)
                .toList();
    }

    public SectionResponseDTO addSectionByInstructor(
            SectionCreateDTO dto,
            String instructorEmail
    ) {

        Courses course = courseRepository
                .findById(dto.courseId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Course not found"
                        )
                );

        if (!course.getInstructor()
                .getEmail()
                .equals(instructorEmail)) {

            throw new AccessDeniedException(
                    "Unauthorized"
            );
        }

        Sections section = sectionMapper
                .toEntity(dto, course);

        section.setInstructor(
                course.getInstructor()
        );

        return sectionMapper.toDto(
                sectionRepository.save(section)
        );
    }

    @Transactional
    public void deleteMySection(
            Long id,
            String instructorEmail
    ) {

        Sections section = sectionRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Section not found"
                        )
                );

        if (!section.getInstructor()
                .getEmail()
                .equals(instructorEmail)) {

            throw new AccessDeniedException(
                    "Unauthorized"
            );
        }

        quizAttemptRepository.deleteAllBySectionId(id);
        sectionRepository.delete(section);
    }

    public SectionResponseDTO updateMySection(
            Long id,
            SectionCreateDTO dto,
            String instructorEmail
    ) {

        Sections section = sectionRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Section not found")
                );

        // SECURITY CHECK
        if (!section.getInstructor()
                .getEmail()
                .equals(instructorEmail)) {

            throw new AccessDeniedException("Unauthorized");
        }

        // UPDATE FIELDS
        section.setTitle(dto.title());
        section.setDuration(dto.duration());

        Sections updated = sectionRepository.save(section);

        return sectionMapper.toDto(updated);
    }
}
