package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCard;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityCardOrder;
import com.voxlearning.utopia.service.campaign.api.mapper.ExchangeFeedMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceVersion(version = "20181210")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface TeacherActivityCardService {

    // 首页接口
    MapMessage index(Long teacherId);

    // 查询兑换记录
    List<ExchangeFeedMapper> loadExchangeFeed();

    // 添加机会
    MapMessage addOpportunity(Long teacherId, String opportunityReason);

    // 兑换
    MapMessage exchange(Long teacherId, String type);

    // 合成
    MapMessage compose(Long teacherId, String cardA, String cardB);

    // 翻牌
    MapMessage turnOverCard(Long teacherId);

    // 签到
    MapMessage sign(Long teacherId);


    // 清理用户数据
    MapMessage clearUserData(Long teacherId);

    // 清理数据
    MapMessage clearData();

    // 控制下一个翻牌为“大”
    MapMessage ctrlNextCard();

    // 每张卡牌的发放情况
    MapMessage statisticsData();

    // 添加库存
    MapMessage setStock(String type, Integer stock);

    MapMessage testDraw(Long teacherId);

    // 给测试添加机会用的
    MapMessage setOpportunity(Long teacherId, Long count);

    List<TeacherActivityCard> loadCard(Long startId, Integer size);

    List<TeacherActivityCardOrder> loadCardOrder();

    MapMessage deleteCardOrder(String id);
}
