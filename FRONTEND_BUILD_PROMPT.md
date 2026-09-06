# LMS Frontend Build Prompt

Build a frontend for this Spring Boot LMS backend using the existing REST APIs.

## Backend API Readiness

The current APIs support a fully functional LMS:

- Authentication with JWT login (email, phone number, or username)
- User registration with `fullName` and `phoneNumber`
- OTP verification
- Password reset OTP flow
- Change password for authenticated users
- Student profile management with photo upload (Cloudinary)
- Instructor profile management with photo upload, bio, expertise (Cloudinary)
- Public instructor directory
- Course catalog with detail view (nested sections, lessons, reviews)
- Course CRUD (admin and instructor-scoped)
- Category CRUD with course listing
- Section CRUD (admin and instructor-scoped)
- Lesson CRUD (admin and instructor-scoped)
- Quiz CRUD
- Quiz submission and attempt scoring
- Question CRUD with point values
- Answer CRUD
- Payment checkout/confirmation with auto-enrollment
- Payment admin status management
- File/media upload for course covers and lesson videos (Cloudinary)
- Enrollment create/list/update/delete
- Course reviews per course
- Admin user management (list, update role, delete)
- Role-based access for `ADMIN`, `INSTRUCTOR`, `STUDENT`, and `USER`

## Shared Frontend Requirements

- Use plain React.
- Store JWT token in `localStorage`.
- Send JWT token on protected API calls:

```js
Authorization: Bearer <token>
```

- Decode the JWT payload on the frontend to determine the user role.
- JWT claims: `sub` = user email, `role` = role name (e.g. `STUDENT`, `INSTRUCTOR`, `ADMIN`, `USER`).
- The backend bridges `USER` and `STUDENT` roles — they are interchangeable for access control.
- Redirect users based on role after login:
  - `ADMIN` -> `/app/admin/dashboard.html`
  - `INSTRUCTOR` -> `/app/instructor/dashboard.html`
  - `USER` or `STUDENT` -> `/app/user/index.html`
- Add logout support by clearing the token.
- Show API errors clearly in Bootstrap alerts.
- Keep code separated by role/page.

## Auth Screens

Build auth screens for all users.

### Login

Page:

```text
/app/auth/login.html
```

API:

```http
POST /authenticate
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

Note: The `email` field accepts **email, username, or phone number** interchangeably. The backend resolves the user from any of these identifiers. Label the input field as "Email, Username, or Phone" to reflect this.

Response:

```text
JWT token string
```

After successful login:

- Save token in `localStorage`.
- Decode role from token claim `role`.
- Redirect to the correct dashboard/page.

### Register

Page:

```text
/app/auth/register.html
```

API:

```http
POST /register
```

Request:

```json
{
  "username": "student1",
  "email": "student@example.com",
  "password": "password",
  "fullName": "Student Name",
  "phoneNumber": "0123456789",
  "role": "STUDENT"
}
```

Allowed roles:

```text
STUDENT
USER
INSTRUCTOR
```

Registration auto-creates a `Students` profile (with code `STU-YYYY-XXXX`) for `STUDENT`/`USER` roles, or an `Instructors` profile for `INSTRUCTOR` role.

After registration, show a message telling the user to verify OTP.

### Verify OTP

Page:

```text
/app/auth/verify-otp.html
```

API:

```http
POST /verify-otp
```

Request:

```json
{
  "email": "student@example.com",
  "otp": "123456"
}
```

Response:

```json
{
  "id": 1,
  "userId": "uuid-string",
  "username": "student1",
  "email": "student@example.com",
  "role": "STUDENT",
  "isVerified": true
}
```

After success, redirect to login.

### Forgot Password

Page:

```text
/app/auth/forgot-password.html
```

API:

```http
POST /send-reset-otp?email=user@example.com
```

After success, redirect to reset password page.

### Reset Password

Page:

```text
/app/auth/reset-password.html
```

API:

```http
POST /reset-password
```

Request:

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "password": "newPassword"
}
```

After success, redirect to login.

### Change Password (Authenticated)

Available on the user profile page or settings area. Requires current JWT token.

API:

```http
POST /api/users/change-password
```

Request:

```json
{
  "oldPassword": "currentPassword",
  "newPassword": "newPassword123"
}
```

Response:

```text
Password changed successfully
```

## User Frontend

The user frontend should sell courses and allow users to pay for and enroll in courses.

### Public Course Store

Page:

```text
/app/user/index.html
```

API:

```http
GET /api/courses
```

Response (each item):

```json
{
  "courseId": 1,
  "title": "Java Basics",
  "description": "Learn Java",
  "price": 49.99,
  "overallDuration": "8 hours",
  "coverUrl": "https://res.cloudinary.com/.../cover.jpg",
  "coverPublicId": "course-covers/abc123",
  "instructorId": 1,
  "instructor": "instructor_username",
  "categoryIds": [1, 2],
  "categories": ["Programming", "Backend"],
  "rating": 4.5
}
```

Display:

- Course cover image (use `coverUrl`)
- Course title
- Description
- Price
- Duration
- Instructor name
- Categories
- Rating (star display)
- Buy/enroll button

Add:

- Search by title/instructor/category
- Category badges
- Responsive Bootstrap course cards
- Hero section focused on selling courses

### Course Detail Page

Page:

```text
/app/user/course-detail.html?id={courseId}
```

API:

```http
GET /api/courses/{id}
```

Auth: Requires `ADMIN`, `INSTRUCTOR`, `USER`, or `STUDENT` role.

Response:

```json
{
  "courseId": 1,
  "title": "Java Basics",
  "description": "Learn Java from scratch",
  "price": 49.99,
  "overallDuration": "8 hours",
  "coverUrl": "https://res.cloudinary.com/.../cover.jpg",
  "coverPublicId": "course-covers/abc123",
  "instructor": "instructor_username",
  "sectionCount": 3,
  "rating": 4.5,
  "categories": ["Programming", "Backend"],
  "sections": [
    {
      "sectionId": 1,
      "title": "Getting Started",
      "duration": "1 hour",
      "lessonCount": 3,
      "lessons": [
        {
          "lessonId": 1,
          "title": "Intro Lesson",
          "videoUrl": "https://res.cloudinary.com/.../video.mp4",
          "videoPublicId": "lesson-videos/xyz789"
        }
      ]
    }
  ],
  "reviews": [
    {
      "reviewId": 1,
      "reviewText": "Great course!",
      "rating": 5,
      "username": "student1",
      "courseTitle": "Java Basics"
    }
  ]
}
```

Display:

- Full course info with cover image
- Accordion of sections with nested lesson list
- Video preview for lessons (if enrolled)
- Reviews list with star ratings
- Buy/enroll button
- Add review form (for enrolled users)

### Buy And Enroll In Course

Users should not be enrolled directly from the course card. The frontend should create a payment checkout first, confirm the payment, and then the backend will create the enrollment.

Create checkout:

API:

```http
POST /api/payments/checkout
```

Request:

```json
{
  "courseId": 1,
  "provider": "MANUAL"
}
```

Requires logged-in user.

If user is not logged in, redirect to login.

Response:

```json
{
  "paymentId": 1,
  "userId": 1,
  "username": "student1",
  "userEmail": "student@example.com",
  "courseId": 1,
  "courseTitle": "Java Basics",
  "amount": 49.99,
  "provider": "MANUAL",
  "providerReference": "checkout_uuid-string",
  "status": "PENDING",
  "createdAt": "2026-05-20T10:00:00.000+00:00",
  "updatedAt": "2026-05-20T10:00:00.000+00:00"
}
```

Confirm payment:

```http
POST /api/payments/{paymentId}/confirm
```

After confirmation:

- The backend marks the payment as `PAID`.
- The backend enrolls the current user in the course.
- The frontend should show a success message and optionally link to `/app/user/enrollments.html`.

For a real gateway such as Stripe or PayPal, replace the manual confirm button with the provider checkout flow. After the provider confirms success, call a backend verification endpoint or webhook handler before marking payment as `PAID`.

### My Payments

Page:

```text
/app/user/payments.html
```

API:

```http
GET /api/payments/me
```

Display:

- Course title
- Amount
- Provider
- Provider reference
- Status (badge: `PENDING`, `PAID`, `FAILED`, `REFUNDED`)
- Created date

### My Enrollments

Page:

```text
/app/user/enrollments.html
```

API:

```http
GET /api/enrollments/me
```

Response (each item):

```json
{
  "enrollmentId": 1,
  "userId": 1,
  "username": "student1",
  "userEmail": "student@example.com",
  "courseId": 1,
  "courseTitle": "Java Basics",
  "instructor": "instructor_username",
  "status": "ACTIVE",
  "enrolledAt": "2026-05-20T10:00:00.000+00:00",
  "updatedAt": "2026-05-20T10:00:00.000+00:00"
}
```

Display:

- Course title
- Instructor
- Status (badge)
- Enrolled date
- Cancel button
- Link to course detail page

Cancel enrollment:

```http
DELETE /api/enrollments/me/courses/{courseId}
```

### Student Profile

Page:

```text
/app/user/profile.html
```

#### Get Profile

API:

```http
GET /api/students/profile
```

Auth: `STUDENT` or `USER` role.

Response:

```json
{
  "studentId": 1,
  "studentCode": "STU-2026-0001",
  "username": "student1",
  "email": "student@example.com",
  "fullName": "Student Name",
  "phoneNumber": "0123456789",
  "gender": "MALE",
  "dateOfBirth": "2000-01-15",
  "educationLevel": "Bachelor",
  "profilePhotoUrl": "https://res.cloudinary.com/.../photo.jpg",
  "profilePhotoPublicId": "profile-photos/students/abc123",
  "createdAt": "2026-01-01T00:00:00.000+00:00",
  "updatedAt": "2026-09-05T00:00:00.000+00:00"
}
```

Display:

- Profile photo (circle avatar)
- Student code (read-only)
- Full name
- Email (read-only)
- Username (read-only)
- Phone number
- Gender dropdown (`MALE`, `FEMALE`, `OTHER`, `NOT_SPECIFIC`)
- Date of birth (date picker)
- Education level
- Edit form

#### Update Profile

API:

```http
PUT /api/students/profile
```

Request:

```json
{
  "fullName": "Updated Name",
  "phoneNumber": "0987654321",
  "gender": "MALE",
  "dateOfBirth": "2000-01-15",
  "educationLevel": "Master",
  "profilePhotoUrl": "https://res.cloudinary.com/.../photo.jpg",
  "profilePhotoPublicId": "profile-photos/students/abc123"
}
```

#### Upload Profile Photo

API:

```http
POST /api/students/profile/photo
```

Request:

```js
const formData = new FormData();
formData.append("file", fileInput.files[0]);

await fetch("/api/students/profile/photo", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`
  },
  body: formData
});
```

Do not set the `Content-Type` header manually when sending `FormData`.

Response: Updated `StudentProfileResponseDTO`.

### Course Reviews

Users can add reviews to courses they are enrolled in.

Add review:

```http
POST /api/courses/{courseId}/review
```

Request:

```json
{
  "reviewText": "Excellent course content!",
  "rating": 5
}
```

`rating` must be between 1 and 5.

List reviews for a course:

```http
GET /api/courses/{courseId}/reviews
```

### Quiz Taking

When a student views a lesson, they can take the associated quiz.

Get quiz for a lesson:

```http
GET /api/lessons/{lessonId}/quiz
```

Response:

```json
{
  "quizId": 1,
  "title": "Quiz 1",
  "totalPoints": 10.0,
  "question": [
    {
      "questionId": 1,
      "questionText": "What is Java?",
      "point": 5,
      "answers": [
        {
          "answerId": 1,
          "answerText": "A programming language",
          "isCorrect": true
        },
        {
          "answerId": 2,
          "answerText": "A coffee brand",
          "isCorrect": false
        }
      ]
    }
  ]
}
```

Submit quiz:

```http
POST /api/quizzes/{quizId}/submit
```

Request:

```json
{
  "answerIds": [1, 4, 7]
}
```

The user must have an active enrollment in the course that contains this quiz.

Response:

```json
{
  "attemptId": 1,
  "quizId": 1,
  "enrollmentId": 1,
  "earnedPoints": 8.0,
  "totalPoints": 10.0,
  "correctAnswers": 2,
  "totalQuestions": 3,
  "submittedAt": "2026-09-05T10:00:00.000+00:00",
  "updatedAt": "2026-09-05T10:00:00.000+00:00"
}
```

Get my past attempt:

```http
GET /api/quizzes/{quizId}/attempt/me
```

Display:

- Question list with radio buttons for each answer
- Submit button
- After submission, show score breakdown (earned/total points, correct/total questions)
- If already attempted, show previous results

## File Uploads

The backend uses **Cloudinary** for file storage. Uploaded files are stored on Cloudinary and the response returns a `url` (Cloudinary secure URL) and `publicId` (for deletion).

Use these upload APIs from admin and instructor forms before creating or updating courses/lessons. Save the returned `url` and `publicId` into the appropriate fields.

Do not set the `Content-Type` header manually when sending `FormData`; the browser will add the multipart boundary.

### Upload Course Cover

API:

```http
POST /api/uploads/course-cover
```

Auth:

```text
ADMIN or INSTRUCTOR
```

Request:

```js
const formData = new FormData();
formData.append("file", fileInput.files[0]);

await fetch("/api/uploads/course-cover", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`
  },
  body: formData
});
```

Allowed image types:

```text
jpg, jpeg, png, webp, gif
```

Default max image size:

```text
5MB
```

Response:

```json
{
  "originalFileName": "cover.jpg",
  "publicId": "course-covers/generated-name",
  "contentType": "image/jpeg",
  "size": 120000,
  "url": "https://res.cloudinary.com/.../generated-name.jpg"
}
```

Use the returned URL and publicId in course create/update:

```json
{
  "coverUrl": "https://res.cloudinary.com/.../generated-name.jpg",
  "coverPublicId": "course-covers/generated-name"
}
```

### Upload Lesson Video

API:

```http
POST /api/uploads/lesson-video
```

Auth:

```text
ADMIN or INSTRUCTOR
```

Request:

```js
const formData = new FormData();
formData.append("file", fileInput.files[0]);

await fetch("/api/uploads/lesson-video", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`
  },
  body: formData
});
```

Allowed video types:

```text
mp4, webm, mov, avi
```

Default max video size:

```text
500MB
```

Response:

```json
{
  "originalFileName": "lesson.mp4",
  "publicId": "lesson-videos/generated-name",
  "contentType": "video/mp4",
  "size": 5000000,
  "url": "https://res.cloudinary.com/.../generated-name.mp4"
}
```

Use the returned URL and publicId in lesson create/update:

```json
{
  "videoUrl": "https://res.cloudinary.com/.../generated-name.mp4",
  "videoPublicId": "lesson-videos/generated-name"
}
```

## Admin Dashboard

The admin dashboard must provide full control over CRUD operations exposed by the APIs.

Page:

```text
/app/admin/dashboard.html
```

Use a sidebar layout with sections:

- Overview
- Users
- Courses
- Categories
- Sections
- Lessons
- Quizzes
- Questions
- Answers
- Payments
- Enrollments
- Reviews

Only allow users with role `ADMIN`.

### Admin Users Management

List all users:

```http
GET /api/users
```

Response (each item):

```json
{
  "id": 1,
  "userId": "uuid-string",
  "username": "student1",
  "email": "student@example.com",
  "role": "STUDENT",
  "isVerified": true
}
```

Update user role:

```http
PATCH /api/users/{id}/role
```

Body:

```json
{
  "role": "INSTRUCTOR"
}
```

Valid roles:

```text
STUDENT
USER
INSTRUCTOR
ADMIN
```

When role is changed to `STUDENT`/`USER`, the backend auto-creates a `Students` profile if missing. When changed to `INSTRUCTOR`, the backend auto-creates an `Instructors` profile.

Delete user:

```http
DELETE /api/users/{id}
```

This cascades to delete all associated profiles, enrollments, payments, quiz attempts, reviews, and instructor-owned courses.

Admin users table should show:

- User ID
- Username
- Email
- Role dropdown
- Verified status badge
- Delete button

Create admin:

```http
POST /admin/register
```

Body:

```json
{
  "username": "admin2",
  "email": "admin@example.com",
  "password": "password",
  "fullName": "Admin Name",
  "phoneNumber": "0123456789",
  "role": "ADMIN"
}
```

### Admin Courses CRUD

List:

```http
GET /api/courses
```

Create:

```http
POST /api/courses
```

Update:

```http
PUT /api/courses/{id}
```

Delete:

```http
DELETE /api/courses/{id}
```

Body:

```json
{
  "title": "Java Basics",
  "description": "Learn Java",
  "price": 49.99,
  "overallDuration": "8 hours",
  "coverUrl": "https://res.cloudinary.com/.../cover.jpg",
  "coverPublicId": "course-covers/abc123",
  "instructor": 1,
  "categoryId": [1, 2]
}
```

### Admin Categories CRUD

List:

```http
GET /api/categories
```

Get category with courses:

```http
GET /api/categories/{id}
```

Response:

```json
{
  "categoryId": 1,
  "category": "Programming",
  "courses": [
    {
      "courseId": 1,
      "title": "Java Basics",
      "description": "Learn Java",
      "price": 49.99,
      "overallDuration": "8 hours",
      "coverUrl": "https://...",
      "coverPublicId": "...",
      "instructor": "instructor_username"
    }
  ]
}
```

Create:

```http
POST /api/categories
```

Update:

```http
PUT /api/categories/{id}
```

Delete:

```http
DELETE /api/categories/{id}
```

Body:

```json
{
  "category": "Programming"
}
```

### Admin Sections CRUD

List:

```http
GET /api/sections
```

Create:

```http
POST /api/sections
```

Update:

```http
PUT /api/sections/{id}
```

Delete:

```http
DELETE /api/sections/{id}
```

Body:

```json
{
  "title": "Getting Started",
  "duration": "1 hour",
  "courseId": 1
}
```

### Admin Lessons CRUD

List:

```http
GET /api/lessons
```

Get lesson detail with quiz:

```http
GET /api/lessons/{id}
```

Create:

```http
POST /api/lessons
```

Update:

```http
PUT /api/lessons/{id}
```

Delete:

```http
DELETE /api/lessons/{id}
```

Body:

```json
{
  "title": "Intro Lesson",
  "videoUrl": "https://res.cloudinary.com/.../video.mp4",
  "videoPublicId": "lesson-videos/abc123",
  "sectionId": 1
}
```

### Admin Quizzes CRUD

List:

```http
GET /api/quizzes
```

Get quiz for a lesson:

```http
GET /api/lessons/{lessonId}/quiz
```

Create:

```http
POST /api/quizzes
```

Update:

```http
PUT /api/quizzes/{id}
```

Delete:

```http
DELETE /api/quizzes/{id}
```

Body:

```json
{
  "title": "Quiz 1",
  "totalPoints": 10,
  "lessonId": 1
}
```

### Admin Questions CRUD

List:

```http
GET /api/questions
```

Create:

```http
POST /api/questions
```

Update:

```http
PUT /api/questions/{id}
```

Delete:

```http
DELETE /api/questions/{id}
```

Body:

```json
{
  "questionText": "What is Java?",
  "point": 5,
  "quizId": 1
}
```

### Admin Answers CRUD

List:

```http
GET /api/answers
```

Create:

```http
POST /api/answers
```

Update:

```http
PUT /api/answers/{id}
```

Delete:

```http
DELETE /api/answers/{id}
```

Body:

```json
{
  "answerText": "A programming language",
  "isCorrect": true,
  "questionId": 1
}
```

### Admin Payments

The admin should be able to list payments, update payment status, and delete test/manual payments.

List:

```http
GET /api/payments
```

Update payment status:

```http
PATCH /api/payments/{id}/status
```

Body:

```json
{
  "status": "PAID",
  "providerReference": "manual-admin-confirmation"
}
```

Delete:

```http
DELETE /api/payments/{id}
```

Valid payment statuses:

```text
PENDING
PAID
FAILED
REFUNDED
```

Admin payment table should show:

- Payment ID
- Student username/email
- Course title
- Amount
- Provider
- Provider reference
- Status dropdown
- Delete button

When an admin changes a payment to `PAID`, the backend automatically creates the course enrollment for that student.

### Admin Enrollments

List:

```http
GET /api/enrollments
```

Enroll a user:

```http
POST /api/enrollments/manage
```

Body:

```json
{
  "userId": 1,
  "courseId": 1,
  "status": "ACTIVE"
}
```

Update status:

```http
PATCH /api/enrollments/{id}/status
```

Body:

```json
{
  "status": "COMPLETED"
}
```

Delete:

```http
DELETE /api/enrollments/{id}
```

Valid enrollment statuses:

```text
ACTIVE
COMPLETED
CANCELLED
```

### Admin Reviews

List all reviews:

```http
GET /api/reviews
```

List reviews for a course:

```http
GET /api/courses/{courseId}/reviews
```

Admin reviews table should show:

- Review ID
- Username
- Course title
- Rating (stars)
- Review text
- Filter by course

### Admin Instructors Directory

List all instructors (public):

```http
GET /api/instructors
```

View instructor detail:

```http
GET /api/instructors/{id}
```

Response:

```json
{
  "instructorId": 1,
  "userId": 1,
  "username": "instructor1",
  "email": "instructor@example.com",
  "fullName": "Instructor Name",
  "phoneNumber": "0123456789",
  "profilePhotoUrl": "https://res.cloudinary.com/.../photo.jpg",
  "profilePhotoPublicId": "profile-photos/instructors/abc123",
  "biography": "Expert in Spring Boot",
  "expertise": "Spring Boot, Cloud Architecture",
  "averageRating": 4.8,
  "totalCourses": 5,
  "courses": [...],
  "createdAt": "2026-01-01T00:00:00.000+00:00",
  "updatedAt": "2026-09-05T00:00:00.000+00:00"
}
```

## Instructor Dashboard

Page:

```text
/app/instructor/dashboard.html
```

Only allow users with role `INSTRUCTOR` or `ADMIN`.

Instructor should be able to:

- View and edit their profile
- View their courses
- Create/update/delete their courses
- Manage sections for their courses
- Manage lessons for their sections
- View enrollments for courses they manage
- View enrolled students for a selected course
- Update enrollment statuses

### Instructor Profile

Get profile:

```http
GET /api/instructors/me
```

Update profile:

```http
PUT /api/instructors/me
```

Body:

```json
{
  "fullName": "Updated Instructor Name",
  "phoneNumber": "0987654321",
  "biography": "Experienced developer and educator",
  "expertise": "Spring Boot, Microservices, Cloud",
  "profilePhotoUrl": "https://res.cloudinary.com/.../photo.jpg",
  "profilePhotoPublicId": "profile-photos/instructors/abc123"
}
```

Upload profile photo:

```http
POST /api/instructors/me/photo
```

Request:

```js
const formData = new FormData();
formData.append("file", fileInput.files[0]);

await fetch("/api/instructors/me/photo", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`
  },
  body: formData
});
```

Display:

- Profile photo with upload button
- Full name
- Phone number
- Biography (textarea)
- Expertise
- Average rating (read-only)
- Total courses count (read-only)

### Instructor Courses

List my courses:

```http
GET /api/courses/instructor/me
```

Create my course:

```http
POST /api/courses/instructor/me
```

Body:

```json
{
  "title": "Spring Boot Basics",
  "description": "Learn backend development",
  "price": 39.99,
  "overallDuration": "6 hours",
  "coverUrl": "https://res.cloudinary.com/.../cover.jpg",
  "coverPublicId": "course-covers/abc123",
  "instructor": null,
  "categoryId": [1]
}
```

The backend automatically sets the JWT-authenticated instructor as the course owner. Set `instructor` to `null`.

Update my course:

```http
PUT /api/courses/instructor/me/{id}
```

Delete my course:

```http
DELETE /api/courses/instructor/me/{id}
```

### Instructor Sections

List my sections (optionally filter by course):

```http
GET /api/sections/instructor/me
GET /api/sections/instructor/me?courseId=1
```

Create my section:

```http
POST /api/sections/instructor/me
```

Body:

```json
{
  "title": "Getting Started",
  "duration": "1 hour",
  "courseId": 1
}
```

Update my section:

```http
PUT /api/sections/instructor/me/{id}
```

Delete my section:

```http
DELETE /api/sections/instructor/me/{id}
```

### Instructor Lessons

List my lessons (optionally filter by section or course):

```http
GET /api/lessons/instructor/me
GET /api/lessons/instructor/me?sectionId=1
GET /api/lessons/instructor/me?courseId=1
```

Create my lesson:

```http
POST /api/lessons/instructor/me
```

Body:

```json
{
  "title": "Intro Lesson",
  "videoUrl": "https://res.cloudinary.com/.../video.mp4",
  "videoPublicId": "lesson-videos/abc123",
  "sectionId": 1
}
```

Update my lesson:

```http
PUT /api/lessons/instructor/me/{id}
```

Delete my lesson:

```http
DELETE /api/lessons/instructor/me/{id}
```

### Instructor Enrollments

List enrollments the instructor can manage:

```http
GET /api/enrollments
```

List students enrolled in a course:

```http
GET /api/courses/{courseId}/enrollments
```

Update enrollment status:

```http
PATCH /api/enrollments/{id}/status
```

Delete enrollment:

```http
DELETE /api/enrollments/{id}
```

The backend checks whether the instructor can manage that course.

## UI Requirements

Use Bootstrap components:

- Sidebar navigation for admin
- Tables for CRUD lists
- Forms for create/update
- Bootstrap alerts for API errors
- Badges for statuses/categories
- Cards for course store
- Payment status dropdowns in admin
- File inputs for course cover and lesson video uploads
- Profile photo with circular avatar display
- Star rating display for reviews and instructors
- Responsive layout for mobile and desktop

Admin dashboard should feel like an operational tool:

- Dense but readable tables
- Clear forms
- Edit and delete buttons per row
- Refresh data button
- Metrics overview (total users, courses, enrollments, payments)
- No marketing hero inside admin

User course store should feel like a course-selling page:

- Hero area
- Search/filter
- Course cards with cover images from Cloudinary
- Price and rating visible
- Clear buy/enroll button

Student profile page should feel like a settings page:

- Photo upload with preview
- Form with save button
- Student code displayed prominently

Instructor dashboard should feel professional:

- Profile section with photo and bio
- Course management with inline editing
- Section/lesson drill-down from course view

## Suggested Build Order

1. Build shared API helper in `assets/js/lms-api.js`.
2. Build auth pages: login, register, verify OTP, forgot/reset password.
3. Build user course catalog, course detail, payment checkout, and enrollment pages.
4. Build student profile page.
5. Build quiz taking interface.
6. Build instructor dashboard with profile, courses, sections, lessons management.
7. Build admin sidebar dashboard.
8. Add CRUD forms and tables for admin resources (users, courses, categories, etc.).
9. Add file upload controls for course covers, lesson videos, and profile photos.
10. Add admin payment management.
11. Add admin user management (list, role update, delete).
12. Add role guards and redirects.
13. Test each page manually with real JWT tokens.

## Manual Test Checklist

- Register a user (with `fullName` and `phoneNumber`).
- Verify OTP.
- Login as user.
- View and edit student profile.
- Upload student profile photo.
- Browse courses in the store.
- View course detail with sections, lessons, and reviews.
- Create a payment checkout.
- Confirm payment.
- Verify the course enrollment was created after payment.
- View user payment history.
- View user enrollments.
- Take a quiz and submit answers.
- View quiz attempt results.
- Add a course review.
- Cancel enrollment.
- Change password.
- Login as instructor.
- View and edit instructor profile.
- Upload instructor profile photo.
- Create instructor course.
- Create sections for the course.
- Create lessons for the sections.
- Upload lesson video.
- View course enrollments.
- Login as admin.
- Upload a course cover.
- Create/update/delete courses.
- Create/update/delete categories.
- Create/update/delete sections.
- Upload a lesson video.
- Create/update/delete lessons.
- Create/update/delete quizzes.
- Create/update/delete questions.
- Create/update/delete answers.
- List all users.
- Update a user's role.
- Delete a test user.
- List payments as admin.
- Update payment status as admin.
- Delete a test payment as admin.
- Create enrollment for a user.
- Update enrollment status.
- Delete enrollment.
- View all reviews.
- View instructor directory.
- Create an admin user.
