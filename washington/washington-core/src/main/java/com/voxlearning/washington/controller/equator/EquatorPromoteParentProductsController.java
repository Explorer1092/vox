package com.voxlearning.washington.controller.equator;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.equator.common.api.constants.EquatorPromoteProductsConstants;
import com.voxlearning.equator.service.rubik.api.client.PromoteParentProductsLoaderClient;
import com.voxlearning.equator.service.rubik.api.client.PromoteParentProductsServiceClient;
import com.voxlearning.equator.service.rubik.api.constant.promoteproducts.PromoteProductsBuyWayType;
import com.voxlearning.equator.service.rubik.api.data.promoteproducts.SingleCourseInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.ParentCrosHeaderSupport;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author fugui.chang
 * @since 2019/2/12.
 */
@Controller
@Slf4j
@NoArgsConstructor
@RequestMapping("/activity/promote/parent/products")
public class EquatorPromoteParentProductsController extends AbstractController {

    @Inject
    private PromoteParentProductsLoaderClient promoteParentProductsLoaderClient;
    @Inject
    private PromoteParentProductsServiceClient promoteParentProductsServiceClient;


    private long fetchEndTime() {
        if (RuntimeMode.current().le(Mode.TEST)) {
            return promoteParentProductsLoaderClient.getRemoteReference().fetchEndTime();
        }
        return EquatorPromoteProductsConstants.fetchEndDate().getTime();
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage index() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());

        long endTime = fetchEndTime();
        if (endTime < System.currentTimeMillis()) {
            return MapMessage.errorMessage().set("currentTime", System.currentTimeMillis()).setErrorCode("100").setInfo("活动已结束");
        }


        long inviter = getRequestLong("inviter", 0);//邀请人
        boolean fromMarket = getRequestBool("fromMarket", false);//是否是来自市场的来源
        long marketId = getRequestLong("marketId", 0);//市场人员的id
        int layer = getRequestInt("layer", 0);//层


        long lastInviter = getRequestLong("lastInviter", 0);//inviter的上家
        long initSource = getRequestLong("initSource", 0);//最初发起人来源

        if (inviter > 0) {
            promoteParentProductsServiceClient.getRemoteReference().incrViewCount(inviter);
        }


        User user = currentUser();
        boolean loginFlag = user != null && user.isParent();

        if (!loginFlag) {
            //二维码
            String qrCodeUrl = "";
            if (fromMarket) {//无登录时市场专员
                layer++;
                qrCodeUrl = "/karp/products_29/index/introduction?clerk_id=" + marketId + "&layer=" + layer;
            }

            //广播信息
            List<String> broadcastMsgs = promoteParentProductsLoaderClient.getRemoteReference().fetchBroadcastMsgs();

            //课程信息
            List<SingleCourseInfo> courseInfo = new ArrayList<>();
            for (EquatorPromoteProductsConstants.ProductType productType : EquatorPromoteProductsConstants.ProductType.values()) {
                SingleCourseInfo singleCourseInfo = new SingleCourseInfo(productType.productName, productType.name());

                List<SingleCourseInfo.CourseProduct> courseProducts = new ArrayList<>();
                for (EquatorPromoteProductsConstants.ProductInfo productInfo : productType.productInfos) {
                    courseProducts.add(new SingleCourseInfo.CourseProduct(productInfo.fetchProductId(), productInfo.getGrade(), false, PromoteProductsBuyWayType.NotBuy.name()));
                }
                singleCourseInfo.setCourseProducts(courseProducts);
                courseInfo.add(singleCourseInfo);
            }

            return MapMessage.successMessage()
                    .set("currentTime", System.currentTimeMillis())
                    .add("loginFlag", false)
                    .add("endTime", endTime)
                    .add("broadcastMsgs", broadcastMsgs)
                    .add("suspendedStatus", 1)
                    .add("parentImg", "")
                    .add("parentName", "")
                    .add("privilegeCount", "0")
                    .add("privilegeTotalCount", EquatorPromoteProductsConstants.product_count)
                    .add("viewCount", 0)
                    .add("buyCount", 0)
                    .add("courseInfo", courseInfo)
                    .add("currentCourseType", EquatorPromoteProductsConstants.ProductType.palacemuseum.name())
                    .add("commonCouponFlag", false)
                    .add("hanaCouponFlag", false)
                    .add("qrCodeUrl", qrCodeUrl)
                    ;
        }

        //家长登录时
        if (!Objects.equals(user.getId(), inviter)) {
            layer++;//当登录家长和上家分享的家长，不是同一个人是，层加1
        }
        MapMessage mapMessage = promoteParentProductsLoaderClient.getRemoteReference().fetchActivityIndex(user.getId(), marketId, layer, inviter, lastInviter, initSource);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }


        mapMessage.set("parentImg", getCdnBaseUrlAvatarWithSep() + SafeConverter.toString(mapMessage.get("parentImg")));
        return mapMessage;
    }

}
