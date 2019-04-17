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

package com.voxlearning.washington.controller.thirdparty.qiyukf;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.config.api.QiyukfService;
import com.voxlearning.utopia.service.config.api.entity.QiyukfInfo;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.washington.controller.thirdparty.qiyukf.model.QiYuKFConfig;
import com.voxlearning.washington.data.enums.Terminal;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 七鱼客服跳转
 *
 * @author Wenlong Meng
 * @since Jan 14, 2019
 */
@Slf4j
@Controller
@RequestMapping("/redirect/qiyukf")
public class QiyukfRedirectController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    //local variables
    @Inject protected UserLoaderClient userLoaderClient;
    public static final EnumSet ENABLE_ROBOT = EnumSet.of(Terminal.teacher, Terminal.student, Terminal.parent,
            Terminal.juniorstu,Terminal.juniortea);
    @ImportService(interfaceClass = QiyukfService.class) private QiyukfService qiyukfService;
    //appKey->七鱼客服配置
    LoadingCache<String, List<QiyukfInfo>> appKey2Qiyukf = CacheBuilder.newBuilder().maximumSize(300).refreshAfterWrite(1, TimeUnit.HOURS).build(
            new CacheLoader<String, List<QiyukfInfo>>() {
                @Override
                public List<QiyukfInfo> load(String appKey) {
                    return qiyukfService.loadByAppkey(appKey);
                }
            });
    //Logic
    /**
     * 获取七鱼客服的配置信息。为壳使用七鱼的SDK准备的
     *
     * @return
     */
    @RequestMapping(value = "loadConfig.vpage")
    @ResponseBody
    public MapMessage loadConfig() {
        //参数
        String questionType = getRequestString("questionType");
        Long reqUserId = currentUserId();
        String appKey = this.getRequestString("app_key");

        List<QiyukfInfo> qiyukfInfos = appKey2Qiyukf.getUnchecked(appKey);
        if(ObjectUtils.anyBlank(qiyukfInfos)){
            LoggerUtils.info("app_key.error", questionType, appKey, reqUserId);
            return MapMessage.errorMessage("app_key error");
        }
        QiyukfInfo qiyukfInfo = qiyukfInfos.stream().filter(q->StringUtils.equals(q.getQuestionType(), questionType)).findFirst().orElse(null);
        if(qiyukfInfo == null){
            LoggerUtils.info("questionType.error", questionType, appKey, reqUserId);
            qiyukfInfo = qiyukfInfos.get(0);
        }

        //构建信息
        User user = getUser(reqUserId);
        String name = ObjectUtils.get(()->user.fetchRealname());

        MapMessage resultMsg = new MapMessage();
        resultMsg.add("result", "success");
        resultMsg.add("destId", qiyukfInfo.getCsGroupId());
        resultMsg.add("robustOption", qiyukfInfo.getRobotId() > 0 ? 1 : 0);
        resultMsg.add("qType", qiyukfInfo.getQtype());
        resultMsg.add("robotId", qiyukfInfo.getRobotId());
        resultMsg.add("origin", appKey + "." + questionType);

        // 代上自定义crm参数“用户类型”
        List<Map<String, Object>> crmParams = new ArrayList<>();

        // 七鱼在用调用用户信息接口的时候，无法保存name。需要显示的在js传入
        Map<String, Object> nameParam = new HashMap<>();
        nameParam.put("key", "real_name");
        nameParam.put("value", name);
        crmParams.add(nameParam);

        Map<String, Object> crmParam = new HashMap<>();
        crmParam.put("type", "crm_param");
        crmParam.put("key", "usertype");
//        crmParam.put("value", terminal == Terminal.market ? 1 : 0);
        crmParam.put("value", 0);//FIXME 市场用户传1
        crmParams.add(crmParam);

        resultMsg.add("data", JsonUtils.toJson(crmParams));

        LoggerUtils.info("result", reqUserId, appKey, questionType, resultMsg);
        return resultMsg;
    }

    /**
     * 跳转到七鱼客服系统
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        //参数
        String questionType = getRequestString("questionType").trim();
        String imei = getRequestString("imei");
        String appVersion = getRequestString("app_version");
        String reqTerminal = getRequestString("terminal");//pc端不传该参数
        String clientType = StringUtils.isNotBlank(reqTerminal) ? "app":"pc";
        Long reqUserId = getRequestLong("userId");

        //获取user
        User user = getUser(reqUserId);
        String name = "";
        String avatarUrl = "";
        Long userId = null;
        if(user != null){
            userId = user.getId();
            avatarUrl = getUserAvatarImgUrl(user);
            name = user.fetchRealname();
        }
        //端标识
        Terminal terminal =  getTerminalByUser(user, reqTerminal);
        String pageOrigin = String.join("-", clientType, terminal.desc, getRequestStringCleanXss("origin"));

        //获取七鱼客服配置
        QiYuKFConfig qiYuConfig = this.qiYuKFConfig(terminal, questionType, user);

        int robotId = 0;// 机器人id
        //启用机器人设置
        if(ENABLE_ROBOT.contains(terminal)){
            robotId = qiYuConfig.getRobotId();
        }
        // 代上自定义crm参数“用户类型”
        List<Map<String, Object>> crmParams = new ArrayList<>();

        // 七鱼在用调用用户信息接口的时候，无法保存name。需要显示的在js传入
        Map<String, Object> nameParam = new HashMap<>();
        nameParam.put("key", "real_name");
        nameParam.put("value", name);
        crmParams.add(nameParam);

        // 头像信息
        Map<String, Object> avatarParam = new HashMap<>();
        avatarParam.put("key", "avatar");
        avatarParam.put("index", 0);
        avatarParam.put("value", avatarUrl);
        avatarParam.put("href", avatarUrl);
        crmParams.add(avatarParam);

        Map<String, Object> crmParam = new HashMap<>();
        crmParam.put("type", "crm_param");
        crmParam.put("key", "usertype");
        crmParam.put("value", terminal.equals("marketer") ? 1 : 0);//天玑传1
        crmParams.add(crmParam);

        model.addAttribute("destId", qiYuConfig.getCsGroupId());
        model.addAttribute("uid", userId);
        model.addAttribute("data", JsonUtils.toJson(crmParams));
        model.addAttribute("pageOrigin", pageOrigin);
        model.addAttribute("robustOption", robotId > 0 ? 1 : 0);
        model.addAttribute("qtype", qiYuConfig.getQtype());
        model.addAttribute("robotId", robotId);

        LoggerUtils.info("redirectQiyukfIndex", userId, terminal, imei, appVersion, reqUserId, robotId, qiYuConfig.getCsGroupId(),
                qiYuConfig.getQtype(),questionType,pageOrigin,crmParams);
        return "other/qiyukf_jump";
    }

    /**
     * 获取端标识
     *
     * @param user
     * @param reqTerminal
     * @return
     */
    private Terminal getTerminalByUser(User user, String reqTerminal) {
        Terminal terminal = null;
        if(user != null){
            Long userId = user.getId();
            UserType userType = UserType.of(user.getUserType());
            switch (userType){
                case STUDENT:
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
                    terminal = studentDetail.isSeniorStudent() || studentDetail.isJuniorStudent() ? Terminal.juniorstu : Terminal.student;
                    break;
                case PARENT:
                    terminal = Terminal.parent;
                    break;
                case TEACHER:
                    Teacher teacher = teacherLoaderClient.loadTeacher(userId);
                    if(teacher.isJuniorTeacher() || teacher.isSeniorTeacher()){
                        terminal = Terminal.juniortea;
                    }else{
                        terminal = Terminal.teacher;
                    }
                    break;
                default:
                    log.error("QiyukfRedirectController.index.userType.error", userId);
                    break;
            }
        }else{
            if(StringUtils.isNotEmpty(reqTerminal)){
                terminal = Terminal.of(reqTerminal);
            }
        }
        return terminal != null ? terminal : Terminal.student;
    }

    /**
     * 查询用户
     *
     * @return
     */
    private User getUser(Long reqUserId){
        User user = currentUser();
        if (user == null && reqUserId != null){
            user = raikouSystem.loadUser(reqUserId);
        }
        if (user != null) {
            // 如果是姓名为空的家长，则把名字置成孩子姓名 + 称呼
            if (StringUtils.isEmpty(user.fetchRealname()) && user.getUserType() == UserType.PARENT.getType()) {
                List<StudentParentRef> spRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
                StudentParentRef spr = ObjectUtils.get(()->spRefs.get(0));
                if (spr != null) {
                    User stu = raikouSystem.loadUser(spr.getStudentId());
                    user.getProfile().setRealname(stu.fetchRealname() + spr.getCallName());
                }
            }
        }
        return user;
    }


    /**
     * 获取七鱼在线客服配置信息
     *
     * @param terminal
     * @param questionType
     * @param user
     * @return
     */
    private QiYuKFConfig qiYuKFConfig(Terminal terminal, String questionType, User user){
        if(user == null){
            QiYuKFConfig result = QiYuKFConfig.nameOf(terminal.name() + "_" + questionType);
            return result == null ? QiYuKFConfig.student_question_other : result;
        }
        QiYuKFConfig result = null;
        if (user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            Subject subject = teacherDetail.getSubject();
            result = QiYuKFConfig.nameOf(terminal + "_"+questionType + "_"+subject.name().toLowerCase());
            if(result == null){
                result = QiYuKFConfig.nameOf(terminal + "_"+questionType);
            }
        }
        if(result == null){
            result = QiYuKFConfig.nameOf(terminal + "_" + questionType);
        }
        if (result == null) {
            result = QiYuKFConfig.nameOf(terminal + "_question_other");
        }
        if(result==null){
            result = QiYuKFConfig.student_question_other;
        }
        return result;
    }

}
