package com.voxlearning.washington.controller.bonus;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamConstant;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.QuestionDataAnswer;
import com.voxlearning.utopia.service.newhomework.consumer.AbilityExamServiceLoader;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-2
 */
@Controller
@RequestMapping("/bonus/ability")
public class AbilityExamController extends AbstractMobileController {

    @Inject private AbilityExamServiceLoader abilityExamServiceLoader;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public MapMessage index() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode("900");
        }
        return abilityExamServiceLoader.getHydraRemoteReference().index(currentUserId());
    }


    @RequestMapping(value = "do.vpage", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public MapMessage doData() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode("900");
        }

        MapMessage mapMessage = abilityExamServiceLoader.getHydraRemoteReference().doData(currentUserId());
        if (mapMessage.isSuccess()) {
            Map<String, Object> vars = (Map<String, Object>) mapMessage.get("data");
            vars.put("ipAddress", HttpRequestContextUtils.getWebAppBaseUrl());
            vars.put("imgDomain", getCdnBaseUrlStaticSharedWithSep());

            String flashVars = JsonUtils.toJson(vars);
            Map<String, Object> data = new HashMap<>();
            data.put("flashVars", flashVars);
            return MapMessage.successMessage().add("data", data);
        } else {
            return mapMessage;
        }
    }

    @RequestMapping(value = "question.vpage", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public MapMessage question() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode("900");
        }
        Map<String, Object> result = abilityExamServiceLoader.getHydraRemoteReference().loadQuestion(currentUserId());
        return MapMessage.successMessage().add("result", result);
    }

    @RequestMapping(value = "answer.vpage", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public MapMessage questionAnswer() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode("900");
        }
        Map<String, Object> result = abilityExamServiceLoader.getHydraRemoteReference().loadQuestionAnswer(currentUserId());
        return MapMessage.successMessage().add("result", result);
    }

    @RequestMapping(value = "processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public MapMessage processResult() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode("900");
        }

        AbilityExamAnswerContext context = new AbilityExamAnswerContext();
        context.setUserId(currentUserId());
        context.setId(String.valueOf(currentUserId()));
        context.setLearningType(AbilityExamConstant.ABILITY_EXAM_STUDY_TYPE);

        String json = getRequestParameter("data", "");

        QuestionDataAnswer questionDataAnswer = JsonUtils.fromJson(json, QuestionDataAnswer.class);
        if (questionDataAnswer == null || questionDataAnswer.checkValid()) {
            return MapMessage.errorMessage("提交结果数据异常");
        }

        context.setAnswer(questionDataAnswer);
        context = abilityExamServiceLoader.getHydraRemoteReference().postQuestionAnswer(context);
        if (context.isSuccessful()) {
            return MapMessage.successMessage().add("result", context.getResultMap());
        } else {
            return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
        }
    }

}
