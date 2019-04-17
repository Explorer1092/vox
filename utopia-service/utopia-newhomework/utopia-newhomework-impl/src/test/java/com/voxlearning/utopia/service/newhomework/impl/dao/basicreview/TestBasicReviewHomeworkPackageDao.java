package com.voxlearning.utopia.service.newhomework.impl.dao.basicreview;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DropMongoDatabase
public class TestBasicReviewHomeworkPackageDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testInsertBasicReviewHomeworkPackage() {
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
        BasicReviewHomeworkPackage homeworkPackage1 = basicReviewHomeworkPackageDao.load("BR_5a0bdd81af81a317e4ff0345");
        assertEquals(homeworkPackage.getId(), homeworkPackage1.getId());
    }

    @Test
    public void testLoadBasicReviewHomeworkPackageByClazzGroupIds() {
        BasicReviewHomeworkPackage homeworkPackage = new BasicReviewHomeworkPackage();
        homeworkPackage.setId("BR_5a0bdd81af81a317e4ff0345");
        homeworkPackage.setCreateAt(new Date());
        homeworkPackage.setUpdateAt(new Date());
        homeworkPackage.setClazzGroupId(1L);
        homeworkPackage.setSubject(Subject.MATH);

        List<BasicReviewStage> stages = new LinkedList<>();
        homeworkPackage.setStages(stages);
        homeworkPackage.setDisabled(false);
        BasicReviewStage stage = new BasicReviewStage();
        stages.add(stage);
        stage.setHomeworkId("1");
        stage.setStageId(1);
        stage.setStageName("关卡1");
        basicReviewHomeworkPackageDao.insert(homeworkPackage);
        Map<Long, List<BasicReviewHomeworkPackage>> map = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(Collections.singleton(1L));
        List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages = map.get(1L);
        assertEquals(basicReviewHomeworkPackages.size(), 1);
        assertEquals(basicReviewHomeworkPackages.get(0).getId(), "BR_5a0bdd81af81a317e4ff0345");
    }

    @Test
    public void testUpdateDisableTrue() {
        BasicReviewHomeworkPackage homeworkPackage = new BasicReviewHomeworkPackage();
        homeworkPackage.setId("BR_5a0bdd81af81a317e4ff0345");
        homeworkPackage.setCreateAt(new Date());
        homeworkPackage.setUpdateAt(new Date());
        homeworkPackage.setClazzGroupId(1L);
        homeworkPackage.setSubject(Subject.MATH);

        List<BasicReviewStage> stages = new LinkedList<>();
        homeworkPackage.setStages(stages);
        homeworkPackage.setDisabled(false);
        BasicReviewStage stage = new BasicReviewStage();
        stages.add(stage);
        stage.setHomeworkId("1");
        stage.setStageId(1);
        stage.setStageName("关卡1");
        basicReviewHomeworkPackageDao.insert(homeworkPackage);
        basicReviewHomeworkPackageDao.updateDisableTrue("BR_5a0bdd81af81a317e4ff0345");
        BasicReviewHomeworkPackage h = basicReviewHomeworkPackageDao.load("BR_5a0bdd81af81a317e4ff0345");
        assertTrue(h.getDisabled());

    }

}
