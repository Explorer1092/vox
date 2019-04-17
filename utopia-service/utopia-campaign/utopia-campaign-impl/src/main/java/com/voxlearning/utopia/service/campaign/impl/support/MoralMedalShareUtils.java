package com.voxlearning.utopia.service.campaign.impl.support;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.campaign.api.enums.MoralMedalEnum;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MoralMedalShareUtils {

    private static Map<Integer, List<String>> medalTextMap = new ConcurrentHashMap<>();
    private static Map<Integer, List<String>> typeTextMap = new ConcurrentHashMap<>();
    private static List<String> commonText = new ArrayList<>();

    private static Handler handler;

    static {
        for (MoralMedalEnum value : MoralMedalEnum.values()) {
            medalTextMap.put(value.getId(), new ArrayList<>());
            typeTextMap.put(value.getTypeId(), new ArrayList<>());
        }

        loadMedalText();
        loadTypeText();
        loadCommonText();

        initHandler();
    }

    private static void initHandler() {
        handler = new SameMedalHandler();
        Handler sameTypeHandler = new SameTypeHandler();
        Handler commonHandler = new CommonHandler();
        handler.nextHandler = sameTypeHandler;
        sameTypeHandler.nextHandler = commonHandler;
    }

    private static void loadCommonText() {
        commonText.add("生活就像海洋，只有意志坚强的人才能到达彼岸。");
        commonText.add("成功永远属于一直在跑的人。");
        commonText.add("如果要飞得高，就该把地平线忘掉。");
        commonText.add("一个人的态度，决定他的高度。");
        commonText.add("只要还有明天，今天就永远是起跑线。");
    }

    private static void loadTypeText() {
        typeTextMap.put(1, Arrays.asList(
                "在世界上一切道德品质之中，善良的本性是最需要的。",
                "学问是智慧的泉源，品德乃事业的根本。",
                "道德是永存的，而财富是每天都在更换主人的。",
                "善良是历史中稀有的珍珠，善良的人便几乎优于伟大的人。",
                "良好的品德就像是用玉石来购买去天堂的翅膀。"
        ));
        typeTextMap.put(2, Arrays.asList(
                "玉不琢，不成器；木不雕，不成材；人不学，不知理。",
                "笨鸟先飞早入林，功夫不负苦心人。",
                "宝剑锋从磨砺出，梅花香自苦寒来。",
                "锲而舍之，朽木不折；锲而不舍，金石可镂。",
                "让刻苦成为习惯，用汗水浇灌未来。"
        ));
    }

    private static void loadMedalText() {
        medalTextMap.put(10, Arrays.asList(
                "单丝不成线，独木不成林。",
                "一人难挑千斤担，众人能移万座山。",
                "一切使人团结的是善与美，一切使人分裂的是恶与丑。",
                " 同心才能走得更远，同德才能走得更近。"
        ));

        medalTextMap.put(11, Arrays.asList(
                "疾学在于尊师。",
                "君子隆师而亲友。",
                "经师易求，人师难得。",
                "那些博得了自己子女的热爱和尊敬的父亲和母亲是非常幸福的。"
        ));

        medalTextMap.put(12, Arrays.asList(
                "人类只有一个可生息的村庄——地球，保护环境是每个地球村民的责任。",
                "环境与人类共存，资源开发与环境保护协调。",
                "环保不分民族，生态没有国界。",
                "善待地球就是善待自己。"
        ));

        medalTextMap.put(13, Arrays.asList(
                "能承担多大的责任，就能取得多大的成功。",
                "细节体现责任，责任决定成败。",
                "能承担多大的责任，就能取得多大的成功。",
                "诚信是立国之本,责任是立身之本。"
        ));

        medalTextMap.put(14, Arrays.asList(
                "勇敢是处于逆境时的光芒。",
                "勇气不仅仅是一种美德，而且还是各种美德在经受考验时，也即在最逼真的情形下的一种表现形式。",
                "坦白是诚实和勇敢的产物。",
                "懦弱的人只会裹足不前，莽撞的人只能引为烧身，只有真正勇敢的人才能所向披靡。"
        ));

        medalTextMap.put(15, Arrays.asList(
                "自立自重，不可跟人脚迹，学人言语。",
                "有信心的人，可以化渺小为伟大，化平庸为神奇。",
                "能够使我飘浮于人生的泥沼中而不致陷污的，是我的信心。",
                "信心永远是成功的第一把钥匙。"
        ));

        medalTextMap.put(16, Arrays.asList(
                "三军可夺帅也，匹夫不可夺志也。",
                "时穷节乃见，一一垂丹青。",
                "不降其志，不辱其身。",
                "自主自律，自强自信。"
        ));

        medalTextMap.put(17, Arrays.asList(
                "宽宏精神是一切事物中最伟大的。",
                "宽容就如同自由，只是一味乞求是得不到的，只有永远保持警惕，才能拥有。",
                "紫罗兰把它的香气留在那踩扁了它的脚踝上。这就是宽怒。",
                "太山不让土壤，故能成其大；河海不择细流，故能就其深。"
        ));

        medalTextMap.put(18, Arrays.asList(
                "礼貌使有礼貌的人喜悦，也使那些受人以礼貌相待的人们喜悦。",
                "夫君子之行，静以修身，俭以养德，非淡泊无以明志，非宁静无以致远。",
                "国尚礼则国昌，家尚礼则家大，身有礼则身修，心有礼则心泰。",
                "礼貌是最容易做到的事情，也是最容易忽视的事情，但她却是最珍贵的事情。"
        ));

        medalTextMap.put(19, Arrays.asList(
                "聪明在于学习，天才在于积累。",
                "书山有路勤为径，学海无涯苦作舟。",
                "知之者不如好之者，好之者不如乐之者。",
                "勤学，勤思，勤问，苦钻。"
        ));

        medalTextMap.put(20, Arrays.asList(
                "一言之辩,重于九鼎之宝;三寸之舌,强于百万之师。",
                "思想充满庄严的人，言语就会充满崇高。",
                "发生在成功人物身上的奇迹，至少有一半是由口才创造的。"
        ));

        medalTextMap.put(21, Arrays.asList(
                "没有规矩，不成方圆。",
                "节制是一种秩序，一种对于快乐与欲望的控制",
                "纪律是达到一切雄图的阶梯。",
                "付给纪律一个恪守，恪守还你一份优秀。"
        ));

        medalTextMap.put(22, Arrays.asList(
                "应当随时学习，学习一切；应该集中全力，以求知道得更多，知道一切。",
                "读书之法无它，惟是笃志虚心，反复详玩，为有功耳。",
                "只要认真细心，什么也难不倒你。"
        ));
    }

    public static abstract class Handler {
        Handler nextHandler;

        abstract boolean match(List<MoralMedalEnum> enums);

        abstract List<String> execute(List<MoralMedalEnum> enums);

        public List<String> handler(List<MoralMedalEnum> enums) {
            boolean match = this.match(enums);
            if (match) {
                return execute(enums);
            } else {
                return nextHandler.handler(enums);
            }
        }
    }


    public static class SameMedalHandler extends Handler {
        @Override
        public boolean match(List<MoralMedalEnum> enums) {
            Set<Integer> set = enums.stream().map(MoralMedalEnum::getId).collect(Collectors.toSet());
            return set.size() == 1;
        }

        @Override
        public List<String> execute(List<MoralMedalEnum> enums) {
            MoralMedalEnum next = enums.iterator().next();
            return medalTextMap.get(next.getId());
        }
    }

    public static class SameTypeHandler extends Handler {
        @Override
        public boolean match(List<MoralMedalEnum> enums) {
            Set<Integer> set = enums.stream().map(MoralMedalEnum::getTypeId).collect(Collectors.toSet());
            return set.size() == 1;
        }

        @Override
        public List<String> execute(List<MoralMedalEnum> enums) {
            MoralMedalEnum next = enums.iterator().next();
            return typeTextMap.get(next.getTypeId());
        }
    }

    public static class CommonHandler extends Handler {
        @Override
        public boolean match(List<MoralMedalEnum> enums) {
            return true;
        }

        @Override
        public List<String> execute(List<MoralMedalEnum> enums) {
            return commonText;
        }
    }


    public static String getShareText(Collection<Integer> medalIds) {
        if (CollectionUtils.isEmpty(medalIds)) {
            return "";
        }

        List<MoralMedalEnum> collect = medalIds.stream().map(MoralMedalEnum::valueOfById).distinct().collect(Collectors.toList());

        List<String> handler = MoralMedalShareUtils.handler.handler(collect);

        if (CollectionUtils.isEmpty(handler)) {
            return "";
        }

        int index = RandomUtils.nextInt(handler.size() - 1);
        return handler.get(index);
    }
}
