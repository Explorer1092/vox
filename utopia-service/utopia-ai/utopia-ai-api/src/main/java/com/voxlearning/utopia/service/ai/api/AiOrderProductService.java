package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.data.ChipsUserCourseMapper;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2018/8/15
 */
@ServiceVersion(version = "20190222")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiOrderProductService {

    /**
     * 获取当前有效的薯条英语产品， beginDate < now && endDate > now
     */
    @Deprecated
    OrderProduct loadCurrentValidProduct();

    /**
     * 获取用户购买的薯条英语的短期课(已经结课，或者正在上课 即 beginDate < now),
     *
     * @return 因为一个用户只能购买一个薯条英语的短期课程，所以返回一个对象
     */
    OrderProduct loadBeginPaidShortProduct(Long userId);

    /**
     * 获取用户购买的薯条英语的短期课(正在上的， 即 beginDate < now && endDate > now)
     */
    OrderProduct loadCurrentValidPaidShortProduct(Long userId);

    MapMessage loadUserCourseInfo(Long userId);

    MapMessage changeUserBookRef(Long userId, String id);

    AIUserLessonBookRef loadUserBookRef(Long userId);

    /**
     * @param typeList "1"-已完结,"2"-当前,"3"-未开始
     * @return 如果typeList 是空 返回所有的
     */
    List<OrderProduct> loadProductByType(List<String> typeList);

    // 获取用户全部课程 包含已过期的
    List<ChipsUserCourseMapper> loadUserAllCourseInfo(Long userId);
}
