package com.voxlearning.washington.controller.mobile.parent;


import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 类MobileParentMistakenNotebookController.java的实现描述：期末复习错题本
 *
 * @author zhangbin
 * @since 2016/11/28 11:43
 */
@Deprecated
@Controller
@RequestMapping(value = "/parent/mistakennotebook")
public class MobileParentMistakenNotebookController extends AbstractParentApiController {

    // 错题本首页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "";
    }

    /**
     * 获取每个科目及其错题数目
     */
    @RequestMapping(value = "subjectcount.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage subjectCount() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取该学生该学科下各个教材的各个单元及其错题数目
     */
    @RequestMapping(value = "book/unit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getBookUnitInfo() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取某个教材下的某个单元错题信息
     */
    @RequestMapping(value = "unit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getUnitInfo() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     *  开始消灭错题，保存做题结果
     *
     *  接口移动到MobileFlashLoaderController "/flash/loader/processresult.vpage"
     */
}

