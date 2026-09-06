package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.service.CourseFavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class CourseFavoriteController {

    private final CourseFavoriteService favoriteService;

    public CourseFavoriteController(CourseFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getMyFavorites(Authentication authentication) {
        return ResponseEntity.ok(favoriteService.getFavorites(authentication.getName()));
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> addFavorite(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        CourseResponseDTO result = favoriteService.addFavorite(courseId, authentication.getName());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        favoriteService.removeFavorite(courseId, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Course removed from favorites successfully"));
    }

    @GetMapping("/check/{courseId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(favoriteService.isFavorite(courseId, authentication.getName()));
    }
}
