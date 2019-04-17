package com.voxlearning.washington.controller.wonderland;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

/**
 * @author Ruib
 * @since 2018/4/17
 */
@Controller
@RequestMapping("/")
public class WonderlandActivityTombController extends AbstractController {

    // 自学乐园家长端 -- 首页
    @RequestMapping(value = "parentMobile/fairyland/homepage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage home() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 自学乐园家长端 -- 首页 -- 成长进度
    @RequestMapping(value = "parentMobile/fairyland/progress.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage progress() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 自学乐园家长端 -- 首页 -- 已经开通和未开通的服务列表
    @RequestMapping(value = "parentMobile/fairyland/applist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage appList() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 自学乐园家长端 -- 自学能力排行
    @RequestMapping(value = "parentMobile/fairyland/ability/rank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage abilityRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 成长进度页::成长进度信息
    @RequestMapping(value = "parentMobile/fairyland/growthschedule/schedule.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage growUpSchedule() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 成长进度页::根据科目和年级查询教材列表
    @RequestMapping(value = "parentMobile/fairyland/growthschedule/gradebooklist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchGradeBookList() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 成长进度页::更改选用教材
    @RequestMapping(value = "parentMobile/fairyland/growthschedule/changetrackbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeGradeBookByBookId() {
        return WonderlandResult.ErrorType.OPERATION_ERROR.result();
    }

    // 成长轨迹::查看指定日成长记录列表
    @RequestMapping(value = "parentMobile/fairyland/growthtrack/record/listbyday.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studyRecordForToday() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 成长轨迹::查看指定周成长记录列表
    @RequestMapping(value = "parentMobile/fairyland/growthtrack/record/listbyweek.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studyRecordForTargetWeek() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 成长轨迹::查看详细学习报告
    @RequestMapping(value = "parentMobile/fairyland/growthtrack/record/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studyRecordReport() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 成长规划::购买详情页面
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 获取西游第二季预热首页
    @RequestMapping(value = "wonderland/activity/westtwowarmup.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchWestTwoWarmUpHome() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 老未5折
    @RequestMapping(value = "wonderland/activity/sale.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage sale() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    // 四级宠物售卖页面 - 特殊人群
    @RequestMapping(value = "wonderland/activity/tpproductstag.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchTravelPandaProductsUsingTag() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "studentMobile/fairyland/task/livetasks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage liveTasks() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "studentMobile/fairyland/task/livetasksdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage liveTasksDetail() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "studentMobile/fairyland/task/livetasksdetail9.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage liveTasksDetail9() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "studentMobile/fairyland/task/homeworkcorrecttasks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkCorrectTasks() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "studentMobile/growingworld/task/fctbts.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchCtbTaskStatus() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "studentMobile/fairyland/task/gwtasks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage growingWorldTasks() {
        return MapMessage.successMessage().add("total", 0).add("unfinished", 0);
    }

    @RequestMapping(value = "/studentMobile/classBoss/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/attackclassboss.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage attackClassBoss() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/receivereward.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveReward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/rank/studentclass.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage studentClassRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/rank/classschool.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage classSchoolRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/rank/classprovince.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage classProvinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/rank/result.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rankResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/rank/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rankReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/reward/creditexchangechances.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage creditExchangeChances() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/addanswerchances.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addAnswerChances() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/onehorserace.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage oneHorseRaceInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/like.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addLike() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/likemessage.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage likeMessage() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/studentclassrank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMonsterStudentClassRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/classschoolrank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMonsterClassSchoolRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/studentprovincerank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMonsterStudentProvinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/classprovincerank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMonsterClassProvinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/result.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage result() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/grab.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage grabTheMonsterReward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/themonster/rewardinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchAllPrizeInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/classboss/fetchquestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchQuestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/classboss/processcastleresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processCastleResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/reward/rewardinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rewardInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/weapon/pagedata.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage pageData() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/weapon/sendreceiverecords.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendReceiveRecords() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/classBoss/weapon/sendweapon.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendWeapon() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ctindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/applyticket.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage applyTicket() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/signindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage signIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/sign.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sign() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/giftindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage giftIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/drawgift.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage drawGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentmobile/climbtowerwarmup/addgiftchances.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addGiftChances() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/growingupfund/exchangefund/loadfund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadExchangeInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/growingupfund/exchangefund/exchangecredit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangeCredit() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/growingupfund/exchangefund/exchangeqfund.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exchangeQFund() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/task/micropaymenttasks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage microPaymentTasks() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/task/micropaymentdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage microPaymentDetail() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/task/receivemptawards.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage receiveMicroPaymentTasksAwards() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/task/micropaymenthistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage microPaymentHistory() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/box/summerboxindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage summerBoxIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/box/opensummerbox.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage openSummerBox() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/box/addsummerbox.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addSummerBox() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lzindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/briefindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage briefIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/supplyenergy.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage supplyEnergy() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/pickupfruits.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage pickUpFruits() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/ornamentdebris/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage list() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/ornamentdebris/synthesis.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage synthesis() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/energy/tasks.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage tasks() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/energy/reward.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage reward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/growthbox/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lzbindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/growthbox/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lzblist() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/createDebris.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage createDebris() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/participateAppLevel.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage participateAppLevel() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/updateAllPayProductRewardStatus.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateAllPayProductRewardStatus() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/updateUsingOldProductRewardStatus.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUsingOldProductRewardStatus() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/updateUsingNewProductRewardStatus.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUsingNewProductRewardStatus() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/deleteUsingOldProductRewardStatus.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteUsingOldProductRewardStatus() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/updateUsingOldProductRewardAchieveType.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUsingOldProductRewardAchieveType() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/initdata/removeUserOrnament.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage removeUserOrnament() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/class/rank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage classRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/province/rank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage provinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/classmate/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage classmate() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/classmate/giveenergy.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage giveEnergy() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/classmate/pickfruit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage pickFruit() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/classmate/message.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage messageCenter() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/province/addtestdata.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addtestdata() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/province/testrank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addtestrank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/province/updateprovincerank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateProvinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lzmindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/dreamshow/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dreamShow() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/dreamshow/achievementdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dreamShowAchievementDetail() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/dreamshow/receiveskin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dreamShowReceiveSkin() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/saveornaments.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage saveOrnaments() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/initfairy.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage initFairy() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/dressup.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dressup() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/learningzone/mall/ornamentsuit/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ornamentSuitIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tcindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/usegascard.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage useGasCard() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/homemap.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadMap() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/receivearrivedgift.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage receiveArrivedGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/stations/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage stationsIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/stations/sign.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage stationsSign() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/stations/fire.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage stationFire() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/classbus/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage classBusIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/classbus/receiveleadergift.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveLeaderGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/gasstation/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage gasStation() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/gasstation/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage gasStationReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/rank/studentprovince.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rankForStudent() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/rank/classprovince.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rankForClass() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/rank/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tcrankReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/collectionroom/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage collectionRoomIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/collectionroom/exchange.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage collectionRoomExchange() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/collectionroom/classmate.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage collectionRoomClassmate() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/collectionroom/keepsakerank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage collectionRoomKeepsakeRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/endindex.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage endIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/receivegift.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/firstbuy/home.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage firstBuyHome() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/firstbuy/exchange.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage firstBuyExchange() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/classbus/fishinggift/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fishingGiftIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/classbus/fishinggift/finishing.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage finishing() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/classbus/drawgift/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage drawGiftIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/classbus/drawgift/draw.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage draw() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/exchangeDynasty.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneExchangeDynasty() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/dynasty.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneDynasty() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/dynastyrank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneRankIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/collectionroom.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneCollectionRoom() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/dynastyquestion.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneQuestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/detective.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneDetective() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/detectivequestion.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneDetectiveQuestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprintone/detectivereceive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprintOneDetectiveReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprint2/toolsindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage toolsIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/sprint2/exchange.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage exchange() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/warmup/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage warmUpIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/warmup/sign.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage tcsign() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundchina2/warmup/openbox.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage openBox() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/useflightcard.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage useFlightCard() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/classairship/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage classBalloonIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/classairship/receiveleadergift.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tereceiveLeaderGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/workshop/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage workshopIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/workshop/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage workshopReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/rank/studentprovince.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage terankForStudent() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/rank/classprovince.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage terankForClass() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/rank/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage terankReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/homemap.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teloadMap() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/visa/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage visaIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/firstbuy/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage firstBuyIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/firstbuy/exchage.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tefirstBuyExchange() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/shoppingbox/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shoppingBoxIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/shoppingbox/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receoveShoppingBox() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/receivegift.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tereceiveGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/report.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage reportIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sign/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tesignIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sign/sign.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tesign() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sign/openbox.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teopenBox() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/classairship/drawgift/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tedrawGiftIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/classairship/drawgift/draw.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tedrawGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/classairship/fishinggift/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage tefishingGiftIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/classairship/fishinggift/fishing.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fishingGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint1/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprint1Index() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint1/exchange.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprint1Exchange() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint1/rank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprint1Rank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint1/answerPointQuestion.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage answerPointQuestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint1/answerDetectiveQuestion.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage answerDetectiveQuestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint2/toolsindex.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sprint2Index() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/sprint2/exchange.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage exchangeSprint2() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/travelaroundeurope/receivearrivedgift.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveStationArrivedGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/initareatype.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage initAreaType() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ccindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/battleindex.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage battleIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/battle/record.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage battleRecord() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/battle/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage battleReceive() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/invite.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage invite() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/ftmcri.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchTeamMemberCountRewardInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/tmcr.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage teamMemberCountReward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/rank/student.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage ccstudentClassRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/rank/class.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage ccclassProvinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/checkexregion.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage checkExRegion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/addrankdata.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage addRankDataEx() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/fetchavailableregion.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchAvailableRegion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/shop/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shopIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/shop/buy.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shopBuyGoods() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/matchquestionInfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage matchPkInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/processanswerinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processPkAnswerInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/clazzcompetition/island/receivetodayexcellentreward.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveTodayExcellentReward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/consumecompetition.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage consumeCompetition() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/fetchmatchinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchMatchInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/processcastleresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ciprocessCastleResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/changepet.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changePet() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/useonekey.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage useOneCompetitionKey() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/challengehistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage challengeHistory() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/strengtheningquestions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage strengtheningQuestions() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/strengtheningresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage strengtheningResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/revengeinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage revengeInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/processrevengeresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processRevengeQuestionResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ciindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/rewardinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage cirewardInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/receivetodaygift.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveTodayGift() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/receiverankrewards.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveRankRewards() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/getcompetitionrankinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getCompetitionRankInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/getexamrankinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getExamRankInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/exam/questioninfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage questionInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/exam/takeexam.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage takeExam() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/competition/island/exam/resultinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage resultInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/consumesnowball.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage consumeSnowball() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/addsnowball.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addSnowball() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/rank/class.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchClassRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/rank/school.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchSchoolRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/rank/province.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchProvinceRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/rank/rewardlist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchRankRewardList() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/rank/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveRankReward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/dressup.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage dressUp() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/signindex.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage smsignIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/sign.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage smsign() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/snowman/openbox.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage smopenBox() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/dxyp.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage dxyp() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/dxyr.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage dxyr() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/xxlbp.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage xxlbp() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/xxlbr.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage xxlbr() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/rwp.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage rwp() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/islandpage.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage ip() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/sales.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sales() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/commonsales.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage commonSales() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/rwr.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage rwr() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/mup.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage makeUpPage() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/task/bmur.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage bmur() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/lotto/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage lotofetchIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/lotto/play.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage playLotto() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/lotto/status.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchStatus() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/achievement/loadmedalgradeinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadAchievementGradeInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/achievement/receivereward.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage receivereward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/achievement/loadallmedals.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadAllMedals() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/achievement/loadmedallevels.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadMedalLevels() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/picture/fetchenglishbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchenglishbook() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/picture/fetchmathquestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchmathquestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/picture/fetchencyclopediaquestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchencyclopediaquestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/picture/processcastleresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processcastleresult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/picture/completequestion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage completeQuestion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/wrongquestion/fetchquestions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchQuestionIds() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/growingworld/wrongquestion/processcastleresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage wqprocesscastleresult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/config/resourceversion.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage resourceversion() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/config/resourcelist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage resourcelist() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/config/resourcedata.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage resourcedata() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/addpageview.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addArticlePageViews() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/pageviews.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage articlePageViews() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/rewardhw.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage rewardHomework() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/hwrewarded.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage homeworkRewarded() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/receivetrial.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveTrial() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/gwfeedback.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage gwfeedback() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/getproblemsrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getProblemsRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/getdaysrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getDaysRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/getmedalrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getMedalRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/gethaicurrentweek.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getMsHaiCurrentweek() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/gethaiyesterday.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getMsHaiYesterday() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/applist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage gwappList() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/subjectislanddata.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getSubjectIslandData() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petfollowed.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage petollowed() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petevolve.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage petEvolve() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petevolveindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage petEvolveIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/feedpet.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage feedPet() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petskillupgrade.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage petskillupgrade() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/feedpetindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage feedpetindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage petIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/classmatehomepage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage classmatehomepage() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/personalhomepage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalhomepage() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/classrank.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage gwclassRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/achievedefaultpet.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage achievedefultpet() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/reminder.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage reminder() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/finishcurrentpetpk.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage finishCurrentPetPk() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/savecurrentanswverresult.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage saveCurrentAnswerResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/opennewpetpk.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage openNewPetPK() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petpkproduct.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage petPkProduct() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petpkinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage petPkInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/savequestionanswerforpetinvade.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage saveQuestionAnswerForPetInvade() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/petinvadeinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage petInvadeInfo() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/growingworld/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage gwindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/fetchactivitydata.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchActivityData() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/gumihoreward.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage gumihoReward() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/gumiho.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage gumiho() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/tpproducts.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchTravelPandaProducts() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/fairy.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fairy() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/elvreading.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage eLevelReading() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/wrongquestion/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage ctbindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/wrongquestion/pagelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage questionPageList() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/wrongquestion/processcorrectedresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processCorrectedResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/activity/popups.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchPopups() {
        return MapMessage.successMessage().add("popups", new ArrayList<>());
    }

    @RequestMapping(value = "/wonderland/activity/fetchappactivities.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchAppActivities() {
        return MapMessage.successMessage().add("activities", new ArrayList<>());
    }

    @RequestMapping(value = "/wonderland/order/fasp.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchAfentiSpecificProducts() {
        return MapMessage.successMessage().add("products", new ArrayList<>()).add("name", "")
                .add("desc", "").add("descImg", "");
    }

    @RequestMapping(value = "/wonderland/order/fsapsap.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchSpecificAppPurchaseStatusAndProducts() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/wonderland/order/multipleappproducts.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchMultipleAppProducts() {
        return MapMessage.successMessage().add("results", new ArrayList<>());
    }

    @RequestMapping(value = "/studentMobile/fairyland/backpack/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bpindex() {
        return MapMessage.successMessage().add("goodsList", new ArrayList<>());
    }

    @RequestMapping(value = "/studentMobile/fairyland/backpack/headwear/change.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changePrivilege() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/products.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage productList() {
        return MapMessage.successMessage().add("productTypeConfigs", new ArrayList<>())
                .add("products", new ArrayList<>()).add("fairyType", "FT_1");
    }

    @RequestMapping(value = "student/fairyland/creditmall/exchangeproduct.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage exchangeProduct() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/productdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage productDetail() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/dressup.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage cmdressup() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/updateStatusAll.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateAll() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/getcreditmarketgiftlist.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getCreditMarketGiftList() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/getstudentgiftrecord.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getStudentGiftRecord() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/getclassmategiftrecord.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getClassmateGiftRecord() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "student/fairyland/creditmall/getgiftlottery.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getGiftLottery() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/credit/weeksummary.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage weekSummary() {
        return MapMessage.successMessage().add("creditSum", 0).add("thisWeekCreditSum", 0);
    }

    @RequestMapping(value = "/studentMobile/fairyland/credit/weekrank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage weekRank() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/studentMobile/fairyland/credit/weekcredithistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage currentWeekCreditHistory() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/parentMobile/travelaroundchina2/endindex.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage tcendIndex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/parentMobile/travelaroundchina2/parentsendcredit.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage sendCredit() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/validate.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage validate() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage spindex() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/do.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage doHomework() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/type/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage typeResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/questions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestions() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/questions/answer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestionsAnswer() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }

    @RequestMapping(value = "/student/smallpayment/homework/processresult.vpage")
    @ResponseBody
    public MapMessage processResult() {
        return WonderlandResult.ErrorType.ACTIVITY_FNINISHED.result();
    }
}
