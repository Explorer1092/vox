package com.voxlearning.utopia.api.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点读机功能
 *
 * @author jiangpeng
 * @since 2017-04-17 下午12:07
 **/
@Getter
public enum PicListenFunction {

    PIC_LISTEN(1, "点读机", "NATIVE", "",
            "/public/skin/parentMobile/images/piclisten/english_piclisten.png",
            "/public/skin/parentMobile/images/piclisten/chinese_piclisten.png", //点读机特殊点,小图是语文的,大图是英语的。
            "",
            "PicListenBook"),
    PIC_LISTEN_BUY_PAGE(1, "点读机购买页", "H5", "",
            "/public/skin/parentMobile/images/piclisten/piclisten_buy.png",
            "/public/skin/parentMobile/images/piclisten/piclisten_buy.png",
            "",
            null),

    WALK_MAN(2, "随身听", "NATIVE", "",
            "/public/skin/parentMobile/images/piclisten/suishenting_big.png",
            "/public/skin/parentMobile/images/piclisten/suishenting.png",
            "#b7de00",
            "WalkerMan"),

    TEXT_READ(3, "语文朗读", "NATIVE", "",
            "",
            "/public/skin/parentMobile/images/piclisten/text_read.png",
            "#43d085",
            null),

    FOLLOW_READ(4, "跟读", "NATIVE", "",
            "",
            "/public/skin/parentMobile/images/piclisten/follow_read.png",
            "#c374f8",
            "FollowRead"),


    ENGLISH_WORD_LIST(5, "英语单词表", "NATIVE", "",
            "",
            "/public/skin/parentMobile/images/piclisten/english_word.png",
            "#43cf84",
            null),

    CHINESE_WORD_LIST(6, "语文生词表", "NATIVE", "",
            "",
            "/public/skin/parentMobile/images/piclisten/chinese_word.png",
            "#fdbe2d",
            null),


    READING(7, "英语绘本", "H5", "/view/mobile/parent/learning_tool/huiben/index",
            "",
            "/public/skin/parentMobile/images/piclisten/huiben.png",
            "#fa9045",
            null),

    NONE(99, "无", "NONE", "",
            "",
            "/public/skin/parentMobile/images/piclisten/more.png",
            "",
            null);


    private int order;

    private String desc;

    private String functionType;

    private String url;

    private String bigImageUrl;

    private String smallImageUrl;

    private String color;

    private String productServiceType;//OrderProductServiceType

    private String v3ImageUrl;

    PicListenFunction(int order, String desc, String functionType, String url,
                      String bigImageUrl, String smallImageUrl, String color, String productServiceType) {
        this.order = order;
        this.desc = desc;
        this.functionType = functionType;
        this.url = url;
        this.bigImageUrl = bigImageUrl;
        this.smallImageUrl = smallImageUrl;
        this.productServiceType = productServiceType;
        this.color = color;
    }

    public static List<PicListenFunction> bodyFunctionList;
    public static Map<PicListenFunction, String> v3ImgUrlMap;

    static {
        bodyFunctionList = Arrays.stream(PicListenFunction.values()).filter(t -> t != PIC_LISTEN && t != NONE).collect(Collectors.toList());
    }



    // v3 image url


    static {
        v3ImgUrlMap = new HashMap<>();
        v3ImgUrlMap.put(CHINESE_WORD_LIST, "public/skin/parentMobile/images/piclisten/v3/chinese_word_list.png");
        v3ImgUrlMap.put(ENGLISH_WORD_LIST, "public/skin/parentMobile/images/piclisten/v3/english_word_list.png");
        v3ImgUrlMap.put(FOLLOW_READ, "public/skin/parentMobile/images/piclisten/v3/follow_read.png");
        v3ImgUrlMap.put(PIC_LISTEN, "public/skin/parentMobile/images/piclisten/v3/pic_listen.png");
        v3ImgUrlMap.put(TEXT_READ, "public/skin/parentMobile/images/piclisten/v3/text_read.png");
        v3ImgUrlMap.put(WALK_MAN, "public/skin/parentMobile/images/piclisten/v3/walk_man.png");
    }


    public String getV3ImageUrl() {
        String url = v3ImgUrlMap.get(this);
        if (url == null) {
            url = "";
        }
        return url;
    }
}
