package com.voxlearning.washington.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.consumer.IndependentOcrServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/12/25
 */
@Controller
@RequestMapping("/newhomework/independent/ocr/")
public class IndependentOcrController extends AbstractController {

    @Inject private IndependentOcrServiceClient independentOcrServiceClient;

    /**
     * 查询我的练习册列表
     */
    @RequestMapping(value = "ocrworkbook/fetch.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchOcrStudentWorkbook() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId < 1) {
            return MapMessage.errorMessage("studentId不允许为空");
        }

        return independentOcrServiceClient.fetchOcrStudentWorkbook(studentId);
    }

    /**
     * 删除我的练习册
     */
    @RequestMapping(value = "ocrworkbook/remove.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage removeOcrStudentWorkbook(@RequestParam("myWorkbookId") String myWorkbookId) {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = user.isStudent() ? user.getId() : getRequestLong("studentId");
        if (studentId < 1) {
            return MapMessage.errorMessage("studentId不允许为空");
        }

        return independentOcrServiceClient.removeOcrStudentWorkbook(studentId, myWorkbookId);
    }
}
