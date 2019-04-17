package com.voxlearning.utopia.service.afenti.impl.util;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolYearPhase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.voxlearning.alps.annotation.meta.Subject.*;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_GRADE;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * @author Ruib
 * @since 2016/7/1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract public class AfentiUtils {
    private static final String preparationBookIdPrefix = "YX_";

    private static final String reviewBookIdPrefix = "RX_";

    private static final String newRankBookIdPrefix = "N_";

    public static boolean isSubjectAvailable(Subject subject) {
        return subject != null && UtopiaAfentiConstants.AVAILABLE_SUBJECT.contains(subject);
    }

    public static boolean isQuizSubjectAvailable(Subject subject) {
        return subject != null && UtopiaAfentiConstants.AVAILABLE_QUIZ_SUBJECT.contains(subject);
    }

    public static boolean isTermQuizSubjectAvailable(Subject subject) {
        return subject != null && UtopiaAfentiConstants.AVAILABLE_TERM_QUIZ_SUBJECT.contains(subject);
    }

    public static OrderProductServiceType getOrderProductServiceType(Subject subject) {
        if (!AVAILABLE_SUBJECT.contains(subject)) return null;

        switch (subject) {
            case ENGLISH:
                return OrderProductServiceType.AfentiExam;
            case MATH:
                return OrderProductServiceType.AfentiMath;
            case CHINESE:
                return OrderProductServiceType.AfentiChinese;
            default:
                return null;
        }
    }

    public static List<ParentRewardType> getParentRewardType(Subject subject) {
        if (!AVAILABLE_SUBJECT.contains(subject)) return null;

        List<ParentRewardType> rewardTypes = new ArrayList<>();
        switch (subject) {
            case ENGLISH:
                rewardTypes.add(ParentRewardType.AFENTI_ENGLISH_PASS_DAY);
                break;
            case MATH:
                rewardTypes.add(ParentRewardType.AFENTI_MATH_PASS_DAY);
                break;
            case CHINESE:
                rewardTypes.add(ParentRewardType.AFENTI_CHINESE_PASS_DAY);
                break;
            default:
                break;
        }
        return rewardTypes;
    }

    public static OrderProductServiceType getAfentiVideoServiceType(Subject subject) {
        switch (subject) {
            case ENGLISH:
                return OrderProductServiceType.AfentiExamVideo;
            case MATH:
                return OrderProductServiceType.AfentiMathVideo;
            case CHINESE:
                return OrderProductServiceType.AfentiChineseVideo;
            default:
                return null;
        }
    }

    public static Subject getSubject(OrderProductServiceType type) {
        switch (type) {
            case AfentiExam:
                return ENGLISH;
            case AfentiMath:
                return MATH;
            case AfentiChinese:
                return CHINESE;
            default:
                return null;
        }
    }

    public static List<String> parsePreparationBookId(List<String> bookIds) {
        List<String> res = new ArrayList<>();

        if (CollectionUtils.isEmpty(bookIds)) return res;

        bookIds.forEach(e -> {
            res.add(e.replace(preparationBookIdPrefix, ""));
        });

        return res;
    }

    public static String getBookId(String bookId, AfentiLearningType learningType) {
        if (learningType == null || StringUtils.isBlank(bookId)) {
            return bookId;
        }

        if (learningType == AfentiLearningType.castle) {
            return StringUtils.replace(bookId, preparationBookIdPrefix, "").replace(reviewBookIdPrefix, "");
        }

        if (learningType == AfentiLearningType.preparation) {
            if (StringUtils.contains(bookId, preparationBookIdPrefix)) {
                return bookId;
            } else {
                return preparationBookIdPrefix + bookId;
            }
        }

        if (learningType == AfentiLearningType.review) {
            if (StringUtils.contains(bookId, reviewBookIdPrefix)) {
                return bookId;
            } else {
                return reviewBookIdPrefix + bookId;
            }
        }

        return bookId;
    }

    // 新关卡生成逻辑后的bookId
    public static String getNewBookId(String bookId) {
        return newRankBookIdPrefix + bookId;
    }

    // 新关卡生成逻辑后的bookId 获取原 bookId
    public static String getBookIdByNewBookId(String newBookId) {
        return StringUtils.replace(newBookId, newRankBookIdPrefix, "");
    }


    public static ClazzLevel getAfentiPreparationClazzLevel(ClazzLevel clazzLevel, EduSystemType eduSystemType) {
        if (!AVAILABLE_GRADE.contains(clazzLevel)) return ClazzLevel.FIRST_GRADE;
        // 暑假年级处理
        SchoolYearPhase schoolYearPhase = SchoolYear.newInstance().currentPhase();
        if (schoolYearPhase == SchoolYearPhase.SUMMER_VACATION) {
            if (eduSystemType == EduSystemType.P5) {
                if (clazzLevel == ClazzLevel.FIFTH_GRADE) {
                    return ClazzLevel.FIFTH_GRADE;
                } else {
                    return ClazzLevel.parse(clazzLevel.getLevel() + 1);
                }
            } else if (eduSystemType == EduSystemType.P6) {
                if (clazzLevel == ClazzLevel.SIXTH_GRADE) {
                    return ClazzLevel.SIXTH_GRADE;
                } else {
                    return ClazzLevel.parse(clazzLevel.getLevel() + 1);
                }
            } else {
                // 默认一年级
                return ClazzLevel.FIRST_GRADE;
            }
        } else {
            return clazzLevel;
        }
    }

    public static String getPreparationBookIdPrefix() {
        return preparationBookIdPrefix;
    }
}
