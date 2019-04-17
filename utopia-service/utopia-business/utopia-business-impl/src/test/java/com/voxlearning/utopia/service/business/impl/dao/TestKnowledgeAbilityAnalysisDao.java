package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.KnowledgeAbilityAnalysis;
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
public class TestKnowledgeAbilityAnalysisDao {
    @Inject
    private KnowledgeAbilityAnalysisDao knowledgeAbilityAnalysisDao;

    @Test
    @MockBinder(
            type = KnowledgeAbilityAnalysis.class,
            jsons = {
                    "{'schoolId':10001,'yearmonth':201609,'subject':'ENGLISH'}",
                    "{'schoolId':10001,'yearmonth':201610,'subject':'ENGLISH'}",
                    "{'schoolId':10001,'yearmonth':201701,'subject':'ENGLISH'}",
                    "{'schoolId':10002,'yearmonth':201704,'subject':'ENGLISH'}",
                    "{'schoolId':10002,'yearmonth':201705,'subject':'ENGLISH'}"
            },
            persistence = KnowledgeAbilityAnalysisDao.class
    )
    public void testloadBySchoolIdSubjectDt(){
        List<KnowledgeAbilityAnalysis> knowledgeAbilityAnalysisList = knowledgeAbilityAnalysisDao.loadBySchoolIdSubjectDt(10001L,"ENGLISH",201609L,201701L);
        assertEquals(3,knowledgeAbilityAnalysisList.size());
        knowledgeAbilityAnalysisList = knowledgeAbilityAnalysisDao.loadBySchoolIdSubjectDt(10002L,"ENGLISH",201704L,201705L);
        assertEquals(2,knowledgeAbilityAnalysisList.size());
    }

}
