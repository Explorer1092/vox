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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.ucenter.CertificationApplicationOperatingLog;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by XiaoPeng.Yang on 15-5-8.
 */
@Controller
@RequestMapping("/site/ambassador")
public class SiteAmbassadorController extends CrmAbstractController {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private CertificationServiceClient certificationServiceClient;

    @RequestMapping(value = "auditambassador.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String auditAmbassador(Model model) {
        //默认查两周之内的数据
        Date beginDate = DateUtils.calculateDateDay(new Date(), -14);
        String sql = "SELECT " +
                " saa.USER_ID " +
                "FROM  " +
                " VOX_SCHOOL_AMBASSADOR saa,  " +
                " VOX_USER_SCHOOL_REF sr  " +
                "WHERE  " +
                " saa.USER_ID = sr.USER_ID  " +
                "AND saa.USER_ID NOT IN (  " +
                " SELECT  " +
                "  sa.USER_ID  " +
                " FROM  " +
                "  VOX_SCHOOL_AMBASSADOR sa,  " +
                "  VOX_SCHOOL s  " +
                " WHERE  " +
                "  sa.USER_ID = s.AMBASSADOR  " +
                " AND sa.CREATE_DATETIME >= ?  " +
                "  AND s.DISABLED=0  " +
                ")  " +
                "AND saa.CREATE_DATETIME >= ?  " +
                "AND sr.DISABLED=0  " +
                "ORDER BY  " +
                " sr.SCHOOL_ID,  " +
                " saa.CREATE_DATETIME DESC";
        List<Long> teacherIds = utopiaSql.withSql(sql).useParamsArgs(beginDate, beginDate).queryColumnValues(Long.class);
        //拼装数据
        List<Map<String, Object>> data = new ArrayList<>();
        Map<Long, School> schoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(teacherIds)
                .getUninterruptibly();
        Map<Long, List<Clazz>> clazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherIds);
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        Map<Long, TeacherExtAttribute> teacherExtAttributeMap = teacherLoaderClient.loadTeacherExtAttributes(teacherIds);
        Map<Long, List<User>> studentMap = studentLoaderClient.loadTeacherStudents(teacherIds);
        for (Long teacherId : teacherIds) {
            Map<String, Object> dataMap = new HashMap<>();

            dataMap.put("userId", teacherId);
            dataMap.put("userName", teacherMap.get(teacherId).fetchRealname());
            dataMap.put("schoolId", schoolMap.get(teacherId).getId());
            dataMap.put("userLevel", teacherExtAttributeMap.containsKey(teacherId) ? SafeConverter.toInt(teacherExtAttributeMap.get(teacherId).getLevel()) : 0);
            List<CertificationApplicationOperatingLog> logList = certificationServiceClient.getRemoteReference()
                    .findCertificationApplicationOperatingLogs(teacherId)
                    .getUninterruptibly();
            if (CollectionUtils.isNotEmpty(logList)) {
                for (CertificationApplicationOperatingLog operatingLog : logList) {
                    if ((null == dataMap.get("verifyTime")) || operatingLog.getCreateTime().after((Date) dataMap.get("verifyTime"))) {
                        dataMap.put("verifyTime", operatingLog.getCreateTime());
                    }
                }
            }
            dataMap.put("clazzCount", clazzMap.get(teacherId).size());
            dataMap.put("studentCount", studentMap.get(teacherId).size());
            data.add(dataMap);
        }
        model.addAttribute("dataList", data);
        return "site/ambassador/auditambassador";
    }

    @RequestMapping(value = "setambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setAmbassador() {
        Long teacherId = getRequestLong("teacherId");
        try {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacherDetail == null || teacherDetail.fetchCertificationState() != AuthenticationState.SUCCESS) {
                return MapMessage.errorMessage("用户不存在或者用户不是认证老师");
            }
            Long schoolId = teacherDetail.getTeacherSchoolId();
            if (schoolId == null || schoolId == 0L) {
                return MapMessage.errorMessage("该用户没有学校");
            }

            //查询本校大使
            List<AmbassadorSchoolRef> refList = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorRefs(schoolId);
            if (CollectionUtils.isEmpty(refList)) {
                //直接添加
                AmbassadorSchoolRef ref = new AmbassadorSchoolRef();
                ref.setAmbassadorId(teacherId);
                ref.setSchoolId(schoolId);
                ambassadorServiceClient.getAmbassadorService().$insertAmbassadorSchoolRef(ref);
            } else {
                //判断有没有同学科的大使
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), schoolId);
                if (ref != null) {
                    return MapMessage.errorMessage("已经存在同学科校园大使");
                } else {
                    AmbassadorSchoolRef schoolRef = new AmbassadorSchoolRef();
                    schoolRef.setAmbassadorId(teacherId);
                    schoolRef.setSchoolId(schoolId);
                    ambassadorServiceClient.getAmbassadorService().$insertAmbassadorSchoolRef(schoolRef);
                }
            }

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.校园大使.name());
            userServiceRecord.setOperationContent("管理员添加用户为校园大使");
            userServiceRecord.setComments("学校:" + schoolId);
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            //清除老师奖品中心愿望盒缓存
            String key = CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_WISH_ORDER, (String[]) null, new Object[]{teacherId});
            RewardCache.getRewardCache().delete(key);
            asyncUserServiceClient.getAsyncUserService()
                    .evictUserCache(teacherId)
                    .awaitUninterruptibly();
            // 将新老师的激活请求改为校园大使的
            businessTeacherServiceClient.changeActivationType(teacherId, true);
            //发短信
            sendMsgToRemind(teacherId);
            //右下角弹窗
            String comment = "<p style=\"border: 1px solid #ddd; width: 96px; height: 96px; margin: 0 auto;\"><img src=\"//cdn.17zuoye.com/static/project/app/publiccode_teacherAcademy.jpg\" width=\"96\"></p>\n" +
                    "<p style=\" margin: 10px; text-align: center;\"><font color=\"red\">恭喜您成为校园大使。关注“校园大使”微信号<br/></font></p>";
            userPopupServiceClient.createPopup(teacherId)
                    .content(comment)
                    .type(PopupType.AMBASSADOR_REMIND)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            return MapMessage.successMessage().setInfo("操作成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    private void sendMsgToRemind(Long userId) {
//        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(userId);
//        if (authentication != null && authentication.isMobileAuthenticated()) {
//            smsServiceClient.createSmsMessage(authentication.getMobile())
//                    .content("恭喜您成为一起作业校园大使，关注“一起作业校园大使”微信公众号，答疑、抽奖、活动抢先知。在校园大使专区，完成“大使学院”课程即可轻松得金币！")
//                    .type(SmsType.AMBASSADOR_REMIND_SMS)
//                    .send();
//        }
    }


}
