package com.voxlearning.utopia.admin.productpromotion.controller;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.crm.ProductFeedbackController;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageInfo;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageResult;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.input.ProductPromotionExportSmsParams;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionCreateSmsDto;
import com.voxlearning.utopia.admin.productpromotion.service.ProductPromotionSmsService;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * 产品推广管理
 * Created by kaibo.he
 * on 2019/1/29.
 */
@Controller
@RequestMapping("/crm/product_promotion")
public class CrmProductPromotionController extends ProductFeedbackController {
    @Inject
    private ProductPromotionSmsService productPromotionSmsService;

    /**
     * 老师反馈记录页
     *
     * @param model 模板
     * @return 老师反馈记录页
     */
    @RequestMapping(value = "sms_promotion_list.vpage", method = RequestMethod.GET)
    public String teacherProductFeedbackList(Model model) {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        Integer page = getRequestInt("page", 0);
        PageResult result = queryPage(user.getAdminUserName(), page, 10);
        Integer size = Long.valueOf(result.getTotalSize()/result.getSize()).intValue();
        result.setSize((result.getTotalSize()%result.getSize())>0 ? size + 1:size);
        model.addAttribute("totalPages", result.getSize());
        model.addAttribute("page", page);
        model.addAttribute("dataList", result.getData());
        model.addAttribute("beginDay", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("endDay", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
        return "crm/product_promotion/list";
    }

    /**
     * 新建短信
     *
     * @return
     */
    @RequestMapping(value = "create_sms.vpage", method = RequestMethod.POST)
    @ResponseBody
    protected MapMessage createSms(@RequestBody ProductPromotionCreateSmsDto dto) {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        MapMessage msg = MapMessage.successMessage();
        productPromotionSmsService.create(dto, user);
        return msg;
    }

    /**
     * 发送短信
     *
     * @return
     */
    @RequestMapping(value = "send_sms.vpage", method = RequestMethod.POST)
    @ResponseBody
    protected MapMessage sendSms() {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        MapMessage msg = MapMessage.successMessage();
        String id = requestString("id", "");
        productPromotionSmsService.send(id, user);
        return msg;
    }

    /**
     * 分页查询
     *
     * @return
     */
    @RequestMapping(value = "queryPage.vpage", method = RequestMethod.GET)
    @ResponseBody
    protected PageResult queryPage() {
        Integer page = requestInteger("page");
        Integer size = requestInteger("size");
        AuthCurrentAdminUser user = getCurrentAdminUser();
        return queryPage(user.getAdminUserName(), page, size);
    }

    private PageResult queryPage(String operationUserName, Integer page, Integer size) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setSize(size);

        return productPromotionSmsService.queryPage(operationUserName, pageInfo);
    }

    /**
     * 导出短信
     *
     * @return
     */
    @RequestMapping(value = "export.vpage", method = RequestMethod.GET)
    protected void export(HttpServletResponse response) {
        String beginDay = getRequestParameter("beginDay", "");
        String endDay = getRequestParameter("endDay", "");
        ProductPromotionExportSmsParams params = ProductPromotionExportSmsParams.builder().beginDay(beginDay).endDay(endDay).build();
        HSSFWorkbook hssfWorkbook = productPromotionSmsService.export(params);
        String filename = "推广短信" + beginDay + "--" + endDay + ".xls";
        try {
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ignored) {
            try {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e) {
                logger.error("download auditing order exception!");
            }
        }
        return;
    }

}
