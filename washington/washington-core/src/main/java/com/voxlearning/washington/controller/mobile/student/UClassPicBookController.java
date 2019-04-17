package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.afenti.client.UserPicBookServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_GENERAL_ERROR;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.bouncycastle.crypto.agreement.jpake.JPAKEUtil.validateNotNull;

/**
 * Controller of 英文绘本
 */
@Controller
@RequestMapping("/v2/uclassPicBook/levelreading")
public class UClassPicBookController extends AbstractController {

    private static final String APP_KEY = "ELevelReading";

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private UserPicBookServiceClient userPicBookServiceClient;

    public static final List<String> SERIES = Arrays.asList("PBS_10300000010606","PBS_10300000079028","PBS_10300000076294");

    @RequestMapping(value = "/check_book_status.vpage")
    @ResponseBody
    public MapMessage checkBookStatus() {
        try {
            Long userId = currentUserId();
            validateNotNull(userId, "非法的请求!");

            String bookIdsStr = getRequestString("bookIds");
            Validate.notBlank(bookIdsStr, "绘本id不存在!");
            MapMessage resultMsg = MapMessage.successMessage();
            List<String> bookIds = Arrays.stream(bookIdsStr.split(",")).collect(toList());

            List<PictureBookPlus> pictureBookPluses = userPicBookServiceClient.loadPicBooks(bookIds, pictureBookPlusServiceClient);
            Validate.noNullElements(pictureBookPluses, "该绘本已经下线!");
            //查询所有的绘本商品
            Map<String, List<OrderProduct>> stringListMap = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds);

            // 不能重复购买
            AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(APP_KEY, userId, true);
            for (PictureBookPlus picBook : pictureBookPluses) {
                resultMsg.add("bookId",picBook.getId());
                //uClassResult.setBook(picBook);  暂时不返回绘本信息
                if (payMapper.containsAppItemId(picBook.getId())) {
                    resultMsg.add("isBuy",true);
                }else{
                    resultMsg.add("isBuy",false);
                }
                if (stringListMap.containsKey(picBook.getId())) {
                    // 获得绘本的价格
                    double price = Optional.ofNullable(stringListMap.get(picBook.getId()))
                            .orElse(emptyList())
                            .stream()
                            // 保留两位小数，向上约起
                            .map(op -> op.getPrice().setScale(2, RoundingMode.UP))
                            .map(BigDecimal::doubleValue)
                            .findFirst()
                            .orElse(0d);
                    String productId = Optional.ofNullable(stringListMap.get(picBook.getId()))
                            .orElse(emptyList())
                            .stream()
                            .map(op -> op.getId())
                            .findFirst()
                            .orElse("");
                    resultMsg.add("price",BigDecimal.valueOf(price));
                    resultMsg.add("productId",productId);
                    resultMsg.add("puductId",productId);
                }
            }
            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    @RequestMapping(value = "/find_books.vpage")
    @ResponseBody
    public MapMessage findBooks() {
        try {
            Long userId = currentUserId();
            validateNotNull(userId, "非法的请求!");
            MapMessage resultMsg = MapMessage.successMessage();

            // 不能重复购买
            AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(APP_KEY, userId, true);

            //判断是否存在缓存
            UtopiaCache cache = AfentiCache.getPersistent();
            CacheObject<Object> objectCacheObject = cache.get(generateUserCacheKey(userId));
            if (objectCacheObject != null && objectCacheObject.getValue() != null){//缓存存在
                List<Map<String,Object>> resultList = (List<Map<String, Object>>) objectCacheObject.getValue();
                resultList.stream().forEach(item ->{
                    if (payMapper.containsAppItemId((String) item.get("bookId")))
                        item.put("isBuy",true);
                });
                resultMsg.add("books",resultList);
            }else {
                List<Map<String,Object>> resultList = new ArrayList<>();
                //获取学科
                OrderProductServiceType type = OrderProductServiceType.safeParse(APP_KEY);
                Subject subject = userPicBookServiceClient.loadTypeSubject(type);

                // 过滤付费绘本的条件
                Predicate<PictureBookPlus> filterChargedPd = pb -> {
                    Integer freeFlag = pb.getFreeMap().getOrDefault(ApplyToType.SELF, 0);
                    return freeFlag == 2;
                };
                //根据系列过滤
                Predicate<PictureBookPlus> filterSeriesPd = pb -> {
                    boolean boo = SERIES.contains(pb.getSeriesId());
                    return boo;
                };

                // 查询所有的英语绘本  并过滤英语绘本 本人付费的绘本 以及付费绘本 guol
                List<PictureBookPlus> picBooksStream = userPicBookServiceClient.loadSelfPicBooks(pictureBookPlusServiceClient)
                        .stream()
                        .filter(pb -> Objects.equals(subject.getId(), pb.getSubjectId()))
                        .filter(filterChargedPd)
                        .filter(filterSeriesPd)
                        .filter(pb -> !payMapper.containsAppItemId(pb.getId()))
                        .collect(Collectors.toList());


                List<String> chargeBookIds = picBooksStream.stream().map(pb -> pb.getId()).collect(toList());
                // 获得绘本的付费配置信息
                Map<String, List<OrderProduct>> productMap = userOrderLoaderClient.loadOrderProductByAppItemIds(chargeBookIds);
                Function<String, Boolean> hadPayInfoFunc = bookId -> productMap.getOrDefault(bookId, emptyList()).size() > 0;

                // 在这里面就过滤掉没有付费配置信息的绘本，以防后面推荐的地方出现空位
                List<PictureBookPlus> chargePicBooks = picBooksStream.stream()
                        .filter(b -> hadPayInfoFunc.apply(b.getId()))
                        .collect(toList());

                //随机出两本
                PictureBookPlus[] tmpBookArrays = new PictureBookPlus[2];
                int pickNum = Math.min(chargePicBooks.size(), 2);
                RandomUtils.randomPickFew(chargePicBooks, pickNum, tmpBookArrays);
                chargePicBooks = Arrays.asList(tmpBookArrays);

                //整理数据
                chargePicBooks.stream().forEach( pb ->{
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("bookId",pb.getId());
                    map.put("coverThumbnailUrl",pb.getCoverThumbnailUrl());
                    map.put("productId",productMap.get(pb.getId()).get(0)!=null?productMap.get(pb.getId()).get(0).getId():"0");
                    map.put("ename",pb.getEname());
                    map.put("isBuy",false);
                    // 获得绘本的价格
                    Double price = Optional.ofNullable(productMap.get(pb.getId()))
                            .orElse(emptyList())
                            .stream()
                            // 保留两位小数，向上约起
                            .map(op -> op.getPrice().setScale(2, RoundingMode.UP))
                            .map(BigDecimal::doubleValue)
                            .findFirst()
                            .orElse(0d);
                    map.put("price",price);
                    resultList.add(map);
                });

                //放到缓存中
                cache.set(generateUserCacheKey(userId), getRemainSecondsOneDay(new Date()), resultList);
                resultMsg.add("books",resultList);
            }

            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    //生成缓存key
    private String generateUserCacheKey(Long userId) {
        return CacheKeyGenerator.generateCacheKey(
                "UserPictureBook:userRecommendEnglishBook",
                new String[]{"userId"},
                new Object[]{userId});
    }

    //获取当天结束秒数
    public static Integer getRemainSecondsOneDay(Date currentDate) {
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }

}
