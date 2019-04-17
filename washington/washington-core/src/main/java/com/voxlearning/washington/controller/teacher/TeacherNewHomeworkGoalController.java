package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * GOAL的
 * 这套服务目前只针对PC端，逻辑暂时放在controller中便于调试
 *
 * @author xuesong.zhang
 * @since 2016-08-02
 */
@Controller
@RequestMapping("/teacher/new/homework/goal/")
public class TeacherNewHomeworkGoalController extends AbstractTeacherController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String goalIndex(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "clazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClazz() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取默认教材
     */
    @RequestMapping(value = "book.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getBook() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 更换教材
     */
    @RequestMapping(value = "changebook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage changeBook() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "summary.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage summaryInfo() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detailInfo() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 教学目标设置保存
     */
    @RequestMapping(value = "saveteachingobjective.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeachingObjective() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "assignknowledgereport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage assignKnowledgeReport() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 17奖学金抽奖，首次查看学情评估、布置作业获取更多积分 赠送一把钥匙
     */
    @RequestMapping(value = "scholarship.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage processScholarship() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 17奖学金抽奖，获取老师钥匙记录
     */
    @RequestMapping(value = "scholarship/keyrecord.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getScholarshipKeyRecord() {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 17奖学金抽奖，该接口可以人工的给teacherId的老师增加deltaKey个钥匙（抽奖次数）
     */
    @RequestMapping(value = "manualaddchance.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage manualAddChance() {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 17奖学金抽奖，手动mock单元达标率的钥匙数, 东双在测试环境下调用
     */
    @RequestMapping(value = "manualaddratekey.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage manualAddRatekey() {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 知识点报告，根据书本的单元取
     * 这个业务如果扔到homework的Service中，会造成NewHomework依赖雅典娜，考虑考虑
     */
    @RequestMapping(value = "knowledgereport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage knowledgeReport() {
        return MapMessage.errorMessage("接口已下线");
    }

    /**
     * 知识点诊断
     */
    @RequestMapping(value = "knowledgequestionreport.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage knowledgeQuestionReport() {
        return MapMessage.errorMessage("接口已下线");
    }

    /**
     * 一个屎黄色的tip，好丑
     */
    @RequestMapping(value = "knowledgetip.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shitYellowKnowledgeTip() {
        return MapMessage.errorMessage("接口已下线");
    }

    /**
     * 教学目标设置
     */
    @RequestMapping(value = "teachingobjective.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teachingObjective() {
        return MapMessage.errorMessage("接口已下线");
    }
}
