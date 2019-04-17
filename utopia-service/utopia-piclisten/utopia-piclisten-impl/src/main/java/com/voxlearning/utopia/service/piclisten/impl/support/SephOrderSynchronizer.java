package com.voxlearning.utopia.service.piclisten.impl.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Base64Utils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.piclisten.impl.service.VendorQueueService;
import com.voxlearning.utopia.service.piclisten.support.OrderSynchronizer;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeBookInfo;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/1/17
 * 沪教点读机订单同步
 */
@Named
@Slf4j
public class SephOrderSynchronizer implements OrderSynchronizer {

    @Inject
    private VendorQueueService vendorService;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;


    /**
     * Performs this operation on the given argument.
     *
     * @param userOrder the input argument
     */
    @Override
    public void accept(UserOrder userOrder) {
        if (!isValid(userOrder)) {
            return;
        }

        Map<String, Object> map = generateNotifyParam(userOrder);
        vendorService.sendHttpNotify(userOrder.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_ORDER_SYNCHRONIZE_TARGET_SEPH), map);

    }

    private boolean isValid(UserOrder userOrder) {
        if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook) {
            return false;
        }
        if (StringUtils.isBlank(userOrder.getExtAttributes())) {
            return false;
        }

        Map<String, Object> attrs = JsonUtils.fromJson(userOrder.getExtAttributes());
        if (MapUtils.isEmpty(attrs)) {
            return false;
        }

        OrderSynchronizeContext context = JsonUtils.fromJson(userOrder.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) {
            return false;
        }
        for (OrderSynchronizeBookInfo info : context.getBooks()) {
            if (StringUtils.isNotBlank(info.getSource()) && info.getSource().equals(PUBLISHER_SEPH)) {
                return true;
            }
        }
        return false;
    }


    private Map<String, Object> generateNotifyParam(UserOrder order) {
//        Map<String, Object> attrs = JsonUtils.fromJson(order.getExtAttributes());
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        SephOrderInfo sephOrderInfo = new SephOrderInfo();
        sephOrderInfo.setUserId(SafeConverter.toString(order.getUserId()));
        sephOrderInfo.setOrderId(order.getId());
        sephOrderInfo.setPrice(order.getOrderPrice());
        sephOrderInfo.setRTime(DateUtils.dateToString(new Date()));
        sephOrderInfo.setTextBookId(context.getBooks().get(0).getBookId());
        sephOrderInfo.setCycle(SafeConverter.toString(context.getBooks().get(0).getPrice()));
        String json = JsonUtils.toJson(sephOrderInfo);
        String sephSecret = generateSephSecret(json);
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("Info", sephSecret);

        return returnMap;
    }

    public static String generateSephSecret(String json) {
        if (StringUtils.isBlank(json)) {
            return "";
        }
        String secretString = ProductConfig.get(CONFIG_PICLISTEN_SEPH_SECRET);
//        String secretString = "YQZYW123";
        String iv = ProductConfig.get(CONFIG_PICLISTEN_SEPH_IV);
//        String iv = "12345678";
        if (StringUtils.isBlank(secretString) || StringUtils.isBlank(iv)) {
            return "";
        }
        try {
            DESKeySpec desKey = new DESKeySpec(secretString.getBytes("UTF8"));
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] bytes = cipher.doFinal(json.getBytes("UTF8"));
            return Base64Utils.encodeBase64String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

//    public static void main(String[] args) {
//        String json = "{\"UserID\":\"258186\",\"OrderID\":\"51781577611127686\",\"RTime\":\"2018-02-27 15:29:36\",\"TextBookID\":\"109\",\"Cycle\":\"365\",\"Price\":60}";
//        System.out.println(generateSephSecret(json));
//    }
}
