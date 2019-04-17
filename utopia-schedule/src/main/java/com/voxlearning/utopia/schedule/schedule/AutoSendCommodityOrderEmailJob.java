package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.galaxy.service.mall.api.CommodityBufferLoaderClient;
import com.voxlearning.galaxy.service.mall.api.DPCommodityOrderLoader;
import com.voxlearning.galaxy.service.mall.api.constant.OrderQueryMessage;
import com.voxlearning.galaxy.service.mall.api.constant.OrderStatus;
import com.voxlearning.galaxy.service.mall.api.constant.SendStatus;
import com.voxlearning.galaxy.service.mall.api.entity.Commodity;
import com.voxlearning.galaxy.service.mall.api.entity.CommodityOrder;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/07/26
 */
@Named
@ScheduledJobDefinition(
        jobName = "学习币商城新订单发送邮件",
        jobDescription = "学习币商城新订单发送邮件，每周一上午10点运行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 10 ? * MON"
)
public class AutoSendCommodityOrderEmailJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = DPCommodityOrderLoader.class)
    private DPCommodityOrderLoader dpCommodityOrderLoader;
    @Inject
    private CommodityBufferLoaderClient commodityBufferLoaderClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        WeekRange weekRange;
        if (RuntimeMode.le(Mode.TEST)) {
            weekRange = WeekRange.current();
        } else {
            weekRange = WeekRange.current().previous();
        }
        Date startDate = weekRange.getStartDate();
        Date endDate = weekRange.getEndDate();

        OrderQueryMessage queryMessage = new OrderQueryMessage();
        queryMessage.setStartDate(startDate);
        queryMessage.setEndDate(endDate);
        queryMessage.setOrderStatus(OrderStatus.PAID.name());
        queryMessage.setSendStatus(SendStatus.SEND_PENDING.name());

        Pageable pageable = new PageRequest(0, 1000);
        Page<CommodityOrder> orderPage = dpCommodityOrderLoader.crmLoadOrder(queryMessage, pageable);
        if (orderPage != null && CollectionUtils.isNotEmpty(orderPage.getContent())) {
            List<Commodity> commodityList = commodityBufferLoaderClient.getCommodityList();
            Map<Integer, String> commodityNameMap = commodityList.stream()
                    .collect(Collectors.toMap(Commodity::getId, Commodity::getName));
            List<CommodityOrder> orderList = orderPage.getContent();
            List<Map<String, String>> orders = new ArrayList<>();
            orderList.forEach(order -> orders.add(convert(order, commodityNameMap)));
            Map<String, Object> emailMap = getEmailMap();
            if (MapUtils.isNotEmpty(emailMap)) {
                Map<String, Object> content = new HashMap<>();
                content.put("orderList", orders);
                String fileUrl = generateExcel(orders);
                content.put("fileUrl", fileUrl);
                String to = SafeConverter.toString(emailMap.get("to"));
                String cc = SafeConverter.toString(emailMap.get("cc"));
                if (StringUtils.isNotBlank(to)) {
                    emailServiceClient.createTemplateEmail(EmailTemplate.commodityorder)
                            .to(to)
                            .cc(cc)
                            .subject("学习币商城新订单")
                            .content(content)
                            .send();
                }
            }
        }
    }

    private Map<String, String> convert(CommodityOrder order, Map<Integer, String> commodityNameMap) {
        Map<String, String> map = new HashMap<>();
        map.put("date", DateUtils.dateToString(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        map.put("id", order.getId());
        map.put("commodityName", commodityNameMap.getOrDefault(order.getCommodityId(), ""));
        map.put("coin", SafeConverter.toString(order.getCoin()));
        map.put("status", order.getOrderStatus() == null ? "" : order.getOrderStatus().getDesc());
        return map;
    }

    private Map<String, Object> getEmailMap() {
        String config = commonConfigServiceClient.getCommonConfigBuffer()
                .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), "COMMODITY_ORDER_EMAIL");
        return JsonUtils.fromJson(config);
    }

    private String generateExcel(List<Map<String, String>> orders) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("序号");
        row.createCell(1).setCellValue("订单日期");
        row.createCell(2).setCellValue("订单ID");
        row.createCell(3).setCellValue("商品名称");
        row.createCell(4).setCellValue("学习币数量");
        row.createCell(5).setCellValue("订单状态");
        int rowNum = 1;
        for (Map<String, String> map : orders) {
            row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(map.get("date"));
            row.createCell(2).setCellValue(map.get("id"));
            row.createCell(3).setCellValue(map.get("commodityName"));
            row.createCell(4).setCellValue(map.get("coin"));
            row.createCell(5).setCellValue(map.get("status"));
            rowNum++;
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);

        try {
            @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] content = os.toByteArray();
            @Cleanup InputStream is = new ByteArrayInputStream(content);
            String env = "mall";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "mall/test";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + ".xls";
            String realName = storageClient.upload(is, fileName, path);
            return StringUtils.defaultString(ConfigManager.getInstance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        } catch (IOException e) {
            logger.error("AutoSendCommodityOrderEmailJob generate excel error");
        }
        return "";
    }
}
