--
-- PostgreSQL database dump
--


-- Dumped from database version 16.15 (Ubuntu 16.15-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 18.6 (Ubuntu 18.6-1.pgdg24.04+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users (id, created_at, email, fullname, is_verified, otp, password, phone_number, reset_otp, reset_otp_expire_at, role, updated_at, user_id, username, verify_otp_expire_at) VALUES (1, '2026-09-27 13:23:46.175481', 'admin1234@test.com', NULL, true, NULL, '$2a$10$lWMwNmn0X8FsfYeXSFG1B..gZhPgYTRhLiryCTM6CjWxPHI0TRk5y', NULL, NULL, NULL, 'ADMIN', '2026-09-27 13:23:46.175524', NULL, 'admin', NULL);
INSERT INTO public.users (id, created_at, email, fullname, is_verified, otp, password, phone_number, reset_otp, reset_otp_expire_at, role, updated_at, user_id, username, verify_otp_expire_at) VALUES (2, '2026-09-27 13:49:07.359032', 'honglylay14@gmail.com', 'Lay Hongly', true, NULL, '$2a$10$IFY.lfTGFXJE4BllCo6uXOA0IQX3jJU0YhPT9WmhnjEkZrIoNC9pW', '069339084', NULL, NULL, 'INSTRUCTOR', '2026-09-27 13:49:29.544984', '887d0629-4ea7-431f-ba22-31f6fdc0fcd6', 'Hongly Lay', 0);
INSERT INTO public.users (id, created_at, email, fullname, is_verified, otp, password, phone_number, reset_otp, reset_otp_expire_at, role, updated_at, user_id, username, verify_otp_expire_at) VALUES (3, '2026-09-27 15:03:21.459226', 'honglylay18@gmail.com', 'Lay Honglyy', true, NULL, '$2a$10$IJw1ec2gp/JCjhr5BdMQ0O2g56t.E3/V8KUlrZbToI7hVI8JD1tCK', '069339084', NULL, NULL, 'STUDENT', '2026-09-27 15:03:38.090498', '8446e643-1c39-4e39-9ac0-bc065ae218df', 'Honglyy', 0);


--
-- Data for Name: activity_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (1, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 13:26:42.676255', 1);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (2, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 13:49:36.813937', 2);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (3, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 14:31:24.83391', 2);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (4, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 14:33:51.57079', 1);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (5, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 14:34:11.058703', 1);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (6, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 14:35:41.515508', 2);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (7, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 15:03:50.168498', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (8, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 15:04:43.183154', 1);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (9, 'ENROLLED', 'Enrolled in course: Django: Complete Python Web Development & REST Framework', '2026-09-27 15:05:01.705852', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (10, 'LESSON_COMPLETED', 'Completed lesson: What is Django in Django: Complete Python Web Development & REST Framework', '2026-09-27 15:05:14.353187', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (11, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:29:45.167663', 2);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (12, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:30:46.349864', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (13, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:40:41.344547', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (14, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:42:19.039519', 2);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (15, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:42:39.374406', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (16, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:43:01.264841', 1);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (17, 'ENROLLED', 'Enrolled in course: Spring Boot: Full-Stack Web Development & Microservices (Access valid for 1 days)', '2026-09-27 21:43:45.796776', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (18, 'USER_LOGIN', 'User logged in to LMS', '2026-09-27 21:44:46.583807', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (19, 'USER_LOGIN', 'User logged in to LMS', '2026-09-28 09:06:50.064833', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (20, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 08:13:34.575414', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (21, 'USER_LOGOUT', 'User logged out of LMS', '2026-09-29 08:15:12.371286', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (22, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 19:32:34.696064', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (23, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 19:37:36.573527', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (24, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 19:45:19.533802', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (25, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 20:00:41.276882', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (26, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 20:07:23.309736', 3);
INSERT INTO public.activity_logs (id, activity_type, description, "timestamp", user_id) VALUES (27, 'USER_LOGIN', 'User logged in to LMS', '2026-09-29 20:15:10.069743', 2);


--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.courses (course_id, cover_public_id, cover_url, created_at, created_by, description, learning_outcomes, level, overall_duration, price, requirements, status, title, updated_at, updated_by, instructor_id, access_duration_days) VALUES (1, 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/5e76cb37-0cb2-42ce-a25f-d05604fea7cd', 'https://res.cloudinary.com/lyxxfhot/image/upload/v1790495340/course/hongly-lay/spring-boot-full-stack-web-development-microservices/5e76cb37-0cb2-42ce-a25f-d05604fea7cd.webp', '2026-09-27 14:49:56.242786', 'honglylay14@gmail.com', 'Master modern Java web development with Spring Boot 3. Build robust REST APIs, configure MySQL database with Spring Data JPA, secure applications with Spring Security, and deploy containers with Docker.', '', 'ALL_LEVELS', '6 hours', 49.99, '', 'PUBLISHED', 'Spring Boot: Full-Stack Web Development & Microservices', '2026-09-27 21:42:26.523819', 'honglylay14@gmail.com', 2, 1);
INSERT INTO public.courses (course_id, cover_public_id, cover_url, created_at, created_by, description, learning_outcomes, level, overall_duration, price, requirements, status, title, updated_at, updated_by, instructor_id, access_duration_days) VALUES (2, 'course/hongly-lay/django-complete-python-web-development-rest-framework/eff821c5-7430-4919-94eb-293132b29f67', 'https://res.cloudinary.com/lyxxfhot/image/upload/v1790495465/course/hongly-lay/django-complete-python-web-development-rest-framework/eff821c5-7430-4919-94eb-293132b29f67.png', '2026-09-27 14:51:30.821239', 'honglylay14@gmail.com', 'Learn modern Python web development with Django from scratch. Master the MVT architecture, Django ORM, built-in Admin Panel, Django REST Framework (DRF) APIs, and complete user authentication.', 'Understand Django architecture, apps structure, and request-response lifecycle
Build data models and execute queries using the Django ORM
Leverage the power of Django''s built-in Admin Panel for content management
Develop production-ready RESTful APIs with Django REST Framework (DRF)
Implement secure user authentication and authorization in Django', 'ALL_LEVELS', '5 hours', 39.99, 'Basic Python programming knowledge (functions, classes, modules)
Basic familiarity with HTML, HTTP, and databases', 'PUBLISHED', 'Django: Complete Python Web Development & REST Framework', '2026-09-27 14:51:30.821239', 'honglylay14@gmail.com', 2, 1);


--
-- Data for Name: sections; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (1, '2026-09-27 14:51:41.485066', 'honglylay14@gmail.com', '30m', 'Section 1 - Introduction', '2026-09-27 14:51:41.485066', 'honglylay14@gmail.com', 1, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (2, '2026-09-27 14:51:41.506535', 'honglylay14@gmail.com', '45m', 'Section 2 - Getting Started', '2026-09-27 14:51:41.506535', 'honglylay14@gmail.com', 1, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (3, '2026-09-27 14:51:41.530975', 'honglylay14@gmail.com', '1h', 'Section 3 - REST APIs', '2026-09-27 14:51:41.530975', 'honglylay14@gmail.com', 1, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (4, '2026-09-27 14:51:41.552091', 'honglylay14@gmail.com', '1h 15m', 'Section 4 - Database', '2026-09-27 14:51:41.552091', 'honglylay14@gmail.com', 1, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (5, '2026-09-27 14:51:41.570404', 'honglylay14@gmail.com', '1h', 'Section 5 - Security', '2026-09-27 14:51:41.570404', 'honglylay14@gmail.com', 1, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (6, '2026-09-27 14:51:41.587908', 'honglylay14@gmail.com', '1h 30m', 'Section 6 - Deployment', '2026-09-27 14:51:41.587908', 'honglylay14@gmail.com', 1, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (13, '2026-09-27 14:52:36.258374', 'honglylay14@gmail.com', '30m', 'Section 1 - Introduction', '2026-09-27 14:52:36.258374', 'honglylay14@gmail.com', 2, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (14, '2026-09-27 14:52:36.280824', 'honglylay14@gmail.com', '45m', 'Section 2 - Getting Started', '2026-09-27 14:52:36.280824', 'honglylay14@gmail.com', 2, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (15, '2026-09-27 14:52:36.297847', 'honglylay14@gmail.com', '1h', 'Section 3 - Database', '2026-09-27 14:52:36.297847', 'honglylay14@gmail.com', 2, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (16, '2026-09-27 14:52:36.319879', 'honglylay14@gmail.com', '45m', 'Section 4 - Admin', '2026-09-27 14:52:36.319879', 'honglylay14@gmail.com', 2, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (17, '2026-09-27 14:52:36.337661', 'honglylay14@gmail.com', '1h', 'Section 5 - REST API', '2026-09-27 14:52:36.337661', 'honglylay14@gmail.com', 2, 2);
INSERT INTO public.sections (section_id, created_at, created_by, section_duration, section_title, updated_at, updated_by, course_id, instructor_id) VALUES (18, '2026-09-27 14:52:36.355257', 'honglylay14@gmail.com', '1h', 'Section 6 - Auth', '2026-09-27 14:52:36.355257', 'honglylay14@gmail.com', 2, 2);


--
-- Data for Name: lessons; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (1, '2026-09-27 14:53:23.79523', 'honglylay14@gmail.com', 'Learn the fundamentals of Spring Boot, how it simplifies Java development with auto-configuration, and its key architectural components.', '10:00', true, 1, 'Spring Boot is an opinionated framework that simplifies the setup and configuration of Spring-based applications. It embeds Tomcat by default and uses classpath scanning to auto-configure beans.', 'What is Spring Boot?', '2026-09-27 14:53:23.79523', 'honglylay14@gmail.com', 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/what-is-spring-boot/5c494a4a-98d3-4db7-8297-ed62b9f31ff5', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495591/course/hongly-lay/spring-boot-full-stack-web-development-microservices/what-is-spring-boot/5c494a4a-98d3-4db7-8297-ed62b9f31ff5.mp4', 2, 1);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (2, '2026-09-27 14:54:56.458926', 'honglylay14@gmail.com', 'Initialize and set up a production-ready Spring Boot project using Spring Initializr, Maven dependencies, and application properties.', '12:00', false, 1, 'Spring Initializr provides starter dependencies such as spring-boot-starter-web, spring-boot-starter-data-jpa, and Lombok.', 'Setting Up a Spring Boot Project', '2026-09-27 14:54:56.458926', 'honglylay14@gmail.com', 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/setting-up-a-spring-boot-project/6f209c49-0123-499b-b4bb-db0ba4d08b42', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495687/course/hongly-lay/spring-boot-full-stack-web-development-microservices/setting-up-a-spring-boot-project/6f209c49-0123-499b-b4bb-db0ba4d08b42.mp4', 2, 2);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (3, '2026-09-27 14:55:22.134152', 'honglylay14@gmail.com', 'Learn how to build RESTful web services using @RestController, @GetMapping, @PostMapping, and JSON serialization.', '15:00', false, 1, 'Use @RestController to combine @Controller and @ResponseBody. Standard HTTP methods include GET, POST, PUT, DELETE, and PATCH.', 'Creating Your First REST Controller', '2026-09-27 14:55:22.134152', 'honglylay14@gmail.com', 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/creating-your-first-rest-controller/c08327e0-af73-4739-81cf-29162df6fd74', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495712/course/hongly-lay/spring-boot-full-stack-web-development-microservices/creating-your-first-rest-controller/c08327e0-af73-4739-81cf-29162df6fd74.mp4', 2, 3);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (4, '2026-09-27 14:55:48.68148', 'honglylay14@gmail.com', 'Connect Spring Boot to MySQL database using Spring Data JPA, Hibernate ORM, and configure Datasource credentials.', '18:00', false, 1, 'Configure spring.datasource.url, username, password, and spring.jpa.hibernate.ddl-auto=update in application.properties.', 'Spring Boot and MySQL Connection', '2026-09-27 14:55:48.68148', 'honglylay14@gmail.com', 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/spring-boot-and-mysql-connection/1a1a15e4-fa14-4c2c-8806-61e668f13f40', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495742/course/hongly-lay/spring-boot-full-stack-web-development-microservices/spring-boot-and-mysql-connection/1a1a15e4-fa14-4c2c-8806-61e668f13f40.mp4', 2, 4);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (5, '2026-09-27 14:56:17.560329', 'honglylay14@gmail.com', 'Secure your endpoints with Spring Security filter chain, password hashing with BCrypt, and role-based access control.', '15:00', false, 1, 'SecurityFilterChain bean defines authorization rules for different route matchers and authentication providers.', 'Spring Security in 15 Seconds', '2026-09-27 14:56:17.560329', 'honglylay14@gmail.com', 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/spring-security-in-15-seconds/87ff0a8f-241b-4de2-9d13-50ab3c2aeade', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495770/course/hongly-lay/spring-boot-full-stack-web-development-microservices/spring-security-in-15-seconds/87ff0a8f-241b-4de2-9d13-50ab3c2aeade.mp4', 2, 5);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (6, '2026-09-27 14:56:50.818488', 'honglylay14@gmail.com', 'Package Spring Boot application as an executable JAR and build optimized multi-stage Docker container images.', '20:00', false, 1, 'Dockerfile uses eclipse-temurin:21-jre-alpine base image for minimal production image footprint.', 'Deploying Spring Boot to Docker', '2026-09-27 14:56:50.818488', 'honglylay14@gmail.com', 'course/hongly-lay/spring-boot-full-stack-web-development-microservices/deploying-spring-boot-to-docker/718f8770-0541-4aec-a92d-0c73c34de244', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495799/course/hongly-lay/spring-boot-full-stack-web-development-microservices/deploying-spring-boot-to-docker/718f8770-0541-4aec-a92d-0c73c34de244.mp4', 2, 6);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (7, '2026-09-27 14:57:12.13706', 'honglylay14@gmail.com', 'Introduction to Python''s batteries-included web framework, its philosophy, and MVT pattern.', '10:00', true, 1, 'Django follows the Model-View-Template architecture and provides built-in admin, auth, and ORM.', 'What is Django', '2026-09-27 14:57:12.13706', 'honglylay14@gmail.com', 'course/hongly-lay/django-complete-python-web-development-rest-framework/what-is-django/fcfe101e-b511-44aa-9b3c-64c92db6b116', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495827/course/hongly-lay/django-complete-python-web-development-rest-framework/what-is-django/fcfe101e-b511-44aa-9b3c-64c92db6b116.mp4', 2, 13);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (8, '2026-09-27 14:57:42.037595', 'honglylay14@gmail.com', 'Set up a virtual environment, install Django, run django-admin startproject and startapp, and configure settings.py.', '12:00', false, 1, 'Use python -m venv venv, pip install django, django-admin startproject config ., and python manage.py runserver.', 'Django Project Setup', '2026-09-27 14:57:42.037595', 'honglylay14@gmail.com', 'course/hongly-lay/django-complete-python-web-development-rest-framework/django-project-setup/e2d52b6b-daa5-417e-ab05-19b949295150', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495856/course/hongly-lay/django-complete-python-web-development-rest-framework/django-project-setup/e2d52b6b-daa5-417e-ab05-19b949295150.mp4', 2, 14);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (9, '2026-09-27 14:58:05.520192', 'honglylay14@gmail.com', 'Define data models using django.db.models, run makemigrations and migrate, and perform CRUD with the QuerySet API.', '15:00', false, 1, 'Django ORM translates Python classes into relational database tables and queries.', 'Django Models and ORM', '2026-09-27 14:58:05.520192', 'honglylay14@gmail.com', 'course/hongly-lay/django-complete-python-web-development-rest-framework/django-models-and-orm/3c340856-e2aa-48d1-b91a-a2f3bf345df4', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495879/course/hongly-lay/django-complete-python-web-development-rest-framework/django-models-and-orm/3c340856-e2aa-48d1-b91a-a2f3bf345df4.mp4', 2, 15);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (10, '2026-09-27 14:58:44.196339', 'honglylay14@gmail.com', 'Create a superuser and register models in admin.py with custom list_display, search_fields, and list_filter.', '15:00', false, 1, 'Use python manage.py createsuperuser and @admin.register(MyModel) in admin.py.', 'Django Admin Panel', '2026-09-27 14:58:44.196339', 'honglylay14@gmail.com', 'course/hongly-lay/django-complete-python-web-development-rest-framework/django-admin-panel/1da1cefb-09c8-42ec-a3d6-03b002fadc83', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495921/course/hongly-lay/django-complete-python-web-development-rest-framework/django-admin-panel/1da1cefb-09c8-42ec-a3d6-03b002fadc83.mp4', 2, 16);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (11, '2026-09-27 14:59:13.56199', 'honglylay14@gmail.com', 'Build RESTful APIs with Django REST Framework (DRF) serializers, APIViews, ModelViewSet, and DefaultRouter.', '18:00', false, 1, 'Install djangorestframework, add rest_framework to INSTALLED_APPS, create ModelSerializer, and register ViewSets.', 'Django REST Framework', '2026-09-27 14:59:13.56199', 'honglylay14@gmail.com', 'course/hongly-lay/django-complete-python-web-development-rest-framework/django-rest-framework/d7213368-c000-47e9-a61f-14a44afdc4ef', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495947/course/hongly-lay/django-complete-python-web-development-rest-framework/django-rest-framework/d7213368-c000-47e9-a61f-14a44afdc4ef.mp4', 2, 17);
INSERT INTO public.lessons (lesson_id, created_at, created_by, description, duration, is_free, order_index, text_content, lesson_title, updated_at, updated_by, video_public_id, video_url, instructor_id, section_id) VALUES (12, '2026-09-27 14:59:57.821619', 'honglylay14@gmail.com', 'Implement user authentication, registration, login, logout, and token authentication or JWT in Django.', '16:00', false, 1, 'Use django.contrib.auth for session auth or rest_framework_simplejwt for JWT token authentication.', 'Django Authentication', '2026-09-27 14:59:57.821619', 'honglylay14@gmail.com', 'course/hongly-lay/django-complete-python-web-development-rest-framework/django-authentication/761d1d7c-4fe9-4a3e-b4a4-efd9e2b5ddbe', 'https://res.cloudinary.com/lyxxfhot/video/upload/v1790495994/course/hongly-lay/django-complete-python-web-development-rest-framework/django-authentication/761d1d7c-4fe9-4a3e-b4a4-efd9e2b5ddbe.mp4', 2, 18);


--
-- Data for Name: quizzes; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.quizzes (quiz_id, created_at, created_by, quiz_title, total_point, updated_at, updated_by, instructor_id, lesson_id) VALUES (1, '2026-09-27 15:00:23.525089', 'honglylay14@gmail.com', 'Spring Boot Fundamentals Quiz', 50, '2026-09-27 15:00:23.525089', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.quizzes (quiz_id, created_at, created_by, quiz_title, total_point, updated_at, updated_by, instructor_id, lesson_id) VALUES (2, '2026-09-27 15:00:24.05787', 'honglylay14@gmail.com', 'Django Fundamentals Quiz', 50, '2026-09-27 15:00:24.05787', 'honglylay14@gmail.com', NULL, 7);


--
-- Data for Name: questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (1, '2026-09-27 15:00:23.557692', 'honglylay14@gmail.com', 10, 'What is the primary purpose of Spring Boot?', '2026-09-27 15:00:23.557692', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (2, '2026-09-27 15:00:23.667347', 'honglylay14@gmail.com', 10, 'Which of the following is a key feature of Spring Boot?', '2026-09-27 15:00:23.667347', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (3, '2026-09-27 15:00:23.770052', 'honglylay14@gmail.com', 10, 'What embedded server does Spring Boot include by default?', '2026-09-27 15:00:23.770052', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (4, '2026-09-27 15:00:23.868466', 'honglylay14@gmail.com', 10, 'Which annotation marks the main entry point of a Spring Boot application?', '2026-09-27 15:00:23.868466', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (5, '2026-09-27 15:00:23.96362', 'honglylay14@gmail.com', 10, 'Spring Boot is built on top of which framework?', '2026-09-27 15:00:23.96362', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (6, '2026-09-27 15:00:24.074699', 'honglylay14@gmail.com', 10, 'Django is a framework for which programming language?', '2026-09-27 15:00:24.074699', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (7, '2026-09-27 15:00:24.166939', 'honglylay14@gmail.com', 10, 'What architectural pattern does Django follow?', '2026-09-27 15:00:24.166939', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (8, '2026-09-27 15:00:24.251071', 'honglylay14@gmail.com', 10, 'What does ''batteries included'' mean in the context of Django?', '2026-09-27 15:00:24.251071', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (9, '2026-09-27 15:00:24.337152', 'honglylay14@gmail.com', 10, 'Which company originally created Django?', '2026-09-27 15:00:24.337152', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.questions (question_id, created_at, created_by, point, question_text, updated_at, updated_by, instructor_id, quiz_id) VALUES (10, '2026-09-27 15:00:24.418471', 'honglylay14@gmail.com', 10, 'What is Django''s built-in admin interface used for?', '2026-09-27 15:00:24.418471', 'honglylay14@gmail.com', NULL, 2);


--
-- Data for Name: answers; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (1, 'To replace the Java programming language', '2026-09-27 15:00:23.586682', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.586682', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (2, 'To simplify the setup and development of Spring applications', '2026-09-27 15:00:23.609128', 'honglylay14@gmail.com', true, '2026-09-27 15:00:23.609128', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (3, 'To provide a database management system', '2026-09-27 15:00:23.629063', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.629063', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (4, 'To create mobile applications', '2026-09-27 15:00:23.648373', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.648373', 'honglylay14@gmail.com', NULL, 1);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (5, 'Manual XML configuration required', '2026-09-27 15:00:23.687501', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.687501', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (6, 'Must deploy to external application server only', '2026-09-27 15:00:23.709634', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.709634', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (7, 'Auto-configuration based on classpath', '2026-09-27 15:00:23.7312', 'honglylay14@gmail.com', true, '2026-09-27 15:00:23.7312', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (8, 'Requires Java EE container', '2026-09-27 15:00:23.750971', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.750971', 'honglylay14@gmail.com', NULL, 2);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (9, 'JBoss', '2026-09-27 15:00:23.788471', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.788471', 'honglylay14@gmail.com', NULL, 3);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (10, 'WebLogic', '2026-09-27 15:00:23.80888', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.80888', 'honglylay14@gmail.com', NULL, 3);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (11, 'GlassFish', '2026-09-27 15:00:23.828473', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.828473', 'honglylay14@gmail.com', NULL, 3);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (12, 'Tomcat', '2026-09-27 15:00:23.848793', 'honglylay14@gmail.com', true, '2026-09-27 15:00:23.848793', 'honglylay14@gmail.com', NULL, 3);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (13, '@SpringApplication', '2026-09-27 15:00:23.889035', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.889035', 'honglylay14@gmail.com', NULL, 4);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (14, '@EnableSpring', '2026-09-27 15:00:23.908834', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.908834', 'honglylay14@gmail.com', NULL, 4);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (15, '@SpringBootApplication', '2026-09-27 15:00:23.928517', 'honglylay14@gmail.com', true, '2026-09-27 15:00:23.928517', 'honglylay14@gmail.com', NULL, 4);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (16, '@BootApplication', '2026-09-27 15:00:23.946528', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.946528', 'honglylay14@gmail.com', NULL, 4);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (17, 'Hibernate', '2026-09-27 15:00:23.981901', 'honglylay14@gmail.com', false, '2026-09-27 15:00:23.981901', 'honglylay14@gmail.com', NULL, 5);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (18, 'Spring Framework', '2026-09-27 15:00:23.999351', 'honglylay14@gmail.com', true, '2026-09-27 15:00:23.999351', 'honglylay14@gmail.com', NULL, 5);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (19, 'Struts', '2026-09-27 15:00:24.018942', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.018942', 'honglylay14@gmail.com', NULL, 5);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (20, 'Jakarta EE', '2026-09-27 15:00:24.03903', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.03903', 'honglylay14@gmail.com', NULL, 5);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (21, 'Ruby', '2026-09-27 15:00:24.091502', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.091502', 'honglylay14@gmail.com', NULL, 6);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (22, 'JavaScript', '2026-09-27 15:00:24.11017', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.11017', 'honglylay14@gmail.com', NULL, 6);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (23, 'Python', '2026-09-27 15:00:24.128014', 'honglylay14@gmail.com', true, '2026-09-27 15:00:24.128014', 'honglylay14@gmail.com', NULL, 6);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (24, 'Go', '2026-09-27 15:00:24.148768', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.148768', 'honglylay14@gmail.com', NULL, 6);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (25, 'MVC (Model-View-Controller)', '2026-09-27 15:00:24.185464', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.185464', 'honglylay14@gmail.com', NULL, 7);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (26, 'MVP (Model-View-Presenter)', '2026-09-27 15:00:24.205099', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.205099', 'honglylay14@gmail.com', NULL, 7);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (27, 'MVT (Model-View-Template)', '2026-09-27 15:00:24.221574', 'honglylay14@gmail.com', true, '2026-09-27 15:00:24.221574', 'honglylay14@gmail.com', NULL, 7);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (28, 'MVVM (Model-View-ViewModel)', '2026-09-27 15:00:24.234559', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.234559', 'honglylay14@gmail.com', NULL, 7);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (29, 'Django includes a mobile battery monitoring library', '2026-09-27 15:00:24.264549', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.264549', 'honglylay14@gmail.com', NULL, 8);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (30, 'Django comes with many built-in features like auth, admin, ORM', '2026-09-27 15:00:24.283327', 'honglylay14@gmail.com', true, '2026-09-27 15:00:24.283327', 'honglylay14@gmail.com', NULL, 8);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (31, 'Django requires extra installation for every feature', '2026-09-27 15:00:24.299097', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.299097', 'honglylay14@gmail.com', NULL, 8);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (32, 'Django uses batteries for encryption', '2026-09-27 15:00:24.318421', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.318421', 'honglylay14@gmail.com', NULL, 8);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (33, 'Google', '2026-09-27 15:00:24.356589', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.356589', 'honglylay14@gmail.com', NULL, 9);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (34, 'Facebook', '2026-09-27 15:00:24.373065', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.373065', 'honglylay14@gmail.com', NULL, 9);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (35, 'Lawrence Journal-World newspaper', '2026-09-27 15:00:24.386424', 'honglylay14@gmail.com', true, '2026-09-27 15:00:24.386424', 'honglylay14@gmail.com', NULL, 9);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (36, 'Mozilla', '2026-09-27 15:00:24.400087', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.400087', 'honglylay14@gmail.com', NULL, 9);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (37, 'Code editing', '2026-09-27 15:00:24.43354', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.43354', 'honglylay14@gmail.com', NULL, 10);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (38, 'Managing application data through a GUI', '2026-09-27 15:00:24.451445', 'honglylay14@gmail.com', true, '2026-09-27 15:00:24.451445', 'honglylay14@gmail.com', NULL, 10);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (39, 'Deployment management', '2026-09-27 15:00:24.466374', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.466374', 'honglylay14@gmail.com', NULL, 10);
INSERT INTO public.answers (answer_id, answer_text, created_at, created_by, is_correct, updated_at, updated_by, instructor_id, question_id) VALUES (40, 'API documentation', '2026-09-27 15:00:24.485506', 'honglylay14@gmail.com', false, '2026-09-27 15:00:24.485506', 'honglylay14@gmail.com', NULL, 10);


--
-- Data for Name: assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.assignments (assignment_id, allow_resubmission, created_at, description, due_date, instructions, max_score, start_date, supporting_file_public_id, supporting_file_url, title, updated_at, course_id, instructor_id, section_id) VALUES (1, true, '2026-09-27 15:00:31.641799', 'Write a research summary on Spring Boot in modern software engineering.', NULL, 'Task: Write a 1-page summary covering:
1. List 3 real-world companies that use Spring Boot and what they build with it.
2. Explain the "convention over configuration" principle in Spring Boot.
3. Compare Spring Boot to one alternative (e.g., Quarkus, Micronaut) in a table with 3 criteria.

Acceptance Criteria:
- All 3 companies identified with specific use case
- Convention over configuration explained clearly
- Comparison table has at least 3 rows
Bonus: Spring Boot GitHub repository star count and last release date.', 100, NULL, NULL, NULL, 'Spring Boot Research Report', '2026-09-27 15:00:31.641837', 1, 2, 1);
INSERT INTO public.assignments (assignment_id, allow_resubmission, created_at, description, due_date, instructions, max_score, start_date, supporting_file_public_id, supporting_file_url, title, updated_at, course_id, instructor_id, section_id) VALUES (2, true, '2026-09-27 15:00:31.662806', 'Comprehensive architectural analysis of Django''s MVT pattern and web applications.', NULL, 'Task:
1. List 3 popular websites built with Django and describe what they do.
2. Explain how Django''s MVT pattern differs from the classic MVC pattern.
3. Create a diagram or detailed explanation showing how an HTTP request flows through Django''s URLconf, View, Model, and Template.

Acceptance Criteria:
- 3 websites with descriptions
- MVT vs MVC comparison
- Request flow explanation or diagram
Bonus: List at least 5 built-in security protections provided by Django.', 100, NULL, NULL, NULL, 'Django Architecture & Request Lifecycle Report', '2026-09-27 15:00:31.662816', 2, 2, 13);


--
-- Data for Name: assignment_submissions; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (1, 'Backend', '2026-09-27 13:26:54.851072', 'admin1234@test.com', '2026-09-27 13:26:54.851072', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (2, 'Frontend', '2026-09-27 13:27:02.399128', 'admin1234@test.com', '2026-09-27 13:27:02.399128', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (3, 'Mobile App Development', '2026-09-27 13:27:14.768949', 'admin1234@test.com', '2026-09-27 13:27:14.768949', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (4, 'Full-stack Development', '2026-09-27 13:27:23.362982', 'admin1234@test.com', '2026-09-27 13:27:23.362982', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (5, 'Devops', '2026-09-27 13:27:28.637552', 'admin1234@test.com', '2026-09-27 13:27:28.637552', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (6, 'Cyber-Security', '2026-09-27 13:27:36.652938', 'admin1234@test.com', '2026-09-27 13:27:36.652938', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (7, 'Artificial Intelligence', '2026-09-27 13:41:57.322505', 'admin1234@test.com', '2026-09-27 13:41:57.322505', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (8, 'Cybersecurity', '2026-09-27 13:42:06.923692', 'admin1234@test.com', '2026-09-27 13:42:06.923692', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (9, 'Cloud Computing', '2026-09-27 13:42:16.374399', 'admin1234@test.com', '2026-09-27 13:42:16.374399', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (10, 'Mobile Development', '2026-09-27 13:42:26.195919', 'admin1234@test.com', '2026-09-27 13:42:26.195919', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (11, 'Game Development', '2026-09-27 13:42:36.035556', 'admin1234@test.com', '2026-09-27 13:42:36.035556', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (12, 'DevOps & CI/CD', '2026-09-27 13:42:47.969767', 'admin1234@test.com', '2026-09-27 13:42:47.969767', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (13, 'Blockchain & Web3', '2026-09-27 13:42:57.038185', 'admin1234@test.com', '2026-09-27 13:42:57.038185', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (14, 'UI/UX Design', '2026-09-27 13:43:05.679935', 'admin1234@test.com', '2026-09-27 13:43:05.679935', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (15, 'Digital Marketing', '2026-09-27 13:43:15.349183', 'admin1234@test.com', '2026-09-27 13:43:15.349183', 'admin1234@test.com');
INSERT INTO public.categories (category_id, category, created_at, created_by, updated_at, updated_by) VALUES (16, 'Data Engineering', '2026-09-27 13:43:26.208345', 'admin1234@test.com', '2026-09-27 13:43:26.208345', 'admin1234@test.com');


--
-- Data for Name: certificates; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: course_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.course_category (course_id, category_id) VALUES (2, 1);
INSERT INTO public.course_category (course_id, category_id) VALUES (2, 4);
INSERT INTO public.course_category (course_id, category_id) VALUES (1, 1);
INSERT INTO public.course_category (course_id, category_id) VALUES (1, 4);


--
-- Data for Name: course_favorites; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: course_reviews; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: enrollments; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.enrollments (enrollment_id, earned_points, enrolled_at, status, total_points, updated_at, course_id, user_id, expiration_date) VALUES (1, 0, '2026-09-27 15:05:01.693212', 'ACTIVE', 150, '2026-09-27 15:05:08.740744', 2, 3, '2027-03-26 15:05:01.693212');
INSERT INTO public.enrollments (enrollment_id, earned_points, enrolled_at, status, total_points, updated_at, course_id, user_id, expiration_date) VALUES (2, 0, '2026-09-27 21:43:45.774742', 'ACTIVE', 150, '2026-09-27 21:44:46.6521', 1, 3, '2026-09-28 21:43:45.766');


--
-- Data for Name: instructors; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.instructors (instructor_id, average_rating, biography, created_at, expertise, full_name, phone_number, profile_photo_public_id, profile_photo_url, updated_at, user_id) VALUES (1, 5, NULL, '2026-09-27 13:49:07.373821', NULL, 'Lay Hongly', '069339084', NULL, NULL, '2026-09-27 13:49:07.37385', 2);


--
-- Data for Name: lesson_materials; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.lesson_materials (material_id, file_public_id, file_size, file_type, file_url, title, uploaded_at, lesson_id) VALUES (1, 'course/hongly-lay/general/materials/3f32b4e6-131a-4fcd-80f6-fd7a73b21408', 2403, 'DOC', 'https://res.cloudinary.com/lyxxfhot/raw/upload/v1790496047/course/hongly-lay/general/materials/3f32b4e6-131a-4fcd-80f6-fd7a73b21408', 'quiz_and_assignment', '2026-09-27 15:00:50.96558', 1);
INSERT INTO public.lesson_materials (material_id, file_public_id, file_size, file_type, file_url, title, uploaded_at, lesson_id) VALUES (2, 'course/hongly-lay/general/materials/856141f9-e91f-4438-aec5-f50e11f97fc4', 2304, 'DOC', 'https://res.cloudinary.com/lyxxfhot/raw/upload/v1790496080/course/hongly-lay/general/materials/856141f9-e91f-4438-aec5-f50e11f97fc4', 'quiz_and_assignment', '2026-09-27 15:01:25.063409', 7);


--
-- Data for Name: students; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.students (student_id, created_at, date_of_birth, education_level, full_name, gender, phone_number, profile_photo_public_id, profile_photo_url, student_code, total_points, updated_at, user_id) VALUES (1, '2026-09-27 15:03:21.46131', NULL, NULL, 'Lay Honglyy', 'NOT_SPECIFIC', '069339084', 'profile-picture/honglyy/349c65f0-eb34-46d5-8c9b-bd8464eb1a1b', 'https://res.cloudinary.com/lyxxfhot/image/upload/v1790685479/profile-picture/honglyy/349c65f0-eb34-46d5-8c9b-bd8464eb1a1b.png', 'STU-2026-0001', 0, '2026-09-29 19:38:00.499038', 3);
INSERT INTO public.students (student_id, created_at, date_of_birth, education_level, full_name, gender, phone_number, profile_photo_public_id, profile_photo_url, student_code, total_points, updated_at, user_id) VALUES (2, '2026-09-29 20:15:21.99528', NULL, NULL, 'Lay Hongly', 'NOT_SPECIFIC', '069339084', NULL, NULL, 'STU-2026-0002', 0, '2026-09-29 20:15:21.995309', 2);


--
-- Data for Name: lesson_progress; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.lesson_progress (id, completed_at, is_completed, last_playback_position_seconds, updated_at, lesson_id, student_id) VALUES (1, '2026-09-27 15:05:14.347', true, 5.1, '2026-09-27 15:05:18.40416', 7, 1);
INSERT INTO public.lesson_progress (id, completed_at, is_completed, last_playback_position_seconds, updated_at, lesson_id, student_id) VALUES (2, NULL, false, 6.8, '2026-09-27 15:05:32.814542', 8, 1);
INSERT INTO public.lesson_progress (id, completed_at, is_completed, last_playback_position_seconds, updated_at, lesson_id, student_id) VALUES (3, NULL, false, 9, '2026-09-29 20:12:30.833507', 1, 1);


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.notifications (id, action_url, created_at, is_read, message, title, type, user_id) VALUES (4, 'http://localhost:5173/courses/1', '2026-09-27 21:43:49.136917', true, 'Your payment for ''Spring Boot: Full-Stack Web Development & Microservices'' has been approved! You can now access the course.', 'Payment Approved', 'PAYMENT', 3);
INSERT INTO public.notifications (id, action_url, created_at, is_read, message, title, type, user_id) VALUES (3, '/api/courses/1', '2026-09-27 21:43:45.807311', true, 'You are now enrolled in Spring Boot: Full-Stack Web Development & Microservices. Start learning today! Access valid until 2026-09-28 21:43:45.766', 'Welcome to the Course!', 'COURSE', 3);
INSERT INTO public.notifications (id, action_url, created_at, is_read, message, title, type, user_id) VALUES (2, 'http://localhost:5173/courses/2', '2026-09-27 15:05:03.460213', true, 'Your payment for ''Django: Complete Python Web Development & REST Framework'' has been approved! You can now access the course.', 'Payment Approved', 'PAYMENT', 3);
INSERT INTO public.notifications (id, action_url, created_at, is_read, message, title, type, user_id) VALUES (1, '/api/courses/2', '2026-09-27 15:05:01.719791', true, 'You are now enrolled in Django: Complete Python Web Development & REST Framework. Start learning today!', 'Welcome to the Course!', 'COURSE', 3);


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.payments (payment_id, amount, created_at, provider, provider_reference, status, updated_at, course_id, user_id, discount_amount, is_re_enrollment_discount, original_amount) VALUES (1, 39.99, '2026-09-27 15:04:54.299629', 'ABA_PAYWAY', 'admin-confirmed', 'PAID', '2026-09-27 15:05:01.667357', 2, 3, 0.00, false, 39.99);
INSERT INTO public.payments (payment_id, amount, created_at, provider, provider_reference, status, updated_at, course_id, user_id, discount_amount, is_re_enrollment_discount, original_amount) VALUES (2, 49.99, '2026-09-27 21:42:44.280014', 'ABA_PAYWAY', 'admin-confirmed', 'PAID', '2026-09-27 21:43:45.660383', 1, 3, 0.00, false, 49.99);


--
-- Data for Name: quiz_attempts; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Name: activity_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.activity_logs_id_seq', 27, true);


--
-- Name: answers_answer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.answers_answer_id_seq', 40, true);


--
-- Name: assignment_submissions_submission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.assignment_submissions_submission_id_seq', 1, false);


--
-- Name: assignments_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.assignments_assignment_id_seq', 2, true);


--
-- Name: categories_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categories_category_id_seq', 16, true);


--
-- Name: certificates_certificate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.certificates_certificate_id_seq', 1, false);


--
-- Name: course_favorites_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.course_favorites_id_seq', 1, false);


--
-- Name: course_reviews_review_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.course_reviews_review_id_seq', 1, false);


--
-- Name: courses_course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.courses_course_id_seq', 3, true);


--
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.enrollments_enrollment_id_seq', 2, true);


--
-- Name: instructors_instructor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.instructors_instructor_id_seq', 1, true);


--
-- Name: lesson_materials_material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lesson_materials_material_id_seq', 2, true);


--
-- Name: lesson_progress_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lesson_progress_id_seq', 3, true);


--
-- Name: lessons_lesson_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lessons_lesson_id_seq', 12, true);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notifications_id_seq', 4, true);


--
-- Name: payments_payment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.payments_payment_id_seq', 2, true);


--
-- Name: questions_question_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.questions_question_id_seq', 10, true);


--
-- Name: quiz_attempts_attempt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.quiz_attempts_attempt_id_seq', 1, false);


--
-- Name: quizzes_quiz_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.quizzes_quiz_id_seq', 2, true);


--
-- Name: sections_section_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sections_section_id_seq', 18, true);


--
-- Name: students_student_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.students_student_id_seq', 2, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 3, true);


--
-- PostgreSQL database dump complete
--
