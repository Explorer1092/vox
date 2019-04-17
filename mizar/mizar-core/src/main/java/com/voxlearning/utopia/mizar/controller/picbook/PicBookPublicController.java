package com.voxlearning.utopia.mizar.controller.picbook;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.afenti.api.ReadingBookSummaryLoader;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookOrderStat;
import com.voxlearning.utopia.service.afenti.api.entity.ReadingBookSummary;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;
import static com.voxlearning.alps.core.util.MapUtils.m;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;

/**
 * 绘本公共Controller
 */
@Controller
@RequestMapping({"picbook_ps/", "picbook"})
public class PicBookPublicController extends AbstractMizarController {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private UserOrderLoaderClient usrOrderLoaderCli;
    @Inject private PictureBookLoaderClient picBookLoaderCli;

    @ImportService(interfaceClass = ReadingBookSummaryLoader.class)
    private ReadingBookSummaryLoader readingBookSummaryLoader;

    private static final Map<String, String> SERIES_MAP = new HashMap<>();
    private static final Map<String, Integer> PRICE_MAP = new HashMap<>();

    static {
        SERIES_MAP.put("zhss", "China Kids English");
        SERIES_MAP.put("hds", "BEC");
        SERIES_MAP.put("wk", "Moon Kkang");
        SERIES_MAP.put("efuture", "e-future Classic Readers");
        SERIES_MAP.put("wk", "Moon Kkang");
        SERIES_MAP.put("jq", "Cambridge Reading Adventure");
        SERIES_MAP.put("farfaria", "Farfaria");
        SERIES_MAP.put("csie", "Cultural Stories in English");

        PRICE_MAP.put("zhss", 8);
        PRICE_MAP.put("hds", 11);
        PRICE_MAP.put("wk", 15);
        PRICE_MAP.put("efuture", 15);
        PRICE_MAP.put("jq", 20);
        PRICE_MAP.put("farfaria", 20);
        PRICE_MAP.put("csie", 8);
    }

    /**
     * 通用版本根据绘本系列查询订单接口
     *
     * @param model
     * @param seriesShort
     * @return
     */
    @RequestMapping("/{seriesShort}/order/stat.vpage")
    public String picBookOrder(Model model, @PathVariable String seriesShort) {
        try {
            Date startDate = getRequestDate("startDate");
            Date endDate = getRequestDate("endDate");

            Validate.notEmpty(seriesShort, "系列不能为空!");
            Validate.isTrue(SERIES_MAP.containsKey(seriesShort), "未查询到配置信息!");
            String series = SERIES_MAP.get(seriesShort);

            Validate.isTrue(PRICE_MAP.containsKey(seriesShort), "未查询到价格!");
            Integer price = PRICE_MAP.get(seriesShort);
            // 如果不选就是默认查今天
            DayRange todayRange = DayRange.current();
            startDate = Optional.ofNullable(startDate).orElse(todayRange.getStartDate());
            endDate = Optional.ofNullable(endDate).orElse(todayRange.getEndDate());

            model.addAttribute("startTime", dateToString(startDate, FORMAT_SQL_DATE));
            model.addAttribute("endTime", dateToString(endDate, FORMAT_SQL_DATE));

            List<PicBookOrderStat> orderStatList = readingBookSummaryLoader.loadPicBookOrderStat(series, startDate, endDate);
            long orderCount = orderStatList
                    .stream()
                    .mapToLong(PicBookOrderStat::getOrderCount)
                    .sum();
            long orderRefundCount = orderStatList
                    .stream()
                    .filter(e -> e.getRefundOrderCount() != null)
                    .mapToLong(PicBookOrderStat::getRefundOrderCount)
                    .sum();

            model.addAttribute("leftMenu", seriesShort + "OrderStat");
            model.addAttribute("series", seriesShort);
            model.addAttribute("orderCount", orderCount);
            model.addAttribute("totalMoney", orderCount * price);
            model.addAttribute("orderRefundCount", orderRefundCount);
            model.addAttribute("totalRefundMoney", orderRefundCount * price);
            return "order/picbook_order";
        } catch (Exception e) {
            return "order/picbook_order";
        }
    }

    /**
     * 培生绘本订单查询
     *
     * @return
     */
    @RequestMapping("/order/stat.vpage")
    public String pearsonPicBookOrder(Model model) {
        try {
            Date startDate = getRequestDate("startDate");
            Date endDate = getRequestDate("endDate");

            String series = requestString("series", "Longman eReading");
            Validate.notEmpty(series, "系列不能为空!");

            // 如果不选就是默认查今天
            DayRange todayRange = DayRange.current();
            startDate = Optional.ofNullable(startDate).orElse(todayRange.getStartDate());
            endDate = Optional.ofNullable(endDate).orElse(todayRange.getEndDate());

            model.addAttribute("startTime", dateToString(startDate, FORMAT_SQL_DATE));
            model.addAttribute("endTime", dateToString(endDate, FORMAT_SQL_DATE));

            List<PicBookOrderStat> orderStatList = readingBookSummaryLoader.loadPicBookOrderStat(series, startDate, endDate);
            List<String> bookIds = orderStatList.stream().map(PicBookOrderStat::getBookId).collect(Collectors.toList());
            Map<String, List<OrderProduct>> productMap = usrOrderLoaderCli.loadOrderProductByAppItemIds(bookIds);

            AtomicLong newOrderCount = new AtomicLong(0);
            AtomicLong oldOrderCount = new AtomicLong(0);
            AtomicLong newOrderRefundCount = new AtomicLong(0);
            AtomicLong oldOrderRefundCount = new AtomicLong(0);
            double totalMoney = orderStatList.stream()
                    .mapToDouble(ost -> {
                        String bookId = ost.getBookId();
                        double price = Optional.ofNullable(productMap.get(bookId))
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(p -> p.getPrice().doubleValue())
                                .findFirst()
                                .orElse(0d);

                        if (bookId.startsWith("PBP"))
                            newOrderCount.addAndGet(ost.getOrderCount());
                        else if (bookId.startsWith("PB"))
                            oldOrderCount.addAndGet(ost.getOrderCount());

                        return price * ost.getOrderCount();
                    })
                    .sum();

            double totalRefundMoney = orderStatList.stream()
                    .mapToDouble(ost -> {
                        String bookId = ost.getBookId();
                        double price = Optional.ofNullable(productMap.get(bookId))
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(p -> p.getPrice().doubleValue())
                                .findFirst()
                                .orElse(0d);

                        if (bookId.startsWith("PBP"))
                            newOrderRefundCount.addAndGet(ost.getRefundOrderCount() == null ? 0 : ost.getRefundOrderCount());
                        else if (bookId.startsWith("PB"))
                            oldOrderRefundCount.addAndGet(ost.getRefundOrderCount() == null ? 0 : ost.getRefundOrderCount());

                        return price * (ost.getRefundOrderCount() == null ? 0 : ost.getRefundOrderCount());
                    })
                    .sum();

            model.addAttribute("newOrderCount", newOrderCount.get());
            model.addAttribute("oldOrderCount", oldOrderCount.get());
            model.addAttribute("totalMoney", totalMoney);
            model.addAttribute("newOrderRefundCount", newOrderRefundCount.get());
            model.addAttribute("oldOrderRefundCount", oldOrderRefundCount.get());
            model.addAttribute("totalRefundMoney", totalRefundMoney);

            return "order/pearson_order";
        } catch (Exception e) {
            return "order/pearson_order";
        }
    }

    @RequestMapping("/reading/stat.vpage")
    public String pearsonPicBookReading(Model model) {
        String viewResult = "order/pearson_reading";

        try {
            Date startDate = getRequestDate("startDate");
            Date endDate = getRequestDate("endDate");

            int page = getRequestInt("pageIndex");
            page = Math.max(1, page); // 最小从1开始

            String series = requestString("series", "Longman eReading");
            Validate.notEmpty(series, "系列为空!");

            // 搜索指定的绘本ID
            String searchBookName = requestString("bookName");
            model.addAttribute("queryBookName", searchBookName);

            // 如果不选就是默认查今天
            DayRange todayRange = DayRange.current();
            startDate = Optional.ofNullable(startDate).orElse(todayRange.getStartDate());
            endDate = Optional.ofNullable(endDate).orElse(todayRange.getEndDate());

            // 查询旧绘本的起始日期要提前一天，因为要算差值
            Date oldStartDate = DateUtils.addDays(startDate, -1);
            String oldStartDateStr = dateToString(oldStartDate, DATE_FORMAT);

            // 传回前端展示
            model.addAttribute("startDate", dateToString(startDate, FORMAT_SQL_DATE));
            model.addAttribute("endDate", dateToString(endDate, FORMAT_SQL_DATE));

            List<Map<String, Object>> resultMapper = new ArrayList<>();
            // 为了迁就旧绘本的恶心规则，要多查一天
            List<ReadingBookSummary> allRBS = readingBookSummaryLoader.loadReadingBookSummaries(series, oldStartDate, endDate);

            // 汇总新绘本的时候，要滤掉多出来的那一天
            Map<String, Long> newBookQuantityMap = allRBS.stream()
                    .filter(b -> b.getBookId().startsWith("PBP_"))
                    .filter(b -> !oldStartDateStr.equals(b.getUsageDate()))
                    .collect(groupingBy(rbs -> rbs.getBookId(), summingLong(rbs -> rbs.getAppCount())));

            Map<String, List<ReadingBookSummary>> oldRBSMap = allRBS.stream()
                    .filter(b -> b.getBookId().startsWith("PB_"))
                    .collect(Collectors.groupingBy(rbs -> rbs.getBookId()));

            Function<String, Date> formatFunc = d -> DateUtils.stringToDate(d, DATE_FORMAT);
            Map<String, Long> oldBookQuantityMap = MapUtils.transform(oldRBSMap, RBSList -> {
                List<ReadingBookSummary> sortedRBSList = RBSList.stream()
                        .sorted(Comparator.comparing(r -> formatFunc.apply(r.getUsageDate())))
                        .collect(Collectors.toList());

                // 如果只有一条的话，没有对照，没办法求出来增量
                if (CollectionUtils.isEmpty(sortedRBSList) || sortedRBSList.size() <= 1)
                    return 0L;

                long beginAmount = sortedRBSList.get(0).getAppCount();
                long lastAmount = sortedRBSList.get(sortedRBSList.size() - 1).getAppCount();
                return lastAmount - beginAmount;
            });

            // 新旧汇总
            Map<String, Long> quantityMap = new HashMap<>();
            quantityMap.putAll(oldBookQuantityMap);
            quantityMap.putAll(newBookQuantityMap);

            // 查询出来旧的绘本
            Set<String> bookIds = oldBookQuantityMap.keySet();
            Map<String, PictureBook> oldBookMap = picBookLoaderCli.loadPictureBookByDocIds(bookIds);
            // 从buffer中拿新绘本
            Map<String, PictureBookPlus> newBookMap = pictureBookPlusServiceClient.toMap();

            quantityMap.forEach((bookId, num) -> {
                if (num == null || num <= 0)
                    return;

                String name = "";
                if (bookId.startsWith("PB_")) {
                    name = Optional.ofNullable(oldBookMap.get(bookId)).map(b -> b.getName()).orElse("");
                } else if (bookId.startsWith("PBP_")) {
                    name = Optional.ofNullable(newBookMap.get(bookId)).map(b -> b.getEname()).orElse("");
                }

                // 名字的筛选项
                if (StringUtils.isNotEmpty(searchBookName) && !searchBookName.equals(name))
                    return;

                resultMapper.add(m("bookId", bookId, "quantity", num, "name", name));
            });

            // 按照销量倒序排一下
            Function<Map<String, Object>, Long> quantityFunc = mapper -> SafeConverter.toLong(mapper.get("quantity"));
            resultMapper.sort((r1, r2) -> Long.compare(quantityFunc.apply(r2), quantityFunc.apply(r1)));

            Pageable pageRequest = PageableUtils.startFromOne(page, 20);
            Page<Map<String, Object>> pageResult = PageableUtils.listToPage(resultMapper, pageRequest);

            model.addAttribute("records", pageResult.getContent());
            model.addAttribute("totalPages", pageResult.getTotalPages());
            model.addAttribute("pageIndex", page);

            return viewResult;
        } catch (Exception e) {
            model.addAttribute("errorInfo", e.getMessage());
            return viewResult;
        }
    }

}
