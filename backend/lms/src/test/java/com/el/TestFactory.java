package com.el;

import com.el.course.application.dto.CourseDTO;
import com.el.course.application.dto.CourseUpdateDTO;
import com.el.course.domain.Course;
import com.el.course.domain.Language;

import java.util.Set;

public class TestFactory {

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
                "teacher123"
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

}
