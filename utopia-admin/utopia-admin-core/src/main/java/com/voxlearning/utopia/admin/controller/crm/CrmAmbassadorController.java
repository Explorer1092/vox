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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.AmbassadorReportType;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportInfo;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Longlong Yu
 * @since 下午2:05,13-12-4.
 */
@Controller
@RequestMapping("/crm/ambassador")
public class CrmAmbassadorController extends CrmAbstractController {

    private static final Map<String, String> authStateMap;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("ALL", "全部");
        map.put("SUCCESS", "已认证");
        map.put("FAILURE", "未通过");
        map.put("WAITING_REACH", "未认证符合认证条件");
        map.put("WAITING_NOT_REACH", "未认证不符合条件");
        authStateMap = Collections.unmodifiableMap(map);
    }

    private static final Integer countPerPage = 20;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;

    //    @RequestMapping(value = "ambassadorindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String ambassadorIndexPost(Model model) {
//
////        def conditionKeys = ['authState', 'pageNum', 'totalPageNum']
//
//        Map<String, Object> conditionMap = new HashMap<>();
//        // 默认条件是未认证
//        conditionMap.put("authState", getRequestParameter("authState", "WAITING_REACH"));
//        conditionMap.put("pageNum", getRequestInt("pageNum", 0));
//        conditionMap.put("totalPageNum", getRequestInt("pageNum", 0));
//
//        model.addAttribute("ambassadorInfoList", getAmbassadorInfoList(conditionMap));
//        model.addAttribute("authStateMap", authStateMap);
//        model.addAttribute("conditionMap", conditionMap);
//        return "crm/ambassador/ambassadorindex";
//    }

    @RequestMapping(value = "report.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String ambassadorReport(Model model) {
        Integer type = getRequestInt("type");
        if (type == 0) {
            type = AmbassadorReportType.APPLY_CANCLE_TEACHER_AUTH.getType();
        }
        List<AmbassadorReportInfo> infoList = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorReportInfosByType(type);
        model.addAttribute("infoList", infoList);
        model.addAttribute("type", type);
        return "crm/ambassador/report";
    }

    @RequestMapping(value = "deletereport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteReport() {
        Long infoId = getRequestLong("infoId");
        ambassadorServiceClient.getAmbassadorService().$disableAmbassadorReportInfo(infoId);
        return MapMessage.successMessage().setInfo("操作成功");
    }

//    @RequestMapping(value = "batchmoveschool.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage batchMoveSchool() {
//        String teacherIds = getRequestString("teacherIds");
//        String schoolId = getRequestString("schoolId");
//        String[] teacherIdArr = teacherIds.split(",");
//        Long curSid = ConversionUtils.toLong(schoolId);
//        School school = schoolLoaderClient.loadSchool(curSid);
//        if (school == null) {
//            return MapMessage.errorMessage("学校不存在");
//        }
//        for (String teacherId : teacherIdArr) {
//            try {
//                Long curTid = ConversionUtils.toLong(teacherId);
//                List<GroupMapper> groupList = groupLoaderClient.loadTeacherGroupsByTeacherId(curTid, false);
//                boolean clazzFlag = false;
//                for (GroupMapper group : groupList) {
//                    Set<Long> sharedGroupIds = groupLoaderClient.loadSharedGroupIds(group.getId());
//                    if (sharedGroupIds.size() > 0) {
//                        clazzFlag = true;
//                        break;
//                    }
//                }
//                boolean groupFlag = false;
//                Set<Long> teacherGroupIds = groupList.stream().map(GroupMapper::getId).collect(Collectors.toSet());
//                Map<Long, Set<Long>> sharedGroupIds = groupLoaderClient.loadSharedGroupIds(teacherGroupIds);
//                for (Long key : sharedGroupIds.keySet()) {
//                    if (sharedGroupIds.get(key).size() > 0) {
//                        groupFlag = true;
//                        break;
//                    }
//                }
//                // FIX BUG
//                // 需要检查老师名下的学生是否在其他组，如果在则不能转校
//                boolean studentFlag = false;
//                Map<Long, List<Long>> studentIdMap = studentLoaderClient.loadGroupStudentIds(teacherGroupIds);
//                Set<Long> studentIdSet = studentIdMap.values().stream().flatMap(e -> e.stream()).collect(Collectors.toSet());
//                Map<Long, List<GroupMapper>> studentGroups = groupLoaderClient.loadStudentGroups(studentIdSet, false);
//                for (Map.Entry<Long, List<GroupMapper>> entry : studentGroups.entrySet()) {
//                    Long u = entry.getKey();
//                    List<GroupMapper> gs = entry.getValue();
//                    if (gs.size() > 1) {
//                        studentFlag = true;
//                        break;
//                    }
//                }
//                if(studentFlag){
//                    continue;
//                }
//                if (clazzFlag) {
//                    continue;
//                }
//                if (groupFlag) {
//                    continue;
//                }
//                School originalSchool = schoolLoaderClient.loadTeacherSchool(curTid);
//                if (originalSchool == null) {
//                    continue;
//                }
//                if (Objects.equals(originalSchool.getId(), curSid)) {
//                    continue;
//                }
//                //删除原来关系
//                userSchoolRefPersistence.deleteByUserId(curTid);
//                //插入新关系
//                UserSchoolRef userSchoolRef = UserSchoolRef.newInstance(curTid, curSid);
//                userSchoolRefPersistence.persist(userSchoolRef);
//                //新的转移逻辑
//                crmTeacherSystemClazzService.changeTeacherSchool(curTid, teacherGroupIds, curSid);
//                //清理前台cache
//                userCacheClient.evictUserCache(curTid);
//                //处理VOX_CLAZZ_TEACHER_ALTERATION表
//                Collection<String> keys = clazzTeacherAlterationPersistence.calculateCacheKeys(
//                        "WHERE (APPLICANT_ID=:teacherId OR RESPONDENT_ID=:teacherId) AND ALTERATION_STATE='PENDING'",
//                        MiscUtils.m("teacherId", curTid)
//                );
//                // FIXME remove delete sql
//                String sql = "UPDATE VOX_CLAZZ_TEACHER_ALTERATION SET DISABLED=TRUE, UPDATE_DATETIME=NOW() " +
//                        "WHERE (APPLICANT_ID=:teacherId OR RESPONDENT_ID=:teacherId) AND ALTERATION_STATE='PENDING'";
//                int rows = clazzTeacherAlterationPersistence.getUtopiaSql().withSql(sql)
//                        .useParams(MiscUtils.m("teacherId", curTid)).executeUpdate();
//                if (rows > 0) {
//                    clazzTeacherAlterationPersistence.getCache().delete(keys);
//                }
//
//                //处理举报信息 c
//                List<AmbassadorReportInfo> list = ambassadorReportInfoPersistence.cache().loadByTeacherId(curTid);
//                for (AmbassadorReportInfo info : list) {
//                    ambassadorReportInfoPersistence.deleteById(info.getId());
//                }
//
//            } catch (Exception ex) {
//                logger.error("move teacher school error, the error is ", ex.getMessage());
//            }
//        }
//        //记录管理员操作日志
//        addAdminLog(getCurrentAdminUser().getAdminUserName() + "在【校园大使举报老师】批量移动了老师到银座十号小学");
//        return MapMessage.successMessage().setInfo("操作成功");
//    }

    /**
     * *********************private method*****************************************************************
     */
//    private List getAmbassadorInfoList(Map<String, Object> conditionMap) {
//
//        String queryFields = "select uea.CREATETIME as createDatetime, uea.USER_ID as recommendedTeacherId, uea.EXTENSION_ATTRIBUTE_VALUE as ambassadorId, " +
//                " uu.REALNAME as recommendedTeacherName, uu1.REALNAME as ambassadorName, uu.AUTHENTICATION_STATE as authState ";
//
//        String query = "from VOX_USER_EXTENSION_ATTRIBUTE uea " +
//                " inner join UCT_USER uu on uu.ID = uea.USER_ID ";
//
//        Map<String, Object> queryParamsMap = new HashMap<>();
//        queryParamsMap.put("attributeKey", UserExtensionAttributeKeyType.SCHOOL_AMBASSADOR_RECOMMEND_TEACHER_AUTHENTICATION.toString());
//
//        String checkAuthQuery = "";
//        if ((conditionMap.get("authState") != null) && !"ALL".equals(conditionMap.get("authState"))) {
//            query += " and uu.AUTHENTICATION_STATE = :authState ";
//            switch (String.valueOf(conditionMap.get("authState"))) {
//                case "SUCCESS":
//                    queryParamsMap.put("authState", AuthenticationState.SUCCESS.getState());
//                    break;
//                case "FAILURE":
//                    queryParamsMap.put("authState", AuthenticationState.FAILURE.getState());
//                    break;
//                case "WAITING_REACH":  // 未认证但符合认证条件
//                    checkAuthQuery = " and exists( " +
//                            " select 1 from UCT_USER_AUTHENTICATION uua " +
//                            " inner join VOX_CLASS_TEACHER_REF vctr on vctr.USER_ID = uua.USER_ID and vctr.DISABLED = 0 " +
//                            " and exists ( " +
//                            "   select vshs.CLAZZ_ID,COUNT(vshs.STUDENT_ID) AS studentAccount,vshs.TEACHER_ID" +
//                            "   from VOX_CLASS_TEACHER_REF vctr" +
//                            "   inner join VOX_STUDENT_HOMEWORK_STAT  vshs  on vctr.USER_ID=vshs.TEACHER_ID AND vctr.CLASS_ID = vshs.CLAZZ_ID" +
//                            "   WHERE vctr.USER_ID =uua.USER_ID AND  vctr.DISABLED=FALSE AND  vshs.FINISH_HOMEWORK_COUNT >=3" + //完成作业数量>=3
//                            "   GROUP BY vshs.CLAZZ_ID  HAVING studentAccount >=8" +  //同一班级下人数>=8
//                            " )" +
//                            " and exists (" +   //绑定家长手机的学生不少于3人，不限定同一班级
//                            "   SELECT tr.USER_ID, COUNT(DISTINCT spr.STUDENT_ID) as studentAccount " +
//                            "   FROM VOX_CLASS_TEACHER_REF tr" +
//                            "   INNER JOIN VOX_CLASS_STUDENT_REF sr ON tr.CLASS_ID=sr.CLAZZ_ID AND sr.DISABLED=0 " +
//                            "   INNER JOIN VOX_STUDENT_PARENT_REF spr ON spr.KEY_PARENT=1 AND spr.DISABLED=0 AND spr.STUDENT_ID=sr.USER_ID" +
//                            "   WHERE tr.USER_ID=uua.USER_ID AND tr.DISABLED=0 " +
//                            "   GROUP BY tr.USER_ID HAVING studentAccount >= 3" +
//                            " )" +
//                            " where uua.USER_ID = uea.USER_ID and uua.MOBILE is not null and uua.DISABLED = 0 " +
//                            " ) ";
//                    queryParamsMap.put("authState", AuthenticationState.WAITING.getState());
//                    break;
//                case "WAITING_NOT_REACH":  //未认证而且不符合认证条件
//                    checkAuthQuery = " and not exists( " +
//                            " select 1 from UCT_USER_AUTHENTICATION uua " +
//                            " inner join VOX_CLASS_TEACHER_REF vctr on vctr.USER_ID = uua.USER_ID and vctr.DISABLED = 0 " +
//                            " and exists ( " +
//                            "   select vshs.CLAZZ_ID,COUNT(vshs.STUDENT_ID) AS studentAccount,vshs.TEACHER_ID" +
//                            "   from VOX_CLASS_TEACHER_REF vctr" +
//                            "   inner join VOX_STUDENT_HOMEWORK_STAT  vshs  on vctr.USER_ID=vshs.TEACHER_ID AND vctr.CLASS_ID = vshs.CLAZZ_ID" +
//                            "   WHERE vctr.USER_ID =uua.USER_ID AND  vctr.DISABLED=FALSE AND  vshs.FINISH_HOMEWORK_COUNT >=3" + //完成作业数量>=3
//                            "   GROUP BY vshs.CLAZZ_ID  HAVING studentAccount >=8" +  //同一班级下人数>=8
//                            " )" +
//                            " and exists (" +  //绑定家长手机的学生不少于3人，不限定同一班级
//                            "   SELECT tr.USER_ID, COUNT(DISTINCT spr.STUDENT_ID) as studentAccount " +
//                            "   FROM VOX_CLASS_TEACHER_REF tr" +
//                            "   INNER JOIN VOX_CLASS_STUDENT_REF sr ON tr.CLASS_ID=sr.CLAZZ_ID AND sr.DISABLED=0 " +
//                            "   INNER JOIN VOX_STUDENT_PARENT_REF spr ON spr.KEY_PARENT=1 AND spr.DISABLED=0 AND spr.STUDENT_ID=sr.USER_ID" +
//                            "   WHERE tr.USER_ID=uua.USER_ID AND tr.DISABLED=0 " +
//                            "   GROUP BY tr.USER_ID HAVING studentAccount >=3" +
//                            " )" +
//                            " where uua.USER_ID = uea.USER_ID and uua.MOBILE is not null and uua.DISABLED = 0 " +
//                            " ) ";
//                    queryParamsMap.put("authState", AuthenticationState.WAITING.getState());
//                    break;
//            }
//        }
//
//        query += " inner join UCT_USER uu1 on uu1.ID = uea.EXTENSION_ATTRIBUTE_VALUE " +
//                " where uea.EXTENSION_ATTRIBUTE_KEY = :attributeKey " + checkAuthQuery;
//
//
//        String queryTotalCount = "select count(1) " + query;
//        int totalCount = utopiaSql.withSql(queryTotalCount).useParams(queryParamsMap).queryValue(Integer.class);
//
//        conditionMap.put("totalPageNum", (int) Math.ceil(totalCount * 1.0 / countPerPage));
//        Integer pageNum = crmUserService.getAppropriatePageNum(totalCount, (Integer) conditionMap.get("pageNum"), countPerPage);
//        conditionMap.put("pageNum", pageNum);
//        queryParamsMap.put("offset", pageNum * countPerPage);
//        queryParamsMap.put("amount", countPerPage);
//
//        query = queryFields + query +
//                " group by uea.USER_ID,uea.EXTENSION_ATTRIBUTE_VALUE " +
//                " order by uea.CREATETIME desc limit :offset, :amount ";
//        List<Map<String, Object>> ambassadorInfoList = utopiaSql.withSql(query).useParams(queryParamsMap).queryAll();
//
//        for (Map<String, Object> ambassadorInfo : ambassadorInfoList) {
//            School school = schoolLoaderClient.loadTeacherSchool(NumberUtils.toLong(String.valueOf(ambassadorInfo.get("recommendedTeacherId"))));
//            ambassadorInfo.put("schoolId", (school == null) ? null : school.getId());
//            ambassadorInfo.put("schoolName", (school == null) ? null : school.getCname());
//        }
//
//        return ambassadorInfoList;
//    }
}
