package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.annotation.MockBinders;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;

/**
 * @author xuesong.zhang
 * @since 2016/12/2
 */
@DropMongoDatabase
public class TestVacationHomeworkCacheLoaderImpl extends NewHomeworkUnitTestSupport {

    @Test
    @MockBinders({
            @MockBinder(
                    type = VacationHomeworkPackage.class,
                    jsons = {
                            "{'id':'p1','teacherId':30009,'clazzGroupId':1,'subject':'MATH','bookId':'bk_1','actionId':'a1'}",
                            "{'id':'p2','teacherId':30009,'clazzGroupId':2,'subject':'MATH','bookId':'bk_1','actionId':'a1'}",
                    },
                    persistence = VacationHomeworkPackageDao.class
            ),
            @MockBinder(
                    type = VacationHomework.class,
                    jsons = {
                            "{'packageId':'p1','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
                            "{'packageId':'p1','weekRank':1,'dayRank':2,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
                            "{'packageId':'p1','weekRank':1,'dayRank':3,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",
                            "{'packageId':'p1','weekRank':1,'dayRank':4,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10001}",

                            "{'packageId':'p2','weekRank':1,'dayRank':1,'subject':'MATH','clazzGroupId':1,'teacherId':30009,'studentId':10002}",
                    },
                    persistence = VacationHomeworkDao.class
            )
    })
    public void testInsertVacationHomeworkCacheMapper() throws Exception {
        String packageId = "p1";
        Long clazzGroupId = 1L;
        Long studentId = 10001L;
        Long teacherId = 30009L;

        VacationHomeworkCacheMapper mapper = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMapper(clazzGroupId, studentId);
        LinkedHashMap<String, VacationHomeworkDetailCacheMapper> detailMap = mapper.getHomeworkDetail();
        Assert.assertEquals(detailMap.size(), 4);

        VacationHomework homework = new VacationHomework();
        homework.setPackageId(packageId);
        homework.setClazzGroupId(clazzGroupId);
        homework.setWeekRank(1);
        homework.setDayRank(5);
        homework.setStudentId(studentId);
        homework.setTeacherId(teacherId);

        vacationHomeworkDao.insert(homework);
        mapper = vacationHomeworkCacheLoader.addOrModifyVacationHomeworkCacheMapper(homework);

        Assert.assertEquals(mapper.getHomeworkDetail().size(), 5);
    }


}
