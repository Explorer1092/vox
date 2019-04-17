package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Ruib
 * @since 2016/7/26
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestWrongQuestionLibraryDao {
    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Test
    public void testInsert() throws Exception {
        Date current = new Date();
        String id = WrongQuestionLibrary.generateId(30013L, Subject.ENGLISH, "10320400004-1");
        WrongQuestionLibrary wrongQuestionLibrary = new WrongQuestionLibrary();
        wrongQuestionLibrary.setId(id);
        wrongQuestionLibrary.setState(AfentiState.INCORRECT);
        wrongQuestionLibrary.setCreateAt(current);
        wrongQuestionLibrary.setUpdateAt(current);
        wrongQuestionLibrary.setDisabled(false);
        wrongQuestionLibrary.setSource("homework");
        wrongQuestionLibrary.setUserId(30013L);
        wrongQuestionLibrary.setEid("10320400004");
        wrongQuestionLibrary.setSubject(Subject.ENGLISH);
        wrongQuestionLibraryDao.insert(wrongQuestionLibrary);
        wrongQuestionLibraryDao.insert(wrongQuestionLibrary);
    }
}
