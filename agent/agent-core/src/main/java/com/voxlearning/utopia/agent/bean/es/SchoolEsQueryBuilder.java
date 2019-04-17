package com.voxlearning.utopia.agent.bean.es;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;

import java.util.*;

/**
 * SchoolEsQueryBuilder
 *
 * @author song.wang
 * @date 2018/5/24
 */
public class SchoolEsQueryBuilder extends EsQueryBuilder  {

    public SchoolEsQueryBuilder(){
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

    public static void  withAuthStates(EsQueryConditions conditions, Collection<Integer> authenticationStates){
        if (CollectionUtils.isNotEmpty(authenticationStates)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("authenticationstate", authenticationStates));
        }
    }

    public static void  withSchoolLevels(EsQueryConditions conditions, Collection<SchoolLevel> schoolLevels){
        if (CollectionUtils.isNotEmpty(schoolLevels)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("schoollevel", schoolLevels));
        }
    }

    public static void withDisabled(EsQueryConditions conditions, boolean disabled) {
        // term
        conditions.getMustItems().add(EsQueryConditions.createTermCondition("disabled", disabled));
    }



    public static void withSchoolIds(EsQueryConditions conditions, Collection<Long> schoolIds) {
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            conditions.getMustItems().add(EsQueryConditions.createTermsCondition("schoolid", schoolIds));
        }
    }

    public static void withSchoolId(EsQueryConditions conditions, Long schoolId) {
        if (schoolId != null) {
            conditions.getMustItems().add(EsQueryConditions.createTermCondition("schoolid", schoolId));
        }
    }


    public SchoolEsQueryBuilder withSchoolName(String schoolName) {
        // wildcard 只能在query中使用
        if (StringUtils.isNoneBlank(schoolName)) {
            query.getMustItems().add(EsQueryConditions.createWildcardQueryCondition("schoolname", schoolName));
        }
        return this;
    }

    public static void main(String[] args) {
        SchoolEsQueryBuilder schoolEsQueryBuilder = new SchoolEsQueryBuilder();
        schoolEsQueryBuilder.withSchoolName("育才");
        Set<Integer> provinceCodes = new HashSet<>();
        Set<Integer> cityCodes = new HashSet<>();
        Set<Integer> countyCodes = new HashSet<>();
        countyCodes.add(500112);
        countyCodes.add(500231);
        cityCodes.add(50000);

        EsQueryConditions regionConditions = new EsQueryConditions();
        regionConditions.addShouldCondition(EsQueryConditions.createTermsCondition("provincecode", provinceCodes));
        regionConditions.addShouldCondition(EsQueryConditions.createTermsCondition("citycode", cityCodes));
        regionConditions.addShouldCondition(EsQueryConditions.createTermsCondition("countycode", countyCodes));
        schoolEsQueryBuilder.getFilter().addMustQueryConditions(regionConditions);

        List<SchoolLevel> schoolLevelList = new ArrayList<>();
        schoolLevelList.add(SchoolLevel.JUNIOR);
        schoolLevelList.add(SchoolLevel.MIDDLE);
        SchoolEsQueryBuilder.withSchoolLevels(schoolEsQueryBuilder.getFilter(), schoolLevelList);

        List<Integer> authState = new ArrayList<>();
        authState.add(0);
        authState.add(1);
        SchoolEsQueryBuilder.withAuthStates(schoolEsQueryBuilder.getFilter(), authState);
        SchoolEsQueryBuilder.withDisabled(schoolEsQueryBuilder.getFilter(), false);

        schoolEsQueryBuilder.withPageFrom(0, 100);
        List<String> source = new ArrayList<>();
        source.add("schoolname");
        schoolEsQueryBuilder.withSource(source);

        System.out.println(schoolEsQueryBuilder.buildQueryString());

        System.out.println(System.currentTimeMillis());
        SchoolEsQueryContent esQueryContent = new SchoolEsQueryContent(schoolEsQueryBuilder.buildQueryString());
        System.out.println(System.currentTimeMillis());
        System.out.println(esQueryContent.getSchoolWithSourceData());

    }







}
