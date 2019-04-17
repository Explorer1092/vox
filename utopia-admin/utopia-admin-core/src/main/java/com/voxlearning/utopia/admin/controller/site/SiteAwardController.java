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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量发奖励
 * Created by Shuai Huan on 2014/10/30.
 */
@Controller
@RequestMapping("/site/award")
public class SiteAwardController extends AbstractAdminSystemController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

//    @RequestMapping(value = "batchawardhomepage.vpage", method = RequestMethod.GET)
//    String batchAwardHomepage(Model model) {
//        Collection<BabelPet> babelPets = babelLoaderClient.loadAvailablePets().values();
//        model.addAttribute("babelPets", babelPets);
//        return "site/batch/batchawardhomepage";
//    }

//    @RequestMapping(value = "batchawardsend.vpage", method = RequestMethod.POST)
//    public String batchSendAwardHomepage(@RequestParam String content, Model model) {
//        if (StringUtils.isEmpty(content)) {
//            getAlertMessageManager().addMessageError("奖励内容不能为空");
//        }
//
//        String[] awards = content.split("\\n");
//        List<String> lstSuccess = new ArrayList<>();
//        List<String> lstFailed = new ArrayList<>();
//
//        for (String m : awards) {
//            String[] info = m.split("\\t");
//            if (info.length < 3) {
//                lstFailed.add(m);
//                continue;
//            }
//
//            String userId = StringUtils.deleteWhitespace(info[0]);
//            String awardType = StringUtils.deleteWhitespace(info[1]);
//            String awardContent = StringUtils.deleteWhitespace(info[2]);
//            String awardCountStr = StringUtils.deleteWhitespace(info[3]);
//
//            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(awardType)) {
//                lstFailed.add(m);
//                continue;
//            }
//            CrmAwardType crmAwardType = CrmAwardType.valueOf(awardType);
//            if (crmAwardType == null) {
//                lstFailed.add(m);
//                continue;
//            }
//
//            boolean success = false;
//            try {
//                User user = userLoaderClient.loadUser(Long.parseLong(userId));
//                if (user == null) {
//                    lstFailed.add(m);
//                    continue;
//                }
//                BabelRole role = babelLoaderClient.loadRole(user.getId());
//                if (role == null) {
//                    lstFailed.add(m);
//                    continue;
//                }
//                switch (crmAwardType) {
//                    case PK武装: {
////                        if (StringUtils.isEmpty(awardContent)) {
////                            success = false;
////                            break;
////                        }
////                        if (StringUtils.isEmpty(awardCountStr)) {
////                            success = false;
////                            break;
////                        }
////                        String[] args = StringUtils.split(awardContent, "_");
////                        if (args.length != 2) {
////                            success = false;
////                            break;
////                        }
////                        success = pkServiceClient.addEquipment(user.getId(), args[0], args[1], Integer.parseInt(awardCountStr)).isSuccess();
//                        break;
//                    }
//                    case PK时装:
////                        success = !StringUtils.isEmpty(awardContent) &&
////                                pkServiceClient.addFashions(user.getId(), Collections.singleton(awardContent), null).isSuccess();
//                        break;
//                    case 通天塔复活卡:
//                        success = !StringUtils.isEmpty(awardCountStr) &&
//                                babelManagementClient.changeItemCount(role, BabelItem.REVIVE, Integer.parseInt(awardCountStr)).isSuccess();
//                        break;
//                    case 通天塔换题卡:
//                        success = !StringUtils.isEmpty(awardCountStr) &&
//                                babelManagementClient.changeItemCount(role, BabelItem.SUBSTITUTE, Integer.parseInt(awardCountStr)).isSuccess();
//                        break;
//                    case 通天塔精力卡:
//                        success = !StringUtils.isEmpty(awardCountStr) &&
//                                babelManagementClient.changeItemCount(role, BabelItem.VITALITY_REFILL, Integer.parseInt(awardCountStr)).isSuccess();
//                        break;
//                    case 通天塔星星:
//                        success = !StringUtils.isEmpty(awardCountStr) &&
//                                babelManagementClient.increaseStar(role, Integer.parseInt(awardCountStr), BabelStarChange.CRM_CHANGE, "CRM手动添加星星").isSuccess();
//                        break;
//                    case 通天塔宠物蛋:
//                        success = !StringUtils.isEmpty(awardContent) && !StringUtils.isEmpty(awardCountStr) &&
//                                babelManagementClient.changePetCount(role, Integer.parseInt(awardContent), Integer.parseInt(awardCountStr)).isSuccess();
//                        break;
//                    case PK活力: {
////                        success = !StringUtils.isEmpty(awardCountStr) &&
////                                pkServiceClient.addVitality(user, Integer.parseInt(awardCountStr), VitalityType.MANUAL);
//                        break;
//                    }
//                    case PK经验: {
////                        success = !StringUtils.isEmpty(awardCountStr) &&
////                                pkServiceClient.upgrade(user, Integer.parseInt(awardCountStr));
//                        break;
//                    }
//                    default:
//                        success = false;
//                        break;
//                }
//            } catch (Exception ex) {
//                success = false;
//            }
//            if (success) {
//                lstSuccess.add(m);
//                // admin log
//                addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "批量发送奖励消息",
//                        "", null, "USER_ID:" + userId + ", 奖励类型:" + awardType + ", 奖励内容:" + awardContent + ", 奖励数量:" + awardCountStr);
//            } else {
//                lstFailed.add(m);
//            }
//        }
//        model.addAttribute("successlist", lstSuccess);
//        model.addAttribute("failedlist", lstFailed);
//        return "/site/batch/batchawardhomepage";
//    }

    //批量为智慧课堂充值学豆
    @RequestMapping(value = "changeclazzintegralhomepage.vpage", method = RequestMethod.GET)
    String changeClazzIntegralHomepage() {
        return "site/batch/changeclazzintegralhomepage";
    }

    @RequestMapping(value = "changeclazzintegral.vpage", method = RequestMethod.POST)
    public String changeClazzIntegral(@RequestParam String content,
                                      @RequestParam String comment,
                                      Model model) {
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("充值学豆内容不能为空");
        }
        if (StringUtils.isEmpty(comment)) {
            getAlertMessageManager().addMessageError("充值原因不能为空");
        }

        String[] clazzIntegrals = content.split("\\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();

        for (String m : clazzIntegrals) {
            String[] info = m.split("\\t");
            if (info.length < 3) {
                lstFailed.add(m);
                continue;
            }

            String clazzId = StringUtils.deleteWhitespace(info[0]);
//            String subjectStr = StringUtils.deleteWhitespace(info[1]);
            String teacherIdStr = StringUtils.deleteWhitespace(info[1]);
            String integral = StringUtils.deleteWhitespace(info[2]);

            if (StringUtils.isEmpty(clazzId) || StringUtils.isEmpty(teacherIdStr) || StringUtils.isEmpty(integral)) {
                lstFailed.add(m);
                continue;
            }
            long teacherId = SafeConverter.toLong(teacherIdStr);
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher == null) {
                lstFailed.add(m);
                continue;
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(Long.parseLong(clazzId));
            if (clazz == null) {
                lstFailed.add(m);
                continue;
            }
            try {
                // 直接支持系统自建班级
                GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazz.getId(), false);
                ClazzIntegralHistory history = new ClazzIntegralHistory();
                history.setGroupId(group.getId());
                history.setClazzIntegralType(ClazzIntegralType.系统充值.getType());
                history.setIntegral(Integer.parseInt(integral));
                history.setComment(ClazzIntegralType.系统充值.getDescription());
                history.setAddIntegralUserId(getCurrentAdminUser().getFakeUserId());
                MapMessage msg = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(history)
                        .getUninterruptibly();
                if (msg.isSuccess()) {
                    String message = "系统为" + clazz.formalizeClazzName() + teacher.getSubject() + "课堂充值" + integral + "个学豆，快去<a style=\"color:#35a4fa;\" href=\"/teacher/smartclazz/list.vpage\">智慧课堂</a>给学生发放奖励吧!";
                    teacherLoaderClient.sendTeacherMessage(teacher.getId(), message);

                    // 记录 UserServiceRecord
                    UserServiceRecord userServiceRecord = new UserServiceRecord();
                    userServiceRecord.setUserId(teacher.getId());
                    userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                    userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                    userServiceRecord.setOperationContent("管理员为智慧课堂充值学豆");
                    userServiceRecord.setComments("学科:" + teacher.getSubject() + ", group:" + group.getId() + ", 学豆数量:" + integral);
                    userServiceClient.saveUserServiceRecord(userServiceRecord);

                    lstSuccess.add(m);
                } else {
                    lstFailed.add(m);
                }
            } catch (Exception ex) {
                lstFailed.add(m);
            }
        }
        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        return "/site/batch/changeclazzintegralhomepage";
    }
}
