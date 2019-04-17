package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.parent.api.UserOrderAddressService;
import com.voxlearning.utopia.service.parent.api.entity.common.UserOrderAddress;
import com.voxlearning.utopia.service.region.api.entities.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author jiangpeng
 * @since 2019-01-15 3:56 PM
 **/
@Controller
@RequestMapping("opmanager/studyTogether/address")
@Slf4j
public class CrmUserOrderAddressController extends AbstractStudyTogetherController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = UserOrderAddressService.class)
    private UserOrderAddressService userOrderAddressService;

    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        long userId = getRequestLong("userId");
        String orderId = getRequestString("orderId");
        String startDateStr = getRequestString("startDate");
        String endDateStr = getRequestString("endDate");
        Integer pageNum = getRequestInt("page", 1);
        if (userId == 0L && StringUtils.isBlank(orderId)
                && StringUtils.isBlank(startDateStr)
                && StringUtils.isBlank(endDateStr)) {
            startDateStr = "";
            endDateStr = "";
        }

        PageRequest pageRequest = new PageRequest(pageNum - 1, 20);
        Date startDate = DateUtils.stringToDate(startDateStr, DateUtils.FORMAT_SQL_DATE);
        Date endDate = DateUtils.stringToDate(endDateStr, DateUtils.FORMAT_SQL_DATE);
        Page<UserOrderAddress> userOrderAddressPage = userOrderAddressService.loadUserOrderAddressList(userId, orderId, startDate, endDate, pageRequest, null);
        List<UserOrderAddress> addressList = userOrderAddressPage.getContent();
        List<Map<String, String>> mapList = queryMapList(addressList, false);

        if (0L != userId) {
            model.addAttribute("userId", userId);
        }
        if (StringUtils.isNotBlank(orderId)) {
            model.addAttribute("orderId", orderId);
        }
        if (StringUtils.isNotBlank(startDateStr)) {
            model.addAttribute("startDate", startDateStr);
        }
        if (StringUtils.isNotBlank(endDateStr)) {
            model.addAttribute("endDate", endDateStr);
        }
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", mapList);
        model.addAttribute("totalPage", userOrderAddressPage.getTotalPages());
        model.addAttribute("hasPrev", userOrderAddressPage.hasPrevious());
        model.addAttribute("hasNext", userOrderAddressPage.hasNext());
        return "/opmanager/studyTogether/orderaddress/index";
    }

    private List<Map<String, String>> queryMapList(List<UserOrderAddress> addressList, boolean decodePhone) {
        List<Map<String, String>> mapList = new ArrayList<>();
        for (UserOrderAddress orderAddress : addressList) {
            UserOrderAddress.Address address = orderAddress.getAddress();
            if (address == null) {
                continue;
            }
            Region county = raikouSystem.loadRegion(address.getCountyCode());
            if (county == null) {
                continue;
            }
            Region city = raikouSystem.loadRegion(county.getPcode());
            if (city == null) {
                continue;
            }
            Region province = raikouSystem.loadRegion(city.getPcode());
            if (province == null) {
                continue;
            }
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(orderAddress.getProductId());
            if (orderProduct == null) {
                continue;
            }
            String addressOrderId = orderAddress.getId();
            Map<String, String> map = new HashMap<>();

            UserOrderAddress.OrderStatus orderStatus = orderAddress.safeGetOrderStatus();
            String payStatus;
            String payDateStr;
            if (orderStatus == UserOrderAddress.OrderStatus.INIT) {
                payStatus = "未支付";
                payDateStr = "-";
            } else if (orderStatus == UserOrderAddress.OrderStatus.PAID) {
                payStatus = "已支付";
                payDateStr = DateUtils.dateToString(orderAddress.getPayTime());
            } else {
                payStatus = "已申请退款";
                payDateStr = DateUtils.dateToString(orderAddress.getPayTime());
            }
            map.put("payStatus", payStatus);
            map.put("payDate", payDateStr);
            map.put("consigneeName", address.getConsigneeName());

            map.put("province", province.getName());
            map.put("city", city.getName());
            map.put("county", county.getName());
            map.put("detail", address.getAddressDetail());
            if (decodePhone) {
                map.put("phone", SensitiveLib.decodeMobile(address.getPhoneNumber()));
            } else {
                map.put("phone", address.getPhoneNumber());
            }
            map.put("productId", orderAddress.getProductId());
            map.put("userId", SafeConverter.toString(orderAddress.getParentId()));
            map.put("orderId", addressOrderId);
            map.put("payAmount", orderAddress.getPayAmount());
            map.put("productName", orderProduct.getName());
            mapList.add(map);
        }
        return mapList;
    }


    @RequestMapping(value = "/get_address_phone.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getAddressPhone() {
        String orderId = getRequestString("orderId");
        if (StringUtils.isBlank(orderId)) {
            return MapMessage.errorMessage("订单 id 呢？");
        }
        addAdminLog("查询用户订单收货人手机号", orderId);
        Page<UserOrderAddress> userOrderAddresses = userOrderAddressService.loadUserOrderAddressList(null, orderId, null, null, new PageRequest(0, 10), null);
        List<UserOrderAddress> list = userOrderAddresses.getContent();
        if (CollectionUtils.isEmpty(list)) {
            return MapMessage.successMessage().add("phone", "-");
        } else {
            UserOrderAddress userOrderAddress = list.get(0);
            UserOrderAddress.Address address = userOrderAddress.getAddress();
            if (address == null) {
                return MapMessage.successMessage().add("phone", "-");
            }
            String phoneNumber = address.getPhoneNumber();
            String phone = SensitiveLib.decodeMobile(phoneNumber);
            return MapMessage.successMessage().add("phone", phone);
        }
    }

    private final static List<String> titleList = Arrays.asList("家长 id", "订单 id", "支付状态", "支付金额", "支付时间", "收货人", "联系电话", "省", "市", "区", "详细地址", "商品 id", "商品名称");
    private final static List<String> keyList = Arrays.asList("userId", "orderId", "payStatus", "payAmount", "payDate", "consigneeName", "phone", "province", "city", "county", "detail", "productId", "productName");

    @RequestMapping(value = "/export.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public void export() {
        long userId = getRequestLong("userId");
        String orderId = getRequestString("orderId");
        String startDateStr = getRequestString("startDate");
        String endDateStr = getRequestString("endDate");
        Date startDate = DateUtils.stringToDate(startDateStr, DateUtils.FORMAT_SQL_DATE);
        Date endDate = DateUtils.stringToDate(endDateStr, DateUtils.FORMAT_SQL_DATE);
        List<UserOrderAddress> userOrderAddresses = userOrderAddressService.loadUserOrderAddressListForExport(userId, orderId, startDate, endDate, null);
        List<Map<String, String>> maps = queryMapList(userOrderAddresses, true);

        buildExportCsv("export:" + System.currentTimeMillis() + ".csv", getResponse(), maps, titleList, keyList);
    }

    private void buildExportCsv(String fileName, HttpServletResponse response, List<Map<String, String>> mapList, List<String> titleList, List<String> keyList) {
        String csvString = toCsvString(mapList, titleList, keyList);
        try {
//            response.setContentType("application/x-msdownload");
//            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
//            response.getOutputStream().write(csvString.getBytes("UTF-8"));
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/x-msdownload",
                    csvString.getBytes("GBK"));
        } catch (Exception e) {

        }
    }

    private String toCsvString(List<Map<String, String>> mapList, List<String> titleList, List<String> keys) {
        if (titleList.size() != keys.size()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String titles = String.join(",", titleList);
        sb.append(titles).append("\n");
        for (Map<String, String> map : mapList) {
            for (String key : keys) {
                String value = map.get(key);
                sb.append(value).append(",");
            }
            sb.append("\n");
        }
        return sb.toString();
    }


}
