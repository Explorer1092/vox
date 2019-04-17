/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.entity.ambassador;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.SchoolAmbassadorSource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 校园大使实体，临时表，活动结束删除
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-10-30
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_SCHOOL_AMBASSADOR")
public class SchoolAmbassador implements Serializable {
    private static final long serialVersionUID = -1363366362331756746L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") Date createDatetime;
    @DocumentField("USER_ID") private Long userId;
    @DocumentField("NAME") private String name;
    @DocumentField("MOBILE") private String sensitiveMobile;
    @DocumentField("QQ") private String sensitiveQq;
    @DocumentField("EMAIL") private String sensitiveEmail;
    @DocumentField("LEADER") private String leader;
    @DocumentField("TOTAL_COUNT") private Integer totalCount;
    @DocumentField("USING_COUNT") private Integer usingCount;
    @DocumentField("SUGGESTION") private String suggestion;
    @DocumentField("GENDER") private String gender;
    @DocumentField("ADDRESS") private String address;
    @DocumentField("ENGLISH_COUNT") private Integer englishCount;
    @DocumentField("MATH_COUNT") private Integer mathCount;
    @DocumentField("CHINESE_COUNT") private Integer chineseCount;
    @DocumentField("STUDENT_COUNT") private Integer studentCount;
    @DocumentField("CLAZZ_COUNT") private Integer clazzCount;
    @DocumentField("EDU_SYSTEM_TYPE") private EduSystemType eduSystemType;
    @DocumentField("SOURCE") private SchoolAmbassadorSource source;
    @DocumentField("PNAME") private String pname;
    @DocumentField("CNAME") private String cname;
    @DocumentField("ANAME") private String aname;
    @DocumentField("T_YEAR") private Integer tYear;
    @DocumentField("B_YEAR") private Integer bYear;
    @DocumentField("B_MONTH") private Integer bMonth;
    @DocumentField("B_DAY") private Integer bDay;
    @DocumentField("IS_FX") private Boolean isFx;
    @DocumentField("FX_CLASS") private String fxClass;
    @DocumentField("SCHOOL_NAME") private String schoolName;
    @DocumentField("SCHOOL_LEVEL") private String schoolLevel;
    @DocumentField("ONE_GRADE_CLAZZ_COUNT_BEGIN") private Integer oneGradeClazzCountBegin;
    @DocumentField("ONE_GRADE_CLAZZ_COUNT_END") private Integer oneGradeClazzCountEnd;
    @DocumentField("ONE_CLAZZ_STUDENT_COUNT_BEGIN") private Integer oneClazzStudentCountBegin;
    @DocumentField("ONE_CLAZZ_STUDENT_COUNT_END") private Integer oneClazzStudentCountEnd;

    public static SchoolAmbassador of(Long userId,
                                      String name,
                                      String leader,
                                      Integer totalCount,
                                      Integer usingCount,
                                      String suggestion,
                                      String gender,
                                      String address,
                                      Integer englishCount,
                                      Integer mathCount,
                                      Integer chineseCount,
                                      Integer studentCount,
                                      Integer clazzCount,
                                      EduSystemType eduSystemType,
                                      SchoolAmbassadorSource source,
                                      String pname,
                                      String cname,
                                      String aname,
                                      Integer bYear,
                                      Integer bMonth,
                                      Integer bDay,
                                      Integer tYear,
                                      Boolean isFx,
                                      String fxClass,
                                      String schoolName,
                                      String schoolLevel,
                                      Integer oneClazzStudentCountBegin,
                                      Integer oneClazzStudentCountEnd,
                                      Integer oneGradeClazzCountBegin,
                                      Integer oneGradeClazzCountEnd) {
        SchoolAmbassador ambassador = new SchoolAmbassador();
        ambassador.setCreateDatetime(new Date());
        ambassador.setUserId(userId);
        ambassador.setName(StringUtils.defaultString(name));
        ambassador.setLeader(StringUtils.defaultString(leader));
        ambassador.setTotalCount(totalCount);
        ambassador.setUsingCount(usingCount);
        ambassador.setSuggestion(StringUtils.defaultString(suggestion));
        ambassador.setGender(gender);
        ambassador.setAddress(StringUtils.defaultString(address));
        ambassador.setEnglishCount(englishCount);
        ambassador.setMathCount(mathCount);
        ambassador.setChineseCount(chineseCount);
        ambassador.setStudentCount(studentCount);
        ambassador.setClazzCount(clazzCount);
        ambassador.setEduSystemType(eduSystemType);
        ambassador.setSource(source);
        ambassador.setPname(pname);
        ambassador.setCname(cname);
        ambassador.setAname(aname);
        ambassador.setBYear(bYear);
        ambassador.setBMonth(bMonth);
        ambassador.setBDay(bDay);
        ambassador.setTYear(tYear);
        ambassador.setIsFx(isFx);
        ambassador.setFxClass(fxClass);
        ambassador.setSchoolName(schoolName);
        ambassador.setSchoolLevel(schoolLevel);
        ambassador.setOneClazzStudentCountBegin(oneClazzStudentCountBegin);
        ambassador.setOneClazzStudentCountEnd(oneClazzStudentCountEnd);
        ambassador.setOneGradeClazzCountBegin(oneGradeClazzCountBegin);
        ambassador.setOneGradeClazzCountEnd(oneGradeClazzCountEnd);
        return ambassador;
    }
}