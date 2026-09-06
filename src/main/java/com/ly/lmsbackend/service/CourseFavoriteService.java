package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.model.CourseFavorite;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseFavoriteRepository;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class CourseFavoriteService {

    private final CourseFavoriteRepository favoriteRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    public CourseFavoriteService(
            CourseFavoriteRepository favoriteRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            CourseMapper courseMapper
    ) {
        this.favoriteRepository = favoriteRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional
    public CourseResponseDTO addFavorite(Long courseId, String userEmail) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (!favoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, courseId)) {
            CourseFavorite favorite = CourseFavorite.builder()
                    .user(user)
                    .course(course)
                    .build();
            favoriteRepository.save(favorite);
        }

        return courseMapper.toDTO(course, true);
    }

    @Transactional
    public void removeFavorite(Long courseId, String userEmail) {
        if (!favoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, courseId)) {
            // If doesn't exist, no-op or throw NOT_FOUND
            return;
        }
        favoriteRepository.deleteByUser_EmailAndCourse_CourseId(userEmail, courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getFavorites(String userEmail) {
        List<CourseFavorite> favorites = favoriteRepository.findByUser_EmailOrderByCreatedAtDesc(userEmail);
        return favorites.stream()
                .map(f -> courseMapper.toDTO(f.getCourse(), true))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> isFavorite(Long courseId, String userEmail) {
        boolean fav = favoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, courseId);
        return Map.of("isFavorite", fav);
    }
}
