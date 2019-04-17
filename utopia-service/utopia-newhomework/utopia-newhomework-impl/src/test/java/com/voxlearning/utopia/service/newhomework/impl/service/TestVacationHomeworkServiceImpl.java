package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.annotation.MockBinders;
import com.voxlearning.utopia.core.helper.ClassifyImageUtils;
import com.voxlearning.utopia.core.helper.classify.images.ClassifyImagesReponseBody;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.athena.SelfStudyRecomLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2016/12/12
 */
@DropMongoDatabase
public class TestVacationHomeworkServiceImpl extends NewHomeworkUnitTestSupport {

    @Inject private VacationHomeworkServiceImpl vacationHomeworkService;
    @Inject private SelfStudyRecomLoaderClient selfStudyRecomLoaderClient;


    @Test
    @MockBinders({
            @MockBinder(
                    type = VacationHomeworkPackage.class,
                    jsons = {
                            "{'id':'584e6ea9b2d0484354a4dc7d','teacherId':127109,'clazzGroupId':11725,'subject':'MATH','bookId':'BK_10200001567973','actionId':'127109_1481535145652'}",
                    },
                    persistence = VacationHomeworkPackageDao.class
            )
    })
    public void testGenerateVacationHomework() {
        String packageId = "584e6ea9b2d0484354a4dc7d";
        Integer weekRank = 1;
        Integer dayRank = 1;
        Long studentId = 333906005L;

        VacationHomework vacationHomework = vacationHomeworkService.generateVacationHomework(packageId, weekRank, dayRank, studentId);
    }

    @Test
    public void test(){
        String param = "{\"studentId\":"+302565+",\"srcDocIds\":[{\"userAnswer\": [[\"0\"]],\"questionId\":\"Q_10211175607013-2\",\"sectionId\":\"000\"},{\"userAnswer\": [[\"1\"]],\"questionId\": \"Q_10201084041787-8\",\"sectionId\":\"000\"},{\"userAnswer\": [[\"5\"]],\"questionId\":\"Q_10211271598435-2\",\"sectionId\":\"000\"}]}";

        String diagnoseCourseResp = selfStudyRecomLoaderClient.getCuotizhenduanLoader().loadZhenduanRecommendation(param);
        System.out.println(diagnoseCourseResp);
    }


    /**
     * 金山云鉴黄测试
     * @throws Exception
     */
    @Test
    public void testimage()throws Exception{
        List<String> imageUrls = Arrays.asList("https://cdn-live-image.17zuoye.cn/training/acf/20181112/deb486635c7d46dfad3a9508f7c771f7");
        ClassifyImagesReponseBody reponseBody = ClassifyImageUtils.checkImage(imageUrls);
        System.out.println("---------------------------------------------------------------------this_is_the_result");
        System.out.println(JSONObject.toJSONString(reponseBody));
    }
}
