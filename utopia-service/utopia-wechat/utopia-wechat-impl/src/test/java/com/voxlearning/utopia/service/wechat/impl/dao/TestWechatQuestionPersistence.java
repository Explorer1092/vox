/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.wechat.api.constants.SourceType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatQuestionState;
import com.voxlearning.utopia.service.wechat.api.entities.WechatQuestion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xin
 * @since 14-4-24 上午10:39
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WechatQuestion.class)
public class TestWechatQuestionPersistence {

    @Inject private WechatQuestionPersistence wechatQuestionPersistence;

    @Test
    public void testWechatQuestionPersistence() {
        WechatQuestion question = WechatQuestion.newInstance(
                "aks@3=diqffw",
                "this is a wechat question",
                WechatQuestionState.WAITING,
                SourceType.WECHAT
        );
        question.setReply("this is reply");
        question.setReplyer("admin");

        wechatQuestionPersistence.insert(question);
        Long id = question.getId();

        WechatQuestion nq = wechatQuestionPersistence.load(id);

        assertTrue(nq.getOpenId().equals(question.getOpenId()));
        assertTrue(nq.getContent().equals(question.getContent()));
        assertTrue(nq.getReply().equals(question.getReply()));
    }

    @Test
    public void testFindWechatQuestionByCreateTimeOrState() throws Exception {
        WechatQuestion question = new WechatQuestion();
        question.setOpenId("");
        question.setContent("");
        question.setState(1);
        wechatQuestionPersistence.insert(question);
        List<WechatQuestion> questions = wechatQuestionPersistence.findWechatQuestionByCreateTimeOrState(1,
                new Date(System.currentTimeMillis() - 10000),
                new Date(System.currentTimeMillis() + 10000));
        assertEquals(1, questions.size());
    }
}
