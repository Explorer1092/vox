package com.voxlearning.washington.controller.teacher;

import com.google.common.collect.Maps;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.DPLiveCastHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkBrief;

import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.service.DPLiveCastHomeworkService;
import com.voxlearning.utopia.service.newhomework.consumer.DPLiveCastHomeworkReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.DPLiveCastHomeworkServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/teacher/livecast/homework")
public class TeacherLiveCastHomeworkReportController extends AbstractTeacherController {

    @Inject
    private DPLiveCastHomeworkServiceClient dpLiveCastHomeworkServiceClient;

    @Inject
    private DPLiveCastHomeworkReportLoaderClient dpLiveCastHomeworkReportLoaderClient;

    @Inject
    private DPLiveCastHomeworkService dpLiveCastHomeworkService;

    /**
     * test
     */
    @RequestMapping(value = "test.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage test() {
        MapMessage mapMessage=MapMessage.successMessage();
/*
        MapMessage mapMessage;
        String homeworkId = "201810_5bc0609977748779e3820fde";
        Long studentId = 333939383L;
        Map<String, Object> correctInfoMap = Maps.newHashMap();
        correctInfoMap.put("imgUrl","https://oss-image.17zuoye.com/app/test2018/10/12/20181012171957116958.jpg,https://oss-image.17zuoye.com/app/test2018/10/12/20181012171957116958.jpg");
        correctInfoMap.put("score",10);
        correctInfoMap.put("voice","https://static.17xueba.com/test/pan/audio/2018/10/20181012204453471600_rm3X8mk6J8.mp3");
        correctInfoMap.put("percentage",6);
        ObjectiveConfigType type = ObjectiveConfigType.PHOTO_OBJECTIVE;
        mapMessage = dpLiveCastHomeworkService.newCorrectQuestions(homeworkId, studentId, correctInfoMap, type);
        */

        LiveHomeworkReport liveHomeworkReport = dpLiveCastHomeworkReportLoaderClient.obtainLiveHomeworkReport("201810_5bc0609977748779e3820fde");
        mapMessage.add("liveHomeworkReport",liveHomeworkReport);

//        mapMessage = dpLiveCastHomeworkServiceClient.noteComment(12L, "xxx", Collections.singleton(333916270L), "201707_59772b7777748726f8d4ed61");
//
//        return mapMessage;

        // LiveHomeworkReport liveHomeworkReport = dpLiveCastHomeworkReportLoaderClient.obtainLiveHomeworkReport("201708_5993f7ed8edbc8aa87eba290");
//
//        mapMessage.add("liveHomeworkReport", liveHomeworkReport);
//
//        return mapMessage;

//
////        mapMessage.add("page", dpLiveCastHomeworkReportLoaderClient.personalReadingDetail("201707_596caf16ac74592706d66628",333916270L, "PB_10300000210685-2"));
////        return mapMessage;
//
//        dpLiveCastHomeworkServiceClient.deleteHomework(12976938L, "201708_5985647a8edbc83495eab4dc");
//


//        mapMessage.add("liveHomeworkReport", liveHomeworkReport);
//        return mapMessage;


        //MapMessage mapMessage1 = dpLiveCastHomeworkReportLoaderClient.personalReadingDetail("201803_5aa0fe108edbc86ba2dbf064", 333910440L, "PBP_10300000024439", ObjectiveConfigType.LEVEL_READINGS);

        //return dpLiveCastHomeworkReportLoaderClient.loadLiveCastHomeworkReportDetail("201708_59940adb777487065ad7faf3");


//        return dpLiveCastHomeworkReportLoaderClient.fetchSubjectiveQuestion("201708_598000568edbc88b52340dcd", ObjectiveConfigType.PHOTO_OBJECTIVE, "Q_10200786534654-3");

//        return dpLiveCastHomeworkReportLoaderClient.personalReadingDetail("201707_596caf16ac74592706d66628", 333916270L, "PB_10300000194316-2");

//        return dpLiveCastHomeworkReportLoaderClient.reportDetailsBaseApp("201707_596caf16ac74592706d66628", "10306", "BKC_10300040020140", 333916270L, ObjectiveConfigType.BASIC_APP);


//        return dpLiveCastHomeworkReportLoaderClient.reportDetailsBaseApp("201707_5971c5e1ac74597b27f238e1", "10303", "BKC_10300106707775", 333916270L, ObjectiveConfigType.BASIC_APP);

//        return dpLiveCastHomeworkReportLoaderClient.loadLiveCastHomeworkReportDetail("201707_596caf16ac74592706d66628");

//        MapMessage mapMessage = MapMessage.successMessage();
//        mapMessage.add("LiveHomeworkReport", dpLiveCastHomeworkReportLoaderClient.obtainLiveHomeworkReport("201612_5841b4637774877874e2d8b2"));
//        return mapMessage;

//        return dpLiveCastHomeworkReportServiceClient.personalReadingDetail("201612_5841b4637774877874e2d8b2", 333909237L, "PB_10300000004287");


//        return liveCastHomeworkReportServiceClient.loadLiveCastHomeworkReportDetail("201612_5841b4637774877874e2d8b2", 333909237L);

//        MapMessage mapMessage = MapMessage.successMessage();
//        mapMessage.add("data", liveCastHomeworkReportServiceClient.obtainLiveHomeworkReport(hid));
//        mapMessage.add("d", liveHomeworkBriefs);
        return mapMessage;

    }
}
