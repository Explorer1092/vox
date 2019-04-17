package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.OutsideReadingLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;


@Controller
@RequestMapping("/container/outside/reading")
public class OutsideReadingReportShareController extends AbstractTeacherController {

    @Inject  private OutsideReadingLoaderClient outsideReadingLoaderClient;

    /**
     * 图书阅读报告详情
     */
    @RequestMapping(value = "report/book/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadReportBookDetail() {

        String readingId = getRequestString("readingId");
        if (StringUtils.isBlank(readingId)) {
            return MapMessage.errorMessage("课外阅读任务不存在");
        }

        return outsideReadingLoaderClient.loadReportBookDetail(readingId);
    }
    /**
     * 查询学生主观题答题详情
     */
    @RequestMapping(value = "answer/sharedetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchAnswerShareDetail() {

        String readingId = getRequestString("readingId");
        String processResultId = getRequestString("processResultId");

        return outsideReadingLoaderClient.fetchAnswerShareDetail(readingId, processResultId);
    }
}
