/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.business.api.SummerReporterService;
import com.voxlearning.utopia.service.business.api.entity.summerreport.CollectSchoolInfo;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCityInterviewCount;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCollectSchool;
import com.voxlearning.utopia.service.business.impl.dao.summerreporter.CollectSchoolInfoDao;
import com.voxlearning.utopia.service.business.impl.dao.summerreporter.StudentCityInterviewCountDao;
import com.voxlearning.utopia.service.business.impl.dao.summerreporter.StudentCollectSchoolDao;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiangpeng on 16/6/12.
 */

@Named
@Service(interfaceClass = SummerReporterService.class)
@ExposeService(interfaceClass = SummerReporterService.class)
public class SummerReporterServiceImpl extends BusinessServiceSpringBean implements SummerReporterService {

    private static String lockPrefix = "SUMMER_REPORTER";

    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;

    @Inject
    private CollectSchoolInfoDao collectSchoolInfoDao;

    @Inject
    private StudentCollectSchoolDao studentCollectSchoolDao;

    @Inject
    private StudentCityInterviewCountDao studentCityInterviewCountDao;

    @Override
    public String test() {
        return "success";
    }

    /**
     * 获取该城市的采访排行榜
     * 显示该学生所在城市的排行
     * 按带有位置信息的学校提交数计算排名
     * 数量>0才能上榜
     * 数量>0的人数超过100时，显示前100名
     * 如果出现相同采访数量，最后一次提交时间较早的学生排在前
     * 4小时缓存
     *
     * @param cityId 城市id
     * @return
     */
    @Override
    public List<StudentCityInterviewCount> loadCityTopRange(@CacheParameter Integer cityId) {

        List<StudentCityInterviewCount> cityInterviewList = studentCityInterviewCountDao.__findByCityIds(cityId);
        if (CollectionUtils.isEmpty(cityInterviewList))
            return new ArrayList<>();
        if (cityInterviewList.size() > 100)
            cityInterviewList = new ArrayList<>(cityInterviewList.subList(0, 100));
        List<Long> studentIds = cityInterviewList.stream().map(StudentCityInterviewCount::fetchStudentId).collect(Collectors.toList());
        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studentIds);

        AlpsFutureMap<Long, School> schoolFutures = AlpsFutureBuilder.<Long, School>newBuilder()
                .ids(studentIds)
                .generator(id -> asyncStudentServiceClient.getAsyncStudentService()
                        .loadStudentSchool(id))
                .buildMap();
        cityInterviewList.forEach(t -> {
            Student student = studentMap.get(t.fetchStudentId());
            if (student == null)
                return;
            t.setStudentName(student.fetchRealname());
            School school = schoolFutures.getUninterruptibly(student.getId());
            if (school != null && !StringUtils.isBlank(school.getShortName()))
                t.setSchoolName(school.getShortName());
        });

        return cityInterviewList;
    }

    /**
     * 获取学生所在的城市排名
     * 4小时缓存 效率低啊 调用加结果缓存.
     *
     * @param userId
     * @return 返回-1 表示没有排名 防止击穿缓存
     */
    @Override
    public Integer loadStudentCityRank(Long userId) {
        if (userId == null)
            return -1;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null)
            return -1;

        Integer cityId = studentDetail.getCityCode();
        if (cityId == null)
            return -1;
        //根据产品预估,最多load出十万条数据...
        List<StudentCityInterviewCount> cityInterviewList = studentCityInterviewCountDao.__findByCityIds(cityId);
        if (CollectionUtils.isEmpty(cityInterviewList))
            return -1;
        int range = 0;
        for (StudentCityInterviewCount t : cityInterviewList) {
            range++;
            if (userId.equals(t.fetchStudentId()))
                return range;
        }
        return -1;
    }

    private static List<Integer> randomNumberList = new ArrayList<>();

    static {
        for (int i = 1; i <= 20; i++) {
            randomNumberList.add(i);
        }
    }

    /**
     * 获取该区域随机推荐20个学校信息
     * 大数据 提供的特定学校数据；
     * >=3个学生提交相同学校名称的、且系统内无重名的学校；
     * 通过学生所在区域，随机显示当前区域的20所学校；
     * 每4小时刷新一次；
     * 不显示采访人数>=10人的学校，通过学校名称判断；
     * 显示该学校目前的采访人数；
     * 4小时缓存
     *
     * @param countyId 区id
     * @return
     */

    @Override
    public List<Map<String, Object>> loadRecommendSchoolByCounty(Integer countyId) {
        //数据来源两个 一个是collectSchoolInfo 里 该区域 standard为true的学校,10个
        //二是 studentCollectSchool 里,该区域 符合条件的学校 10个.
        //先都按10个取,如果最终没取够20个,再补取.都他么算醉了

        Map<Integer, List<StudentCollectSchool>> byCountyIds = studentCollectSchoolDao.findByCountyIds(Collections.singleton(countyId));
        List<StudentCollectSchool> studentCollectSchoolList;
        if (MapUtils.isEmpty(byCountyIds))
            studentCollectSchoolList = new ArrayList<>();
        else
            studentCollectSchoolList = byCountyIds.get(countyId);

        //计算出这个区域 每个学校被采访的次数
        Map<String, Integer> schoolId2InterviewCountMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(studentCollectSchoolList)) {
            for (StudentCollectSchool studentCollectSchool : studentCollectSchoolList) {
                String schoolId = studentCollectSchool.getSchoolId();
                Integer interviewCount = schoolId2InterviewCountMap.get(schoolId);
                if (interviewCount == null) {
                    schoolId2InterviewCountMap.put(schoolId, 1);
                } else
                    schoolId2InterviewCountMap.put(schoolId, interviewCount + 1);
            }
        }

        //从大数据给的学校中随机取10个符合条件学校(采集数量<10)
        Set<String> resultSchoolIdSet = new HashSet<>();
        Map<Integer, List<CollectSchoolInfo>> standardByCountyId = collectSchoolInfoDao.findStandardByCountyId(Collections.singleton(countyId));
        List<CollectSchoolInfo> standardCollectSchoolInfos = new ArrayList<>();
        if (MapUtils.isNotEmpty(standardByCountyId)) {
            standardCollectSchoolInfos = standardByCountyId.get(countyId);
            if (!CollectionUtils.isEmpty(standardCollectSchoolInfos))
                resultSchoolIdSet.addAll(fetchMatchStandardSchool(standardCollectSchoolInfos, schoolId2InterviewCountMap));
        }

        //从学生采集的学校中随机取10个...如果不够10个,那就是真没有了.
        resultSchoolIdSet.addAll(fetchMatchCollectSchool(schoolId2InterviewCountMap));

        //如果还是不够20个,只能再从大数据给的学校中取3次,再不够20个,就算了...
        if (resultSchoolIdSet.size() < 20 && !CollectionUtils.isEmpty(standardCollectSchoolInfos)) {
            int i = 0;
            while (i < 3) {
                resultSchoolIdSet.addAll(singleFetchMatchStandardSchool(standardCollectSchoolInfos, schoolId2InterviewCountMap));
                if (resultSchoolIdSet.size() >= 20)
                    break;
                ;
                i++;
            }
        }

        //行了 最终得到多少个就多少个吧....
        final List<Map<String, Object>> resultMapList = new ArrayList<>();
        Map<String, CollectSchoolInfo> collectSchoolInfoMap = collectSchoolInfoDao.loads(resultSchoolIdSet);
        resultSchoolIdSet.stream().forEach(t -> {
            Integer count = schoolId2InterviewCountMap.get(t);
            Map<String, Object> map = new LinkedHashMap<>();
            CollectSchoolInfo collectSchoolInfo = collectSchoolInfoMap.get(t);
            if (collectSchoolInfo == null)
                return;
            map.put("schoolName", collectSchoolInfo.getSchoolName());
            if (count == null) {
                map.put("count", 0);
            } else {
                map.put("count", count);
            }
            resultMapList.add(map);
        });
        if (resultMapList.size() > 20) {
            return new ArrayList<>(resultMapList.subList(0, 20));
        } else
            return resultMapList;
    }

    private Set<String> fetchMatchCollectSchool(Map<String, Integer> schoolId2InterviewCountMap) {
        Set<String> tempSchoolIdSet = new HashSet<>();
        for (Map.Entry<String, Integer> entry : schoolId2InterviewCountMap.entrySet()) {
            int count = entry.getValue();
            if (count >= 3 && count < 10)
                tempSchoolIdSet.add(entry.getKey());
        }

        if (tempSchoolIdSet.size() < 10)
            return tempSchoolIdSet;
        else {
            String schoolIds[] = new String[10];
            RandomUtils.randomPickFew(new ArrayList<>(tempSchoolIdSet), 10, schoolIds);
            return new HashSet<>(Arrays.asList(schoolIds));
        }
    }

    /**
     * 这个区域的标准的学校信息,从中随机取10个学校,得满足条件.如果不够10个随机取3次,还是不够10个就算了.
     *
     * @param standardCollectSchoolInfos
     * @param schoolId2InterviewCountMap
     * @return
     */
    private Set<String> fetchMatchStandardSchool(List<CollectSchoolInfo> standardCollectSchoolInfos, Map<String, Integer> schoolId2InterviewCountMap) {

        Set<String> matchSchoolIdSet = singleFetchMatchStandardSchool(standardCollectSchoolInfos, schoolId2InterviewCountMap);
        int i = 0;
        while (i < 3) {
            if (matchSchoolIdSet.size() < 10) {
                matchSchoolIdSet.addAll(singleFetchMatchStandardSchool(standardCollectSchoolInfos, schoolId2InterviewCountMap));
            }
            i++;
        }
        if (matchSchoolIdSet.size() > 10) {
            List<String> list = new ArrayList<>(matchSchoolIdSet);
            matchSchoolIdSet = new HashSet<>(list.subList(0, 10));
        }
        return matchSchoolIdSet;
    }

    private Set<String> singleFetchMatchStandardSchool(List<CollectSchoolInfo> standardCollectSchoolInfos, Map<String, Integer> schoolId2InterviewCountMap) {
        if (CollectionUtils.isEmpty(standardCollectSchoolInfos))
            return new HashSet<>();
        if (CollectionUtils.isEmpty(standardCollectSchoolInfos))
            return new HashSet<>();
        if (CollectionUtils.isEmpty(standardCollectSchoolInfos))
            return new HashSet<>();
        CollectSchoolInfo[] random10 = new CollectSchoolInfo[10];
        Set<String> randomSchoolIdSet;
        if (standardCollectSchoolInfos.size() > 10) {
            RandomUtils.randomPickFew(standardCollectSchoolInfos, 10, random10);
            randomSchoolIdSet = Arrays.asList(random10).stream().map(CollectSchoolInfo::getId).collect(Collectors.toSet());
        } else {
            randomSchoolIdSet = standardCollectSchoolInfos.stream().map(CollectSchoolInfo::getId).collect(Collectors.toSet());
        }
        return filterMatchSchoolId(randomSchoolIdSet, schoolId2InterviewCountMap);
    }

    /**
     * 筛选出指定学校id,
     * 标准学校 被采集数量在0-10之间的
     *
     * @param schoolIdSet
     * @return
     */
    private Set<String> filterMatchSchoolId(Set<String> schoolIdSet, Map<String, Integer> schoolId2InterviewCountMap) {
        return schoolIdSet.stream().filter(t -> {
            Integer count = schoolId2InterviewCountMap.get(t);
            return count == null || count < 10;
        }).collect(Collectors.toSet());
    }

    @Override
    public List<StudentCollectSchool> loadInterviewRecord(Long userId) {
        return studentCollectSchoolDao.findByStudentIds(Collections.singleton(userId)).get(userId);
    }

    @Override
    public Integer loadInterviewCount(Long userId) {
        return null;
    }

    @Override
    public MapMessage uploadInterviewSchool(StudentCollectSchool studentCollectSchool) {
        if (studentCollectSchool == null)
            return MapMessage.errorMessage("参数错误");

        Long studentId = studentCollectSchool.getStudentId();
        if (studentId == null)
            return MapMessage.errorMessage("学生错误");

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("学生错误");

        Integer cityId = studentDetail.getCityCode();
        if (cityId == null)
            return MapMessage.errorMessage("学生城市错误");

        if (studentCollectSchool.getSchoolName().length() > 50) {
            return MapMessage.errorMessage("学校名称过长");
        }

        String studentSchoolName = studentDetail.getStudentSchoolName();

        String schoolName = studentCollectSchool.getSchoolName();

        String schoolInfoId = getCollectSchoolInfoBySchoolName(schoolName);
        if (schoolInfoId == null)
            return MapMessage.errorMessage("get school info error!");

        studentCollectSchool.setSchoolId(schoolInfoId);

        studentCollectSchoolDao.insert(studentCollectSchool);

        studentCityInterviewCountDao.updateCountAddOne(studentId, cityId, studentSchoolName);

        return MapMessage.successMessage();
    }

    /**
     * 保证同一时间 同学校名称只插入一条数据
     *
     * @param schoolName
     * @return
     */
    private String getCollectSchoolInfoBySchoolName(String schoolName) {
        CollectSchoolInfo collectSchoolInfo = collectSchoolInfoDao.loadIfPresentElseInsertBySchoolName(schoolName, CollectSchoolInfo.instance(schoolName));
        return collectSchoolInfo.getId();
//        CollectSchoolInfo schoolInfo =  collectSchoolInfoDao.findBySchoolNames(Collections.singletonList(schoolName)).get(schoolName);
//        String lock = lockPrefix + "_ADD_COLLECT_SCHOOL_" + schoolName;
//        if(schoolInfo == null) {
//            try{
//                int c = 0 ;
//                while(c <5) {
//                    try {
//                        AtomicLockManager.instance().acquireLock(lock);
//                        break;
//                    } catch (CannotAcquireLockException ignore) {
//                        c++;
//                        Thread.sleep(5);
//                    }
//                }
//                schoolInfo = collectSchoolInfoDao.findBySchoolNames(Collections.singletonList(schoolName)).get(schoolName);
//                if(schoolInfo == null) {
//                    schoolInfo = CollectSchoolInfo.instance(schoolName);
//                    return collectSchoolInfoDao.insert(schoolInfo);
//                }else
//                    return schoolInfo.getId();
//
//            }catch (InterruptedException e){
//                return null;
//            }finally {
//                AtomicLockManager.instance().releaseLock(lock);
//            }
//        }else
//            return schoolInfo.getId();
    }
//                    c++;
}
