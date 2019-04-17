package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserService;
import com.voxlearning.utopia.service.ai.api.ChipsOrderProductLoader;
import com.voxlearning.utopia.service.ai.data.ChipsUserOrderBO;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.controller.open.ApiConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_BAD_REQUEST_CODE;

@Controller
@RequestMapping("/chips/order")
public class ChipsOrderController extends AbstractAiController {

    @ImportService(interfaceClass = ChipsOrderProductLoader.class)
    private ChipsOrderProductLoader chipsOrderProductLoader;

    @ImportService(interfaceClass = ChipsEnglishUserService.class)
    private ChipsEnglishUserService chipsEnglishUserService;

    /**
     * 创建订单 *
     *
     * @return
     */
    @RequestMapping(value = "create.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage createOrder() {
        User curUser = currentUser();
        User user = curUser;

        if (null == user) {
            return MapMessage.errorMessage("请登录后购买").setErrorCode("666");
        }

        if (user.isStudent()) {
            MapMessage mapMessage = parentServiceClient.loadOrRegisteDefaultParentUserByStudentId(user.getId());
            if (!mapMessage.isSuccess()) {
                mapMessage.setInfo("existParent".equals(SafeConverter.toString(mapMessage.get("type"))) ? "家长已存在，请绑定家长" : mapMessage.getInfo());
                return mapMessage;
            }
            user = (User) mapMessage.get("parent");
        }

        if (!user.isParent()) {
            return MapMessage.errorMessage("请使用学生或者家长账号登录");
        }

        String productIdString = getRequestString("productId");
        String productName = getRequestString("productName");
        String refer = getRequestParameter("refer", "330222");
        String channel = getRequestString("channel");

        if (StringUtils.isBlank(productIdString)) {
            return MapMessage.errorMessage("产品为空");
        }

        List<String> productIds = Arrays.stream(productIdString.split(",")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productIds)) {
            return MapMessage.errorMessage("产品为空");
        }

        try {
            ChipsUserOrderBO chipsUserOrderBO = new ChipsUserOrderBO(user.getId(), productIds);
            chipsUserOrderBO.setRefer(refer);
            chipsUserOrderBO.setChannel(channel);
            chipsUserOrderBO.setProductName(productName);
            MapMessage message = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("chipsEnglishUserService.createOrder")
                    .keys(user.getId())
                    .callback(() -> chipsEnglishUserService.createOrder(chipsUserOrderBO))
                    .build()
                    .execute();
            if (message.isSuccess()) {
                String orderId = SafeConverter.toString(message.get("orderId"));
                MapMessage res = MapMessage.successMessage().add("orderId", orderId);
                if (curUser.isStudent()) {
                    Map<String, String> payMap = generatePayParams(orderId, SafeConverter.toString(message.get("orderToken")), curUser.getId());
                    res.add("payParams", payMap);
                }
                return res;
            }
            return message;
        } catch (CannotAcquireLockException e) {
            return failMessage("正在处理中");
        } catch (DuplicatedOperationException e) {
            return failMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            return MapMessage.errorMessage("生成订单失败").setErrorCode(RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    private Map<String, String> generatePayParams(String orderId, String orderToken, Long userId) {
        Map<String, String> params = new HashMap<>();
        params.put(ApiConstants.REQ_APP_KEY, "17Student");
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", userId);
        if (null != vendorAppsUserRef) {
            params.put(ApiConstants.REQ_SESSION_KEY, vendorAppsUserRef.getSessionKey());
        }

        params.put(ApiConstants.REQ_ORDER_ID, orderId);
        params.put(ApiConstants.REQ_ORDER_TOKEN, orderToken);
        params.put(ApiConstants.REQ_RETURN_URL, "/view/mobile/parent/parent_ai/payment_success_in_student?oid=" + orderId);

        VendorApps vendorApps = vendorLoaderClient.loadVendor("17Student");
        if (null == vendorApps) {
            throw new IllegalStateException("No vendorApp found for 17Student");
        }
        params.put(ApiConstants.REQ_SIG, DigestSignUtils.signMd5(params, vendorApps.getSecretKey()));

        return params;
    }

    @RequestMapping(value = "shortProduct/load.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadShortProduct() {
        User user = currentUser();

        Long parenId = Optional.ofNullable(user).filter(u -> u.isParent()).map(User::getId).orElse(null);
        Long studentId = Optional.ofNullable(user).map(u -> {
            if (u.isStudent()) {
                return u.getId();
            }
            return studentLoaderClient.loadParentStudents(u.getId()).stream().findFirst().map(User::getId).orElse(null);
        }).orElse(null);

        String type = getRequestString("type");
        boolean check = (parenId == null && studentId == null) ? getRequestBool("primary") : false;
        return wrapper(mm -> mm.putAll(chipsOrderProductLoader.loadOnSaleShortLevelProductInfo(parenId, studentId, check, type).set("wechatDomain", getWechatSiteUrl())));
    }


    @RequestMapping(value = "shortProduct/be.vpage", method = {RequestMethod.GET})
    public String shortProductBe() {
        User user = currentUser();
        if (null == user) {
            return "redirect:/view/mobile/parent/parent_ai/blank_map";
        }
        String refer = getRequestString("or");
        return "redirect:" + chipsOrderProductLoader.loadShortProductAdPath(user.getId()) + (StringUtils.isNotBlank(refer) ? ("?or=" + refer) : "");
    }

    @RequestMapping(value = "officialProduct/load.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage loadOfficialProduct() {
        User parent = currentUser();
        if (null == parent || !parent.isParent()) {
            return MapMessage.errorMessage("请登录").setErrorCode("666").set("wechatDomain", getWechatSiteUrl());
        }

        String typeName = getRequestString("type");
        if (StringUtils.isBlank(typeName)) {
            return MapMessage.errorMessage("类型为空");
        }
        return wrapper(mm -> mm.set("wechatDomain", getWechatSiteUrl()).putAll(chipsOrderProductLoader.loadOfficialProductInfoByType(typeName, parent.getId())));
    }
    
}
