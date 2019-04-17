package com.voxlearning.utopia.service.reward.util;


import com.voxlearning.utopia.service.reward.api.enums.RewardTagEnum;
import com.voxlearning.utopia.service.reward.api.enums.support.RewardTagNode;
import com.voxlearning.utopia.service.reward.api.enums.support.TagEnumNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardTagUtils {

    public enum TypeEnum {
        smallShop, course, headwear, toby
    }

    private static Map<TypeEnum, List<RewardTagNode>> burrerMap = new HashMap<>();

    static {
        burrerMap.put(TypeEnum.smallShop, initSmallShop());
        burrerMap.put(TypeEnum.course, initCourse());
        burrerMap.put(TypeEnum.headwear, initHeadwear());
        burrerMap.put(TypeEnum.toby, initToby());
    }

    public static List<RewardTagNode> get(TypeEnum type) {
        return burrerMap.get(type);
    }

    private static List<RewardTagNode> initSmallShop() {
        RewardTagEnum[] children = {
                RewardTagEnum.全部,
                RewardTagEnum.图书,
                RewardTagEnum.文具,
                RewardTagEnum.益智,
                RewardTagEnum.体育用品,
        };
        TagEnumNode tagEnumNode1 = new TagEnumNode();
        tagEnumNode1.setTagEnum(RewardTagEnum.公益专区);
        tagEnumNode1.setChildren(children);

        TagEnumNode tagEnumNode2 = new TagEnumNode();
        tagEnumNode2.setTagEnum(RewardTagEnum.一起专属);
        tagEnumNode2.setChildren(children);

        TagEnumNode tagEnumNode3 = new TagEnumNode();
        tagEnumNode3.setTagEnum(RewardTagEnum.全部类品);
        tagEnumNode3.setChildren(children);

        List<TagEnumNode> list = new ArrayList<>();
        list.add(tagEnumNode1);
        list.add(tagEnumNode2);
        list.add(tagEnumNode3);

        return convert(list);
    }

    private static List<RewardTagNode> initCourse() {
        TagEnumNode tagEnumNode1 = new TagEnumNode();
        tagEnumNode1.setTagEnum(RewardTagEnum.万物百科);
        tagEnumNode1.setChildren(new RewardTagEnum[]{
                RewardTagEnum.全部,
                RewardTagEnum.太空奥秘,
                RewardTagEnum.生活常识,
                RewardTagEnum.科学怪谈,
                RewardTagEnum.数学世界,
                RewardTagEnum.玩转魔方,
        });

        TagEnumNode tagEnumNode2 = new TagEnumNode();
        tagEnumNode2.setTagEnum(RewardTagEnum.手工艺术);
        tagEnumNode2.setChildren(new RewardTagEnum[]{
                RewardTagEnum.全部,
                RewardTagEnum.绘画,
                RewardTagEnum.手工
        });

        TagEnumNode tagEnumNode3 = new TagEnumNode();
        tagEnumNode3.setTagEnum(RewardTagEnum.趣学编程);
        tagEnumNode3.setChildren(new RewardTagEnum[]{
                RewardTagEnum.全部,
                RewardTagEnum.益智,
                RewardTagEnum.小游戏,
                RewardTagEnum.动画,
                RewardTagEnum.工具
        });

        List<TagEnumNode> list = new ArrayList<>();
        list.add(tagEnumNode1);
        list.add(tagEnumNode2);
        list.add(tagEnumNode3);

        return convert(list);
    }

    private static List<RewardTagNode> initHeadwear() {
        TagEnumNode tagEnumNode1 = new TagEnumNode();
        tagEnumNode1.setTagEnum(RewardTagEnum.可爱);
        tagEnumNode1.setChildren(new RewardTagEnum[]{
                RewardTagEnum.全部,
                RewardTagEnum.动漫世界,
                RewardTagEnum.可爱本爱,
                RewardTagEnum.梦幻灵动
        });

        TagEnumNode tagEnumNode2 = new TagEnumNode();
        tagEnumNode2.setTagEnum(RewardTagEnum.文艺);
        tagEnumNode2.setChildren(new RewardTagEnum[]{
                RewardTagEnum.全部,
                RewardTagEnum.春之舞,
                RewardTagEnum.夏之歌,
                RewardTagEnum.秋之羽,
                RewardTagEnum.冬之梦,
                RewardTagEnum.校园风,
                RewardTagEnum.星象,
        });

        TagEnumNode tagEnumNode3 = new TagEnumNode();
        tagEnumNode3.setTagEnum(RewardTagEnum.酷炫);
        tagEnumNode3.setChildren(new RewardTagEnum[]{
                RewardTagEnum.全部,
                RewardTagEnum.王者峡谷,
                RewardTagEnum.最强勇士
        });

        TagEnumNode tagEnumNode4 = new TagEnumNode();
        tagEnumNode4.setTagEnum(RewardTagEnum.公益);
        tagEnumNode4.setChildren(new RewardTagEnum[]{});

        TagEnumNode tagEnumNode5 = new TagEnumNode();
        tagEnumNode5.setTagEnum(RewardTagEnum.其他);
        tagEnumNode5.setChildren(new RewardTagEnum[]{});

        List<TagEnumNode> list = new ArrayList<>();
        list.add(tagEnumNode1);
        list.add(tagEnumNode2);
        list.add(tagEnumNode3);
        list.add(tagEnumNode4);
        //list.add(tagEnumNode5);

        return convert(list);
    }

    private static List<RewardTagNode> initToby() {
        RewardTagEnum[] children = {
                RewardTagEnum.全部,
                RewardTagEnum.托比公益,
                RewardTagEnum.托比抓一抓,
        };
        TagEnumNode tagEnumNode1 = new TagEnumNode();
        tagEnumNode1.setTagEnum(RewardTagEnum.托比形象);
        tagEnumNode1.setChildren(children);

        TagEnumNode tagEnumNode2 = new TagEnumNode();
        tagEnumNode2.setTagEnum(RewardTagEnum.托比表情);
        tagEnumNode2.setChildren(children);

        TagEnumNode tagEnumNode3 = new TagEnumNode();
        tagEnumNode3.setTagEnum(RewardTagEnum.托比道具);
        tagEnumNode3.setChildren(children);

        TagEnumNode tagEnumNode4 = new TagEnumNode();
        tagEnumNode4.setTagEnum(RewardTagEnum.托比饰品);
        tagEnumNode4.setChildren(children);

        List<TagEnumNode> list = new ArrayList<>();
        list.add(tagEnumNode1);
        list.add(tagEnumNode2);
        list.add(tagEnumNode3);
        list.add(tagEnumNode4);

        return convert(list);
    }

    private static List<RewardTagNode> convert(List<TagEnumNode> list) {
        List<RewardTagNode> result = new ArrayList<>();

        for (TagEnumNode tagEnumNode : list) {
            List<RewardTagNode> itemList = new ArrayList<>();
            for (RewardTagEnum item : tagEnumNode.getChildren()) {
                RewardTagNode tagNodeItem = new RewardTagNode();
                tagNodeItem.setId(item.getId());
                tagNodeItem.setName(item.getName());
                itemList.add(tagNodeItem);
            }

            RewardTagNode tagNode = new RewardTagNode();
            tagNode.setId(tagEnumNode.getTagEnum().getId());
            tagNode.setName(tagEnumNode.getTagEnum().getName());
            tagNode.setChildren(itemList);
            result.add(tagNode);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(get(TypeEnum.course));
        System.out.println(get(TypeEnum.smallShop));
        System.out.println(get(TypeEnum.headwear));
        System.out.println(get(TypeEnum.toby));
    }

}
