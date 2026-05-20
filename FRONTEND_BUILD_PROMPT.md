# LMS Frontend Build Prompt

Build a  frontend for this Spring Boot LMS backend using the existing REST APIs.

## Backend API Readiness

The current APIs are enough for a functional LMS prototype:

- Authentication with JWT login
- User registration
- OTP verification
- Password reset OTP flow
- Course catalog
- Course CRUD
- Category CRUD
- Section CRUD
- Lesson CRUD
- Quiz CRUD
- Question CRUD
- Answer CRUD
- Payment checkout/confirmation
- Payment admin status management
- File/media upload for course covers and lesson videos
- Enrollment create/list/update/delete
- Reviews list/create
- Role-based access for `ADMIN`, `INSTRUCTOR`, and `USER`
- 
## Shared Frontend Requirements

- Use plain React.
- Store JWT token in `localStorage`.
- Send JWT token on protected API calls:

```js
Authorization: Bearer <token>
```

- Decode the JWT payload on the frontend to determine the user role.
- Redirect users based on role after login:
  - `ADMIN` -> `/app/admin/dashboard.html`
  - `INSTRUCTOR` -> `/app/instructor/dashboard.html`
  - `USER` -> `/app/user/index.html`
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
  "email": "student@example.com",
  "username": "student1",
  "password": "password",
  "role": "USER"
}
```

Allowed roles:

```text
USER
INSTRUCTOR
```

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

Display:

- Course cover image
- Course title
- Description
- Price
- Duration
- Instructor
- Categories
- Rating
- Buy/enroll button

Add:

- Search by title/instructor/category
- Category badges
- Responsive Bootstrap course cards
- Hero section focused on selling courses

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
  "providerReference": "checkout_xxx",
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

Optional user page or panel:

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
- Status
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

Display:

- Course title
- Instructor
- Status
- Enrolled date
- Cancel button

Cancel enrollment:

```http
DELETE /api/enrollments/me/courses/{courseId}
```

## File Uploads

The backend supports multipart uploads for course cover images and lesson videos. Uploaded files are stored under the configured upload directory and served publicly from `/uploads/**`.

Use these upload APIs from admin and instructor forms before creating or updating courses/lessons. Save the returned `url` into `coverDir` or `videoDir`.

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
  "fileName": "generated-name.jpg",
  "contentType": "image/jpeg",
  "size": 120000,
  "url": "/uploads/course-covers/generated-name.jpg"
}
```

Use the returned URL in course create/update:

```json
{
  "coverDir": "/uploads/course-covers/generated-name.jpg"
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
  "fileName": "generated-name.mp4",
  "contentType": "video/mp4",
  "size": 5000000,
  "url": "/uploads/lesson-videos/generated-name.mp4"
}
```

Use the returned URL in lesson create/update:

```json
{
  "videoDir": "/uploads/lesson-videos/generated-name.mp4"
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
- Courses
- Categories
- Sections
- Lessons
- Quizzes
- Questions
- Answers
- Payments
- Enrollments
- Admin users

Only allow users with role `ADMIN`.

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
  "coverDir": "/uploads/course-covers/generated-name.jpg",
  "instructor": 1,
  "categoryId": [1, 2]
}
```

### Admin Categories CRUD

List:

```http
GET /api/categories
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
  "videoDir": "/uploads/lesson-videos/generated-name.mp4",
  "sectionId": 1
}
```

### Admin Quizzes CRUD

List:

```http
GET /api/quizzes
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

### Admin Enrollments

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

When an admin changes a payment to `PAID`, the backend should create the course enrollment for that student.

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

### Admin Creation

Create admin:

```http
POST /admin/register
```

Body:

```json
{
  "email": "admin@example.com",
  "username": "admin2",
  "password": "password",
  "role": "ADMIN"
}
```

Note: the backend does not currently expose a full users CRUD/list endpoint, so the admin dashboard can create admins but cannot list all users unless a user management API is added.

## Instructor Dashboard

Page:

```text
/app/instructor/dashboard.html
```

Only allow users with role `INSTRUCTOR` or `ADMIN`.

Instructor should be able to:

- View their courses
- Create their courses
- View enrollments for courses they manage
- View enrolled students for a selected course
- Update enrollment statuses if allowed by backend

### Instructor Courses

Use:

```http
GET /api/courses/instructor/me
```

Create:

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
  "coverDir": "/uploads/course-covers/generated-name.jpg",
  "instructor": null,
  "categoryId": [1]
}
```

The backend should use the JWT-authenticated instructor as the course owner.

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

The backend already checks whether the instructor can manage that course.

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
- Responsive layout for mobile and desktop

Admin dashboard should feel like an operational tool:

- Dense but readable tables
- Clear forms
- Edit and delete buttons per row
- Refresh data button
- Metrics overview
- No marketing hero inside admin

User course store should feel like a course-selling page:

- Hero area
- Search/filter
- Course cards
- Price and rating visible
- Clear buy/enroll button

## Suggested Build Order

1. Build shared API helper in `assets/js/lms-api.js`.
2. Build auth pages: login, register, verify OTP, forgot/reset password.
3. Build user course catalog, payment checkout, and enrollment pages.
4. Build instructor dashboard.
5. Build admin sidebar dashboard.
6. Add CRUD forms and tables for admin resources.
7. Add file upload controls for course covers and lesson videos.
8. Add admin payment management.
9. Add role guards and redirects.
10. Test each page manually with real JWT tokens.

## Manual Test Checklist

- Register a user.
- Verify OTP.
- Login as user.
- Browse courses.
- Create a payment checkout.
- Confirm payment.
- Verify the course enrollment was created after payment.
- View user payment history.
- View user enrollments.
- Cancel enrollment.
- Login as instructor.
- Create instructor course.
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
- List payments as admin.
- Update payment status as admin.
- Delete a test payment as admin.
- Create enrollment for a user.
- Update enrollment status.
- Delete enrollment.
