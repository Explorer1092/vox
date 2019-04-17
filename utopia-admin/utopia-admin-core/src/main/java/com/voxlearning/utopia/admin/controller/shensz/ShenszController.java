package com.voxlearning.utopia.admin.controller.shensz;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.service.shensz.ShenszService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/crm/shensz")
public class ShenszController extends AbstractAdminSystemController {

    @Inject
    private ShenszService shenszService;

    @RequestMapping(value = "recoverstudentdata.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recoverStudentData() {
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            return MapMessage.errorMessage("暂不支持测试环境");
        }
        Long userId = getRequestLong("userId");
        String phone = getRequestString("phone");
        String realName = getCurrentAdminUser().getRealName();
        return shenszService.recoverStudentData(userId, phone, realName);
    }

    @RequestMapping(value = "multistudentinfo.vpage", method = RequestMethod.GET)
    public String multiStudentInfo(Model model) {
        Long groupId = getRequestLong("groupId");
        MapMessage mapMessage = shenszService.multiStudentInfo(groupId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", mapMessage.getInfo());
        } else {
            model.addAttribute("results", mapMessage.get("results"));
            model.addAttribute("groupInfo", mapMessage.get("group"));
        }
        model.addAttribute("groupId", groupId);
        return "crm/shensz/multistudentinfo";
    }

    @RequestMapping(value = "mergestudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mergeStudent() {
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            return MapMessage.errorMessage("暂不支持测试环境");
        }
        Long groupId = getRequestLong("groupId");
        String students = getRequestString("students");
        List<Map> studentMap = JsonUtils.fromJsonToList(students, Map.class);
        return shenszService.mergeStudent(groupId, studentMap);
    }

    @RequestMapping(value = "mergegroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mergeGroup() {
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            return MapMessage.errorMessage("暂不支持测试环境");
        }
        Long oid = getRequestLong("oid");
        Long nid = getRequestLong("nid");
        return shenszService.mergeGroup(oid, nid);
    }
}
