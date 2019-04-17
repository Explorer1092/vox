package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.afenti.api.AfentiSocialService;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * @author Ruib
 * @since 2016/8/4
 */
@Controller
@RequestMapping("/afenti/api")
public class StudentAfentiSocialController extends StudentAfentiBaseController {

    @ImportService(interfaceClass = AfentiSocialService.class)
    private AfentiSocialService afentiSocialService;

    // 获取邀请首页
    @RequestMapping(value = "invitation/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage invitationIndexMobile() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");

        return afentiSocialService.loadUserInvitationMsg(student, subject);
    }

    // 邀请用户
    @RequestMapping(value = "invitation/inviteclassmate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteNewClassmate() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        Long invitedUserId = SafeConverter.toLong(getRequestString("invitedUserId"));

        return afentiSocialService.inviteNewUser(student, invitedUserId, subject);
    }

    // 加载成就
    @RequestMapping(value = "achievement/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage achievementIndexMobile() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiSocialService.loadUserAchievements(student, subject);
    }

    // 领取成就
    @RequestMapping(value = "achievement/receive.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage achievementReceive() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        Integer level = SafeConverter.toInt(getRequestString("level"), 0);
        String achivementTypeStr = getRequestString("achievementType");
        AchievementType achievementType = AchievementType.of(achivementTypeStr);
        if (level == 0 || achievementType == null)
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        return afentiSocialService.receiveAchievement(student, subject, achievementType, level);
    }

    // 获取当前等级勋章的同学列表
    @RequestMapping(value = "achievement/maxlevelstudents.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage maxlevelstudents() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        AchievementType achievementType = AchievementType.of(getRequestString("achievementType"));
        Integer level = getRequestInt("level");
        if (achievementType == null || level == 0) {
            return MapMessage.errorMessage("参数缺失").setErrorCode(DEFAULT.getCode());
        }
        return afentiSocialService.fetchMaxLevelClassmates(student, subject, achievementType, level);
    }

    // 获取排行
    @RequestMapping(value = "learningrank/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchRankList() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        return afentiSocialService.loadLearningRank(student, subject);
    }

    // 点赞
    @RequestMapping(value = "learningrank/clickliked.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clickLiked() {
        MapMessage mesg = currentAfentiStudentDetailWithSubjectCheck();
        if (!mesg.isSuccess()) return mesg;

        StudentDetail student = (StudentDetail) mesg.get("studentDetail");
        Subject subject = (Subject) mesg.get("subject");
        Long clickUserId = SafeConverter.toLong(getRequestString("clickUserId"), Long.MIN_VALUE);
        AfentiRankType afentiRankType = AfentiRankType.safeParse(getRequestString("afentiRankType"));

        if (clickUserId == Long.MIN_VALUE || afentiRankType == null)
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        return afentiSocialService.clickLiked(student, clickUserId, subject, afentiRankType);

    }

    // 刷新成就
    @RequestMapping(value = "achievement/refersh.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage achievementRefersh() {
        StudentDetail studentDetail = currentStudentDetail();
        Long refreshUserId = getRequestLong("refreshUserId");
        Subject subject = Subject.safeParse(getRequestString("subject"));
        if (studentDetail != null && studentDetail.getId() == 30010 && subject != null && refreshUserId != 0) {
            return afentiSocialService.refreshAchievement(refreshUserId, subject);
        }
        return MapMessage.errorMessage("参数错误");
    }

}
