package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkReportServiceClient;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/container/dubbing")
public class DubbingController extends AbstractTeacherController {

    @Inject
    private NewHomeworkReportServiceClient newHomeworkReportServiceClient;

    @RequestMapping(value = "sharedubbing.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchPictureBookPlusDubbing() {
        String dubbingId = this.getRequestString("dubbingId");
        if (StringUtils.isBlank(dubbingId)) {
            return MapMessage.errorMessage("dubbingId 参数为空");
        }
        MapMessage mapMessage = newHomeworkReportServiceClient.fetchPictureBookPlusDubbing(dubbingId);
        if (mapMessage.isSuccess()) {
            mapMessage.put("imageUrl", getUserAvatarImgUrl(mapMessage.get("imageUrl").toString()));
        }
        return mapMessage;
    }
}
