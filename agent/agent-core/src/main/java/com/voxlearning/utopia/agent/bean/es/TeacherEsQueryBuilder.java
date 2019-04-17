package com.voxlearning.utopia.agent.bean.es;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;

import java.util.*;

/**
 * TeacherEsQueryBuilder
 *
 * @author song.wang
 * @date 2018/5/24
 */
public class TeacherEsQueryBuilder extends EsQueryBuilder{
    public TeacherEsQueryBuilder(){
        super();
    }

    public static void withProvinceCodes(EsQueryConditions conditions, Collection<Integer> provinceCodes) {
        if (CollectionUtils.isNotEmpty(provinceCodes)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("provincecode", provinceCodes));
        }
    }

    public static void withCityCodes(EsQueryConditions conditions, Collection<Integer> cityCodes) {
        if (CollectionUtils.isNotEmpty(cityCodes)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("citycode", cityCodes));
        }
    }

    public static void withCountyCodes(EsQueryConditions conditions, Collection<Integer> countyCodes){
        if (CollectionUtils.isNotEmpty(countyCodes)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("countycode", countyCodes));
        }
    }

    public static void withTeacherId(EsQueryConditions conditions, Long teacherId) {
        if (teacherId != null) {
            conditions.getMustItems().add(EsQueryConditions.createTermCondition("teacherid", teacherId));
        }
    }

    public static void withSchoolIds(EsQueryConditions conditions, Collection<Long> schoolIds) {
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("schoolid", schoolIds));
        }
    }

    public static void  withSchoolLevels(EsQueryConditions conditions, Collection<SchoolLevel> schoolLevels){
        if (CollectionUtils.isNotEmpty(schoolLevels)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("schoollevel", schoolLevels));
        }
    }

    public static void withTeacherMobile(EsQueryConditions conditions, String encodedMobile){
        if(StringUtils.isNotBlank(encodedMobile)){
            conditions.getMustItems().add(EsQueryConditions.createTermCondition("mobile", encodedMobile));
        }
    }

    public TeacherEsQueryBuilder withTeacherName(String teacherName) {
        if (StringUtils.isNoneBlank(teacherName)) {
            this.query.getMustItems().add(EsQueryConditions.createWildcardQueryCondition("realname", teacherName));
        }
        return this;
    }

}
