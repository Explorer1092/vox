package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;

/**
 * 用户级别工具
 * @author qiuxu
 */
@Controller
@Slf4j
@RequestMapping("/toolkit/userlevel")
@NoArgsConstructor
public class ToolKitUserLevelController extends ToolKitAbstractController {

    @ImportService(interfaceClass = UserLevelService.class)
    private UserLevelService userLevelService;

    /**
     * 完成作业
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentFinishHomework.vpage", method = RequestMethod.POST)
    public String studentFinishHomework(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }


        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String subjectName = requestString("subject");
        if(StringUtils.isEmpty(subjectName)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }
        Subject subject = Subject.of(subjectName);

        String homeworkId = requestString("homeworkId");
        if(StringUtils.isEmpty(homeworkId)){
            getAlertMessageManager().addMessageError("作业id不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentFinishHomework(userId, subject.name(), homeworkId);
        getAlertMessageManager().addMessageSuccess("完成作业操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 补做作业
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentRepairHomework.vpage",method = RequestMethod.POST)
    public String studentRepairHomework(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String subjectName = requestString("subject");
        if(StringUtils.isEmpty(subjectName)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }
        Subject subject = Subject.of(subjectName);

        String homeworkId = requestString("homeworkId");
        if(StringUtils.isEmpty(homeworkId)){
            getAlertMessageManager().addMessageError("作业id不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentRepairHomework(userId, subject.name(), homeworkId);
        getAlertMessageManager().addMessageSuccess("补做作业操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 订正作业
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentCorrectHomework.vpage",method = RequestMethod.POST)
    public String studentCorrectHomework(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String subjectName = requestString("subject");
        if(StringUtils.isEmpty(subjectName)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }
        Subject subject = Subject.of(subjectName);

        String homeworkId = requestString("homeworkId");
        if(StringUtils.isEmpty(homeworkId)){
            getAlertMessageManager().addMessageError("作业id不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentCorrectHomework(userId, subject.name(), homeworkId);
        getAlertMessageManager().addMessageSuccess("订正作业操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 竞技挑战
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentGrowthWorldAthletics.vpage",method = RequestMethod.POST)
    public String studentGrowthWorldAthletics(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        userLevelService.studentGrowthWorldAthletics(userId);
        getAlertMessageManager().addMessageSuccess("完成竞技岛挑战操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 成长世界
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentGrowthWorldSubject.vpage",method = RequestMethod.POST)
    public String studentGrowthWorldSubject(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String subjectName = requestString("subject");
        if(StringUtils.isEmpty(subjectName)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentGrowthWorldSubject(userId, subjectName);
        getAlertMessageManager().addMessageSuccess("完成成长任务操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 黄金任务
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentGoldenMission.vpage",method = RequestMethod.POST)
    public String studentGoldenMission(){

        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String subjectName = requestString("subject");
        if(StringUtils.isEmpty(subjectName)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentGoldenMission(userId, subjectName);
        getAlertMessageManager().addMessageSuccess("完成黄金任务操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 白金任务
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentPlatinumMission.vpage",method = RequestMethod.POST)
    public String studentPlatinumMission(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String subjectName = requestString("subject");
        if(StringUtils.isEmpty(subjectName)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentPlatinumMission(userId, subjectName);
        getAlertMessageManager().addMessageSuccess("完成白金任务操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 打败boss
     * @return 返回模板地址
     */
    @RequestMapping(value = "/studentFightBoss.vpage",method = RequestMethod.POST)
    public String studentFightBoss(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String boss = requestString("boss");
        if(StringUtils.isEmpty(boss)){
            getAlertMessageManager().addMessageError("学科不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.studentFightBoss(userId, boss);
        getAlertMessageManager().addMessageSuccess("打败boss操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 家长登陆
     * @return
     */
    @RequestMapping(value = "/parentLogin.vpage", method = RequestMethod.POST)
    public String parentLogin(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("用户不为空，大于0；");
            return "toolkit/toolkit";
        }

        userLevelService.parentLogin(userId);
        getAlertMessageManager().addMessageSuccess("家长登陆操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 家长查看作业报告
     * @return
     */
    @RequestMapping(value = "/parentViewHomeworkReport.vpage", method = RequestMethod.POST)
    public String parentViewHomeworkReport(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long parentId = requestLong("parentId");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }
        String homeworkId = requestString("homeworkId");
        if(StringUtils.isEmpty(homeworkId)){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }

        userLevelService.parentViewHomeworkReport(parentId, homeworkId);

        getAlertMessageManager().addMessageSuccess("家长查看作业报告操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 家长查看自学报告
     * @return
     */
    @RequestMapping(value = "/parentViewSelfStudyReport.vpage", method = RequestMethod.POST)
    public String parentViewSelfStudyReport(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }
        Long parentId = requestLong("parentId");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }
        userLevelService.parentViewSelfStudyReport(parentId);
        getAlertMessageManager().addMessageSuccess("家长查看自学报告操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 家长送花给老师
     * @return
     */
    @RequestMapping(value = "/parentSendFlowerToTeacher.vpage", method = RequestMethod.POST)
    public String parentSendFlowerToTeacher(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long parentId = requestLong("parentId");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }

        Long teacherId = requestLong("teacherId");
        if(teacherId == null || teacherId <= 0){
            getAlertMessageManager().addMessageError("老师id不为空，大于0；");
            return "toolkit/toolkit";
        }
        userLevelService.parentSendFlowerToTeacher(parentId, teacherId);
        getAlertMessageManager().addMessageSuccess("家长给老师送花操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 家长发放奖励
     * @return
     */
    @RequestMapping(value = "/parentSendReward.vpage", method = RequestMethod.POST)
    public String parentSendReward(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long parentId = requestLong("parentId");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }

        Long studentId = requestLong("studentId");
        if(studentId == null || studentId <= 0){
            getAlertMessageManager().addMessageError("学生id不为空，大于0；");
            return "toolkit/toolkit";
        }

        userLevelService.parentSendReward(parentId, studentId);
        getAlertMessageManager().addMessageSuccess("家长发放奖励操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 补全家长信息
     * @return
     */
    @RequestMapping(value = "/supplementParentInfo.vpage", method = RequestMethod.POST)
    public String supplementParentInfo() {
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }
        Long parentId = requestLong("parentId");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }
        userLevelService.supplementParentInfo(parentId);
        getAlertMessageManager().addMessageSuccess("家长信息首次补全操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 开通活动
     * @return
     */
    @RequestMapping(value = "/parentPayment.vpage", method = RequestMethod.POST)
    public String parentPayment(){
        if(RuntimeModeLoader.getInstance().isProduction()){
            getAlertMessageManager().addMessageError("生产环境不允许使用");
            return "toolkit/toolkit";
        }

        Long parentId = requestLong("parentId");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("家长id不为空，大于0；");
            return "toolkit/toolkit";
        }

        Integer integer = getRequestInt("amount");
        if(parentId == null || parentId <= 0){
            getAlertMessageManager().addMessageError("支付总额不为空，大于0；");
            return "toolkit/toolkit";
        }
        BigDecimal amount = new BigDecimal(integer);

        String orderId= getRequestString("orderId");
        if(StringUtils.isEmpty(orderId)){
            getAlertMessageManager().addMessageError("订单id不为空；");
            return "toolkit/toolkit";
        }

        userLevelService.parentPayment(parentId, amount, orderId);
        getAlertMessageManager().addMessageSuccess("家长开通产品操作成功！");
        return "toolkit/toolkit";
    }

    /**
     * 更新活跃值缓存
     * @return
     */
    @RequestMapping(value = "/clearActivationCache.vpage", method = RequestMethod.POST)
    public String clearActivationCache(){
        Long userId = requestLong("userId");
        if(userId == null || userId <= 0){
            getAlertMessageManager().addMessageError("用户不为空，大于0；");
            return "toolkit/toolkit";
        }

        userLevelService.clearUserActivationCache(userId);
        getAlertMessageManager().addMessageSuccess("清理活跃值缓存操作成功！");
        return "toolkit/toolkit";
    }
}
