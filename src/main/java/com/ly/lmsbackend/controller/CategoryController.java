package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.categorydtos.CategoryDetailDTO;
import com.ly.lmsbackend.dto.categorydtos.CategoryResponseDTO;
import com.ly.lmsbackend.model.Categories;
import com.ly.lmsbackend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<CategoryDetailDTO> getCategory(@PathVariable Long id){
        return new ResponseEntity<>(categoryService.getCategory(id),HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/categories")
    public ResponseEntity<Categories> addCategory(@RequestBody Categories category){
        return new ResponseEntity<>(categoryService.addCategory(category),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/categories/{id}")
    public ResponseEntity<Categories> updateCategory(@PathVariable Long id,@RequestBody Categories categories){
        return new ResponseEntity<>(categoryService.updateCategory(id,categories), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return new ResponseEntity<>("Category id " + id + " has now deleted!!!",HttpStatus.OK);
    }
}
