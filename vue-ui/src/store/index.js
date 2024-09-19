import { createStore } from 'vuex';

const store = createStore({
    state: {
        courses: [],
    },
    mutations: {
        setCourses(state, courses) {
            state.courses = courses;
        },
        addSection(state, { courseIndex, section }) {
            state.courses[courseIndex].sections.push(section);
        },
        updateSection(state, { courseIndex, sectionIndex, section }) {
            state.courses[courseIndex].sections[sectionIndex] = section;
        },
        addLesson(state, { courseIndex, sectionIndex, lesson }) {
            state.courses[courseIndex].sections[sectionIndex].lessons.push(lesson);
        },
        updateLesson(state, { courseIndex, sectionIndex, lessonIndex, lesson }) {
            state.courses[courseIndex].sections[sectionIndex].lessons[lessonIndex] = lesson;
        },
    },
    actions: {
        fetchCourses({ commit }) {
            const demoCourse = {
                title: "Demo course",
                audience: {
                    isPublic: false,
                    emailAuthorities: ["fpt.edu.vn"]
                },
                sections: [
                    {
                        id: null,
                        title: "some section title",
                        description: "some section des",
                        lessons: [
                            {
                                id: null,
                                title: "some lesson title",
                                type: "VIDEO",
                                link: "somelink.com"
                            }
                        ]
                    }
                ],
                price: "USD1,000.00",
                discountedPrice: "USD1,000.00",
                description: "Demo description",
                students: [],
                discountId: null
            };
            commit('setCourses', [demoCourse]);
        },
        saveSection({ commit, state }, { courseIndex, section }) {
            const sectionIndex = state.courses[courseIndex].sections.findIndex(s => s.id === section.id);
            if (section.id === null) {
                commit('addSection', { courseIndex, section });
            } else {
                commit('updateSection', { courseIndex, sectionIndex, section });
            }
        },
        saveLesson({ commit, state }, { courseIndex, sectionIndex, lesson }) {
            const lessonIndex = state.courses[courseIndex].sections[sectionIndex].lessons.findIndex(l => l.id === lesson.id);
            if (lesson.id === null) {
                commit('addLesson', { courseIndex, sectionIndex, lesson });
            } else {
                commit('updateLesson', { courseIndex, sectionIndex, lessonIndex, lesson });
            }
        },
    }
});

export default store;
