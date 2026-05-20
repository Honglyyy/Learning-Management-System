package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.CategoryDetailDTO;
import com.ly.lmsbackend.dto.CategoryResponseDTO;
import com.ly.lmsbackend.dto.CourseDTO;
import com.ly.lmsbackend.model.Categories;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.repository.CategoryRepository;
import com.ly.lmsbackend.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final Categories categories;
    private final Courses courses;

    public CategoryService(CategoryRepository categoryRepository, CourseRepository courseRepository, Categories categories, Courses courses) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.categories = categories;
        this.courses = courses;
    }

    public List<CategoryResponseDTO> getAllCategories(){
        return categoryRepository.findAll().stream().map(c->new CategoryResponseDTO(c.getCategoryId(), c.getCategory())).toList();
    }

    public CategoryDetailDTO getCategory(Long id){

        Categories category = categoryRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Category id " + id + " is not found!!"));

        List<CourseDTO> dto = courseRepository.findByCategories_CategoryId(id)
                .stream()
                .map(courses->new CourseDTO(
                        courses.getCourseId(),
                        courses.getTitle(),
                        courses.getDescription(),
                        courses.getPrice(),
                        courses.getOverallDuration(),
                        courses.getCoverDir(),
                        courses.getInstructor().getUsername()
                ))
                .toList();

        return new CategoryDetailDTO(
                category.getCategoryId(),
                category.getCategory(),
                dto
        );
    }

    public Categories addCategory(Categories category){
        return categoryRepository.save(category);
    }

    public Categories updateCategory(Long id, Categories category){
        Categories existingCategory = new Categories();

        existingCategory.setCategoryId(id);
        existingCategory.setCategory(category.getCategory());

        existingCategory.setCategory(category.getCategory());

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id){
        categoryRepository.deleteById(id);
    }
}
