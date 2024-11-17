package com.el;

import com.el.common.Currencies;
import com.el.course.application.dto.*;
import com.el.course.domain.*;
import com.el.course.web.dto.CourseRequestApproveDTO;
import com.el.course.web.dto.CourseRequestRejectDTO;
import com.el.discount.application.dto.DiscountDTO;
import com.el.discount.domain.Discount;
import com.el.discount.domain.Type;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.LessonProgress;
import com.el.order.domain.Order;
import com.el.order.domain.OrderItem;
import org.javamoney.moneta.Money;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class TestFactory {

    public static final String teacher = "teacher";
    public static final String admin = "boss";
    public static final String user = "user";

    public static Course createDefaultCourse() {
        return new Course(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam a accumsan purus, et pellentesque nunc. Ut tempus massa leo, a finibus metus porttitor a. Nulla. ",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed tempor sed lorem ut tristique. Praesent ornare sem placerat leo iaculis, at accumsan dolor fringilla. Aliquam. ",
                "http://example.com/thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed posuere lacus massa, et ullamcorper massa iaculis in. Curabitur vulputate, magna eu aliquam scelerisque, ligula dolor. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis ac diam sed ante bibendum finibus nec ut tortor. Cras sed tincidunt libero. Donec placerat volutpat. "),
                Language.VIETNAMESE,
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris commodo ligula elementum urna euismod faucibus. In hac habitasse platea dictumst. Suspendisse ac tincidunt tortor. Mauris. "),
                Set.of(Language.ENGLISH, Language.SPANISH),
                teacher
        );
    }

    public static Course createCourseWithSections() {
        Course course = new Course("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam a accumsan purus, et pellentesque nunc. Ut tempus massa leo, a finibus metus porttitor a. Nulla. ",
                " Etiam in luctus lacus. Proin luctus iaculis ipsum, vitae pulvinar sapien. Vivamus blandit vestibulum tempor. Aenean id tincidunt purus. Donec egestas, dolor a rutrum gravida, enim nunc rhoncus nulla, in tincidunt nisi dui sed metus. Phasellus ut leo elementum, condimentum quam non, pretium leo. In sed justo vitae purus gravida dapibus quis et justo. Nulla eu dapibus ex, lobortis consequat est. Pellentesque ac porta diam, id faucibus nisl. Nunc volutpat felis eget libero gravida pretium. ",
                "http://example.com/thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed posuere lacus massa, et ullamcorper massa iaculis in. Curabitur vulputate, magna eu aliquam scelerisque, ligula dolor. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis ac diam sed ante bibendum finibus nec ut tortor. Cras sed tincidunt libero. Donec placerat volutpat. "),
                Language.VIETNAMESE,
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris commodo ligula elementum urna euismod faucibus. In hac habitasse platea dictumst. Suspendisse ac tincidunt tortor. Mauris. "),
                Set.of(Language.ENGLISH, Language.SPANISH),
                teacher);
        // add section 1
        CourseSection section1 = Mockito.spy(new CourseSection("Section 1..."));
        Mockito.when(section1.getId()).thenReturn(1L);
        course.addSection(section1);
        // add lessons to section 1

        Lesson lesson1 = new Lesson("Lesson 1.1...",
                Lesson.Type.VIDEO, "http://example.com/lesson1.mp4");
        Lesson lesson2 = new Lesson("Lesson 1.2...",
                Lesson.Type.VIDEO, "http://example.com/lesson2.mp4");

        course.addLessonToSection(1L, lesson1);
        course.addLessonToSection(1L, lesson2);

        CourseSection section2 = Mockito.spy(new CourseSection("Section 2..."));
        Mockito.when(section2.getId()).thenReturn(2L);
        // add section 2
        course.addSection(section2);

        // add lessons to section 2
        Lesson lesson3 = new Lesson("Lesson 2.1...",
                Lesson.Type.VIDEO, "http://example.com/lesson2.1.mp4");
        Lesson lesson4 = new Lesson("Lesson 2.2...",
                Lesson.Type.VIDEO, "http://example.com/lesson2.2.mp4");
        Lesson lesson5 = new Lesson("Lesson 2.3...",
                Lesson.Type.VIDEO, "http://example.com/lesson2.3.mp4");

        course.addLessonToSection(2L, lesson3);
        course.addLessonToSection(2L, lesson4);
        course.addLessonToSection(2L, lesson5);

        return course;
    }

    public static CourseDTO createDefaultCourseDTO() {
        return new CourseDTO(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin vel est enim. Quisque congue odio arcu, at tristique est feugiat in. Cras nec neque eu. ",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce aliquet congue erat eget rhoncus. Sed eget quam et nibh dapibus dignissim. In finibus eleifend turpis. ",
                "http://example.com/thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut nibh sed tellus dapibus auctor. Donec id sapien turpis. Pellentesque vehicula bibendum orci, et cursus. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum eu eleifend nisi, eget tincidunt lorem. Maecenas ac tortor in diam sagittis dignissim sit amet in. "),
                Language.VIETNAMESE,
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent commodo orci vitae neque bibendum laoreet sit amet ac lectus. Phasellus non elit elit. Orci varius. "),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
    }

    public static CourseDTO createCourseDTOBlankTitle() {
        return new CourseDTO(
                "",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce aliquet congue erat eget rhoncus. Sed eget quam et nibh dapibus dignissim. In finibus eleifend turpis. ",
                "http://example.com/thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut nibh sed tellus dapibus auctor. Donec id sapien turpis. Pellentesque vehicula bibendum orci, et cursus. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum eu eleifend nisi, eget tincidunt lorem. Maecenas ac tortor in diam sagittis dignissim sit amet in. "),
                Language.VIETNAMESE,
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent commodo orci vitae neque bibendum laoreet sit amet ac lectus. Phasellus non elit elit. Orci varius. "),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
    }

    public static CourseDTO createCourseDTOTooLargeString() {
        String largeDescription = "A".repeat(2001);
        return new CourseDTO(
                "",
                largeDescription,
                "http://example.com/thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ut nibh sed tellus dapibus auctor. Donec id sapien turpis. Pellentesque vehicula bibendum orci, et cursus. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum eu eleifend nisi, eget tincidunt lorem. Maecenas ac tortor in diam sagittis dignissim sit amet in. "),
                Language.VIETNAMESE,
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent commodo orci vitae neque bibendum laoreet sit amet ac lectus. Phasellus non elit elit. Orci varius. "),
                Set.of(Language.ENGLISH, Language.SPANISH)
        );
    }

    public static CourseUpdateDTO createDefaultCourseUpdateDTO() {
        return new CourseUpdateDTO(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi rhoncus fermentum tortor id vehicula. Suspendisse sodales, lectus at varius egestas, erat dolor fermentum enim, eget. ",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc efficitur, turpis ut porta posuere, odio nibh efficitur ligula, et gravida massa turpis nec quam. Vivamus. ",
                "http://example.com/new-thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec consectetur placerat tincidunt. Fusce in euismod nibh, eget aliquam elit. Fusce fringilla, lorem nec efficitur condimentum. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris quam libero, vulputate ac accumsan a, rutrum ac felis. Pellentesque dapibus faucibus elementum. Fusce vitae quam. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus purus augue, gravida a orci vitae, lacinia malesuada nunc. Integer ornare velit massa, id euismod leo. "),
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam vehicula, magna vitae euismod feugiat, leo ex elementum ligula, ac consequat mi metus nec urna. Sed. "),
                Set.of(Language.ENGLISH, Language.CHINESE)
        );
    }

    public static CourseUpdateDTO createCourseUpdateDTOBlankTitle() {
        return new CourseUpdateDTO(
                "",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc efficitur, turpis ut porta posuere, odio nibh efficitur ligula, et gravida massa turpis nec quam. Vivamus. ",
                "http://example.com/new-thumbnail.jpg",
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec consectetur placerat tincidunt. Fusce in euismod nibh, eget aliquam elit. Fusce fringilla, lorem nec efficitur condimentum. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris quam libero, vulputate ac accumsan a, rutrum ac felis. Pellentesque dapibus faucibus elementum. Fusce vitae quam. ",
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus purus augue, gravida a orci vitae, lacinia malesuada nunc. Integer ornare velit massa, id euismod leo. "),
                Set.of("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam vehicula, magna vitae euismod feugiat, leo ex elementum ligula, ac consequat mi metus nec urna. Sed. "),
                Set.of(Language.ENGLISH, Language.CHINESE)
        );
    }

    public static Discount createDefaultDiscount() {
        return new Discount(
                UUID.randomUUID().toString(),
                Type.PERCENTAGE,
                10.0,
                Money.of(20000, Currencies.VND),
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100
        );
    }

    public static DiscountDTO createDefaultDiscountDTO() {
        return new DiscountDTO(
                UUID.randomUUID().toString(),
                Type.PERCENTAGE,
                10.0,
                Money.of(20000, Currencies.VND),
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100
        );
    }

    public static CourseRequest createDefaultCourseRequestPublish() {
        return new CourseRequest(RequestType.PUBLISH,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec ultrices tellus quis augue molestie scelerisque. Donec laoreet risus eget commodo blandit. Etiam auctor erat sed ullamcorper cursus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Duis elementum ante lorem, vitae pretium augue varius quis. Proin malesuada auctor semper. Aliquam erat volutpat. Ut nibh leo, condimentum eu sollicitudin in, lacinia id ligula. Integer malesuada dui tortor, non varius lectus interdum at. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Suspendisse vitae purus sit amet magna suscipit lobortis volutpat eu ipsum. Ut venenatis eros quis lectus varius volutpat. ",
                teacher);
    }

    public static CourseRequest createDefaultCourseRequestUnPublish() {
        return new CourseRequest(RequestType.UNPUBLISH,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec porttitor, turpis a suscipit volutpat, lacus odio fermentum dui, dapibus varius sem lorem ut elit. Sed nec sem eu ex congue pharetra eu nec metus. Aenean at nibh enim. Donec tincidunt mi et nisl luctus, eget condimentum ex bibendum. Proin varius efficitur dui. Integer a ultrices nunc. Etiam suscipit iaculis ex eget convallis. Nunc ac fringilla dui, nec vehicula lorem. Cras dictum felis ac tellus consequat, sagittis venenatis erat dapibus. Donec tempor faucibus sem at dignissim. Aliquam erat volutpat. Vestibulum et luctus est. Proin porttitor bibendum ipsum ut euismod. Donec condimentum libero congue, consequat nisl a, aliquam tortor. ",
                teacher);
    }

    public static CourseRequestDTO createDefaultCourseRequestDTOPublish() {
        return new CourseRequestDTO(RequestType.PUBLISH, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam a sapien porttitor, maximus risus vitae, congue augue. Aliquam fringilla varius mi, eu malesuada lorem laoreet eget. Donec malesuada lacus lacus, nec accumsan lacus hendrerit in. Donec iaculis tortor nulla, at vestibulum nisl consequat nec. Nunc semper ac est sed volutpat. Nulla ornare, mi nec laoreet maximus, nunc nisl porttitor quam, vitae fermentum massa augue non felis. Praesent sit amet velit fermentum, tristique elit ut, auctor velit. Nulla id sapien eget eros malesuada posuere eget id nisi. Phasellus vulputate, leo eu accumsan vestibulum, mi dolor euismod nunc, at maximus ex diam eu lacus. Aliquam in elit vitae velit elementum dapibus. Morbi sed ligula sed erat finibus hendrerit. In id finibus sapien, quis tincidunt diam. Aliquam id viverra mauris. Donec nec justo eget nibh pharetra dapibus. ",
                teacher);
    }

    public static CourseRequestDTO createDefaultCourseRequestDTOUnPublish() {
        return new CourseRequestDTO(RequestType.UNPUBLISH, "Morbi commodo a lacus vel accumsan. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Fusce faucibus convallis augue, ac egestas dolor fringilla sed. Cras vel hendrerit lacus, in vestibulum erat. Sed sagittis purus quis mauris laoreet gravida a quis nulla. Maecenas tincidunt, tellus id vehicula lobortis, purus diam consequat eros, vel tempor augue nunc sed ligula. Cras vestibulum turpis vel lectus posuere, ut posuere est pretium. Phasellus aliquam at quam euismod suscipit. Vivamus eget risus ut libero cursus interdum. Nam nec tortor ultricies, porta justo sit amet, mattis felis. Nullam varius augue at est dignissim vestibulum at et ligula. Etiam egestas turpis nec velit mattis fringilla lobortis sit amet urna. Curabitur eros sem, aliquet quis arcu ut, commodo porta nisl. Fusce et viverra ante. Donec posuere dictum eleifend. Duis in auctor quam, lobortis egestas eros. ",
                teacher);
    }

    public static CourseRequestResolveDTO createDefaultCourseRequestResolveDTO() {
        return new CourseRequestResolveDTO(" Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam dictum congue ipsum ut laoreet. Vivamus et mi eu felis convallis luctus at nec turpis. Nullam ac orci sapien. Nunc at tempus urna. Nam tristique arcu a velit semper laoreet. In hac habitasse platea dictumst. Duis at diam nibh. Nunc lorem ante, consectetur ac ipsum a, facilisis pellentesque massa. Cras nec tincidunt erat. Donec in dui nisl. ",
                admin);
    }

    public static CourseRequestApproveDTO createDefaultCourseRequestApproveDTOPublish() {
        return new CourseRequestApproveDTO(RequestType.PUBLISH, " Aliquam tincidunt justo purus, vitae pulvinar felis porttitor pulvinar. Pellentesque tempus aliquam nibh a ullamcorper. Nunc ac varius tortor. Maecenas ullamcorper faucibus eleifend. Phasellus dictum dolor ac varius fermentum. Praesent congue accumsan consectetur. Proin in elementum velit. Phasellus convallis lobortis ligula eu tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Fusce lobortis placerat enim, non suscipit dui. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed quis justo ut tortor dictum aliquam. Duis sit amet nibh dolor. ",
                admin);
    }

    public static CourseRequestApproveDTO createDefaultCourseRequestApproveDTOUnPublish() {
        return new CourseRequestApproveDTO(RequestType.UNPUBLISH, " Aliquam tincidunt justo purus, vitae pulvinar felis porttitor pulvinar. Pellentesque tempus aliquam nibh a ullamcorper. Nunc ac varius tortor. Maecenas ullamcorper faucibus eleifend. Phasellus dictum dolor ac varius fermentum. Praesent congue accumsan consectetur. Proin in elementum velit. Phasellus convallis lobortis ligula eu tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Fusce lobortis placerat enim, non suscipit dui. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed quis justo ut tortor dictum aliquam. Duis sit amet nibh dolor. ",
                admin);
    }

    public static CourseRequestRejectDTO createDefaultCourseRequestRejectDTOPublish() {
        return new CourseRequestRejectDTO(RequestType.PUBLISH, " Quisque orci metus, dignissim et ultrices vitae, condimentum at augue. Etiam euismod commodo accumsan. Suspendisse at tellus lectus. Vivamus est velit, hendrerit a erat sed, tempor sollicitudin turpis. Mauris porttitor sagittis sem, aliquet tempor mauris consectetur in. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam accumsan leo eget risus hendrerit, eu euismod ligula pulvinar. Donec ut turpis quis metus lacinia sollicitudin. Proin ut tellus dolor. Nam quis tincidunt tellus. Mauris ultricies dolor quam, eu feugiat mauris condimentum vitae. Maecenas non aliquam nisi, et efficitur dui. ",
                admin);
    }
    // Orders

    public static Order createDefaultOrder() {
        Set<OrderItem> items = Set.of(
                new OrderItem(1L, Money.of(1000, Currencies.VND)),
                new OrderItem(2L, Money.of(2000, Currencies.VND))
        );
        return new Order(items);
    }
    // Course Enrollment

    public static CourseEnrollment createDefaultCourseEnrollment() {
        return new CourseEnrollment(user, 1L, teacher, Set.of(
                new LessonProgress("Course Lesson 1", 1L), new LessonProgress("Course Lesson 2", 2L)));
    }

    public static CourseEnrollment createCourseEnrollmentWithEmptyLessonProgress() {
        return new CourseEnrollment(user, 1L, teacher, Set.of());
    }

    public static Post createDefaultPost() {
        return new Post(
                """
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam feugiat suscipit ultricies. Sed suscipit massa quis lorem fermentum, et rutrum augue pretium. Phasellus placerat tortor sapien, vel tristique diam accumsan ultrices. Proin facilisis pharetra orci, non eleifend lorem varius at. Proin malesuada augue eu mi consequat varius. Curabitur cursus lobortis neque, eu posuere dui. Etiam consectetur, orci quis dapibus luctus, ipsum arcu sagittis lectus, eget consectetur quam nulla ut leo. Pellentesque eu ligula sagittis, aliquam quam ac, fermentum neque. Suspendisse scelerisque augue eu gravida faucibus. Mauris tristique convallis erat at iaculis. Integer sit amet ultrices massa. Vestibulum quam turpis, vestibulum id tincidunt vitae, sollicitudin nec nunc. Praesent varius et magna scelerisque volutpat. Aenean rhoncus libero id commodo finibus. Pellentesque orci neque, vulputate et commodo non, placerat eu libero.
                        
                        Morbi risus quam, placerat non sem a, ullamcorper pharetra lorem. Quisque pharetra nisl in efficitur viverra. Vestibulum dictum nisl sit amet pharetra viverra. Sed eros mauris, mollis eget iaculis in, vulputate sit amet velit. Aliquam quis dictum enim. Duis vestibulum vulputate sapien id dictum. Suspendisse vitae leo et eros cursus sagittis. Mauris quis nisi eu mauris tristique ullamcorper ut id lacus.
                        
                        Aenean eros sapien, sagittis cursus enim ut, luctus mollis ex. Aliquam eleifend bibendum quam, nec condimentum augue accumsan id. Suspendisse congue commodo nulla, ac faucibus est hendrerit ac. Donec a commodo ipsum. Ut imperdiet augue non ultrices venenatis. Morbi ornare urna nec nibh maximus eleifend finibus eu lacus. Praesent a maximus ex. Sed pretium, neque eget fringilla tempor, lorem tortor convallis dolor, eu sollicitudin ante metus vitae arcu. Donec rutrum magna eros, at consequat purus congue a.
                        
                        Duis non elit diam. Cras eleifend rhoncus erat, a consectetur ex. Phasellus vehicula massa eget nunc lacinia maximus. Cras interdum neque sodales dui ullamcorper, id viverra sapien tempus. Nullam consectetur dui vitae imperdiet porta. Sed consectetur leo non tortor imperdiet, eget ullamcorper erat gravida. Sed at dui eget sem suscipit congue pretium nec magna.
                        
                        Donec eleifend dignissim enim, et rutrum eros eleifend vel. Mauris interdum non leo at tincidunt. Aliquam tincidunt ac elit id ornare. Etiam felis magna, lobortis sed luctus hendrerit, semper non velit. Nunc vel odio vel justo gravida mattis at sed felis. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Phasellus sed nisl euismod urna laoreet ullamcorper. Proin feugiat sem at libero ultricies semper. Morbi rutrum magna ac mauris cursus sollicitudin. Cras commodo neque sapien, imperdiet rhoncus sapien pretium ac. Integer sed enim non nisi aliquet placerat. Vivamus scelerisque feugiat diam, non blandit mauris aliquet ac. Vivamus ullamcorper diam dui, sit amet rhoncus velit pellentesque non. Nam luctus urna in purus porttitor eleifend.\s
                        """,
                new UserInfo("thai", "nguyen", "user"),
                Set.of("http://example.com/photo1.jpg", "http://example.com/photo2.jpg")
        );
    }

    public static com.el.common.auth.web.dto.UserInfo createDefaultUserInfo() {
        return new com.el.common.auth.web.dto.UserInfo(
                UUID.randomUUID().toString(),
                "nguyen",
                "thai",
                user,
                "nguyennt11032004@gmail.com");
    }

    public static CoursePostDTO createDefaultCoursePostDTO() {
        return new CoursePostDTO(
                """
                         Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus auctor, turpis eu gravida lobortis, dolor ipsum dignissim elit, quis fringilla velit arcu sed mi. Curabitur bibendum efficitur libero in facilisis. Fusce egestas non neque at pharetra. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Sed porttitor ut enim id luctus. Nunc quis mauris id dolor sollicitudin vestibulum. Phasellus pharetra lectus et dui porta, sit amet sagittis orci convallis. Praesent libero turpis, tincidunt non dapibus non, condimentum vel mauris.
                        
                        Suspendisse fermentum ultrices orci ut porttitor. Pellentesque ut dignissim elit. Fusce faucibus elit sit amet metus ultricies, at fringilla sem lobortis. Donec ornare convallis est, sit amet porta odio accumsan et. Integer posuere mi arcu, at sodales orci consequat eu. Phasellus pharetra posuere facilisis. Nunc accumsan nulla sit amet neque euismod, nec sodales turpis lacinia. Curabitur lobortis neque in lectus condimentum efficitur. Vivamus purus ligula, sagittis malesuada congue ac, scelerisque non dui. Sed ut tincidunt ex.\s""",
                Set.of("http://example.com/photo1.jpg", "http://example.com/photo2.jpg")
        );
    }

    public static CoursePostDTO createCoursePostDTOBadRequest() {
        return new CoursePostDTO(
                "",
                Set.of("http://example.com/photo1.jpg", "http://example.com/photo2.jpg")
        );
    }

}
