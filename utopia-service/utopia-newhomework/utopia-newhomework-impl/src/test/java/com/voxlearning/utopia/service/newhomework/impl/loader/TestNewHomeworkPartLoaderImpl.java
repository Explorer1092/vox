package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkStudyMasterDao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.*;

/**
 * @author shiwe.liao
 * @since 2016-8-9
 */
@DropMongoDatabase
public class TestNewHomeworkPartLoaderImpl extends NewHomeworkUnitTestSupport {

    @Inject
    private NewHomeworkStudyMasterDao newHomeworkStudyMasterDao;

    @Test
    public void test() {
        List<NewHomeworkStudyMaster> masterList = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Long id = 1L; id < 11; id++) {
            NewHomeworkStudyMaster master = new NewHomeworkStudyMaster();
            master.setId(id.toString());
            master.setSubject(Subject.ENGLISH);
            List<NewHomeworkStudyMaster.MasterStudent> studentList = new ArrayList<>();
            for (Long i = 1L; i < 3; i++) {
                NewHomeworkStudyMaster.MasterStudent student = new NewHomeworkStudyMaster.MasterStudent();
                student.setUserId(i);
                student.setUserName(i.toString() + i.toString());
                studentList.add(student);
            }
            master.setMasterStudentList(studentList);
            masterList.add(master);
            ids.add(id.toString());
        }
        newHomeworkStudyMasterDao.inserts(masterList);
        List<String> cacheKey = new ArrayList<>();
        ids.forEach(p -> cacheKey.add(NewHomeworkStudyMaster.generateCacheKey(p)));
        HomeworkCache.getHomeworkCacheFlushable().delete(cacheKey.subList(0, 6));
        Map<String, NewHomeworkStudyMaster> studyMasterMap = newHomeworkPartLoader.getNewHomeworkStudyMasterMap(ids);
        Assert.assertEquals(10, studyMasterMap.values().size());
        Map<String, Object> loads = HomeworkCache.getHomeworkCacheFlushable().loads(cacheKey);
        Assert.assertEquals(10, loads.values().size());
    }

}
