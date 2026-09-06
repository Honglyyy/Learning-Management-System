package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.LessonProgress;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByStudentAndLesson(Students student, Lessons lesson);

    Optional<LessonProgress> findByStudent_IdAndLesson_LessonId(Long studentId, Long lessonId);

    List<LessonProgress> findByStudentAndLessonIn(Students student, List<Lessons> lessons);

    List<LessonProgress> findByStudent_IdAndLesson_LessonIdIn(Long studentId, List<Long> lessonIds);

    void deleteAllByLesson_LessonId(Long lessonId);

    void deleteAllByStudent_Id(Long studentId);
}
