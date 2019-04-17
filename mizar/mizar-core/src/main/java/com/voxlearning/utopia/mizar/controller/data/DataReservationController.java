package com.voxlearning.utopia.mizar.controller.data;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.service.reserve.MizarReserveService;
import com.voxlearning.utopia.mizar.utils.XssfUtils;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarRatingLoaderClient;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户管理-预约信息 相关
 * Created by Yuechen.Wang on 2016/9/7.
 */
@Controller
@RequestMapping(value = "/data/reserve")
public class DataReservationController extends AbstractMizarController {

    @Inject private MizarReserveService mizarReserveService;
    @Inject private MizarRatingLoaderClient mizarRatingLoaderClient;
    private static final int RESERVE_PAGE_SIZE = 10;

    // 预约信息查询
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String reserveIndex(Model model) {
        String shopToken = getRequestString("shopToken"); // 选择门店
        String studentName = getRequestString("studentName"); // 客户名称
        String mobile = getRequestString("mobile"); // 联系电话
        String status = getRequestString("status"); // 客户状态
        Integer page = getRequestInt("page", 1); // 当前页
        Date now = new Date();
        Date start = getRequestDate("start", "yyyy-MM-dd", DateUtils.calculateDateDay(now, -3));
        Date end = getRequestDate("end", "yyyy-MM-dd", now);
        model.addAttribute("shopToken", shopToken);
        model.addAttribute("start", DateUtils.dateToString(start, "yyyy-MM-dd"));
        model.addAttribute("end", DateUtils.dateToString(end, "yyyy-MM-dd"));
        model.addAttribute("page", page);

        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("studentName", studentName);
        filterMap.put("mobile", mobile);
        filterMap.put("status", status);
        model.addAllAttributes(filterMap);

        List<MizarShop> shopList = new ArrayList<>();
        // 输入的是shopId
        if (ObjectId.isValid(shopToken)) {
            MizarShop shop = mizarLoaderClient.loadShopById(shopToken);
            if (shop != null) shopList.add(shop);
        } else if (StringUtils.isNotBlank(shopToken)) {
            shopList = mizarRatingLoaderClient.loadShopByName(shopToken, -1);
        }

        // 如果是非运营人员，那么只能看指定的预约信息
        MizarAuthUser user = getCurrentUser();
        if (!user.isOperator()) {
            List<String> userShopIds = user.getShopList();
            shopList = shopList.stream()
                    .filter(p -> userShopIds.contains(p.getId()))
                    .collect(Collectors.toList());

            if (shopList.isEmpty()) {
                shopList = new ArrayList<>(mizarLoaderClient.loadShopByIds(userShopIds).values());
            }
        }

        List<Map<String, Object>> reserveInfo;
        // 如果不根据机构信息搜索，则使用日期
        if (CollectionUtils.isEmpty(shopList)) {
            reserveInfo = mizarReserveService.loadRecentShopReservations(start, end, filterMap);
        } else {
            reserveInfo = mizarReserveService.loadShopReservations(shopList, filterMap);
            reserveInfo = reserveInfo.stream()
                    .filter(t -> {
                        Date d = (Date) t.get("reserveTime");
                        return d != null && d.before(end) && d.after(start);
                    }).collect(Collectors.toList());
        }
        model.addAttribute("reservePage", splitList(reserveInfo, RESERVE_PAGE_SIZE));
        return "data/reserve";
    }

    @RequestMapping(value = "downloadexcel.vpage", method = RequestMethod.POST)
    public void downloadExcel(HttpServletResponse response) {
        // 默认取近30天的
        Date now = new Date();
        Date start = getRequestDate("start", DateUtils.FORMAT_SQL_DATETIME, DateUtils.calculateDateDay(now, -30));
        Date end = getRequestDate("end", DateUtils.FORMAT_SQL_DATETIME, now);
        if (start.after(end)) return;
        try {
            XSSFWorkbook workbook = mizarReserveService.getDownloadExcel(getCurrentUser(), start, end);
            if (workbook == null) {
                logger.error("Mizar导出预约数据失败!");
                return;
            }
            String filename = XssfUtils.generateFilename("导流预约信息导出", DateUtils.dateToString(start, "yyyyMMdd"), DateUtils.dateToString(end, "yyyyMMdd"));
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            logger.error("Mizar导出预约数据失败!", ex);
        }
    }

}
