package com.ly.lmsbackend.repository.specification;

import com.ly.lmsbackend.model.Categories;
import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;
import com.ly.lmsbackend.model.Courses;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseSpecification {

    public static Specification<Courses> filterCourses(
            String query,
            Long categoryId,
            CourseLevel level,
            BigDecimal maxPrice,
            CourseStatus status
    ) {
        return (root, cq, cb) -> {
            // Ensure distinct results when joining
            if (cq != null) {
                cq.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // Status filter (defaults to PUBLISHED)
            CourseStatus resolvedStatus = (status != null) ? status : CourseStatus.PUBLISHED;
            if (resolvedStatus == CourseStatus.PUBLISHED) {
                predicates.add(cb.or(
                        cb.equal(root.get("status"), CourseStatus.PUBLISHED),
                        cb.isNull(root.get("status"))
                ));
            } else {
                predicates.add(cb.equal(root.get("status"), resolvedStatus));
            }

            // Keyword query: searches in title, description, instructor username, or instructor fullname
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);

                Predicate instructorMatch = cb.disjunction();
                if (root.get("instructor") != null) {
                    Predicate usernameMatch = cb.like(cb.lower(root.get("instructor").get("username")), pattern);
                    Predicate fullnameMatch = cb.like(cb.lower(root.get("instructor").get("fullname")), pattern);
                    instructorMatch = cb.or(usernameMatch, fullnameMatch);
                }

                predicates.add(cb.or(titleMatch, descMatch, instructorMatch));
            }

            // Category filter
            if (categoryId != null) {
                Join<Courses, Categories> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("categoryId"), categoryId));
            }

            // Level filter
            if (level != null && level != CourseLevel.ALL_LEVELS) {
                predicates.add(cb.equal(root.get("level"), level));
            }

            // Max price filter
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
