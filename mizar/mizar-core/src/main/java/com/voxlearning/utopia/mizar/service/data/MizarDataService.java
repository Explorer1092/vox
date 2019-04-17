package com.voxlearning.utopia.mizar.service.data;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.bean.ShopDataMapper;
import com.voxlearning.utopia.mizar.athena.ShopDataServiceClient;
import com.voxlearning.utopia.mizar.service.AbstractMizarService;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import org.springframework.ui.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.*;

/**
 * 大数据报表相关Service.
 * <p>
 * Created by Yuechen.Wang on 16-9-19.
 */
@Named
public class MizarDataService extends AbstractMizarService {

    @Inject private ShopDataServiceClient shopDataServiceClient;

    private static final String ReturnResult = "shopDataQuery";

    @SuppressWarnings("unchecked")
    public MapMessage queryShopData(List<String> shopList, Integer start, Integer end) {
        if (CollectionUtils.isEmpty(shopList) || start == null || end == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            Map<String, MizarShop> shopMap = mizarLoaderClient.loadShopByIds(shopList);
            MapMessage message = shopDataServiceClient.getShopDataService().shopDataQuery(shopList, start, end);
            if (message.isSuccess()) {
                List<ShopDataMapper> result = (List<ShopDataMapper>) message.get(ReturnResult);
                List<Map<String, Object>> shopData = mapShopData(result, shopMap);
                return MapMessage.successMessage().add("result", shopData);
            }
            return message;
        } catch (Exception ex) {
            logger.error("Query Shop Data Failed: shop:{}, start:{}, end:{}", Arrays.toString(shopList.toArray()), start, end, start);
            return MapMessage.errorMessage("数据查询失败：" + ex.getMessage());
        }
    }

    public Integer parseDateParam(String dateName, String dateStr, int offset, Model model) {
        Integer dateVal;
        if (StringUtils.isBlank(dateStr)) {
            Date sDate = calculateDateDay(new Date(), offset);
            dateVal = Integer.valueOf(dateToString(sDate, "yyyyMMdd"));
            model.addAttribute(dateName, dateToString(sDate, FORMAT_SQL_DATE));
        } else {
            dateVal = Integer.valueOf(dateStr.replaceAll("-", ""));
            model.addAttribute(dateName, dateStr);
        }
        return dateVal;
    }

    private List<Map<String, Object>> mapShopData(List<ShopDataMapper> result, Map<String, MizarShop> shopMap) {
        if (MapUtils.isEmpty(shopMap)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(result)) {
            return shopMap.values().stream().map(MizarShop::simpleInfo).collect(Collectors.toList());
        }
        Map<String, List<ShopDataMapper>> shopData = result.stream().collect(Collectors.groupingBy(ShopDataMapper::getShopId));
        // 预约数据
        Map<String, Integer> shopReservation = mizarLoaderClient.loadShopReservations(shopMap.keySet()).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
        List<Map<String, Object>> dataList = new ArrayList<>();
        shopMap.entrySet().forEach(t -> {
            Map<String, Object> info = new HashMap<>();
            String shopId = t.getKey();
            MizarShop shop = t.getValue();
            info.putAll(shop.simpleInfo());
            List<ShopDataMapper> data = shopData.get(shopId);
            if (CollectionUtils.isNotEmpty(data)) {
                Optional<Long> totalUv = data.stream().map(uv -> SafeConverter.toLong(uv.getShowUv())).reduce((sum, i) -> sum + i);
                info.put("showUv", totalUv.isPresent() ? totalUv.get() : 0L);
                Optional<Long> totalPv = data.stream().map(pv -> SafeConverter.toLong(pv.getShowPv())).reduce((sum, i) -> sum + i);
                info.put("showPv", totalPv.isPresent() ? totalPv.get() : 0L);
            }
            // 计算预约量
            info.put("reserveCnt", SafeConverter.toInt(shopReservation.get(shopId)));
            dataList.add(info);
        });
        return dataList;
    }
}
