/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.TrusteeShop;
import com.voxlearning.utopia.service.business.consumer.BusinessVendorServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/*
 * 活动专题页
 * Created by xinqiang.wang on 2015-11-19.
 */
@Controller
@RequestMapping(value = "/parent/activity")
public class ParentActivityController extends AbstractParentWebController {

    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;

    @Inject private SmsServiceClient smsServiceClient;

    @Inject private BusinessVendorServiceClient businessVendorServiceClient;

    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    public String activityList(Model model) {//获取学生列表
        User user = getRequestContext().getCurrentUser().get();
        Long parentId = getRequestContext().getUserId();
        // 验证是否全部选择了家长角色
        String url = callNameAvailable(parentId);
        if (StringUtils.isNotBlank(url)) {
            return "redirect:" + url;
        }
        List<User> students = studentLoaderClient.loadParentStudents(parentId);
        if (students.size() == 0) {
            //跳去绑学生页面
            return "redirect:/parent/ucenter/bindchild.vpage";
        }
        /****  热门活动列表  *****/
        // 获取托管活动banner
        Map<Long, School> schoolMap = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchools(students.stream().map(User::getId).collect(Collectors.toList()))
                .getUninterruptibly();
        if (MapUtils.isEmpty(schoolMap)) {
            return "redirect:/parent/homework/index.vpage";
        }
        for (School school : schoolMap.values()) {
            TrusteeShop trusteeShop = TrusteeShop.getBySchoolIdAndType(school.getId().toString(), "trusteenew");
            if (trusteeShop != null) {
                model.addAttribute("trusteeId", trusteeShop.getShopId());
                break;
            }
        }
        for (School school : schoolMap.values()) {
            TrusteeShop trusteeShop = TrusteeShop.getBySchoolIdAndType(school.getId().toString(), "openclass");
            if (trusteeShop != null) {
                model.addAttribute("openClassId", trusteeShop.getShopId());
                break;
            }
        }
        // 家长是否可见阿分题移动版预热活动，看孩子是否能够购买
        boolean showWarmup = false;
        List<String> wechatAvailableApps = Arrays.asList(OrderProductServiceType.AfentiExam.name());
        List<VendorApps> availableApps = new ArrayList<>();
        List<User> children = studentLoaderClient.loadParentStudents(parentId);
        List<Long> studentIds = children.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds);
        if (!CollectionUtils.isEmpty(children)) {
            for (StudentDetail studentDetail : studentDetails.values()) {
                availableApps = businessVendorServiceClient.getParentAvailableApps(user, studentDetail);
                availableApps.stream().filter(a -> wechatAvailableApps.contains(a.getAppKey())).collect(Collectors.toList());
                showWarmup = !CollectionUtils.isEmpty(availableApps);
                if (showWarmup) break;
            }
        }
        model.addAttribute("showWarmup", showWarmup);
        return "/parent/activity/list";
    }

    @RequestMapping(value = "/getStudentList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentList() {
        // 获取学生列表
        List<User> students = studentLoaderClient.loadParentStudents(getRequestContext().getUserId());
        if (students.size() == 0) {
            // 跳去绑学生页面
            return MapMessage.successMessage().add("students", new LinkedList<>());
        }
        List<Map<String, Object>> stdInfos = mapChildInfos(students);
        return MapMessage.successMessage().add("students", stdInfos);
    }

    @RequestMapping(value = "/globalmath.vpage", method = RequestMethod.GET)
    public String globalMath(Model model) {
        return "/parent/activity/globalmath";
    }

    @RequestMapping(value = "vipkidjump.vpage", method = RequestMethod.GET)
    public String vipkidJump() {
        // vip的h5活动页面地址
        Long parentId = getRequestContext().getUserId();
        // log
        Map<String, String> log = new HashMap<>();
        log.put("module", "activity");
        log.put("op", "activity_vipkid_jump");
        log.put("s0", getRequestContext().getAuthenticatedOpenId());
        log.put("uid", SafeConverter.toString(parentId));
        super.log(log);
        String url = "http://e.maka.im/k/H7CY42NN?DSCKID=1a00693e-5045-4943-bb12-db23b590f4dd&DSTIMESTAMP=1460370928705&from=singlemessage&isappinstalled=0";
        return "redirect:" + url;
    }

    @RequestMapping(value = "vipkidjumpx.vpage", method = RequestMethod.GET)
    public String vipkidJumpx() {
        // vip推广2.0，有A/B分组
        Long parentId = getRequestContext().getUserId();
        String group = getRequestString("group");
        // 默认取A组
        if (StringUtils.isBlank(group)) {
            group = "A";
        }
        // log
        Map<String, String> log = new HashMap<>();
        log.put("module", "activity");
        log.put("op", "activity_vipkid_jumpx_" + group);
        log.put("s0", getRequestContext().getAuthenticatedOpenId());
        log.put("uid", SafeConverter.toString(parentId));
        super.log(log);
        String url;
        if (StringUtils.equals(group, "A")) {
            url = "http://viewer.maka.im/k/4KTHHOX8";
        } else {
            url = "http://viewer.maka.im/k/LQRTYEO8";
        }

        return "redirect:" + url;
    }

    @RequestMapping(value = "ustalkpromot.vpage", method = RequestMethod.GET)
    public String ustalkPromot(Model model) {
        // ustalk直播客微信家长推广
        Long parentId = getRequestContext().getUserId();
        String brand = getRequestString("brand");
        List<User> students = studentLoaderClient.loadParentStudents(parentId);
        List<Map<String, Object>> stdInfos = mapChildInfos(students);
        model.addAttribute("students", stdInfos);
        if (StringUtils.equals(brand, "vipkid")) {
            // 测试vipkid的品牌效果
            return "parent/activity/ustalkpromotvipkid";
        }
        return "parent/activity/ustalkpromot";
    }


    @RequestMapping(value = "ustalkpromotsubscribe.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ustalkPromotSubscribe() {
        String mobile = getRequestString("pmobile");
        String studentName = getRequestString("studentName");
        long studentId = getRequestLong("studentId");
        String gray = getRequestString("gray");
        String verifyCode = getRequestString("verifyCode");
        // 直接用gray当作channelName
        String channelName = gray;
        User student = userLoaderClient.loadUser(studentId);
        // 验证数据
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请填写合法手机号");
        }
        if (StringUtils.isEmpty(verifyCode)) {
            return MapMessage.errorMessage("请填入手机验证码");
        }
        if (student == null) {
            return MapMessage.errorMessage("请选择学生");
        }
        // 验证手机号
        // 验证短信验证码
        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, verifyCode, SmsType.LIVECAST_WEIXIN_PROMOT.name());
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        Map<Object, Object> paramMap = new HashMap<>();
        paramMap.put("mobile", mobile);
        paramMap.put("studentId", studentId);
        paramMap.put("studentName", studentName);
        paramMap.put("clientType", "weixin");
        paramMap.put("channelName", channelName);
        paramMap.put("userAgent", getRequest().getHeader("User-Agent"));
        paramMap.put("geoInfo", "");

        // todo:这里只是把ustalk的额环境地址hardcoded,以后需要使用config
        // 发给ustalk的后端
        String url;
        if (RuntimeMode.current().equals(Mode.STAGING)) {
            url = "http://livecast-student.staging.17zuoye.net/auth/guest.vpage";
        } else if (RuntimeMode.current().equals(Mode.PRODUCTION)) {
            url = "http://www.ustalk.com/auth/guest.vpage";
        } else {
            // 使用test地址
//            url = "http://10.200.8.240:8186/auth/guest.vpage";
            url = "http://livecast-student.test.17zuoye.net/auth/guest.vpage";
        }

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).addParameter(paramMap).execute();

        if (response.hasHttpClientException()) {
            logger.error("往ustalk post失败，" + response.getHttpClientExceptionMessage());
            return MapMessage.errorMessage("UStalk申领失败");
        }
        Map<String, Object> map = JsonUtils.fromJson(response.getResponseString());
        return MapMessage.of(map);
    }

    // 发送验证码
    @RequestMapping(value = "sendustalksmscode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendUstalkSmsCode() {
        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号格式不正确");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                mobile,
                SmsType.LIVECAST_WEIXIN_PROMOT.name(),
                false
        );
    }

    // ustalk微信菜单跳转
    @RequestMapping(value = "ustalkwechatmenu.vpage", method = RequestMethod.GET)
    public String ustalkWechatMenu() {
        Long parentId = getRequestContext().getUserId();
        String url;
        if (parentId != null) {
            // 绑定过家长
            url = "https://www.ustalk.com/auth/sfc.vpage?c=2&d=437&uid=" + parentId;
        } else {
            // 未绑定家长
            url = "https://www.ustalk.com/auth/sfc.vpage?c=2&d=437";
        }
        return "redirect:" + url;
    }
}
