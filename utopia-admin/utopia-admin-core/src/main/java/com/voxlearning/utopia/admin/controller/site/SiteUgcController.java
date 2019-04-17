package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.lang.util.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Summer Yang on 2016/3/10.
 * 新版UGC收集  CRM管理模块
 * 功能移动去运营管理节点 By Wyc 2016-04-28
 */
@Deprecated
@Controller
@Slf4j
@RequestMapping(value = "/site/ugc")
public class SiteUgcController extends SiteAbstractController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "questionindex.vpage", method = RequestMethod.GET)
    public String questionIndex(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "questionref.vpage", method = RequestMethod.GET)
    public String questionRef(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "coderef.vpage", method = RequestMethod.GET)
    public String codeRef(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "editquestion.vpage", method = RequestMethod.GET)
    public String editQuestion(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "savequestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveQuestion() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "editrecord.vpage", method = RequestMethod.GET)
    public String editRecord(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "saverecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecord() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "saverecordquestionref.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecordQuestionRef() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "savecoderef.vpage", method = RequestMethod.POST)
    public String saveCodeRef(Model model) {
        return "opmanager/notice";
    }

}
