package com.voxlearning.utopia.service.crm.api.constants;

import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * 商品类型
 * Created by Alex on 15-3-11.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProductType {

    CARD_WALKER(Walker, "沃克大冒险", 0.00),
    CARD_AFENTI(AfentiExam, "阿分题", 0.00),
    CARD_TRAVEL_AMERICA(TravelAmerica, "走遍美国", 0.60),
    CARD_STUDY_CRAFT(StudyCraft, "口袋学社", 0.00),
    CARD_SANGUODMZ(SanguoDmz, "进击的三国", 0.70),
    CARD_PETSWAR(PetsWar, "宠物大乱斗", 0.70),
    CARD_WALKERELF(WalkerElf, "拯救精灵王", 0.50),
    CARD_SPG(A17ZYSPG, "诺亚传说", 0.70),
    CARD_STEM101(Stem101, "趣味数学训练营", 0.70),

    @Deprecated
    CARD_PICARO(KaplanPicaro, "Picaro", 0.30),
    @Deprecated
    CARD_IANDYOU100(iandyou100, "爱儿优", 0.30),
    ;

    @Getter private final OrderProductServiceType cardType;
    @Getter private final String cardName;
    @Getter private final Double separateRate;

    public static String getProductName(OrderProductServiceType productType) {

        for (ProductType product : values()) {
            if (productType == product.getCardType()) {
                return product.getCardName();
            }
        }

        return null;
    }

    public static Double getSeparateRate(OrderProductServiceType productType) {

        for (ProductType product : values()) {
            if (productType == product.getCardType()) {
                return product.getSeparateRate();
            }
        }
        return null;
    }

    public static List<ProductType> getSharePaymentProducts() {
        return Arrays.asList(
                CARD_AFENTI,
                CARD_WALKER,
                CARD_TRAVEL_AMERICA,
                CARD_SANGUODMZ,
                CARD_PETSWAR,
                CARD_WALKERELF,
                CARD_SPG
        );
    }
}
