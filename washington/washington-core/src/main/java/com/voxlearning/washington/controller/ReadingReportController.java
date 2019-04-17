package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.ReadingReportLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/container/reading")
public class ReadingReportController extends AbstractTeacherController {
    @Inject private ReadingReportLoaderClient readingReportLoaderClient;

    @RequestMapping(value = "/report/recommend.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchRecommend() {
        String hid = getRequestString("hid");
        String pictureId = this.getRequestString("pictureId");
        if (StringUtils.isAnyBlank(hid, pictureId)) {
            return MapMessage.errorMessage("参数缺失，hid&&pictureId");
        }
        return readingReportLoaderClient.fetchRecommend(hid, ObjectiveConfigType.LEVEL_READINGS, pictureId);
    }

    @RequestMapping(value = "/report/fetchuserinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchUserInfo() {
        Long gid = getRequestLong("gid");
        return readingReportLoaderClient.fetchUserInfo(gid);
    }


    @RequestMapping(value = "/report/fetchpicturewordcnt.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchPictureWordCnt() {
        String hidStr = getRequestString("hids");
        String[] split = StringUtils.split(hidStr, ",");
        List<String> hids = new LinkedList<>();
        for (String s : split) {
            hids.add(s);
        }
        return readingReportLoaderClient.fetchPictureWordCnt(hids);
    }
}
