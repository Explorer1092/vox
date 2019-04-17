package com.voxlearning.utopia.admin.controller.equator.sapling;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.configuration.api.entity.activity.SaplingSeedConfigInfo;
import com.voxlearning.equator.service.configuration.client.SaplingConfigInfoClient;
import com.voxlearning.equator.service.sapling.api.client.SaplingLoaderClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * 青苗活动专用
 *
 * @author lei.liu
 * @since 18-11-24
 */
@Controller
@RequestMapping(value = "equator/sapling/activity")
public class SaplingActivityController extends AbstractEquatorController {

    @Inject private SaplingLoaderClient saplingLoaderClient;
    @Inject private SaplingConfigInfoClient saplingConfigInfoClient;

    /**
     * 获取用户参与情况
     */
    @RequestMapping(value = "history.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getStudentPresentInfo(Model model) {
        String responseString = "equator/sapling/activity/index";

        SaplingSeedConfigInfo seedConfigInfo = saplingConfigInfoClient.getCurrentSaplingSeedConfigInfo();
        if (seedConfigInfo == null) {
            getAlertMessageManager().addMessageError("种子配置信息不存在。");
            return responseString;
        }

        model.addAttribute("bgnDate", DateUtils.stringToDate(seedConfigInfo.getBgn()));
        model.addAttribute("endDate", DateUtils.stringToDate(seedConfigInfo.getEnd()));

        long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return responseString;
        }
        model.addAttribute("studentId", studentId);
        Student student = studentLoaderClient.loadStudent(studentId);
        model.addAttribute("studentName", student.fetchRealname());

        MapMessage mapMessage = saplingLoaderClient.getRemoteReference().getUserPresentInfo(studentId);
        if (mapMessage.isSuccess()) {
            model.addAllAttributes(mapMessage);
        }
        return responseString;
    }

    /**
     * 获取用户参与情况
     */
    @RequestMapping(value = "present.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getPresentSendProcess(Model model) {
        String responseString = "equator/sapling/activity/present";
        MapMessage mapMessage = saplingLoaderClient.getRemoteReference().getPresentSendInfo();
        // result.add("presentProcess", saplingCacheSystem.getPresentSendingFlagCacheManager().getSendingInfo());
        // result.add("presentPool", presentPoolDao.getPresentPool());
        // result.add("presentList", presentSendProcessShardDao.query());
        if (mapMessage.isSuccess()) {
            model.addAllAttributes(mapMessage);
        }
        return responseString;
    }
}
