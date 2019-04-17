package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.reward.constant.DuibaCoupon;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.util.DuibaTool;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/4/25
 */
@Controller
@RequestMapping("/v1/reward/coupon")
public class RewardCouponController extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private RewardServiceClient rewardServiceClient;
    @Inject
    protected EmailServiceClient emailServiceClient;

    @RequestMapping(value = "duiba/reduce.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage createOrder() {
        String appkey = getRequestString("appKey");
        if (StringUtils.isBlank(appkey)) {
            return new MapMessage().add("status", "fail").add("credits", 300).add("errorMessage", "签名参数为空");
        }
        DuibaTool.DuibaApp app = getCurrentApp(appkey);
        boolean sign = DuibaTool.signVerify(getRequest(), app);
        if (!sign) {
            return new MapMessage().add("status", "fail").add("credits", 300).add("errorMessage", "签名失败");
        }
        String uid = getRequestString("uid");
        Long credits = getRequestLong("credits");
        String name = getRequestString("description");
        String orderNum = getRequestString("orderNum");
        String type = getRequestString("type");
        String itemCode = getRequestString("itemCode");
        int acturlPrice = getRequestInt("actualPrice");
        Long userId = SafeConverter.toLong(uid);
        if (StringUtils.isAnyBlank(uid, name, orderNum, type) || credits == null || credits <= 0L || userId <= 0L) {
            return new MapMessage().add("status", "fail").add("credits", 300).add("errorMessage", "参数错误");
        }
        DuibaCoupon duibaCoupon = new DuibaCoupon();
        duibaCoupon.setName(name);
        duibaCoupon.setActualPrice(acturlPrice);
        duibaCoupon.setCredits(credits);
        duibaCoupon.setOrderNum(orderNum);
        duibaCoupon.setType(type);
        duibaCoupon.setDuibaAppId(app.getAppId());
        RewardProductDetail productDetail = null;
        long userCredits = 0;
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return new MapMessage().add("status", "fail").add("credits", 300).add("errorMessage", "用户不存在");
        }
        switch (user.fetchUserType()) {
            case STUDENT:
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                if ((RuntimeMode.le(Mode.TEST) && app != DuibaTool.DuibaApp.TEST_XUEDOU) ||
                        (RuntimeMode.gt(Mode.TEST) && app != DuibaTool.DuibaApp.ONLINE_XUEDOU)) {
                    sendNofityEmail(name);
                    return new MapMessage().add("status", "fail").add("credits", userCredits).add("errorMessage", "奖品不存在");
                }
                RewardProduct rewardProduct = rewardLoaderClient.loadAllStudentProducts().stream()
                        .filter(e -> Boolean.TRUE.equals(e.getOnlined()))
                        .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                        .filter(e -> (studentDetail.isPrimaryStudent() && Boolean.TRUE.equals(e.getPrimarySchoolVisible())) ||
                                (studentDetail.isJuniorStudent() &&  Boolean.TRUE.equals(e.getJuniorSchoolVisible())))
                        .filter(e -> (StringUtils.isNotBlank(itemCode) && itemCode.trim().equals(e.getRemarks())) ||
                                (StringUtils.isBlank(itemCode) && name.equals(e.getProductName())))
                        .findFirst().orElse(null);
                if (rewardProduct == null) {
                    sendNofityEmail(name);
                    return new MapMessage().add("status", "fail").add("credits", userCredits).add("errorMessage", "奖品不存在");
                }
                productDetail = rewardLoaderClient.getRewardProductDetailGenerator().generateStudentRewardProductDetail(rewardProduct, studentDetail);
                userCredits = studentDetail.getUserIntegral().getUsable();
                user = studentDetail;
                break;
            case TEACHER:
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userId);
                if ((teacherDetail.isPrimarySchool() && ((RuntimeMode.le(Mode.TEST) && app != DuibaTool.DuibaApp.TEST_YUANDINGDOU) || (RuntimeMode.gt(Mode.TEST) && app != DuibaTool.DuibaApp.ONLINE_YUANDINGDOU))) ||
                        (teacherDetail.isJuniorTeacher() && ((RuntimeMode.le(Mode.TEST) && app != DuibaTool.DuibaApp.TEST_XUEDOU) || (RuntimeMode.gt(Mode.TEST) && app != DuibaTool.DuibaApp.ONLINE_XUEDOU)))) {
                    sendNofityEmail(name);
                    return new MapMessage().add("status", "fail").add("credits", userCredits).add("errorMessage", "奖品不存在");
                }
                RewardProduct product = rewardLoaderClient.loadAllTeacherProducts().stream()
                        .filter(e -> Boolean.TRUE.equals(e.getOnlined()))
                        .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                        .filter(e -> (teacherDetail.isPrimarySchool() && Boolean.TRUE.equals(e.getPrimarySchoolVisible())) ||
                                (teacherDetail.isJuniorTeacher() &&  Boolean.TRUE.equals(e.getJuniorSchoolVisible())))
                        .filter(e -> (StringUtils.isNotBlank(itemCode) && itemCode.trim().equals(e.getRemarks())) ||
                                (StringUtils.isBlank(itemCode) && name.equals(e.getProductName())))
                        .findFirst().orElse(null);
                if (product == null) {
                    sendNofityEmail(name);
                    return new MapMessage().add("status", "fail").add("credits", userCredits).add("errorMessage", "奖品不存在");
                }
                productDetail = rewardLoaderClient.getRewardProductDetailGenerator().generateTeacherRewardProductDetail(product, teacherDetail);
                userCredits = teacherDetail.getUserIntegral().getUsable();
                user = teacherDetail;
                break;
            default:
                return new MapMessage().add("status", "fail").add("credits", userCredits).add("errorMessage", "奖品不存在");
        }

        if (userCredits < credits) {
            return new MapMessage().add("status", "fail").add("credits", userCredits).add("errorMessage", "学豆不足");
        }
        return rewardServiceClient.exchangedDuibaCoupon(duibaCoupon, user, productDetail).add("credits", userCredits);
    }

    @RequestMapping(value = "duiba/finish.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String finishOrder() {
        String appKey = getRequestString("appKey");
        if (StringUtils.isBlank(appKey)) {
            return "fail";
        }
        DuibaTool.DuibaApp app = getCurrentApp(appKey);
        boolean sign = DuibaTool.signVerify(getRequest(), app);
        if (!sign) {
            return "fail";
        }
        String uid = getRequestString("uid");
        boolean result = getRequestBool("success");
        String orderNum = getRequestString("orderNum");
        long orderId= SafeConverter.toLong(getRequestString("bizId"), 0L);
        Long userId = SafeConverter.toLong(uid);
        if (StringUtils.isAnyBlank(uid, orderNum) ||  userId <= 0L || orderId <= 0L) {
            return "fail";
        }
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return "fail";
        }
        MapMessage message = rewardServiceClient.confirmDuibaCoupon(user, result, orderId, orderNum);
        return message.isSuccess() ? "ok" : "fail";
    }

    private void sendNofityEmail(String name) {
        if (RuntimeMode.current().le(Mode.STAGING)) {
            logger.error("duiba product config error. name:{}", name);
            return;
        }
        Map<String, Object> content = new HashMap<>();
        content.put("info", "兑吧商品："+ name + "配置有问题，请检查");
        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                .to("shan.wang@17zuoye.com")
                .cc("zhilong.hu@17zuoye.com;haitian.gan@17zuoye.com")
                .subject("兑吧商品配置环境：" + RuntimeMode.getCurrentStage())
                .content(content)
                .send();
    }

    private DuibaTool.DuibaApp getCurrentApp(String appKey) {
        DuibaTool.DuibaApp app;
        if (RuntimeMode.current().le(Mode.TEST)) {
            app = DuibaTool.DuibaApp.TEST_XUEDOU.getAppKey().equals(appKey) ? DuibaTool.DuibaApp.TEST_XUEDOU : DuibaTool.DuibaApp.TEST_YUANDINGDOU;
        } else {
            app = DuibaTool.DuibaApp.ONLINE_XUEDOU.getAppKey().equals(appKey) ? DuibaTool.DuibaApp.ONLINE_XUEDOU : DuibaTool.DuibaApp.ONLINE_YUANDINGDOU;
        }
        return app;
    }
}
