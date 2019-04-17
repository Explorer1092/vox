package com.voxlearning.utopia.service.newhomework.impl.loader;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.test.DropMongoDatabase;

import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import javax.inject.Inject;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DropMongoDatabase
public class TestBasicReviewHomeworkCacheLoaderImpl extends NewHomeworkUnitTestSupport {

    @Inject
    private BasicReviewHomeworkCacheLoaderImpl basicReviewHomeworkCacheLoader;

    @Test
    public void testLoadBasicReviewHomeworkCacheMapper() throws Exception {
        BasicReviewHomeworkPackage homeworkPackage = new BasicReviewHomeworkPackage();
        homeworkPackage.setId("BR_5a0bdd81af81a317e4ff0345");
        homeworkPackage.setCreateAt(new Date());
        homeworkPackage.setUpdateAt(new Date());
        homeworkPackage.setClazzGroupId(1L);
        homeworkPackage.setSubject(Subject.MATH);

        List<BasicReviewStage> stages = new LinkedList<>();
        homeworkPackage.setStages(stages);
        BasicReviewStage stage = new BasicReviewStage();
        stages.add(stage);
        stage.setHomeworkId("1");
        stage.setStageId(1);
        stage.setStageName("关卡1");

        basicReviewHomeworkPackageDao.insert(homeworkPackage);
        BasicReviewHomeworkCacheMapper mapper = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper("BR_5a0bdd81af81a317e4ff0345", 15L);
        assertTrue(SafeConverter.toBoolean(mapper.getFinished()));
    }

    @Test
    public void testAddOrModifyBasicReviewHomeworkCacheMapper() throws Exception {
        BasicReviewHomeworkPackage homeworkPackage = new BasicReviewHomeworkPackage();
        homeworkPackage.setId("BR_5a0bdd81af81a317e4ff0345");
        homeworkPackage.setCreateAt(new Date());
        homeworkPackage.setUpdateAt(new Date());
        homeworkPackage.setClazzGroupId(1L);
        homeworkPackage.setSubject(Subject.MATH);

        List<BasicReviewStage> stages = new LinkedList<>();
        homeworkPackage.setStages(stages);
        BasicReviewStage stage = new BasicReviewStage();
        stages.add(stage);
        stage.setHomeworkId("1");
        stage.setStageId(1);
        stage.setStageName("关卡1");

        basicReviewHomeworkPackageDao.insert(homeworkPackage);

        BasicReviewHomeworkCacheMapper mapper = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper("BR_5a0bdd81af81a317e4ff0345", 16L);
        assertEquals(mapper.getHomeworkDetail().size(),0);
        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loads(Collections.singleton("20171120-MATH-1-16"), false);
        BasicReviewHomeworkCacheMapper mapper1 = basicReviewHomeworkCacheLoader.addOrModifyBasicReviewHomeworkCacheMapper(newHomeworkResultMap.get("20171120-MATH-1-16"), "BR_5a0bdd81af81a317e4ff0345", 16L);
        assertTrue(SafeConverter.toBoolean(mapper1.getFinished()));

    }


}
