/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.constant;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.voxlearning.alps.annotation.meta.ClazzLevel.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract public class UtopiaAfentiConstants {
    public static final String ULTIMATE_UNIT = "999999"; // 终极关卡单元ID
    public static final String MIDTERM_UNIT = "888888"; // 期中单元ID  小学语文
    public static final String TERMINAL_UNIT = "777777"; // 期末单元ID 小学语文
    public static final Integer MIDTERM_RANK = 1;  // 期中单元关卡数  小学语文
    public static final Integer TERMINAL_RANK = 3; // 期末单元关卡数  小学语文
    public static final Integer ULTIMATE_RANK = 99; // 终极关卡关卡数
    public static final String NO_SIMILAR_QUESTION = "nosq"; // 没有类题的占位字符串
    public static final String CURRENT_QUIZ = "TQ_201612"; // 当前期中或者期末测验，当成单元id使用，如果与教材无关，也当教材id使用

    public static boolean isUltimateUnit(String newUnitId) {
        return StringUtils.isNoneBlank(newUnitId) && Objects.equals(newUnitId, ULTIMATE_UNIT);
    }

    public static UnitRankType getUnitType(String newUnitId) {
        if (StringUtils.isBlank(newUnitId)) {
            return null;
        }
        if (StringUtils.equals(newUnitId, ULTIMATE_UNIT)) {
            return UnitRankType.ULTIMATE;
        }
        if (StringUtils.equals(newUnitId, MIDTERM_UNIT)) {
            return UnitRankType.MIDTERM;
        }
        if (StringUtils.equals(newUnitId, TERMINAL_UNIT)) {
            return UnitRankType.TERMINAL;
        }
        return UnitRankType.COMMON;
    }

    public static final List<Subject> AVAILABLE_SUBJECT = Collections.unmodifiableList(
            Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE)
    );

    public static final List<Subject> AVAILABLE_QUIZ_SUBJECT = Collections.unmodifiableList(
            Arrays.asList(Subject.ENGLISH, Subject.MATH)
    );

    public static final List<Subject> AVAILABLE_TERM_QUIZ_SUBJECT = Collections.unmodifiableList(
            Arrays.asList(Subject.MATH, Subject.ENGLISH, Subject.CHINESE)
    );

    // 目前只有小学有阿分题
    public static final List<ClazzLevel> AVAILABLE_GRADE = Collections.unmodifiableList(
            Arrays.asList(FIRST_GRADE, SECOND_GRADE, THIRD_GRADE, FOURTH_GRADE, FIFTH_GRADE, SIXTH_GRADE)
    );


    public static final List<String> AFENTI_BOOK_BLACK_LIST = Collections.unmodifiableList(
            Arrays.asList("BK_10300000556152", "BK_10300000557518", "BK_10300000558795", "BK_10300000559044",
                    "BK_10300000560861", "BK_10300000561978", "BK_10300000562568", "BK_10300000563511",
                    "BK_10300000564121", "BK_10300000565787", "BK_10300000566950", "BK_10300000567735",
                    "BK_10300000583490", "BK_10300000584160", "BK_10300000585752", "BK_10300000586129",
                    "BK_10300000587874", "BK_10300000588031", "BK_10300000589334", "BK_10300000590599",
                    "BK_10300001674057"
            )
    );

    public static final Map<String, String> BOOK_PAPER_REF;
    public static final Map<Subject, OrderProductServiceType> SUBJECT_AFENTI_REF;
    public static final Map<Subject, OrderProductServiceType> SUBJECT_AFENTI_VIDEO_REF;

    static {
        BOOK_PAPER_REF = new HashMap<>();
        BOOK_PAPER_REF.put("BK_10200001280256", "P_10200057639691");
        BOOK_PAPER_REF.put("BK_10200001292008", "P_10200057334053");
        BOOK_PAPER_REF.put("BK_10200001294341", "P_10200057338847");
        BOOK_PAPER_REF.put("BK_10200001295992", "P_10200057638094");
        BOOK_PAPER_REF.put("BK_10200001296621", "P_10200057629403");
        BOOK_PAPER_REF.put("BK_10200001297213", "P_10200057633405");

        SUBJECT_AFENTI_REF = new HashMap<>();
        SUBJECT_AFENTI_REF.put(Subject.ENGLISH, OrderProductServiceType.AfentiExam);
        SUBJECT_AFENTI_REF.put(Subject.MATH, OrderProductServiceType.AfentiMath);
        SUBJECT_AFENTI_REF.put(Subject.CHINESE, OrderProductServiceType.AfentiChinese);

        SUBJECT_AFENTI_VIDEO_REF = new HashMap<>();
        SUBJECT_AFENTI_VIDEO_REF.put(Subject.ENGLISH, OrderProductServiceType.AfentiExamVideo);
        SUBJECT_AFENTI_VIDEO_REF.put(Subject.MATH, OrderProductServiceType.AfentiMathVideo);
        SUBJECT_AFENTI_VIDEO_REF.put(Subject.CHINESE, OrderProductServiceType.AfentiChineseVideo);
    }
}
