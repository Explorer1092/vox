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

package com.voxlearning.washington.controller.flash;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/flash")
public class FlashHomeworkGameController extends AbstractController {

    protected Map mentalArithmeticData(PracticeType mathPractice, Long pointId, Integer amount) {
        Map<String, String> param = JsonUtils.fromJsonToMapStringString(getRequest().getParameter("param"));
        String dataType = param.get("dataType");
        Map<String, Object> data = new LinkedHashMap<>(flashGameServiceClient.loadMentalArithmeticData(pointId, amount, mathPractice, dataType));
        data.put("param", param);
        return data;
    }

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //       FOR ENGLISH OBTAIN DATA                             //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    @RequestMapping(value = "{flashGameName}/obtain-ENGLISH-{lessonId}.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String obtainEnglishData(@PathVariable("flashGameName") String flashGameName, @PathVariable("lessonId") Long lessonId, Model model) {

        PracticeType englishPractice = practiceLoaderClient.loadNamedPractice(flashGameName);

        if (englishPractice == null) {
            throw new RuntimeException("no flash game data template for " + flashGameName);
        }
        if (englishPractice.getDataType().equals(Constants.GameDataTemplate_VocabularyAndParaphraseData)) {
            vocabularyAndParaphraseData(englishPractice, lessonId, model);
            return "flash/study/vocabularySpellData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_GrammarData)) {
            grammarData(englishPractice, lessonId, model);
            return "flash/study/grammarData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_VocabularySpeakData)) {
            vocabularySpeakData(englishPractice, lessonId, model);
            return "flash/study/vocabularySpeakData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_VocabularySpellData)) {
            vocabularySpeakData(englishPractice, lessonId, model);
            return "flash/study/vocabularySpellData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_VocabularyListenData)) {
            vocabularyListenData(englishPractice, lessonId, model);
            return "flash/study/vocabularyListenData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_VocabularyAndPictureData)) {
            vocabularyAndPictureData(englishPractice, lessonId, model);
            return "flash/study/vocabularySpellData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_ListenAndSpeakData)) {
            listenAndSpeakData(englishPractice, lessonId, model);
            return "flash/study/listenAndSpeakData";
        } else if (englishPractice.getDataType().equals(Constants.GameDataTemplate_MultiuserFollowReadGame)) {
            listenAndSpeakData(englishPractice, lessonId, model);
            model.addAttribute("startTime", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
            return "flash/study/multiuserFollowReadGame";
        } else {
            throw new RuntimeException("unknown flash game data template " + englishPractice.getDataType() + " for " + flashGameName);
        }
    }


    protected void vocabularyAndPictureData(PracticeType englishPractice, Long lessonId, Model model) {
        Map<String, Object> data = flashGameServiceClient.loadVocabularyAndPictureData(lessonId, englishPractice);
        model.addAllAttributes(data);
    }


    protected void listenAndSpeakData(PracticeType englishPractice, Long lessonId, Model model) {
        Map<String, Object> data = flashGameServiceClient.loadListenAndSpeakData(lessonId, englishPractice);
        model.addAllAttributes(data);
    }

    protected void vocabularyListenData(PracticeType englishPractice, Long lessonId, Model model) {
        Map<String, Object> data = flashGameServiceClient.loadVocabularyListenData(lessonId, englishPractice);
        model.addAllAttributes(data);
    }

    protected void vocabularySpeakData(PracticeType englishPractice, Long lessonId, Model model) {
        Map<String, Object> data = flashGameServiceClient.loadVocabularySpeakData(lessonId, englishPractice);
        model.addAllAttributes(data);
    }

    protected void grammarData(PracticeType englishPractice, Long lessonId, Model model) {
        Map<String, Object> data = flashGameServiceClient.loadGrammarData(lessonId, englishPractice);
        model.addAllAttributes(data);
    }

    public void vocabularyAndParaphraseData(PracticeType englishPractice, Long lessonId, Model model) {
        Map<String, Object> data = flashGameServiceClient.loadVocabularyAndParaphraseData(lessonId, englishPractice);
        model.addAllAttributes(data);
    }

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //        FOR ALL PROCESS DATA                               //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    @RequestMapping(value = "{flashGameName}/process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String process(@PathVariable("flashGameName") String flashGameName) {
        return JsonUtils.toJson(MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
    }

}
