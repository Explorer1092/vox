/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.wonderland;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.ParentCrosHeaderSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author Ruib
 * @since 2017/3/28
 */
@Controller
@RequestMapping("/wonderland/order")
public class WonderlandOrderController extends AbstractController {
    @Inject
    private ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;
    @ImportService(interfaceClass = PicListenCommonService.class)
    private PicListenCommonService picListenCommonService;

    // 通过appKey获取应用的产品列表
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "appproducts.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage fetchSpecificAppProducts() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());
        StudentDetail student = fetchStudent();
        if (student == null) return MapMessage.errorMessage("请先登录");

        int term = SchoolYear.newInstance().currentTerm().getKey();

        OrderProductServiceType type = OrderProductServiceType.safeParse(getRequestString("appKey"));
        if (null == type || type == OrderProductServiceType.Unknown) return MapMessage.errorMessage("数据异常，重试一下吧");

        VendorApps va = vendorLoaderClient.loadVendor(type.name());
        if (null == va || !va.isVisible(RuntimeMode.current().getLevel())) return MapMessage.errorMessage("数据异常，重试一下吧");

        List<OrderProduct> pis;
        if (type == OrderProductServiceType.ValueAddedLiveTimesCard || type == OrderProductServiceType.GrowingWorldProp) {
            pis = userOrderLoaderClient.loadItemBaseProductsByType(type);
        } else {
            pis = userOrderLoaderClient.loadAvailableProduct()
                    .stream()
                    .filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == type)
                    .filter(p -> p.getSalesType() == OrderProductSalesType.TIME_BASED)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> products = new ArrayList<>();
        for (OrderProduct pi : pis) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", pi.getId());
            product.put("name", pi.getName());
            product.put("desc", pi.getDesc()); // 标题
            product.put("price", pi.getPrice());
            product.put("originalPrice", pi.getOriginalPrice());
            product.put("type", pi.getProductType());
            product.put("attributes", pi.getAttributes());
            OrderProductItem item = userOrderLoaderClient.loadProductItemsByProductId(pi.getId()).stream().findFirst().orElse(null);
            if (item == null) continue;
            product.put("period", item.getPeriod());
            product.put("rewards", new ArrayList<>());
            products.add(product);
        }

        //如果是阿分提英语、语文、数学要显示提高版信息
        List<Map<String, Object>> improvedProducts = new ArrayList<>();
        List<OrderProduct> improvedOrderProducts = new ArrayList<>();
        Subject subject = null;
        if (type == AfentiChinese) {
            improvedOrderProducts = userOrderLoaderClient.loadAvailableProduct()
                    .stream()
                    .filter(product -> OrderProductServiceType.safeParse(product.getProductType()) == AfentiChineseImproved)
                    .collect(Collectors.toList());
            subject = Subject.CHINESE;
        } else if (type == AfentiMath) {
            improvedOrderProducts = userOrderLoaderClient.loadAvailableProduct()
                    .stream()
                    .filter(product -> OrderProductServiceType.safeParse(product.getProductType()) == AfentiMathImproved)
                    .collect(Collectors.toList());
            subject = Subject.MATH;
        } else if (type == AfentiExam) {
            improvedOrderProducts = userOrderLoaderClient.loadAvailableProduct()
                    .stream()
                    .filter(product -> OrderProductServiceType.safeParse(product.getProductType()) == AfentiExamImproved)
                    .collect(Collectors.toList());
            subject = Subject.ENGLISH;
        }
        for (OrderProduct product : improvedOrderProducts) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", product.getId());
            info.put("name", product.getName());
            info.put("desc", product.getDesc());
            info.put("price", product.getPrice());
            info.put("originalPrice", product.getOriginalPrice());
            info.put("type", product.getProductType());
            OrderProductItem item = userOrderLoaderClient.loadProductItemsByProductId(product.getId()).stream().findFirst().orElse(null);
            if (item == null) continue;
            info.put("period", item.getPeriod());
            Map<String, Object> attr = JsonUtils.fromJson(product.getAttributes());
            if (MapUtils.isNotEmpty(attr) && attr.containsKey("startDate")) {
                Date startDate = DateUtils.stringToDate(attr.get("startDate").toString(), "yyyy-MM-dd HH:mm:ss");
                if (null != startDate) {
                    attr.put("startDate", DateUtils.dateToString(startDate, "yyyy.MM.dd"));
                }
            }
            if (MapUtils.isNotEmpty(attr) && attr.containsKey("endDate")) {
                Date endDate = DateUtils.stringToDate(attr.get("endDate").toString(), "yyyy-MM-dd HH:mm:ss");
                if (null != endDate) {
                    attr.put("endDate", DateUtils.dateToString(endDate, "yyyy.MM.dd"));
                }
            }
            info.put("attributes", attr);

            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
            if (CollectionUtils.isEmpty(orderProductItems)) {
                continue;
            }
            AppPayMapper userAppPaidStatus = userOrderLoader.getUserAppPaidStatus(product.getProductType(), student.getId(), true);
            if (null != userAppPaidStatus && CollectionUtils.isNotEmpty(userAppPaidStatus.getValidProducts()) && userAppPaidStatus.getValidProducts().contains(product.getId())) {
                info.put("paid", true);
            } else {
                info.put("paid", false);
            }

            improvedProducts.add(info);
        }
        boolean piclistenPaid = false;
        String piclistenBookName = "";
        Double piclistenBookPrice = 0.0;
        if (CollectionUtils.isNotEmpty(improvedProducts) && null != subject) {
            String client_type = getRequestString("client_type");
            Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(currentParent());
            List<String> studentDefaultSubjectBook = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(student, client_type, parentAuth, null, subject);
            if (CollectionUtils.isNotEmpty(studentDefaultSubjectBook)) {
                //拿到默认教材的bookId,去查对应的点读机是否买过
                String bookId = studentDefaultSubjectBook.get(0);
                Map<String, List<OrderProduct>> ops = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singleton(bookId));
                if (MapUtils.isNotEmpty(ops)) {
                    List<OrderProduct> orderProducts = ops.get(bookId);
                    if (CollectionUtils.isNotEmpty(orderProducts)) {
                        for (OrderProduct p : orderProducts) {
                            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(p.getId());
                            if (CollectionUtils.isEmpty(orderProductItems)) {
                                continue;
                            }

                            OrderProductItem orderProductItem = orderProductItems.stream().filter(item -> item.getAppItemId().equals(bookId)).findFirst().orElse(null);
                            if (null == orderProductItem || OrderProductServiceType.safeParse(orderProductItem.getProductType()) != PicListenBook) {
                                continue;
                            }

                            AppPayMapper paidStatus = userOrderLoader.getUserAppPaidStatus(OrderProductServiceType.PicListenBook.name(), currentUserId(), true);
                            if (paidStatus != null && CollectionUtils.isNotEmpty(paidStatus.getValidItems()) && paidStatus.getValidItems().contains(orderProductItem.getId())) {
                                piclistenPaid = true;
                                if (paidStatus.getValidProducts().contains(p.getId())) {
                                    piclistenBookName = orderProductItem.getName();
                                    piclistenBookPrice = orderProductItem.getOriginalPrice().doubleValue();
                                }
                            }

                            //如果没购买过，优先返回非打包产品
                            if (!piclistenPaid) {
                                if (StringUtils.isBlank(piclistenBookName)) {
                                    piclistenBookName = orderProductItem.getName();
                                    piclistenBookPrice = orderProductItem.getOriginalPrice().doubleValue();
                                } else if (orderProductItems.size() == 1) {
                                    piclistenBookName = orderProductItem.getName();
                                    piclistenBookPrice = orderProductItem.getOriginalPrice().doubleValue();
                                }
                            }
                        }
                    }
                }
            }
        }

        return MapMessage.successMessage().add("count", businessVendorServiceClient.fetchUserUseNumDesc(
                Collections.singletonList(va.getAppKey()), student).getOrDefault(va.getAppKey(), ""))
                .add("products", products).add("name", va.getCname()).add("desc", va.getDescription())
                .add("descImg", va.getAppmIcon()).add("term", term)
                .add("improvedProducts", improvedProducts)
                .add("picListenPaid", piclistenPaid)
                .add("picListenBookName", piclistenBookName)
                .add("picListenBookPrice", piclistenBookPrice)
                .add("schoolPayMember", 0);
    }

    // private method
    private StudentDetail fetchStudent() {
        User user = currentUser();
        if (user == null || (!user.isParent() && !user.isStudent())) return null;

        StudentDetail student;
        if (user.isParent()) {
            Long studentId = getRequestLong("sid");
            if (studentId == 0L) studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"), 0L);
            if (studentId == 0L) return null;
            student = studentLoaderClient.loadStudentDetail(studentId);
        } else {
            student = user instanceof StudentDetail ? (StudentDetail) user : currentStudentDetail();
        }
        return student;
    }
}
