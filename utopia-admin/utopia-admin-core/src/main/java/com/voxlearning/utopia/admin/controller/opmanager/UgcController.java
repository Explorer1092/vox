/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.UgcCodeType;
import com.voxlearning.utopia.api.constant.UgcQuestionType;
import com.voxlearning.utopia.entity.misc.UgcQuestions;
import com.voxlearning.utopia.entity.misc.UgcRecord;
import com.voxlearning.utopia.entity.misc.UgcRecordCodeRef;
import com.voxlearning.utopia.entity.misc.UgcRecordQuestionsRef;
import com.voxlearning.utopia.service.ugc.client.UgcQuestionServiceClient;
import com.voxlearning.utopia.service.ugc.client.UgcRecordCodeRefServiceClient;
import com.voxlearning.utopia.service.ugc.client.UgcRecordServiceClient;
import com.voxlearning.utopia.service.ugc.client.UgcServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/3/10.
 * 新版UGC收集  CRM管理模块
 */
@Controller
@Slf4j
@RequestMapping(value = "/opmanager/ugc")
public class UgcController extends OpManagerAbstractController {

    @Inject private UgcQuestionServiceClient ugcQuestionServiceClient;
    @Inject private UgcRecordCodeRefServiceClient ugcRecordCodeRefServiceClient;
    @Inject private UgcRecordServiceClient ugcRecordServiceClient;
    @Inject private UgcServiceClient ugcServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 获取所有的UGC活动
        List<UgcRecord> recordList = ugcRecordServiceClient.getUgcRecordService()
                .loadAllUgcRecordsFromDB()
                .getUninterruptibly()
                .stream()
                .sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchCreateTimestamp()))
                .collect(Collectors.toList());
        model.addAttribute("recordList", recordList);
        return "opmanager/ugc/index";
    }

    @RequestMapping(value = "questionindex.vpage", method = RequestMethod.GET)
    public String questionIndex(Model model) {
        // 获取所有题目列表
        List<UgcQuestions> questionsList = ugcQuestionServiceClient.getUgcQuestionService()
                .loadAllUgcQuestionsFromDB()
                .getUninterruptibly()
                .stream()
                .sorted(Comparator.comparingLong(UgcQuestions::getId))
                .collect(Collectors.toList());
        model.addAttribute("questionList", questionsList);
        return "opmanager/ugc/questionindex";
    }

    @RequestMapping(value = "questionref.vpage", method = RequestMethod.GET)
    public String questionRef(Model model) {
        // 获取所有题目列表
        List<UgcQuestions> questionsList = ugcQuestionServiceClient.getUgcQuestionService()
                .loadAllUgcQuestionsFromDB()
                .getUninterruptibly()
                .stream()
                .sorted(Comparator.comparingLong(UgcQuestions::getId))
                .collect(Collectors.toList());
        model.addAttribute("questionList", questionsList);
        Long recordId = getRequestLong("recordId");
        UgcRecord record = ugcServiceClient.getUgcService().loadUgcRecord(recordId).getUninterruptibly();
        model.addAttribute("record", record);
        List<UgcRecordQuestionsRef> refList = ugcServiceClient.getUgcService()
                .findUgcRecordQuestionsRefsByRecordId(recordId)
                .getUninterruptibly();
        List<Long> qids = refList.stream().map(UgcRecordQuestionsRef::getQuestionId).collect(Collectors.toList());
        model.addAttribute("qids", qids);
        return "opmanager/ugc/questionref";
    }

    @RequestMapping(value = "coderef.vpage", method = RequestMethod.GET)
    public String codeRef(Model model) {
        Long recordId = getRequestLong("recordId");
        UgcRecord record = ugcServiceClient.getUgcService().loadUgcRecord(recordId).getUninterruptibly();
        model.addAttribute("record", record);
        List<UgcRecordCodeRef> refList = ugcServiceClient.getUgcService()
                .findUgcRecordCodeRefsByRecordId(recordId)
                .getUninterruptibly();
        model.addAttribute("refList", refList);
        return "opmanager/ugc/coderef";
    }

    @RequestMapping(value = "editquestion.vpage", method = RequestMethod.GET)
    public String editQuestion(Model model) {
        // 编辑题目
        Long questionId = getRequestLong("questionId");
        UgcQuestions question = ugcQuestionServiceClient.getUgcQuestionService()
                .loadUgcQuestion(questionId)
                .getUninterruptibly();
        if (question != null) {
            model.addAttribute("question", question);
        }
        model.addAttribute("types", UgcQuestionType.values());
        return "opmanager/ugc/editquestion";
    }

    @RequestMapping(value = "savequestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveQuestion() {
        Long questionId = getRequestLong("questionId");
        try {
            UgcQuestions question = new UgcQuestions();
            question.setQuestionName(StringUtils.trim(getRequestString("questionName")));
            question.setQuestionType(UgcQuestionType.valueOf(getRequestString("questionType")));
            question.setQuestionOptions(StringUtils.trim(getRequestString("questionOptions")));
            question.setDisabled(getRequestBool("disabled"));
            // 此处表示修改活动信息
            if (questionId != 0L) {
                question.setId(questionId);
                ugcQuestionServiceClient.getUgcQuestionService()
                        .replaceUgcQuestion(question)
                        .awaitUninterruptibly();
                addAdminLog("修改UGC题目信息", questionId, "User: " + getCurrentAdminUser().getRealName());
            } else {
                questionId = ugcQuestionServiceClient.getUgcQuestionService()
                        .insertUgcQuestion(question)
                        .getUninterruptibly()
                        .getId();
                addAdminLog("新增UGC题目信息", questionId, "User: " + getCurrentAdminUser().getRealName());
            }
        } catch (Exception ignored) {
            MapMessage.errorMessage("保存失败！");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "editrecord.vpage", method = RequestMethod.GET)
    public String editRecord(Model model) {
        // 编辑活动
        Long recordId = getRequestLong("recordId");
        UgcRecord record = ugcServiceClient.getUgcService().loadUgcRecord(recordId).getUninterruptibly();
        if (record != null) {
            model.addAttribute("record", record);
        }
        model.addAttribute("types", UgcCodeType.values());
        model.addAttribute("userTypes", UserType.values());
        return "opmanager/ugc/editrecord";
    }

    @RequestMapping(value = "saverecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecord() {
        Long recordId = getRequestLong("recordId");
        Date startDate;
        Date endDate;
        try {
            UgcRecord record = new UgcRecord();
            record.setName(getRequestString("recordName"));
            record.setCodeType(UgcCodeType.valueOf(getRequestString("codeType")));
            record.setUserType(UserType.valueOf(getRequestString("userType")));
            startDate = DateUtils.stringToDate(getRequestString("startDate"));
            endDate = DateUtils.stringToDate(getRequestString("endDate"));
            record.setStartDate(startDate);
            record.setEndDate(endDate);
            record.setAmbassadorOnly(getRequestBool("ambassadorOnly"));
            if (getRequestBool("published")) {
                record.setStatus(1);
            } else {
                record.setStatus(0);
            }
            record.setDisabled(getRequestBool("disabled"));
            // 此处表示修改活动信息
            if (recordId != 0L) {
                record.setId(recordId);
                ugcRecordServiceClient.getUgcRecordService().updateUgcRecord(record).awaitUninterruptibly();
                addAdminLog("修改UGC活动信息", recordId, "User: " + getCurrentAdminUser().getRealName());
            } else {
                record = ugcRecordServiceClient.getUgcRecordService().insertUgcRecord(record).getUninterruptibly();
                recordId = record == null ? null : record.getId();
                addAdminLog("新增UGC活动信息", recordId, "User: " + getCurrentAdminUser().getRealName());
            }
        } catch (Exception ignored) {
            MapMessage.errorMessage("保存失败！");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "saverecordquestionref.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecordQuestionRef() {
        try {
            Long recordId = getRequestLong("recordId");
            String questionIds = getRequestString("questionIds");
            if (recordId == 0L) {
                return MapMessage.errorMessage("参数错误");
            }
            //先删除，再添加
            ugcServiceClient.getUgcService().deleteUgcRecordQuestionsRefsByRecordId(recordId).awaitUninterruptibly();
            String[] questionIdArr = StringUtils.split(questionIds, ",");
            for (String qstr : questionIdArr) {
                UgcRecordQuestionsRef recordQuestionsRef = new UgcRecordQuestionsRef();
                recordQuestionsRef.setQuestionId(SafeConverter.toLong(qstr));
                recordQuestionsRef.setRecordId(recordId);
                ugcServiceClient.getUgcService().persistUgcRecordQuestionsRef(recordQuestionsRef).awaitUninterruptibly();
            }
            addAdminLog("编辑UGC关联题目", recordId, "User: " + getCurrentAdminUser().getRealName());
        } catch (Exception ignored) {
            MapMessage.errorMessage("保存失败！");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "savecoderef.vpage", method = RequestMethod.POST)
    public String saveCodeRef(Model model) {
        Long recordId = getRequestLong("recordId");
        String codes = getRequestString("codes");
        if (StringUtils.isEmpty(codes) || recordId == 0) {
            getAlertMessageManager().addMessageError("参数错误");
            return "opmanager/ugc/coderef";
        }
        String[] codeArr = codes.split("\\r\\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();
        if (codeArr.length > 0) {
            // 删除历史
            ugcRecordCodeRefServiceClient.getUgcRecordCodeRefService()
                    .deleteUgcRecordCodeRefsByRecordId(recordId)
                    .awaitUninterruptibly();
        }
        for (String c : codeArr) {
            if (StringUtils.isEmpty(c)) {
                lstFailed.add(c);
                continue;
            }
            try {
                UgcRecordCodeRef ref = new UgcRecordCodeRef();
                ref.setRecordId(recordId);
                ref.setCode(c);
                ref = ugcRecordCodeRefServiceClient.getUgcRecordCodeRefService()
                        .insertUgcRecordCodeRef(ref)
                        .getUninterruptibly();
                Long id = ref.getId();
                if (id != null && id > 0) {
                    lstSuccess.add(c);
                } else {
                    lstFailed.add(c);
                }
            } catch (Exception ex) {
                lstFailed.add(c);
            }
        }
        UgcRecord record = ugcServiceClient.getUgcService().loadUgcRecord(recordId).getUninterruptibly();
        addAdminLog("编辑UGC关联Code", recordId, "User: " + getCurrentAdminUser().getRealName());
        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        model.addAttribute("record", record);
        return "opmanager/ugc/coderef";
    }

}
