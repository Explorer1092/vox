package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.userlevel.api.UserLevelLoader;
import com.voxlearning.utopia.service.userlevel.api.constant.UserActivationActionEnum;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationHomeLevel;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationLog;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 3/6/18
 */
@Controller
@RequestMapping(value = "/crm/userlevel")
public class CrmUserLevelController extends CrmAbstractController {
    @ImportService(interfaceClass = UserLevelLoader.class)
    private UserLevelLoader userLevelLoader;

    @RequestMapping(value = "/studentuserlevel.vpage", method = RequestMethod.GET)
    public String studentUserLevel(Model model) {
        Long studentId = getRequestLong("userId");
        User student = userLoaderClient.loadUser(studentId);
        model.addAttribute("student", student);

        UserActivationLevel studentLevel = userLevelLoader.getStudentLevel(studentId);
        model.addAttribute("studentLevel", studentLevel);

        UserActivationHomeLevel userHomeLevel = userLevelLoader.getUserHomeLevel(studentId);
        model.addAttribute("homeLevel", userHomeLevel);

        List<Map<String, Object>> parentLevels = new ArrayList<>();
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        for (StudentParentRef ref : studentParentRefs) {
            Map<String, Object> level = new HashMap<>();
            UserActivationLevel parentLevel = userLevelLoader.getParentLevel(ref.getParentId());
            level.put("level", parentLevel);
            level.put("callName", ref.getCallName());
            level.put("parentId", ref.getParentId());
            parentLevels.add(level);
        }
        model.addAttribute("parentLevels", parentLevels);

        List<Map<String, Object>> loginfos = new ArrayList<>();
        List<UserActivationLog> logs = userLevelLoader.getUserActivationLogIn7Days(studentId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (UserActivationLog log : logs) {
            Map<String, Object> info = new HashMap<>();
            info.put("date", LocalDateTime.ofInstant(log.getCreateDatetime().toInstant(), ZoneId.systemDefault()).format(formatter));
            UserActivationActionEnum act = UserActivationActionEnum.of(log.getAction());
            info.put("action", null == act ? log.getAction() : act.getTitle());
            info.put("value", log.getValue());
            if (MapUtils.isNotEmpty(log.getExt())) {
                info.put("ext", JsonUtils.toJson(log.getExt()));
            }
            loginfos.add(info);
        }
        model.addAttribute("logs", loginfos);

        return "/crm/userlevel/studentuserlevel";
    }

    @RequestMapping(value = "/parentuserlevel.vpage", method = RequestMethod.GET)
    public String parentUserLevel(Model model) {
        Long parentId = getRequestLong("userId");
        User parent = userLoaderClient.loadUser(parentId);
        model.addAttribute("parent", parent);

        UserActivationLevel parentLevel = userLevelLoader.getParentLevel(parentId);
        model.addAttribute("parentLevel", parentLevel);

        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        List<Map<String, Object>> studentLevels = new ArrayList<>();
        for (StudentParentRef ref : studentParentRefs) {
            Map<String, Object> info = new HashMap<>();
            info.put("callName", ref.getCallName());
            User child = userLoaderClient.loadUser(ref.getStudentId());
            info.put("child", child);
            UserActivationLevel studentLevel = userLevelLoader.getStudentLevel(ref.getStudentId());
            info.put("studentLevel", studentLevel);
            UserActivationHomeLevel userHomeLevel = userLevelLoader.getUserHomeLevel(ref.getStudentId());
            info.put("homeLevel", userHomeLevel);
            studentLevels.add(info);
        }
        model.addAttribute("studentInfos", studentLevels);

        List<UserActivationLog> logs = userLevelLoader.getUserActivationLogIn7Days(parentId);
        List<Map<String, Object>> infos = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (UserActivationLog log : logs) {
            Map<String, Object> info = new HashMap<>();
            UserActivationActionEnum act = UserActivationActionEnum.of(log.getAction());
            info.put("action", null == act ? log.getAction() : act.getTitle());
            info.put("value", log.getValue());
            info.put("date", LocalDateTime.ofInstant(log.getCreateDatetime().toInstant(), ZoneId.systemDefault()).format(formatter));
            if (MapUtils.isNotEmpty(log.getExt())) {
                info.put("ext", JsonUtils.toJson(log.getExt()));
            }
            infos.add(info);
        }
        model.addAttribute("logs", infos);

        return "/crm/userlevel/parentuserlevel";
    }
}
