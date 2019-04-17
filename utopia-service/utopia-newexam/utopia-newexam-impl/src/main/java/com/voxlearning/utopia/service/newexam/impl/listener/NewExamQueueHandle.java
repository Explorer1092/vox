package com.voxlearning.utopia.service.newexam.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newexam.api.entity.JournalNewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/3/8
 */
@Named
public class NewExamQueueHandle extends NewExamSpringBean {
    public void handleMessage(String messageText) {
        NewExamProcessResult newExamProcessResult = JsonUtils.fromJson(messageText, NewExamProcessResult.class);
        if (newExamProcessResult == null) {
            logger.warn("Ignore unrecognized notify message: {}", messageText);
            return;
        }
        saveResult(newExamProcessResult);
    }

    private void saveResult(NewExamProcessResult nep) {
        JournalNewExamProcessResult jep = new JournalNewExamProcessResult();
        NewExamProcessResult.ID id = new NewExamProcessResult.ID(nep.getUpdateAt());
        jep.setId(id.toString());
        jep.setOldId(nep.getId());
        jep.setCreateAt(nep.getCreateAt());
        jep.setUpdateAt(nep.getUpdateAt());
        jep.setClazzId(nep.getClazzId());
        jep.setClazzGroupId(nep.getClazzGroupId());
        jep.setNewExamId(nep.getNewExamId());
        jep.setPaperDocId(nep.getPaperDocId());
        jep.setPartId(nep.getPartId());
        jep.setUserId(nep.getUserId());
        jep.setQuestionId(nep.getQuestionId());
        jep.setQuestionDocId(nep.getQuestionDocId());
        jep.setScore(nep.getScore());
        jep.setStandardScore(nep.getStandardScore());
        jep.setGrasp(nep.getGrasp());
        jep.setSubGrasp(nep.getSubGrasp());
        jep.setUserAnswers(nep.getUserAnswers());
        jep.setSubScore(nep.getSubScore());
        jep.setCorrectSubScore(nep.getCorrectSubScore());
        jep.setDurationMilliseconds(nep.getDurationMilliseconds());
        jep.setSubject(nep.getSubject());
        jep.setClientName(nep.getClientName());
        jep.setClientType(nep.getClientType());
        jep.setFiles(nep.getFiles());
        jep.setCorrectScore(nep.getCorrectScore());
        jep.setCorrectAt(nep.getCorrectAt());
        jep.setOralDetails(nep.getOralDetails());
        journalNewExamProcessResultDao.insert(jep);
    }
}
