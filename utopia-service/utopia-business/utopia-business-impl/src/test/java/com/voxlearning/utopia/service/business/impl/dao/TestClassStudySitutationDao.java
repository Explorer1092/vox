package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.ClassStudySitutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author fugui.chang
 * @since 2016/9/28
 */

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestClassStudySitutationDao {
    @Inject
    private ClassStudySitutationDao classStudySitutationDao;

    @Test
    @MockBinder(
            type = ClassStudySitutation.class,
            jsons = {
                    "{'schoolId':10001,'yearmonth':201604,'subject':'ENGLISH'}",
                    "{'schoolId':10001,'yearmonth':201604,'subject':'ENGLISH'}",
                    "{'schoolId':10002,'yearmonth':201604,'subject':'ENGLISH'}"
            },
            persistence = ClassStudySitutationDao.class
    )
    public  void testLoadClassStudySitutationBySchoolIdDtSubject(){
        List<ClassStudySitutation> classStudySitutationList = classStudySitutationDao.loadClassStudySitutationBySchoolIdDtSubject(10001L,201604L,"ENGLISH");
        assertEquals(2,classStudySitutationList.size());
        classStudySitutationList = classStudySitutationDao.loadClassStudySitutationBySchoolIdDtSubject(10002L,201604L,"ENGLISH");
        assertEquals(1,classStudySitutationList.size());
    }

}

