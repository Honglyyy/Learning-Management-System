package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.LessonMaterial;
import com.ly.lmsbackend.model.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {

    List<LessonMaterial> findByLesson_LessonId(Long lessonId);

    List<LessonMaterial> findByLesson(Lessons lesson);

    void deleteAllByLesson_LessonId(Long lessonId);
}
