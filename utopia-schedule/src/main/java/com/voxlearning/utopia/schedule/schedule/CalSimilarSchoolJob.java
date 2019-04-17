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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.consumer.service.CrmSimilarSchoolServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2017/4/14
 */
@Named
@ScheduledJobDefinition(
        jobName = "相似学校计算相似度",
        jobDescription = "相似学校相似度手工执行",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 11 30 7 ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class CalSimilarSchoolJob extends ScheduledJobWithJournalSupport {

    @Inject private RaikouSystem raikouSystem;

    @Inject private CrmSimilarSchoolServiceClient crmSimilarSchoolServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        if (RuntimeMode.isProduction()) {
            return;
        }
        //高开区 经开区 高新技术 高新区 开发区 {"likeName":""}
        String likeName = (String) parameters.get("likeName");
        if (StringUtils.isBlank(likeName)) {
            logger.info("CalSimilarSchoolJob--- likeName not setting");
            return;
        }

        List<Map<String, Object>> result = RoutingPolicyExecutorBuilder.getInstance()
                .<List<Map<String, Object>>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {
                    String sql = "SELECT * FROM VOX_SCHOOL WHERE  CNAME LIKE '%" + likeName + "%' and DISABLED=0";
                    return utopiaSql.withSql(sql).queryAll();
                }).execute();

        if (CollectionUtils.isEmpty(result)) {
            logger.info("CalSimilarSchoolJob--- likeName:{} empty", likeName);
            return;
        }

        progressMonitor.worked(3);
        long sum = 0;
        ISimpleProgressMonitor monitor = progressMonitor.subTask(97, result.size());
        logger.info("CalSimilarSchoolJob--- idUgc,nameUgc,level,province,city,countyUgc,idSys,nameSys,countySys,value");
        for (Map<String, Object> stringObjectMap : result) {
            Long schoolId = SafeConverter.toLong(stringObjectMap.get("ID"));
            String nameUgc = SafeConverter.toString(stringObjectMap.get("CNAME"));
            SchoolLevel schoolLevel = SchoolLevel.safeParse(SafeConverter.toInt(stringObjectMap.get("LEVEL")));
            Integer regionCode = SafeConverter.toInt(stringObjectMap.get("REGION_CODE"));

            sum = sum + getSchoolSimilarityInfo(schoolId, nameUgc, schoolLevel, regionCode);
            monitor.worked(1);
        }
        logger.info("CalSimilarSchoolJob--- sum:{}", sum);
    }


    private long getSchoolSimilarityInfo(Long idUgc, String nameUgc, SchoolLevel schoolLevel, Integer regionCode) {
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return 0;
        }
        String cityName = exRegion.getCityName();

        Map<String, School> schoolNameMap = new LinkedHashMap<>();
        List<ExRegion> exRegions = raikouSystem.getRegionBuffer().loadChildRegions(exRegion.getCityCode());//本城市下所有地区
        Map<String, ExRegion> stringExRegionMap = new HashMap<>();

        exRegions.forEach(tempExRegion -> {
            raikouSystem.loadSchools(schoolLoaderClient.getSchoolLoader()
                    .querySchoolLocations(tempExRegion.getCountyCode())
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabled())
                    .filter(e -> e.getLevel() == schoolLevel.getLevel())
                    .map(School.Location::getId)
                    .collect(Collectors.toSet()))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(School::getId))
                    .forEach(tempSchool -> {
                        if (!Objects.equals(tempSchool.getId(), idUgc) && !schoolNameMap.containsKey(tempSchool.getCmainName())) {
                            schoolNameMap.put(tempSchool.getCmainName(), tempSchool);
                            stringExRegionMap.put(tempSchool.getCmainName(), exRegion);
                        }
                    });
        });


        double similarValue = 0.65;
        List<String> schoolNameSysList = schoolNameMap.keySet().stream().collect(Collectors.toList());
        Map<String, Double> similarityMap = new HashMap<>();
        int blockSize = 400;
        int limit = schoolNameSysList.size();
        if (schoolNameSysList.size() > blockSize) {
            int beginIndex = 0;
            int endIndex = blockSize;
            similarityMap = new LinkedHashMap<>();
            while (beginIndex < schoolNameSysList.size()) {
                Map<String, Double> tempMap = crmSimilarSchoolServiceClient.getSchoolNameSimilarity(nameUgc, schoolNameSysList.subList(beginIndex, endIndex), cityName, schoolLevel, limit, similarValue);
                similarityMap.putAll(tempMap);
                beginIndex = endIndex;
                endIndex = endIndex + blockSize;
                if (endIndex > schoolNameSysList.size()) {
                    endIndex = schoolNameSysList.size();
                }
            }
            try {
                similarityMap = similarityMap.entrySet().stream().sorted((l1, l2) -> BigDecimal.valueOf(l2.getValue()).compareTo(BigDecimal.valueOf(l1.getValue())))
                        .limit(limit).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            } catch (Exception e) {
                logger.warn("CalSimilarSchoolJob--- {},{},{},{}", nameUgc, schoolNameSysList, cityName, schoolLevel);
            }
        } else {
            try {
                similarityMap = crmSimilarSchoolServiceClient.getSchoolNameSimilarity(nameUgc, schoolNameSysList, cityName, schoolLevel, limit, similarValue);
            } catch (Exception e) {
                logger.warn("CalSimilarSchoolJob--- {},{},{},{}", nameUgc, schoolNameSysList, cityName, schoolLevel);
            }
        }

        final long[] times = {0};
        similarityMap.entrySet().forEach(stringDoubleEntry -> {
            School tempSchool = schoolNameMap.get(stringDoubleEntry.getKey());
            String countyNameSys = stringExRegionMap.get(stringDoubleEntry.getKey()).getCountyName();
            logger.info("CalSimilarSchoolJob--- {},{},{},{},{},{},{},{},{},{}",
                    idUgc, nameUgc, schoolLevel.getLevel(), exRegion.getProvinceName(), exRegion.getCityName(), exRegion.getCountyName(), tempSchool.getId(), tempSchool.getCmainName(), countyNameSys, stringDoubleEntry.getValue());
            times[0]++;
        });

        return times[0];
    }

}
