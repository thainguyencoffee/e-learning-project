INSERT INTO public.course (id, title, thumbnail_url, published, published_date, unpublished, unpublished_date,
                           description, price, teacher, language, subtitles, benefits, prerequisites, approved_by,
                           created_by, created_date, last_modified_by, last_modified_date, deleted, version)
VALUES (1, 'The Complete Java Development Bootcamp',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/fb777822-edce-4a96-982f-ccf559f1a6ee_3740582_cc2c_7.jpg',
        true, '2024-11-23 20:52:37.809528', false, null,
        e'Are you ready to take your programming skills to the next level? Our comprehensive Java course is designed to help you build a solid foundation in one of the world\'s most popular programming languages,
        opening up a world of opportunities for your career.

Join us as we guide you through the essentials of Java, from basic syntax and data structures to advanced concepts like
        lambda expressions, inheritance, and stream operations.With our interactive course format, you\'ll have access to workbooks and challenges that will help you apply your newfound knowledge and reinforce your learning.

Here\' s a sneak peek into what you can expect in this course :
        Module 1 : Java Fundamentals
        Variables
        Conditionals
        Functions
        Loops
        Arrays
        Module 2 : Object - Oriented Programming
        Objects
        Immutable Objects
        List Collections
        Map Collections
        Exception Handling
        Enums
        Inheritance
        Higher - order Functions
        Lambda Expressions
        Stream Operations
        Interfaces
        Interactive Learning Experience - Our course is designed with your success in mind.Hundreds of Workbooks and
        challenges will help you practice and apply what you\'ve learned, ensuring you\' re ready to tackle real - world
        problems.
        Boost Your Career Prospects - With a thorough understanding of Java, you\'ll be well-equipped to excel in various domains like web development, mobile app development, automation, and more.


Don\' t miss out on this opportunity to enhance your programming skills and gain a competitive edge in the industry.
        Enroll in our Java course today and get ready to transform your programming journey ! We can\'t wait to see you in class!',
        'VND 80000', 'teacher', 'ENGLISH', '{VIETNAMESE}',
        '{Learn advanced Java concepts that you can present to prospective employers.,Be able to program in Java professionally.,Get hands-on experience and solve 100+ coding exercises.,Become proficient in Java 17.,Acquire the Java skills needed to pursue Web Development (Spring Boot), Android Development, Automation, and more!}',
        '{A commitment to learn Java}', 'boss', 'boss', '2024-11-23 20:17:18.433851', 'boss',
        '2024-11-23 20:52:37.810200', false, 29);

INSERT INTO public.course_section (id, title, order_index, published, course)
VALUES (1, 'Introduction to building projects', 1, true, 1);
INSERT INTO public.course_section (id, title, order_index, published, course)
VALUES (2, 'Introduction about Domain Driven Design (DDD)', 2, true, 1);


INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (1, 'Infinite Scroll Introduction', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/4f05b797-5d5c-4b61-8381-55498a9ce38e_WebHD_720p.mp4', 1,
        1);
INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (2, 'Scroll project Resources', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/cc1207c5-cdfe-4704-897a-0b5215f24cb8_WebHD_720p_2.mp4', 2,
        1);
INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (3, 'Setup basic application page', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/4b1826ea-5661-4011-b232-d3c8017aeb34_WebHD_720p_3.mp4', 3,
        1);
INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (4, 'Load more entries tweak source code jQuery', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/f7ca2b71-c023-4e1f-baa2-bcff8e317c7a_WebHD_720p_4.mp4', 1,
        2);
INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (5, 'PHP code examples', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/db1d7c5c-0bd6-49ab-bd65-f0d1b85de073_WebHD_720p_5.mp4', 2,
        2);
INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (6, 'index.html jQuery source code', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/40e794dc-6eb7-443d-910e-463566ac802c_WebHD_720p_6.mp4', 3,
        2);
INSERT INTO public.lesson (id, title, type, link, order_index, course_section)
VALUES (7, 'App development overview PHP', 'VIDEO',
        'https://bookstore-bucket.sgp1.digitaloceanspaces.com/fbfc2f39-4a0e-4ba2-a422-e2da2f48ba23_WebHD_720p_7.mp4', 4,
        2);

INSERT INTO public.quiz (id, course_section, title, description, after_lesson_id, total_score, pass_score_percentage,
                         deleted)
VALUES (1, 1, 'Quiz về lập trình hướng đối tượng OOP', null, 3, 11, 50, false);
INSERT INTO public.quiz (id, course_section, title, description, after_lesson_id, total_score, pass_score_percentage,
                         deleted)
VALUES (2, 2, 'Quiz về Domain Driven Design', null, 7, 32, 50, false);

INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (1, 1, 'Đâu là các đặc điểm chính của lập trình hướng đối tượng?', 'MULTIPLE_CHOICE', 3, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (2, 1, 'Đâu là ĐÚNG về phương thức tĩnh (static) trong OOP?', 'MULTIPLE_CHOICE', 3, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (3, 1, 'Phát biểu nào sau đây về tính đóng gói (Encapsulation) là ĐÚNG?', 'SINGLE_CHOICE', 5, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (4, 2, 'Đâu là các khái niệm chính trong Domain-Driven Design (DDD)?', 'MULTIPLE_CHOICE', 5, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (5, 2, 'Phát biểu nào sau đây về Bounded Context là đúng?', 'MULTIPLE_CHOICE', 5, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (6, 2, 'Aggregate Root có trách nhiệm gì?', 'SINGLE_CHOICE', 5, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (7, 2, 'Đâu là sự khác biệt chính giữa Entity và Value Object trong DDD?', 'MULTIPLE_CHOICE', 4, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (8, 2, 'Phát biểu nào sau đây về Repository trong DDD là đúng?', 'MULTIPLE_CHOICE', 5, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (9, 2, 'Tính năng chính của Ubiquitous Language trong DDD là gì?', 'SINGLE_CHOICE', 5, null);
INSERT INTO public.question (id, quiz, content, type, score, true_false_answer)
VALUES (10, 2, 'Sự khác biệt giữa Domain Event và Application Event là gì?', 'MULTIPLE_CHOICE', 3, null);


INSERT INTO public.answer_option (id, question, content, correct)
VALUES (1, 1, 'Kế thừa (Inheritance)', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (2, 1, 'Trừu tượng hóa (Abstraction)', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (3, 1, 'Đóng gói (Encapsulation)', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (4, 1, 'Đa hình (Polymorphism)', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (5, 1, 'Hàm đệ quy (Recursive Function)', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (6, 2, 'Phương thức tĩnh có thể được gọi mà không cần khởi tạo đối tượng.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (7, 2, 'Phương thức tĩnh có thể ghi đè (override) trong lớp con.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (8, 2, 'Phương thức tĩnh thuộc về lớp, không phải đối tượng.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (9, 2, 'Phương thức tĩnh có thể truy cập trực tiếp các biến không tĩnh của đối tượng.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (10, 3, 'Tính đóng gói yêu cầu tất cả các thuộc tính của lớp phải là private.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (11, 3, 'Lớp có thể sử dụng getter và setter để kiểm soát quyền truy cập vào các thuộc tính.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (12, 3, 'Tính đóng gói giúp giảm thiểu sự phụ thuộc giữa các lớp.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (13, 3, 'Tính đóng gói chỉ liên quan đến việc bảo vệ dữ liệu, không liên quan đến logic xử lý.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (14, 4, 'Aggregate', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (15, 4, 'Value Object', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (16, 4, 'Entity', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (17, 4, 'Bounded Context', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (18, 4, 'Singleton', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (19, 5, 'Hai Bounded Context luôn sử dụng cùng một ngôn ngữ chung (Ubiquitous Language).', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (20, 5, 'Bounded Context và Microservices là hai khái niệm tương tự.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (21, 5, 'Một Bounded Context là một biên giới logic để xác định các phần của domain model.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (22, 5, 'Một domain có thể được chia thành nhiều Bounded Context.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (23, 6, 'Lưu trữ toàn bộ các Entities trong hệ thống.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (24, 6, 'Cung cấp giao diện RESTful API.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (25, 6, 'Đảm bảo tính toàn vẹn (consistency) của toàn bộ Aggregate.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (26, 6, 'Quản lý các bảng trong cơ sở dữ liệu.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (27, 7, 'Value Object có thể được chia sẻ giữa các Entities khác nhau.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (28, 7, 'Value Object không bao giờ thay đổi trạng thái sau khi được tạo (immutable).', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (29, 7, 'Entity có định danh duy nhất (unique identifier), còn Value Object không cần định danh.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (30, 7, 'Entity luôn tồn tại trong cơ sở dữ liệu, còn Value Object chỉ tồn tại trong code.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (31, 8, 'Repository giúp domain model tách biệt khỏi logic lưu trữ dữ liệu.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (32, 8, 'Repository là lớp trung gian để quản lý truy vấn và lưu trữ dữ liệu cho Aggregate.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (33, 8, 'Repository nên trực tiếp xử lý các truy vấn SQL phức tạp.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (34, 8, 'Một Repository có thể quản lý nhiều Aggregate.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (35, 9, 'Ngôn ngữ lập trình duy nhất được sử dụng trong dự án.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (36, 9, 'Ngôn ngữ chung giữa đội phát triển và các bên liên quan trong một domain cụ thể.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (37, 9, 'Ngôn ngữ chính thức được sử dụng trong các tài liệu kỹ thuật.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (38, 9, 'Một framework để tự động hóa giao tiếp giữa các microservices', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (39, 10, 'Application Event thường phục vụ mục đích kỹ thuật (như ghi log hoặc gửi thông báo).', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (40, 10, 'Application Event luôn được lưu trữ trong cơ sở dữ liệu.', false);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (41, 10, 'Domain Event mô tả điều gì đã xảy ra trong domain và là một phần của domain model.', true);
INSERT INTO public.answer_option (id, question, content, correct)
VALUES (42, 10, 'Domain Event có ý nghĩa kinh doanh, còn Application Event thì không.', true);


INSERT INTO public.course_request (id, course, type, status, resolved, resolved_by, requested_by, message,
                                   reject_reason, approve_message)
VALUES (1, 1, 'PUBLISH', 'APPROVED', true, 'boss', 'teacher',
        E'Tôi viết email này để gửi yêu cầu phê duyệt xuất bản khóa học "Java Fundamentals" mà tôi đã hoàn thành. Khóa học này nhằm cung cấp kiến thức cơ bản và nền tảng vững chắc về Java cho người học mới bắt đầu.\n\nTôi đã kiểm tra kỹ nội dung và đảm bảo rằng khóa học tuân thủ các tiêu chuẩn về chất lượng và hình thức. Nếu có bất kỳ thông tin nào cần điều chỉnh hoặc bổ sung, xin vui lòng phản hồi để tôi có thể hoàn thiện.\n\nTôi rất mong nhận được sự phê duyệt của Admin để khóa học có thể đến với học viên sớm nhất.',
        null,
        'Cảm ơn bạn đã gửi yêu cầu xuất bản khóa học "Java Fundamentals". Sau khi xem xét nội dung và các thông tin liên quan, tôi vui mừng thông báo rằng khóa học của bạn đã được phê duyệt xuất bản.');
