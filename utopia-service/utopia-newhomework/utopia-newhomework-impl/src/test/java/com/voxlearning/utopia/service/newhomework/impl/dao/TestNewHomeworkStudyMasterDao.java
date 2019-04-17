package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-8-9
 */
@DropMongoDatabase
public class TestNewHomeworkStudyMasterDao  extends NewHomeworkUnitTestSupport {

    @Inject
    private NewHomeworkStudyMasterDao  newHomeworkStudyMasterDao;

    @Test
    public void testInsert(){
        List<NewHomeworkStudyMaster> masterList = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for(Long id=1L;id<11;id++){

            NewHomeworkStudyMaster master = new NewHomeworkStudyMaster();
            master.setId(id.toString());
            master.setSubject(Subject.ENGLISH);
            List<NewHomeworkStudyMaster.MasterStudent> studentList = new ArrayList<>();
            for(Long i=1L;i<3;i++){
                NewHomeworkStudyMaster.MasterStudent student= new NewHomeworkStudyMaster.MasterStudent();
                student.setUserId(i);
                student.setUserName(i.toString() + i.toString());
                studentList.add(student);
            }
            master.setMasterStudentList(studentList);
            masterList.add(master);
            newHomeworkStudyMasterDao.calculateCacheDimensions(master, keys);
        }
        newHomeworkStudyMasterDao.inserts(masterList);
        Map<String, Object> loads = HomeworkCache.getHomeworkCacheFlushable().loads(keys);
        Assert.assertEquals(10, loads.values().size());
    }

}
