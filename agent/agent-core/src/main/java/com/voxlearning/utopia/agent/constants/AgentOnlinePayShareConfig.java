package com.voxlearning.utopia.agent.constants;

import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.crm.api.constants.ProductType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
/**
 * 定义付费分成比例的文件
 * Created by Alex on 15-2-11.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentOnlinePayShareConfig {
    // ================================================================================================================================================
    // 付费率0-2%（含2%）  50%      付费率2%-5%（含5%）  60%   付费率5%以上  70%
    AFENTI_LEVEL1(AfentiExam,     -1d,    2d,     50d),
    AFENTI_LEVEL2(AfentiExam,     2d,     5d,     60d),
    AFENTI_LEVEL3(AfentiExam,     5d,     100d,   70d),

    // ================================================================================================================================================
    // 沃克大冒险
    // 付费率0-2%（含2%）  50%      付费率2%-5%（含5%）  60%   付费率5%以上  70%
    WALKER_LEVEL1(Walker,    -1d,    2d,     50d),
    WALKER_LEVEL2(Walker,     2d,    5d,     60d),
    WALKER_LEVEL3(Walker,     5d,    100d,   70d),

    // ================================================================================================================================================
    // Picaro
    // 付费率0-2%（含2%）  40%      付费率2%-5%（含5%）  50%   付费率5%以上  60%
    PICARO_LEVEL1(KaplanPicaro,    -1d,    2d,     40d),
    PICARO_LEVEL2(KaplanPicaro,     2d,    5d,     50d),
    PICARO_LEVEL3(KaplanPicaro,     5d,    100d,   60d),

    // ================================================================================================================================================
    // 爱儿优
    // 付费率0-2%（含2%）  40%      付费率2%-5%（含5%）  50%   付费率5%以上  60%
    IANDYOU_LEVEL1(iandyou100,    -1d,    2d,     40d),
    IANDYOU_LEVEL2(iandyou100,     2d,    5d,     50d),
    IANDYOU_LEVEL3(iandyou100,     5d,    100d,   60d),

    // ================================================================================================================================================
    // 走遍美国　　　10% 固定
    // 一级城市 走遍美国非假期 0-0.1%（包含0.1%） 10%
    TRAVELUSA_LEVEL_ALL(TravelAmerica,     -1d,    100d,   10d),

    // ================================================================================================================================================
    // 进击的三国　　　10% 固定
    // 一级城市 走遍美国非假期 0-0.1%（包含0.1%） 10%
    SANGUODMZ_LEVEL_ALL(SanguoDmz,     -1d,    100d,   10d),

    // ================================================================================================================================================
    // 宠物大乱斗　　　10% 固定
    // 一级城市 宠物大乱斗 0-0.1%（包含0.1%） 10%
    PETSWAR_LEVEL_ALL(PetsWar,     -1d,    100d,   10d),
    ;

    @Getter private final OrderProductServiceType productServiceType;
    @Getter private final double levelFrom;
    @Getter private final double levelTo;
    @Getter private final double sharePercent;

    public static AgentOnlinePayShareConfig getOnlinePayShare(String productName, double payRate) {
        AgentOnlinePayShareConfig[] allConfig = values();
        for (AgentOnlinePayShareConfig config : allConfig) {
            String cardName = ProductType.getProductName(config.getProductServiceType());
            if (productName.equals(cardName) && config.getLevelFrom() < payRate && config.getLevelTo() >= payRate) {
                return config;
            }
        }

        return null;
    }

}
