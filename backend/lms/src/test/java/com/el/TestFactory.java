package com.el;

import com.el.course.application.dto.CourseDTO;
import com.el.course.application.dto.CourseRequestDTO;
import com.el.course.application.dto.CourseRequestResolveDTO;
import com.el.course.application.dto.CourseUpdateDTO;
import com.el.course.domain.Course;
import com.el.course.domain.CourseRequest;
import com.el.course.domain.Language;
import com.el.course.domain.RequestType;
import com.el.course.web.CourseRequestApproveDTO;
import com.el.course.web.CourseRequestRejectDTO;
import com.el.discount.application.dto.DiscountDTO;
import com.el.discount.domain.Discount;
import com.el.discount.domain.Type;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class TestFactory {

    public static final String teacherId = "9db2e8a4-ca81-43ca-bfb4-bef6fa9e0844";
    public static final String adminId = "fc23d891-5df9-4fbf-a063-6d77c11f884a";

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
                teacherId
        );
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
                null,
                LocalDateTime.now().minusSeconds(3600),
                LocalDateTime.now().plusSeconds(3600),
                100
        );
    }

    public static CourseRequest createDefaultCourseRequestPublish() {
        return new CourseRequest(RequestType.PUBLISH,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec ultrices tellus quis augue molestie scelerisque. Donec laoreet risus eget commodo blandit. Etiam auctor erat sed ullamcorper cursus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Duis elementum ante lorem, vitae pretium augue varius quis. Proin malesuada auctor semper. Aliquam erat volutpat. Ut nibh leo, condimentum eu sollicitudin in, lacinia id ligula. Integer malesuada dui tortor, non varius lectus interdum at. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Suspendisse vitae purus sit amet magna suscipit lobortis volutpat eu ipsum. Ut venenatis eros quis lectus varius volutpat. ",
                teacherId);
    }

    public static CourseRequest createDefaultCourseRequestUnPublish() {
        return new CourseRequest(RequestType.UNPUBLISH,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec porttitor, turpis a suscipit volutpat, lacus odio fermentum dui, dapibus varius sem lorem ut elit. Sed nec sem eu ex congue pharetra eu nec metus. Aenean at nibh enim. Donec tincidunt mi et nisl luctus, eget condimentum ex bibendum. Proin varius efficitur dui. Integer a ultrices nunc. Etiam suscipit iaculis ex eget convallis. Nunc ac fringilla dui, nec vehicula lorem. Cras dictum felis ac tellus consequat, sagittis venenatis erat dapibus. Donec tempor faucibus sem at dignissim. Aliquam erat volutpat. Vestibulum et luctus est. Proin porttitor bibendum ipsum ut euismod. Donec condimentum libero congue, consequat nisl a, aliquam tortor. ",
                teacherId);
    }

    public static CourseRequestDTO createDefaultCourseRequestDTOPublish() {
        return new CourseRequestDTO(RequestType.PUBLISH, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam a sapien porttitor, maximus risus vitae, congue augue. Aliquam fringilla varius mi, eu malesuada lorem laoreet eget. Donec malesuada lacus lacus, nec accumsan lacus hendrerit in. Donec iaculis tortor nulla, at vestibulum nisl consequat nec. Nunc semper ac est sed volutpat. Nulla ornare, mi nec laoreet maximus, nunc nisl porttitor quam, vitae fermentum massa augue non felis. Praesent sit amet velit fermentum, tristique elit ut, auctor velit. Nulla id sapien eget eros malesuada posuere eget id nisi. Phasellus vulputate, leo eu accumsan vestibulum, mi dolor euismod nunc, at maximus ex diam eu lacus. Aliquam in elit vitae velit elementum dapibus. Morbi sed ligula sed erat finibus hendrerit. In id finibus sapien, quis tincidunt diam. Aliquam id viverra mauris. Donec nec justo eget nibh pharetra dapibus. ",
                teacherId);
    }

    public static CourseRequestDTO createDefaultCourseRequestDTOUnPublish() {
        return new CourseRequestDTO(RequestType.UNPUBLISH, "Morbi commodo a lacus vel accumsan. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Fusce faucibus convallis augue, ac egestas dolor fringilla sed. Cras vel hendrerit lacus, in vestibulum erat. Sed sagittis purus quis mauris laoreet gravida a quis nulla. Maecenas tincidunt, tellus id vehicula lobortis, purus diam consequat eros, vel tempor augue nunc sed ligula. Cras vestibulum turpis vel lectus posuere, ut posuere est pretium. Phasellus aliquam at quam euismod suscipit. Vivamus eget risus ut libero cursus interdum. Nam nec tortor ultricies, porta justo sit amet, mattis felis. Nullam varius augue at est dignissim vestibulum at et ligula. Etiam egestas turpis nec velit mattis fringilla lobortis sit amet urna. Curabitur eros sem, aliquet quis arcu ut, commodo porta nisl. Fusce et viverra ante. Donec posuere dictum eleifend. Duis in auctor quam, lobortis egestas eros. ",
                teacherId);
    }

    public static CourseRequestResolveDTO createDefaultCourseRequestResolveDTO() {
        return new CourseRequestResolveDTO(" Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam dictum congue ipsum ut laoreet. Vivamus et mi eu felis convallis luctus at nec turpis. Nullam ac orci sapien. Nunc at tempus urna. Nam tristique arcu a velit semper laoreet. In hac habitasse platea dictumst. Duis at diam nibh. Nunc lorem ante, consectetur ac ipsum a, facilisis pellentesque massa. Cras nec tincidunt erat. Donec in dui nisl. ",
                adminId);
    }

    public static CourseRequestApproveDTO createDefaultCourseRequestApproveDTOPublish() {
        return new CourseRequestApproveDTO(RequestType.PUBLISH, " Aliquam tincidunt justo purus, vitae pulvinar felis porttitor pulvinar. Pellentesque tempus aliquam nibh a ullamcorper. Nunc ac varius tortor. Maecenas ullamcorper faucibus eleifend. Phasellus dictum dolor ac varius fermentum. Praesent congue accumsan consectetur. Proin in elementum velit. Phasellus convallis lobortis ligula eu tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Fusce lobortis placerat enim, non suscipit dui. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed quis justo ut tortor dictum aliquam. Duis sit amet nibh dolor. ",
                adminId);
    }

    public static CourseRequestApproveDTO createDefaultCourseRequestApproveDTOUnPublish() {
        return new CourseRequestApproveDTO(RequestType.UNPUBLISH, " Aliquam tincidunt justo purus, vitae pulvinar felis porttitor pulvinar. Pellentesque tempus aliquam nibh a ullamcorper. Nunc ac varius tortor. Maecenas ullamcorper faucibus eleifend. Phasellus dictum dolor ac varius fermentum. Praesent congue accumsan consectetur. Proin in elementum velit. Phasellus convallis lobortis ligula eu tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Fusce lobortis placerat enim, non suscipit dui. Interdum et malesuada fames ac ante ipsum primis in faucibus. Sed quis justo ut tortor dictum aliquam. Duis sit amet nibh dolor. ",
                adminId);
    }

    public static CourseRequestRejectDTO createDefaultCourseRequestRejectDTOPublish() {
        return new CourseRequestRejectDTO(RequestType.PUBLISH, " Quisque orci metus, dignissim et ultrices vitae, condimentum at augue. Etiam euismod commodo accumsan. Suspendisse at tellus lectus. Vivamus est velit, hendrerit a erat sed, tempor sollicitudin turpis. Mauris porttitor sagittis sem, aliquet tempor mauris consectetur in. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam accumsan leo eget risus hendrerit, eu euismod ligula pulvinar. Donec ut turpis quis metus lacinia sollicitudin. Proin ut tellus dolor. Nam quis tincidunt tellus. Mauris ultricies dolor quam, eu feugiat mauris condimentum vitae. Maecenas non aliquam nisi, et efficitur dui. ",
                adminId);
    }

}
