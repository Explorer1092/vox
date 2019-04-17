package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/10/19.
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/piclisten")
@Slf4j
public class WechatTeacherPicListenController extends AbstractOpenController {


    @RequestMapping(value = "loadbylessonid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadByLessonId(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = conversionService.convert(jsonMap.get("uid"), Long.class);
        Long lessonId = conversionService.convert(jsonMap.get("lessonId"), Long.class);
        if (userId == null || lessonId == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            List<PicListen> dataList = questionLoaderClient.loadPicListenByLessonIds(Collections.singletonList(lessonId)).get(lessonId);
            if (CollectionUtils.isNotEmpty(dataList)) {
                dataList = dataList.stream().sorted((o1, o2) -> o1.getRank().compareTo(o2.getRank())).collect(Collectors.toList());
            }
            openAuthContext.add("dataList", dataList);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            log.error("wechat load piclisten failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("获取点读机资源失败");
        }
        return openAuthContext;
    }
}
