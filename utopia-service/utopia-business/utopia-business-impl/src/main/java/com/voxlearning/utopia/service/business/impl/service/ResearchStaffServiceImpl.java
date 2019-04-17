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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.GroupByFinder;
import com.voxlearning.utopia.business.api.ResearchStaffService;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import com.voxlearning.utopia.business.api.mapper.ResearchInfo;
import com.voxlearning.utopia.mapper.rstaff.*;
import com.voxlearning.utopia.service.business.api.entity.BizMarketingSchoolData;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementData;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementRegionData;
import com.voxlearning.utopia.service.business.api.entity.RSTeacherAuthStudentCountDaily;
import com.voxlearning.utopia.service.business.impl.dao.BizMarketingSchoolDataDao;
import com.voxlearning.utopia.service.business.impl.dao.DailyIncreasementDataDao;
import com.voxlearning.utopia.service.business.impl.dao.DailyIncreasementRegionDataDao;
import com.voxlearning.utopia.service.business.impl.dao.RSTeacherAuthStudentCountDailyDao;
import com.voxlearning.utopia.service.business.impl.service.rstaff.ResearchStaffReportServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.UserActivity;
import com.voxlearning.utopia.service.user.api.entities.UserExtensionAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType.RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_ARRANGE;
import static com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType.RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_AUTHENTICATE;

@Named
@Service(interfaceClass = ResearchStaffService.class)
@ExposeService(interfaceClass = ResearchStaffService.class)
public class ResearchStaffServiceImpl extends BusinessServiceSpringBean implements ResearchStaffService {

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;

    @Inject
    private BizMarketingSchoolDataDao bizMarketingSchoolDataDao;

    @Inject
    private DailyIncreasementRegionDataDao dailyIncreasementRegionDataDao;

    @Inject
    private DailyIncreasementDataDao dailyIncreasementDataDao;

    @Inject
    private ResearchStaffReportServiceImpl researchStaffReportServiceImpl;

    @Inject
    private RSTeacherAuthStudentCountDailyDao rsTeacherAuthStudentCountDailyDao;

    @Override
    public ResearchInfo getSchoolStatisticData(Subject subject, String date, String endDate, Integer code, Integer regionType, Long schoolId) {
        List<BizMarketingSchoolData> detail;
        if (!StringUtils.isBlank(endDate)) {
            detail = bizMarketingSchoolDataDao.findByDateAndRegionId(subject, date, endDate, code, regionType);
        } else {
            detail = bizMarketingSchoolDataDao.findByDateAndRegionId(subject, date, code, regionType);
        }
        if (CollectionUtils.isEmpty(detail)) {                // update opinion
            return null;
        }
        ResearchInfo resInfo = new ResearchInfo();
        if (schoolId == null) {
            int authStudentCount = 0;
            int rstaffAuthCount = 0;
            Set<Long> set = new HashSet<>();
            for (BizMarketingSchoolData bizs : detail) {
                if (bizs.getAuthenticationState() > 0) {
                    set.add(bizs.getTeacherId());
                }
                if (bizs.getRestaffAuthTotal() != null) {
                    rstaffAuthCount += bizs.getRestaffAuthTotal();
                }
                if (bizs.getDohwAuthStuCount() != null) {
                    authStudentCount += bizs.getDohwAuthStuCount();
                }
            }
            int teacherCount = set.size();
            resInfo.setTeacherCount(teacherCount);
            resInfo.setAuthStudentCount(authStudentCount);
            resInfo.setRstaffAuthCount(rstaffAuthCount);
            resInfo.setDetails(new ArrayList<>());
            resInfo.getDetails().addAll(detail);
        } else {
            resInfo.setDetails(new ArrayList<>());
            int authStudentCount = 0;
            int rstaffAuthCount = 0;
            Set<Long> set = new HashSet<>();
            for (BizMarketingSchoolData bizs : detail) {
                if (schoolId.equals(bizs.getSchoolId())) {

                    if (bizs.getAuthenticationState() > 0) {
                        set.add(bizs.getTeacherId());
                    }
                    if (bizs.getRestaffAuthTotal() != null) {
                        rstaffAuthCount += bizs.getRestaffAuthTotal();
                    }
                    if (bizs.getDohwAuthStuCount() != null) {
                        authStudentCount += bizs.getDohwAuthStuCount();
                    }
                    resInfo.getDetails().add(bizs);
                }
            }
            int teacherCount = set.size();
            resInfo.setTeacherCount(teacherCount);
            resInfo.setRstaffAuthCount(rstaffAuthCount);
            resInfo.setAuthStudentCount(authStudentCount);
        }
        return resInfo;
    }

    /**
     * 返回积分统计
     * TODO
     * 这部分code暂时按之前的方式写
     * 相关部分感觉很乱（包括DAO以及当前实现层），有performance问题，等有时间重构下
     */
    @Override
    public ResearchInfo getSchoolStatisticDataBySchoolIds(Subject subject, String date, String endDate, Integer code, Integer regionType, Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return null;
        }
        List<BizMarketingSchoolData> detail;
        if (!StringUtils.isBlank(endDate)) {
            detail = bizMarketingSchoolDataDao.findByDateAndRegionId(subject, date, endDate, code, regionType);
        } else {
            detail = bizMarketingSchoolDataDao.findByDateAndRegionId(subject, date, code, regionType);
        }
        if (CollectionUtils.isEmpty(detail)) {                // update opinion
            return null;
        }
        ResearchInfo resInfo = new ResearchInfo();
        int authStudentCount = 0;
        int rstaffAuthCount = 0;
        Set<Long> set = new HashSet<>();
        resInfo.setDetails(new ArrayList<>());
        for (BizMarketingSchoolData bizs : detail) {
            if (schoolIds.contains(bizs.getSchoolId())) {
                if (bizs.getAuthenticationState() > 0) {
                    set.add(bizs.getTeacherId());
                }
                if (bizs.getRestaffAuthTotal() != null) {
                    rstaffAuthCount += bizs.getRestaffAuthTotal();
                }
                if (bizs.getDohwStuCount() != null) {
                    authStudentCount += bizs.getDohwStuCount();
                }
                resInfo.getDetails().add(bizs);
            }
        }
        int teacherCount = set.size();
        resInfo.setTeacherCount(teacherCount);
        resInfo.setAuthStudentCount(authStudentCount);
        resInfo.setRstaffAuthCount(rstaffAuthCount);
        return resInfo;
    }

    @Override
    public ResearchInfo getSchoolStatisticDataByDateAndRegionListAndSchool(Subject subject, String date, String endDate, Integer code, Integer regionType, List<Integer> areaCodeList, Long schoolId) {
        /*
        date += " 23:59:59";
        endDate += " 23:59:59";
        String gapDateStr = "2015-10-30 23:59:59";
        Date gapDate = DateUtils.stringToDate(gapDateStr);
        FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");  //定义日期格式
        try {
            Date start = formatter.parse(date);
            Date end = formatter.parse(endDate);
            if (start.getTime() <= end.getTime()) {
                if (start.after(gapDate)) {
                    return getNewData(subject, date, endDate, code, regionType, areaCodeList, schoolId);
                } else if (end.before(gapDate)) {
                    return getCrazyDetaData(date, endDate, areaCodeList, schoolId);
                } else {
                    ResearchInfo oldData = getCrazyDetaData(date, gapDateStr, areaCodeList, schoolId);
                    ResearchInfo newData = getNewData(subject, gapDateStr, endDate, code, regionType, areaCodeList, schoolId);
                    ResearchInfo ret = new ResearchInfo();
                    ret.setAuthStudentCount(oldData.getAuthStudentCount() + newData.getAuthStudentCount());
                    ret.setRstaffAuthCount(oldData.getRstaffAuthCount() + newData.getRstaffAuthCount());
                    ret.setStudentCount(oldData.getStudentCount() + newData.getStudentCount());
                    ret.setTeacherCount(oldData.getTeacherCount() + newData.getTeacherCount());
                    return ret;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
        // new  改从agent查询
        Integer start = Integer.valueOf(date.replaceAll("-", ""));
        Integer end = Integer.valueOf(endDate.replaceAll("-", ""));
        ResearchInfo ret = new ResearchInfo();
        if (schoolId <= 0L) {
            Set<Integer> regionCodeSet = new HashSet<>();
//            regionCodeSet.addAll(areaCodeList);     //改从agent查询数据后不需要了
            regionCodeSet.add(code);
            List<DailyIncreasementRegionData> list = dailyIncreasementRegionDataDao.findData(start, end, regionCodeSet);
            ret.setTeacherCount(list.stream().mapToInt(DailyIncreasementRegionData::getTeacher_auth).sum());
            ret.setAuthStudentCount(list.stream().mapToInt(DailyIncreasementRegionData::getStudent_auth).sum());
        } else {
            List<DailyIncreasementData> list = dailyIncreasementDataDao.findDailyIncreasementBySchool(start, end, Collections.singleton(schoolId));
            ret.setTeacherCount(list.stream().mapToInt(DailyIncreasementData::getTeacher_auth).sum());
            ret.setAuthStudentCount(list.stream().mapToInt(DailyIncreasementData::getStudent_auth).sum());
        }
        return ret;
    }

    @Override
    public ResearchInfo getSchoolStatisticDataByDateAndRegionListAndSchoolList(Subject subject, String date, String endDate, Integer code, Integer regionType, List<Integer> areaCodeList, Collection<Long> schoolIdList) {
        Integer start = Integer.valueOf(date.replaceAll("-", ""));
        Integer end = Integer.valueOf(endDate.replaceAll("-", ""));
        ResearchInfo ret = new ResearchInfo();
        if ((CollectionUtils.isEmpty(schoolIdList))) {
        } else {
            List<DailyIncreasementData> list = dailyIncreasementDataDao.findDailyIncreasementBySchool(start, end, (Set<Long>) schoolIdList);
            ret.setTeacherCount(list.stream().mapToInt(DailyIncreasementData::getTeacher_auth).sum());
            ret.setAuthStudentCount(list.stream().mapToInt(DailyIncreasementData::getStudent_auth).sum());
        }
        return ret;
    }

//    @Deprecated
//    public ResearchInfo getNewData(Subject subject, String date, String endDate, Integer code, Integer regionType, List<Integer> areaCodeList, Long schoolId) {
//        ResearchInfo researchInfo = new ResearchInfo();
//        FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");  //定义日期格式
//        List<BizMarketingSchoolData> list = bizMarketingSchoolDataDao.findByDateAndRegionIds(subject, date, endDate, code, regionType, areaCodeList, schoolId);
////        Set<Long> teacherIdSet = new HashSet<>();
//        int authStuCount = 0;
//        try {
//            Date start = formatter.parse(date);
//            Date end = formatter.parse(endDate);
//            for (BizMarketingSchoolData item : list) {
////                Date registerDate = formatter.parse(item.getRegisterTime());
////                if (registerDate.getTime() >= start.getTime() && registerDate.getTime() <= end.getTime()) {
////                    teacherIdSet.add(item.getTeacherId());
////                }
//                authStuCount += item.getDohwAuthStuCount();
//            }
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        researchInfo.setAuthStudentCount(authStuCount);
//        researchInfo.setTeacherCount(getCrazyDetaData(date, endDate, areaCodeList, schoolId).getTeacherCount());
//        return researchInfo;
//    }

//    @Override
//    public ResearchInfo getCrazyDetaData(String startDate, String endDate, List<Integer> regionIdList, Long schoolId) {
//        ResearchInfo researchInfo = new ResearchInfo();
//        try {
//            FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");  //定义日期格式
//            Date start = formatter.parse(startDate);
//            Date rstaff = formatter.parse("2013-09-12 23:59:59");
//            Date end = formatter.parse(endDate);
//            //统计是班主任的教师
//            String queryTeacher =
//                    "SELECT  COUNT( DISTINCT U.ID ) FROM UCT_USER   U  INNER JOIN VOX_CERTIFICATION_APPLICATION_OPERATING_LOG VCO  ON U.ID=VCO.APPLICANT_ID " +
//                            " AND U.USER_TYPE=1 AND U.AUTHENTICATION_STATE=1 AND VCO.CERTIFICATION_STATE=1  " +
//                            " INNER JOIN  VOX_CLASS_TEACHER_REF  VCT ON U.ID=VCT.USER_ID AND VCT.DISABLED=FALSE  " +
//                            " INNER JOIN VOX_TEACHER_SUBJECT_REF VSRR ON VCT.USER_ID=VSRR.USER_ID AND VSRR.SUBJECT='ENGLISH'" +
//                            " INNER JOIN VOX_CLASS VC ON VCT.CLASS_ID=VC.ID AND VC.DISABLED=FALSE " +
//                            " INNER JOIN VOX_SCHOOL  VS ON VC.SCHOOL_ID= VS.ID  AND VS.DISABLED=FALSE ";
//
//            String queryStudent =
//                    "SELECT  COUNT(DISTINCT VEA.USER_ID) FROM UCT_USER   U  " +
//                            " INNER JOIN  VOX_CLASS_TEACHER_REF  VCT ON U.ID=VCT.USER_ID AND VCT.DISABLED=FALSE AND U.USER_TYPE=1 AND U.AUTHENTICATION_STATE=1   " +
//                            " INNER JOIN VOX_CLASS VC ON VCT.CLASS_ID=VC.ID AND VC.DISABLED=FALSE  AND VC.CLASS_TYPE=1   " +     //刨除自定义班级
//                            " INNER JOIN VOX_CLASS_STUDENT_REF  VSR ON VC.ID=VSR.CLAZZ_ID AND VSR.DISABLED=FALSE    " +
//                            " INNER JOIN  VOX_USER_EXTENSION_ATTRIBUTE  VEA ON VSR.USER_ID=VEA.USER_ID             " +
//                            " INNER JOIN VOX_SCHOOL  VS ON VC.SCHOOL_ID= VS.ID  AND VS.DISABLED=FALSE  AND VS.`LEVEL`=1  ";    //去除非小学学校
//
//            if (start.getTime() <= rstaff.getTime() && end.getTime() <= rstaff.getTime()) {           // 处理<=9.12 之前的认证数据
//
//                if (schoolId == 0) {
//                    String queryTeacherBefore = queryTeacher + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE AND VCO.CREATETIME >=:start  AND VCO.CREATETIME <=:end ";
//                    String queryStudentBefore = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE   AND VEA.CREATETIME >=:start AND VEA.CREATETIME <=:end  AND VEA.EXTENSION_ATTRIBUTE_KEY='authenticatedTime' ";
//
//                    int teacherQty = utopiaSql.withSql(queryTeacherBefore).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end)).queryValue(Integer.class);
//                    int studentQty = utopiaSql.withSql(queryStudentBefore).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end)).queryValue(Integer.class);
//
//                    researchInfo.setTeacherCount(teacherQty);
//                    researchInfo.setAuthStudentCount(studentQty);
//                } else {
//                    String queryTeacherBefore = queryTeacher + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE AND VCO.CREATETIME >=:start  AND VCO.CREATETIME <=:end  AND VS.ID =:schoolId ";
//                    String queryStudentBefore = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE   AND VEA.CREATETIME >=:start AND VEA.CREATETIME <=:end  AND VEA.EXTENSION_ATTRIBUTE_KEY='authenticatedTime'  AND VS.ID =:schoolId ";
//
//                    int teacherQty = utopiaSql.withSql(queryTeacherBefore).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end, "schoolId", schoolId)).queryValue(Integer.class);
//                    int studentQty = utopiaSql.withSql(queryStudentBefore).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end, "schoolId", schoolId)).queryValue(Integer.class);
//
//                    researchInfo.setTeacherCount(teacherQty);
//                    researchInfo.setAuthStudentCount(studentQty);
//                }
//
//            } else if (start.getTime() <= rstaff.getTime() && end.getTime() > rstaff.getTime()) {       // 处理start<=9.12<end  的认证数据
//                //rstaff这里没有《=是因为要和教研员累计逻辑保证一致，否则数据会多一天当天数据，所以<
//                if (schoolId == 0) {
//                    String queryTeacherMid = queryTeacher + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE AND VCO.CREATETIME >=:start  AND VCO.CREATETIME <=:end ";
//                    String queryStudentMid = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE   AND VEA.CREATETIME >=:start AND VEA.CREATETIME <:rstaff  AND VEA.EXTENSION_ATTRIBUTE_KEY='authenticatedTime' ";
//                    String queryNewStudent = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE   AND VEA.CREATETIME >:rstaff  AND VEA.CREATETIME <=:end  AND VEA.EXTENSION_ATTRIBUTE_KEY='rstaffAuthenticatedTime' ";
//                    int teacherQty = utopiaSql.withSql(queryTeacherMid).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end)).queryValue(Integer.class);
//                    int studentQty = utopiaSql.withSql(queryStudentMid).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "rstaff", rstaff)).queryValue(Integer.class);
//                    int studentNewQty = utopiaSql.withSql(queryNewStudent).useParams(MiscUtils.map("regionList", regionIdList, "rstaff", rstaff, "end", end)).queryValue(Integer.class);
//
//                    researchInfo.setTeacherCount(teacherQty);
//                    researchInfo.setAuthStudentCount(studentQty + studentNewQty);
//                } else {
//                    String queryTeacherMid = queryTeacher + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE AND VCO.CREATETIME >=:start  AND VCO.CREATETIME <=:end AND VS.ID=:schoolId ";
//                    String queryStudentMid = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE   AND VEA.CREATETIME >=:start AND VEA.CREATETIME <:rstaff  AND VEA.EXTENSION_ATTRIBUTE_KEY='authenticatedTime' AND VS.ID =:schoolId ";
//                    String queryNewStudent = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)   AND U.DISABLED=FALSE   AND VEA.CREATETIME >:rstaff  AND VEA.CREATETIME <=:end  AND VEA.EXTENSION_ATTRIBUTE_KEY='rstaffAuthenticatedTime' AND VS.ID =:schoolId ";
//                    int teacherQty = utopiaSql.withSql(queryTeacherMid).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end, "schoolId", schoolId)).queryValue(Integer.class);
//                    int studentQty = utopiaSql.withSql(queryStudentMid).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "rstaff", rstaff, "schoolId", schoolId)).queryValue(Integer.class);
//                    int studentNewQty = utopiaSql.withSql(queryNewStudent).useParams(MiscUtils.map("regionList", regionIdList, "rstaff", rstaff, "end", end, "schoolId", schoolId)).queryValue(Integer.class);
//
//                    researchInfo.setTeacherCount(teacherQty);
//                    researchInfo.setAuthStudentCount(studentQty + studentNewQty);
//                }
//
//            } else {                      // 处理start>9.12<end  的认证数据
//
//                if (schoolId == 0) {
//                    String queryTeacherAfter = queryTeacher + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE AND VCO.CREATETIME >=:start  AND VCO.CREATETIME <=:end ";
//                    String queryStudentAfter = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE   AND VEA.CREATETIME >=:start AND VEA.CREATETIME <=:end  AND VEA.EXTENSION_ATTRIBUTE_KEY='rstaffAuthenticatedTime' ";
//                    int teacherQty = utopiaSql.withSql(queryTeacherAfter).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end)).queryValue(Integer.class);
//                    int studentQty = utopiaSql.withSql(queryStudentAfter).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end)).queryValue(Integer.class);
//
//                    researchInfo.setTeacherCount(teacherQty);
//                    researchInfo.setAuthStudentCount(studentQty);
//                } else {
//                    String queryTeacherAfter = queryTeacher + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE AND VCO.CREATETIME >=:start  AND VCO.CREATETIME <=:end  AND VS.ID=:schoolId ";
//                    String queryStudentAfter = queryStudent + " WHERE VS.REGION_CODE IN (:regionList)  AND U.DISABLED=FALSE   AND VEA.CREATETIME >=:start AND VEA.CREATETIME <=:end  AND VEA.EXTENSION_ATTRIBUTE_KEY='rstaffAuthenticatedTime' AND VS.ID =:schoolId  ";
//                    int teacherQty = utopiaSql.withSql(queryTeacherAfter).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end, "schoolId", schoolId)).queryValue(Integer.class);
//                    int studentQty = utopiaSql.withSql(queryStudentAfter).useParams(MiscUtils.map("regionList", regionIdList, "start", start, "end", end, "schoolId", schoolId)).queryValue(Integer.class);
//
//                    researchInfo.setTeacherCount(teacherQty);
//                    researchInfo.setAuthStudentCount(studentQty);
//                }
//
//            }
//
//        } catch (Exception ex) {
//            logger.error("教研员后台分时查询算法异常:" + ex.getMessage(), ex);
//        }
//        return researchInfo;
//    }

    @SuppressWarnings("deprecation")
    @Override
    public List<BizMarketingSchoolData> getGroupbyTeacherIdFinderToSchoolStatistic(List<BizMarketingSchoolData> schoolStatistics) {
        GroupByFinder<BizMarketingSchoolData> bsfinder = GroupByFinder.newInstance(BizMarketingSchoolData.class, schoolStatistics);
        bsfinder.groupBy("province", "provinceCode", "city", "cityCode", "area", "areaCode",
                "teacherId", "teacherName",
                "schoolId", "schoolName");
        bsfinder.sum("restaffAuthTotal");
        bsfinder.sum("ifVoice");
        bsfinder.sum("classNumber");
        Map<GroupByFinder.GroupByKey, Map<String, Object>> gr = bsfinder.find();
        List<BizMarketingSchoolData> bss = new ArrayList<>();
        Map<String, BizMarketingSchoolData> map = new HashMap<>();
        for (GroupByFinder.GroupByKey key : gr.keySet()) {
            key.getCount();
            String province = (String) key.getValue("province");
            Integer provinceCode = (Integer) key.getValue("provinceCode");
            String city = (String) key.getValue("city");
            Integer cityCode = (Integer) key.getValue("cityCode");
            String area = (String) key.getValue("area");
            Integer areaCode = (Integer) key.getValue("areaCode");
            Long teacherId = (Long) key.getValue("teacherId");
            String schoolName = (String) key.getValue("schoolName");
            Long schoolId = (Long) key.getValue("schoolId");
            String teacherName = (String) key.getValue("teacherName");
            @SuppressWarnings("UnusedDeclaration")
            Integer rstaffAuthCount = (Integer) gr.get(key).get("sum_restaffAuthTotal");        // add to column rstaffAuthCount
            Integer ifVoice = (Integer) gr.get(key).get("sum_ifVoice");
            int classSize = (Integer) gr.get(key).get("sum_classNumber");
            String mapKey = teacherId + "_" + schoolId;
            if (map.containsKey(mapKey)) {
                BizMarketingSchoolData oldV = map.get(mapKey);
                oldV.setRestaffAuthTotal(oldV.getRestaffAuthTotal() + rstaffAuthCount);
                oldV.setClassNumber(oldV.getClassNumber() + key.getCount());
                oldV.setClassSize(oldV.getClassSize() + classSize);
                oldV.setIfVoice(oldV.getIfVoice() + ifVoice);
            } else {
                BizMarketingSchoolData bbsdata = new BizMarketingSchoolData();
                bbsdata.setProvince(province);
                bbsdata.setProvinceCode(provinceCode);
                bbsdata.setCity(city);
                bbsdata.setCityCode(cityCode);
                bbsdata.setArea(area);
                bbsdata.setAreaCode(areaCode);
                bbsdata.setTeacherId(teacherId);
                bbsdata.setTeacherName(teacherName);
                bbsdata.setSchoolId(schoolId);
                bbsdata.setSchoolName(schoolName);

                bbsdata.setRestaffAuthTotal(rstaffAuthCount);
                bbsdata.setIfVoice(ifVoice);
                bbsdata.setClassNumber(key.getCount());
                bbsdata.setClassSize(classSize);

                map.put(mapKey, bbsdata);
            }
        }
        bss.addAll(map.values());
        return bss;
    }

    @Override
    public List<Map<String, Object>> findInviteHistoryByUserId(Long rstaffId) {
        if (null == rstaffId) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new LinkedList<>();
        Date date = DateUtils.nextDay(new Date(), -90);
        List<InviteHistory> inviteHistoryPage = asyncInvitationServiceClient.loadByInviter(rstaffId)
                .filter(t -> t.getCreateTime() > date.getTime())
                .toList();

        Set<Long> teacherIds = new LinkedHashSet<>();
        for (InviteHistory history : inviteHistoryPage) {
            Long teacherId = history.getInviteeUserId();
            CollectionUtils.addNonNullElement(teacherIds, teacherId);
        }
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        Map<Long, UserActivity> activities = userActivityServiceClient.getUserActivityService()
                .findUserActivities(teachers.keySet())
                .getUninterruptibly()
                .values()
                .stream()
                .map(t -> t.stream()
                        .filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                        .sorted((o1, o2) -> {
                            long a1 = SafeConverter.toLong(o1.getActivityTime());
                            long a2 = SafeConverter.toLong(o2.getActivityTime());
                            return Long.compare(a2, a1);
                        })
                        .findFirst()
                        .orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toMap(UserActivity::getUserId, t -> t));

        for (InviteHistory inviteHistory : inviteHistoryPage) {
            Map<String, Object> record = generateTeacherBasicInviteRecord(teachers, activities, inviteHistory);
            if (record == null) continue;
            result.add(record);
        }

        Collections.sort(result, (o1, o2) -> ((Integer) o2.get("rank")).compareTo((Integer) o1.get("rank")));

        return result;
    }

    public List<Map<String, Object>> findInviteHistoryByUserId(Long rstaffId, Subject subject, Boolean isSuccessful, Date startDate, Date endDate, boolean needStuCount) {
        if (rstaffId == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new LinkedList<>();
        if (startDate == null)
            startDate = DateUtils.nextDay(new Date(), -90);
        Date theStartDate = startDate;

        List<InviteHistory> inviteHistoryPage = asyncInvitationServiceClient.loadByInviter(rstaffId)
                .filter(t -> {
                    if (isSuccessful == null) return true;
                    if (isSuccessful) {
                        return t.isDisabled();
                    } else {
                        return !t.isDisabled();
                    }
                })
                .filter(t -> t.getCreateTime() >= theStartDate.getTime())
                .filter(t -> endDate == null || t.getCreateTime() <= endDate.getTime())
                .toList();

        // 得到所有老师id
        Set<Long> teacherIds = new LinkedHashSet<>();
        for (InviteHistory history : inviteHistoryPage) {
            Long teacherId = history.getInviteeUserId();
            CollectionUtils.addNonNullElement(teacherIds, teacherId);
        }
        // 老师信息
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        if (teachers != null) {
            Iterator<Map.Entry<Long, Teacher>> entries = teachers.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Long, Teacher> entry = entries.next();
                Teacher teacher = entry.getValue();
                if (teacher == null || !teacher.getSubject().equals(subject)) {
                    entries.remove();
                }
            }
        }
        // 老师行为数据
        Map<Long, UserActivity> activities = userActivityServiceClient.getUserActivityService()
                .findUserActivities(teachers.keySet())
                .getUninterruptibly()
                .values()
                .stream()
                .map(t -> t.stream()
                        .filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                        .sorted((o1, o2) -> {
                            long a1 = SafeConverter.toLong(o1.getActivityTime());
                            long a2 = SafeConverter.toLong(o2.getActivityTime());
                            return Long.compare(a2, a1);
                        })
                        .findFirst()
                        .orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toMap(UserActivity::getUserId, t -> t));

        for (InviteHistory inviteHistory : inviteHistoryPage) {
            Map<String, Object> record = generateTeacherBasicInviteRecord(teachers, activities, inviteHistory);
            if (record == null) continue;
            result.add(record);
        }

        // 获得邀请学生数量数据
        if (needStuCount) {
            List<RSTeacherAuthStudentCountDaily> studentCountDailys = rsTeacherAuthStudentCountDailyDao.findByTeacherIds(teachers.keySet(), startDate, endDate);
            Map<Long, Integer> countMap = new LinkedHashMap<>();
            for (RSTeacherAuthStudentCountDaily studentCountDaily : studentCountDailys) {
                Long teacherId = studentCountDaily.getTeacherId();
                if (!countMap.containsKey(teacherId)) {
                    countMap.put(teacherId, 0);
                }
                countMap.put(teacherId, countMap.get(teacherId) + studentCountDaily.getStudentCount() - studentCountDaily.getDuplicateStudentCount());
            }
            for (Map<String, Object> record : result) {
                Long teacherId = (Long) record.get("teacherId");
                if (countMap.containsKey(teacherId)) {
                    // 学生数量
                    record.put("studentCount", countMap.get(teacherId));
                }
            }
        }

        Collections.sort(result, (o1, o2) -> ((Integer) o2.get("rank")).compareTo((Integer) o1.get("rank")));

        return result;
    }


    // 查询今天的“市场任务”是否成功执行，成功返回“SUCCESS”，否则返回“FAIL”
    @Override
    public String validMarketJobTaskRunSuccessOrFaild() {
        // FIXME 这块早就没人维护了吧。。。
        return "FAIL";
//        // FIXME: 先临时硬编码在这里吧。
//        // FIXME: 以下是JobJournal的mongo信息
//        // FIXME: @UtopiaMongoEntity(mongoName = "mongo-journal", databaseName = "vox-statistics")
//        // FIXME: @Document(collection = "vox_job_journal")
//        MongoClient client = MongoClientBuilder.getInstance().getMongoClient("mongo-journal");
//
//        DB db = client.getClient().getDB("vox-statistics");
//        DBCollection collection = db.getCollection("vox_job_journal");
//
//        String jobStartDate = DayRange.current().toString();
//        BasicDBObject query = new BasicDBObject()
//                .append("jobStartDate", jobStartDate)
//                .append("jobName", "市场任务新")         //市场任务->市场任务新
//                .append("success", true);
//        long count = collection.count(query);
//        return count > 0 ? "SUCCESS" : "FAIL";
    }


    @Override
    public ResearchStaffPatternMapper getPatternData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getPatternData(provinceCodes, cityCodes, areaCodes, schoolIds, year, term);
    }

    @Override
    public ResearchStaffSkillMapper getSkillData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getSkillData(provinceCodes, cityCodes, areaCodes, schoolIds, year, term);
    }

    @Override
    public ResearchStaffKnowledgeMapper getKnowledgeData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getKnowledgeData(provinceCodes, cityCodes, areaCodes, schoolIds, year, term);
    }

    @Override
    public List<ResearchStaffWeakPointUnitMapper> getWeakPointData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getWeakPointData(cityCodes, areaCodes, schoolIds, year, term);
    }

    @Override
    public ResearchStaffUnitWeakPointMapper getUnitWeakPointData(Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getUnitWeakPointData(cityCodes, areaCodes, schoolIds, year, term);
    }

    @Override
    public ResearchStaffSkillMonthlyMapper getSkillMonthlyData(Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getSkillMonthlyData(provinceCodes, cityCodes, areaCodes, schoolIds, year, term);
    }

    @Override
    public List<RSPaperAnalysisReport> getPaperAnalysisReport(String paperId, Integer regionCode, RegionType regionType) {
        return researchStaffReportServiceImpl.getPaperAnalysisReport(paperId, regionCode, regionType);
    }

    @Override
    public List<RSPaperAnalysisReport> getPaperAnalysisReport(String paperId, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds) {
        return researchStaffReportServiceImpl.getPaperAnalysisReport(paperId, cityCodes, areaCodes, schoolIds);
    }

    @Override
    public List<RSOralPaperReportMapper> getOralAnalysisReport(Long pushId) {
        return researchStaffReportServiceImpl.getOralAnalysisReport(pushId);
    }

    @Override
    public List<ResearchStaffBehaviorDataMapper> getBehaviorData(Long rstaffId, Subject subject, Collection<Long> provinceCodes, Collection<Long> cityCodes, Collection<Long> areaCodes, Collection<Long> schoolIds, Integer year, Term term) {
        return researchStaffReportServiceImpl.getBehaviorData(rstaffId, subject, provinceCodes, cityCodes, areaCodes, schoolIds, year, term);
    }

    /////////////////////////////////////////私有方法//////////////////////////////////////////////////////////

    /**
     * 生成老师邀请信息的基础数据
     * 包括：
     * 1. 老师姓名
     * 2. 老师手机
     * 3. 邀请时间
     * 4. 邀请方式
     * 5. 是否登录
     * 6. 布置作业
     * 7. 是否认证
     * 8. 奖励状态
     * 9. 建议
     *
     * @param teachers
     * @param activities
     * @param inviteHistory
     * @return
     * @author changyuan.liu
     */
    private Map<String, Object> generateTeacherBasicInviteRecord(Map<Long, Teacher> teachers, Map<Long, UserActivity> activities, InviteHistory inviteHistory) {
        Map<String, Object> record = new HashMap<>();
        Long teacherId = inviteHistory.getInviteeUserId();
        Teacher teacher = teachers.get(teacherId);
        if (teacher == null) {
            return null;
        }
        int rank = 1;
        // 老师id
        record.put("teacherId", teacherId);
        // 老师姓名
        record.put("teacherName", teacher.getProfile().getRealname());
        // 邀请时间
        record.put("inviteDate", DateUtils.dateToString(inviteHistory.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
        String inviteType;
        if (inviteHistory.getInvitationType() != null && InvitationType.RSTAFF_INVITE_TEACHER_SMS == inviteHistory.getInvitationType()) {
            inviteType = "短信";
            Map<String, List<UserExtensionAttribute>> attributes = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                    .toGroup(UserExtensionAttribute::getKey);
            record.put("arrangeNotified", attributes.containsKey(RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_ARRANGE.name()));
            record.put("authenticationNotified", attributes.containsKey(RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_AUTHENTICATE.name()));
        } else {
            inviteType = "链接";
        }
        // 邀请方式
        record.put("inviteType", inviteType);
        // 老师手机
        // TODO 需要把research staff相关服务都放到user里
        if (StringUtils.isNotBlank(inviteHistory.getInviteSensitiveMobile())) {
            String mobileObscured = sensitiveUserDataServiceClient.loadUserMobileObscured(teacherId);
            record.put("teacherMobile", mobileObscured);
        }

        // 是否登陆
//        Date lastLoginTime = userLoaderClient.findUserLastLoginTime(teacher);
        Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacher.getId());
        record.put("loginFlag", lastLoginTime != null);

        if (lastLoginTime != null) {
            rank = 2;
        }
        UserActivity activity = activities.get(teacher.getId());
        Date teacherLatestCheckHomeworkTime = (activity == null ? null : activity.getActivityTime());
        // 布置作业
        record.put("arrangeFlag", teacherLatestCheckHomeworkTime != null);
        if (teacherLatestCheckHomeworkTime != null) {
            rank = 3;
        }
        boolean authenticated = teacher.fetchCertificationState() == AuthenticationState.SUCCESS;
        // 是否认证
        record.put("teacherAuthState", authenticated);
        if (authenticated) {
            rank = 0;
        }
        record.put("rank", rank);
        // 奖励状态
        record.put("reward", "300园丁豆");
        return record;
    }


}
