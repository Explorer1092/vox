package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.partner.api.constant.ThirdPartyType;
import com.voxlearning.galaxy.service.partner.api.entity.ThirdPartyUserInfo;
import com.voxlearning.galaxy.service.partner.api.entity.ThirdPartyUserInfoRef;
import com.voxlearning.galaxy.service.partner.api.mapper.ThirdPartyConfigMapper;
import com.voxlearning.galaxy.service.partner.api.service.ThirdPartyService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.service.business.consumer.BusinessUserOrderServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.email.api.EmailService;
import com.voxlearning.utopia.service.email.api.client.PlainEmailCreator;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.sms.api.SmsService;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/6/7
 */
@Controller
@RequestMapping(value = "/parentMobile/thirdParty/")
public class MobileParentThirdPartyJoinUpController extends AbstractMobileParentController {

    private static final String THIRD_PARTY_LIMIT_JOIN_UP_COUNT = "THIRD_PARTY_LIMIT_JOIN_UP_COUNT_";
    private static final String THIRD_PARTY_LIMIT_JOIN_UP_COUNT_TOTAL = "THIRD_PARTY_LIMIT_JOIN_UP_COUNT_TOTAL_";

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;
    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @ImportService(interfaceClass = EmailService.class)
    private EmailService emailService;
    @ImportService(interfaceClass = SmsService.class)
    private SmsService smsService;
    @Inject
    private UserOrderServiceClient userOrderServiceClient;
    @Inject
    private BusinessUserOrderServiceClient businessUserOrderServiceClient;

    private static final String THIRD_PARTY_JOIN_UP_EMAIL_LIST = "THIRD_PARTY_JOIN_UP_EMAIL_LIST";

    @RequestMapping(value = "saveUserRecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUserRecord() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        String mobile = getRequestString("mobile");
        String chileName = getRequestString("child_name");
        int childAge = getRequestInt("child_age");
        int clazzLevel = getRequestInt("clazz_level");
        int thirdPartyId = getRequestInt("third_party_id");
        String verifyCode = getRequestString("verify_code");
        String orderId = getRequestString("order_id");
        String device = getRequestString("device");
        long studentId = getRequestLong("sid");
        boolean isVerifyMobile = getRequestBool("is_verify", true);
        if (!ThirdPartyType.generateThirdPartyTypeIds().contains(thirdPartyId)) {
            return MapMessage.errorMessage("id错误");
        }
        if (isVerifyMobile) {
            MapMessage verifySmsMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, verifyCode, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name());
            if (!verifySmsMessage.isSuccess()) {
                return MapMessage.errorMessage("请输入正确的验证码");
            }
        }
        String encodeMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
        String id = ThirdPartyUserInfo.generateId(encodeMobile, thirdPartyId);
        ThirdPartyUserInfo thirdPartyUserInfo = thirdPartyService.loadUserInfoById(id);
        if (thirdPartyUserInfo != null) {
            return MapMessage.errorMessage("该手机号已存在，无需重复报名");
        }
        ThirdPartyConfigMapper partyConfigMapper = thirdPartyService.getThirdPartyConfig(thirdPartyId);
        Integer joinUpLimit = 0;
        if (partyConfigMapper != null) {
            joinUpLimit = SafeConverter.toInt(thirdPartyService.loadJoinUpLimit(thirdPartyId));
            if (partyConfigMapper.getUserLimitCount() != null && joinUpLimit >= partyConfigMapper.getUserLimitCount()) {
                if (partyConfigMapper.getSendEmailCount() != null && joinUpLimit != 0 && joinUpLimit.equals(partyConfigMapper.getSendEmailCount())) {
                    String thirdPartyTypeNameById = ThirdPartyType.generateThirdPartyTypeNameById(thirdPartyId);
                    if (StringUtils.isNotBlank(thirdPartyTypeNameById)) {
                        generateAndSendEmail(thirdPartyTypeNameById, joinUpLimit);
                    }
                }
                return MapMessage.errorMessage("本期名额已满");
            }
        }
        ThirdPartyUserInfoRef thirdPartyUserInfoRef = new ThirdPartyUserInfoRef();
        thirdPartyUserInfoRef.setInfoId(id);
        thirdPartyUserInfoRef.setChildAge(childAge);
        thirdPartyUserInfoRef.setChildName(chileName);
        thirdPartyUserInfoRef.setClazzLevel(clazzLevel);
        thirdPartyUserInfoRef.setParentId(parent.getId());
        thirdPartyUserInfoRef.setMobile(encodeMobile);
        thirdPartyUserInfoRef.setThirdPartyTypeId(thirdPartyId);
        thirdPartyUserInfoRef.setDevice(device);
        if (StringUtils.isNotBlank(orderId)) {
            thirdPartyUserInfoRef.setOrderId(orderId);
        }
        if (studentId != 0L) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail != null && studentDetail.getRootRegionCode() != null) {
                thirdPartyUserInfoRef.setRegionCode(studentDetail.getRootRegionCode());
            }
        }
        thirdPartyUserInfoRef = thirdPartyService.upsertUserInfoRef(thirdPartyUserInfoRef);
        if (thirdPartyUserInfoRef != null) {
            ThirdPartyType typeById = ThirdPartyType.getTypeById(thirdPartyUserInfoRef.getThirdPartyTypeId());
            if (typeById != null) {
                if (StringUtils.isBlank(typeById.getOrderProductServiceType())) {
                    joinUpLimit = SafeConverter.toInt(thirdPartyService.incrJoinUpLimit(thirdPartyId));
                    SmsMessage smsMessage = new SmsMessage();
                    smsMessage.setMobile(mobile);
                    smsMessage.setType(SmsType.PARENT_COURSE_NOTIFY.name());
                    String smsText = ThirdPartyType.getFindThirdPartySmsTextById(thirdPartyId);
                    if (StringUtils.isNotBlank(smsText) && StringUtils.isNotBlank(mobile)) {
                        smsMessage.setSmsContent(smsText);
                        smsService.sendSms(smsMessage);
                    }
                }

                if (partyConfigMapper != null) {
                    if (partyConfigMapper.getSendEmailCount() != null && joinUpLimit != 0 && joinUpLimit.equals(partyConfigMapper.getSendEmailCount())) {
                        String thirdPartyTypeNameById = ThirdPartyType.generateThirdPartyTypeNameById(thirdPartyId);
                        if (StringUtils.isNotBlank(thirdPartyTypeNameById)) {
                            generateAndSendEmail(thirdPartyTypeNameById, joinUpLimit);
                        }
                    }
                }
            }
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "incrCache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage incrCacheForTest() {
        int id = getRequestInt("id");
        Long joinUpLimit = thirdPartyService.incrJoinUpLimit(id);
        return MapMessage.successMessage().add("count", joinUpLimit);
    }


    private void generateAndSendEmail(String thirdPartyName, Integer joinCount) {
//        if (!RuntimeMode.isProduction())
//            return;
        String emailConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), THIRD_PARTY_JOIN_UP_EMAIL_LIST);
        if (StringUtils.isEmpty(emailConfig)) {
            return;
        }
        String[] emailArray = StringUtils.split(emailConfig, ",");
        List<String> emailList = Arrays.asList(emailArray);
        if (CollectionUtils.isEmpty(emailList)) {
            return;
        }
        PlainEmailCreator plainEmailCreator = new PlainEmailCreator(emailService);
        String emailBodyPattern = "{0}课程报名满员通知：\n课程名称:{1}\n当前报名人数:{2}";
        String emailBody = MessageFormat.format(emailBodyPattern, thirdPartyName, thirdPartyName, joinCount);
        plainEmailCreator.body(emailBody);
        plainEmailCreator.subject("课程报名满员通知");
        emailList.forEach(e -> {
            plainEmailCreator.to(e + "@17zuoye.com");
            plainEmailCreator.send();
        });
    }

    /**
     * 0元支付
     * @return
     */
    @RequestMapping(value = "create_zero_order.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createZeroOrder() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        String mobile = getRequestString("mobile");
        String chileName = getRequestString("child_name");
        int childAge = getRequestInt("child_age");
        int clazzLevel = getRequestInt("clazz_level");
        int thirdPartyId = getRequestInt("third_party_id");
        String verifyCode = getRequestString("verify_code");
        String device = getRequestString("device");
        boolean isVerifyMobile = getRequestBool("is_verify", true);
        String productId = getRequestString("productId");
        String orderReferer = getRequestString("orderReferer");
        if (StringUtils.isBlank(productId)) {
            return MapMessage.errorMessage("产品id不能为空");
        }
        if (StringUtils.isBlank(orderReferer)) {
            return MapMessage.errorMessage("refer不能为空");
        }
        if (thirdPartyId == 0) {
            return MapMessage.errorMessage("产品类型不能为空");
        }
        String regionName = getRequestString("region_name");
        String parentName = getRequestString("parent_name");
        int childGender = getRequestInt("child_gender");

        if (StringUtils.isBlank(mobile)) {
            mobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
        }
        if (StringUtils.isBlank(parentName)) {
            parentName = parent.fetchRealname();
        }
        if (isVerifyMobile) {
            MapMessage verifySmsMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, verifyCode, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name());
            if (!verifySmsMessage.isSuccess()) {
                return MapMessage.errorMessage("请输入正确的验证码");
            }
        }
        String encodeMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
        String id = ThirdPartyUserInfo.generateId(encodeMobile, thirdPartyId);
        ThirdPartyUserInfo thirdPartyUserInfo = thirdPartyService.loadUserInfoById(id);
        if (thirdPartyUserInfo != null) {
            return MapMessage.successMessage("该手机号已存在，无需重复报名").add("repetition", true);
        }
        // 数量限制
        MapMessage mapMessage = joinUpLimit(thirdPartyId, regionName);
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage(mapMessage.getInfo());
        }
        // 存订单
        MapMessage orderMessage = userOrderServiceClient.createAppOrder(parent.getId(), "YiQiXueDiversion", Collections.singletonList(productId), orderReferer);
        if (!orderMessage.isSuccess()) {
            return orderMessage;
        }
        // 支付
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPayAmount(BigDecimal.ZERO);
        paymentRequest.setPayUser(parent.getId());
        paymentRequest.setTradeNumber(SafeConverter.toString(orderMessage.get("orderId")));
        PaymentCallbackContext paymentCallbackContext = buildPaymentCallbackContext(paymentRequest);
        UserOrder userOrder = businessUserOrderServiceClient.processUserOrderPayment(paymentCallbackContext);
        if (userOrder == null || userOrder.getPaymentStatus() != PaymentStatus.Paid) {
            return MapMessage.errorMessage("支付失败");
        }
        // 存userInfo
        ThirdPartyUserInfoRef thirdPartyUserInfoRef = new ThirdPartyUserInfoRef();
        thirdPartyUserInfoRef.setInfoId(id);
        thirdPartyUserInfoRef.setChildAge(childAge);
        thirdPartyUserInfoRef.setChildName(chileName);
        thirdPartyUserInfoRef.setClazzLevel(clazzLevel);
        thirdPartyUserInfoRef.setParentId(parent.getId());
        thirdPartyUserInfoRef.setMobile(encodeMobile);
        thirdPartyUserInfoRef.setThirdPartyTypeId(thirdPartyId);
        thirdPartyUserInfoRef.setDevice(device);
        thirdPartyUserInfoRef.setOrderId(userOrder.genUserOrderId());
        thirdPartyUserInfoRef.setChildGender(childGender);
        thirdPartyUserInfoRef.setRegionName(regionName);
        thirdPartyUserInfoRef.setParentName(parentName);
        thirdPartyService.upsertUserInfoRef(thirdPartyUserInfoRef);

        // 增加总报名数
        incrJoinUpCount(thirdPartyId, regionName);
        // 增加每天报名数
        incrDayJoinUpCount(thirdPartyId, regionName);
        // 发短信
        sendJoinUpSms(thirdPartyId, mobile);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "joinUpLimit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinUpLimit(){
        addCrossHeaderForXdomain();

        int thirdPartyId = getRequestInt("third_party_id");
        ThirdPartyConfigMapper partyConfigMapper = thirdPartyService.getThirdPartyConfig(thirdPartyId);
        if (partyConfigMapper != null) {
            Integer joinUpLimit = SafeConverter.toInt(thirdPartyService.loadJoinUpLimit(thirdPartyId));
            if (partyConfigMapper.getUserLimitCount() != null && joinUpLimit >= partyConfigMapper.getUserLimitCount()) {
                if (partyConfigMapper.getSendEmailCount() != null && joinUpLimit != 0 && joinUpLimit.equals(partyConfigMapper.getSendEmailCount())) {
                    String thirdPartyTypeNameById = ThirdPartyType.generateThirdPartyTypeNameById(thirdPartyId);
                    if (StringUtils.isNotBlank(thirdPartyTypeNameById)) {
                        generateAndSendEmail(thirdPartyTypeNameById, joinUpLimit);
                    }
                }
                return MapMessage.errorMessage("本期名额已满");
            }
        }
        return MapMessage.successMessage("本期名额充足");
    }

    @RequestMapping(value = "limit_count.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage limitCount(){
        int thirdPartyId = getRequestInt("third_party_id");
        if (thirdPartyId == 0) {
            return MapMessage.errorMessage("参数有误");
        }
        String regionName = getRequestString("region_name");
        ThirdPartyConfigMapper partyConfigMapper = thirdPartyService.getThirdPartyConfig(thirdPartyId);
        if (partyConfigMapper != null) {
            // 报名总数
            return MapMessage.successMessage().add("data", MapUtils.m(
                    "totalCount", SafeConverter.toInt(partyConfigMapper.getUserLimitCount()),
                    "applyTotalCount", SafeConverter.toInt(loadJoinUpCount(thirdPartyId, regionName)),
                    "dayCount", partyConfigMapper.getLimitCount(),
                    "applyDayCount", loadDayJoinUpCount(thirdPartyId, regionName)));
        }
        return MapMessage.successMessage("本期名额充足");
    }

    /**
     * 非0元支付活动
     * @return
     */
    @RequestMapping(value = "create_order.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createActivityOrder() {
        addCrossHeaderForXdomain();

        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        Long parentId = parent.getId();

        String parentName = getRequestString("parent_name");
        String mobile = getRequestString("mobile");
        int clazzLevel = getRequestInt("clazz_level");
        int thirdPartyId = getRequestInt("third_party_id");
        String productId = getRequestString("product_id");
        String regionCode = getRequestString("region_code");
        String email = getRequestString("email");
        String companyName = getRequestString("company_name");
        String job = getRequestString("job");

        if (!ThirdPartyType.generateThirdPartyTypeIds().contains(thirdPartyId)) {
            return MapMessage.errorMessage("id错误");
        }
        if (StringUtils.isBlank(mobile)) {
            mobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
        }

        if (StringUtils.isBlank(parentName)) {
            parentName = parent.fetchRealname();
        }

        String encodeMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
        String id = ThirdPartyUserInfo.generateId(encodeMobile, thirdPartyId);
        ThirdPartyUserInfo thirdPartyUserInfo = thirdPartyService.loadUserInfoById(id);
        UserOrder userOrder = null;
        String orderId = "";
        if (thirdPartyUserInfo != null) {
            userOrder = userOrderLoaderClient.loadUserOrderList(parentId).stream().filter(o -> Objects.equals(o.getProductId(),productId)).findFirst().orElse(null);
            if(Objects.nonNull(userOrder) && userOrder.getPaymentStatus() == PaymentStatus.Paid){
                return MapMessage.errorMessage("该手机号已存在，无需重复报名");
            }
        }

        ThirdPartyConfigMapper partyConfigMapper = thirdPartyService.getThirdPartyConfig(thirdPartyId);
        Integer joinUpLimit = 0;
        if (partyConfigMapper != null) {
            joinUpLimit = SafeConverter.toInt(thirdPartyService.loadJoinUpLimit(thirdPartyId));
            if (partyConfigMapper.getUserLimitCount() != null && joinUpLimit >= partyConfigMapper.getUserLimitCount()) {
                if (partyConfigMapper.getSendEmailCount() != null && joinUpLimit != 0 && joinUpLimit.equals(partyConfigMapper.getSendEmailCount())) {
                    String thirdPartyTypeNameById = ThirdPartyType.generateThirdPartyTypeNameById(thirdPartyId);
                    if (StringUtils.isNotBlank(thirdPartyTypeNameById)) {
                        generateAndSendEmail(thirdPartyTypeNameById, joinUpLimit);
                    }
                }
                return MapMessage.errorMessage("本期名额已满");
            }
        }

        // 存订单
        if(Objects.isNull(userOrder)){
            MapMessage orderMessage = userOrderServiceClient.createAppOrder(parentId, "YiQiXueDiversion", Collections.singletonList(productId), "");
            if (!orderMessage.isSuccess()) {
                return orderMessage;
            }
            orderId = SafeConverter.toString(orderMessage.get("orderId"));
        }else{
            orderId = userOrder.genUserOrderId();
        }

        // 存userInfo
        ThirdPartyUserInfoRef thirdPartyUserInfoRef = new ThirdPartyUserInfoRef();
        if (thirdPartyUserInfo != null) {
            thirdPartyUserInfoRef.setId(thirdPartyUserInfo.getRecordId());
        }else{
            thirdPartyService.incrJoinUpLimit(thirdPartyId);
        }
        thirdPartyUserInfoRef.setInfoId(id);
        thirdPartyUserInfoRef.setClazzLevel(clazzLevel);
        thirdPartyUserInfoRef.setParentId(parentId);
        thirdPartyUserInfoRef.setMobile(encodeMobile);
        thirdPartyUserInfoRef.setThirdPartyTypeId(thirdPartyId);
        thirdPartyUserInfoRef.setOrderId(orderId);
        thirdPartyUserInfoRef.setRegionCode(SafeConverter.toInt(regionCode));
        thirdPartyUserInfoRef.setParentName(parentName);
        thirdPartyUserInfoRef.setChildName(parentName);
        thirdPartyUserInfoRef.setExt1(email);
        thirdPartyUserInfoRef.setExt2(companyName);
        thirdPartyUserInfoRef.setExt3(job);
        thirdPartyService.upsertUserInfoRef(thirdPartyUserInfoRef);
        return MapMessage.successMessage().set("orderId",orderId);
    }


    /**
     * 获取区域列表接口
     */
    @RequestMapping(value = "/region/get.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getChildrenRegion() {
        addCrossHeaderForXdomain();

        List<Map<String, Object>> regionList = new LinkedList<>();
        Integer pcode = getRequestInt("region_pcode");
        List<ExRegion> regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        if (CollectionUtils.isNotEmpty(regionAll)) {
            for (ExRegion exRegion : regionAll) {
                Map<String, Object> region = new HashMap<>();
                region.put(RES_REGION_CODE, exRegion.getCode());
                region.put(RES_REGION_NAME, exRegion.getName());
                region.put(RES_REGION_TYPE, exRegion.fetchRegionType().getType());
                regionList.add(region);
            }
        }
        return MapMessage.successMessage().add("regionList", regionList);
    }

    /**
     * 增加当日的 报名量
     * @param thirdPartyId
     * @return
     */
    private long incrDayJoinUpCount(Integer thirdPartyId, String ...others) {
        return washingtonCacheSystem.CBS.unflushable.incr(loadKey(THIRD_PARTY_LIMIT_JOIN_UP_COUNT, thirdPartyId, others), 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    /**
     * 获取今日总报名量
     * @param thirdPartyId
     * @return
     */
    private long loadDayJoinUpCount(Integer thirdPartyId, String ...others) {
        return ObjectUtils.get(() -> SafeConverter.toLong(washingtonCacheSystem.CBS.unflushable.get(loadKey(THIRD_PARTY_LIMIT_JOIN_UP_COUNT, thirdPartyId, others)).getValue()), 0L);
    }

    /**
     * 增加总的报名量
     * @param thirdPartyId
     * @return
     */
    private long incrJoinUpCount(Integer thirdPartyId, String ...others) {
        int second = 3 * 30 * 24 * 60 * 60;
        return washingtonCacheSystem.CBS.unflushable.incr(loadKey(THIRD_PARTY_LIMIT_JOIN_UP_COUNT_TOTAL, thirdPartyId, others), 1, 1, second);
    }

    /**
     * 获取总的报名量
     * @param thirdPartyId
     * @return
     */
    private long loadJoinUpCount(Integer thirdPartyId, String ...others) {
        return ObjectUtils.get(() -> SafeConverter.toLong(washingtonCacheSystem.CBS.unflushable.get(loadKey(THIRD_PARTY_LIMIT_JOIN_UP_COUNT_TOTAL, thirdPartyId, others)).getValue()), 0L);
    }

    private String loadKey(String key, Integer thirdPartyId, String ...others) {
        StringBuilder cacheKey = new StringBuilder(key + thirdPartyId);
        if (others != null) {
            if (thirdPartyId == 11) {
                for (String other : others) {
                    cacheKey.append("_").append(other);
                }
            }
        }
        return cacheKey.toString();
    }
    /**
     * 发送报名短信
     * @param thirdPartyId
     * @param mobile
     */
    private void sendJoinUpSms (Integer thirdPartyId, String mobile) {
        // 秒小程
        if (thirdPartyId == 10) {
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(mobile);
            smsMessage.setType("HONEY_COMB_FREE");
            smsMessage.setSmsContent("【妙小程】课程领取成功，课程顾问将在两个工作日内电话联系您，请注意接听021开头的电话，如有问题请咨询400-168-7900。");
            smsService.sendSms(smsMessage);
        }
        // USkid
        if (thirdPartyId == 11) {
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(mobile);
            smsMessage.setType("HONEY_COMB_FREE");
            smsMessage.setSmsContent("【USKid】您已成功报名USKid英语精品小班课，课程顾问将在24小时内电话联系，请耐心等待，如有问题请咨询微信：bmwj0519。");
            smsService.sendSms(smsMessage);
        }
    }

    private MapMessage joinUpLimit(Integer thirdPartyId, String regionName) {
        ThirdPartyConfigMapper partyConfigMapper = thirdPartyService.getThirdPartyConfig(thirdPartyId);
        if (partyConfigMapper != null) {
            Integer joinUpLimit = SafeConverter.toInt(loadJoinUpCount(thirdPartyId, regionName));
            if (partyConfigMapper.getUserLimitCount() != null && joinUpLimit >= partyConfigMapper.getUserLimitCount()) {
                return MapMessage.errorMessage("本期名额已满");
            }
            long dayLimit = loadDayJoinUpCount(thirdPartyId, regionName);
            if (dayLimit >= partyConfigMapper.getLimitCount()) {
                return MapMessage.errorMessage("今日名额已满，明天早点来哦！");
            }
        }
        return MapMessage.successMessage();
    }

}
