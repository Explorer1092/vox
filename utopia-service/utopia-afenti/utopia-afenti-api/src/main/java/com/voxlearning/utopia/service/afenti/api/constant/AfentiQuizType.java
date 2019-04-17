package com.voxlearning.utopia.service.afenti.api.constant;

/**
 * @author Ruib
 * @since 2016/10/20
 */
public enum AfentiQuizType {
    UNIT_QUIZ, TERM_QUIZ;

    public static AfentiQuizType safeParse(String name) {
        try {
            return AfentiQuizType.valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
