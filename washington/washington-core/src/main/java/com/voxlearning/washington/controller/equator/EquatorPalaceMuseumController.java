package com.voxlearning.washington.controller.equator;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.equator.service.rubik.api.client.PalaceMuseumLoaderClient;
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
import java.util.Date;

/**
 * @author fugui.chang
 * @since 2019/1/26.
 * <p>
 * 故宫立春产品运营活动
 */
@Controller
@Slf4j
@NoArgsConstructor
@RequestMapping("/activity/palace/museum/activity")
public class EquatorPalaceMuseumController extends AbstractController {
    @Inject
    private PalaceMuseumLoaderClient palaceMuseumLoaderClient;


    @RequestMapping(value = "index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage index() {
        ParentCrosHeaderSupport.setCrosHeader(getWebRequestContext());
        User parent = currentParent();
        if (parent == null || !parent.isParent()) {
            Date endDate = fetchEndTime();
            if (endDate.getTime() < System.currentTimeMillis()) {
                return MapMessage.errorMessage().set("currentTime", System.currentTimeMillis()).setErrorCode("100").setInfo("活动已结束");
            }

            return MapMessage.successMessage()
                    .set("currentTime", System.currentTimeMillis())
                    .add("loginFlag", false)
                    .add("buyFlag", false)
                    .add("canShareFlag", false)
                    .add("discountsFlag", false)
                    .add("endTime", endDate.getTime())
                    .add("qrCodeUrl", "")
                    .add("productId", "");
        }
        try {
            return palaceMuseumLoaderClient.getRemoteReference().fetchActivityIndex(parent.getId());
        } catch (Exception e) {
            return MapMessage.errorMessage().setErrorCode("100").setInfo("数据异常，请稍后重试");
        }
    }


    private Date fetchEndTime() {
        //测试环境可以动态调整，不走配置
        if (RuntimeMode.current().le(Mode.TEST)) {
            return DateUtils.stringToDate("2019-02-28 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        }

        return DateUtils.stringToDate("2019-02-28 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
    }

}
