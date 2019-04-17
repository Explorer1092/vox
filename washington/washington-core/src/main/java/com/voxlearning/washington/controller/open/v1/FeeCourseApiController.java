package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.FeeCourse;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Ruib
 * @since 2016/12/29
 */
@Controller
@RequestMapping(value = "/v1/feecourse")
@Slf4j
public class FeeCourseApiController extends AbstractApiController {
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;

    // TODO: 2016/12/29 重写
    @RequestMapping(value = "/getfeecoursepayinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getQuestionsByIds() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User curUser = getApiRequestUser();

        // 获取购买的商品
        List<UserActivatedProduct> uaps = userOrderLoaderClient.loadUserActivatedProductList(curUser.getId()).stream()
                .filter(p -> FeeCourse== OrderProductServiceType.safeParse(p.getProductServiceType()))
                .collect(Collectors.toList());

        Set<String> itemIds = uaps.stream()
                .map(UserActivatedProduct::getProductItemId).collect(Collectors.toSet());
        Map<String, OrderProductItem> itemMap = userOrderLoaderClient.loadOrderProductItems(itemIds);

        if (CollectionUtils.isNotEmpty(uaps)) {
            List<Map<String, Object>> pis = new ArrayList<>();
            for (UserActivatedProduct uap : uaps) {
                Map<String, Object> pi = new HashMap<>();
                pi.put("expiredDate", uap.getServiceEndTime());
                pi.put("itemId", uap.getProductItemId());
                if (itemMap.containsKey(uap.getProductItemId())) {
                    pi.put("appItemId", itemMap.get(uap.getProductItemId()).getAppItemId());
                }
                pis.add(pi);
            }
            resultMap.put(RES_PRODUCT_LIST, pis);
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(curUser.getId());
        if (studentDetail.getClazz() != null) {
            ClazzLevel grade = studentDetail.getClazzLevel();
            if (studentDetail.isInfantStudent()) grade = ClazzLevel.FIRST_GRADE;
            Integer region = studentDetail.getStudentSchoolRegionCode();
            List<Map<String, Object>> ubs = new ArrayList<>();
            Set<String> bookIds = new HashSet<>();
            // TODO: 2017/1/3 等谭大爷写完了用他的方法获取默认教材
            bookIds.add(newContentLoaderClient.initializeClazzBook(Subject.ENGLISH, grade, region));
            bookIds.add(newContentLoaderClient.initializeClazzBook(Subject.MATH, grade, region));
            bookIds.add(newContentLoaderClient.initializeClazzBook(Subject.CHINESE, grade, region));
            Map<String, NewBookProfile> books = newContentLoaderClient.loadBooks(bookIds);
            for (NewBookProfile book : books.values()) {
                Map<String, Object> ub = new HashMap<>();
                ub.put("subject", Subject.fromSubjectId(book.getSubjectId()));
                ub.put("bookId", book.getId());
                ub.put("seriesId", book.getSeriesId());
                ubs.add(ub);
            }

            resultMap.put(RES_USER_BOOK, ubs);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}
