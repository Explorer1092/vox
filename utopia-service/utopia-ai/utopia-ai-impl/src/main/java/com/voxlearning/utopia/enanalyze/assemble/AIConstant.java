package com.voxlearning.utopia.enanalyze.assemble;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * ai服务常量
 *
 * @author xiaolei.li
 * @version 2018/8/2
 */
public interface AIConstant {

    @AllArgsConstructor
    enum Code {
        OK("0", true, null),
        NLP_BAD_REQUEST("101", false, "诶？好像和基地失去了联系……"),
        SYNTAX_CHECK_FAILED("102", false, "诶？好像和基地失去了联系……"),
        OPEN_NLP_FAILED("103", false, "诶？好像和基地失去了联系……"),
        RATING_FAILED("104", false, "诶？好像和基地失去了联系……"),
        CHINESE_CHARACTER("111", false, "请不要在英语作文中使用汉字哟"),
        LONG_WORD("112", false, "有些单词hin奇怪，请再检查检查吧"),
        CONTINUOUS_PUNCTUATION("113", false, "标点的使用貌似不规范，请再检查检查吧"),
        EXTRA_EMAIL("114", false, "有些内容hin奇怪，请再检查检查吧"),
        EXTRA_URL("115", false, "有些内容hin奇怪，请再检查检查吧"),
        EXTRA_IP("116", false, "有些内容hin奇怪，请再检查检查吧"),
        NOT_ENOUGH_WORDS("117", false, "本文中英语单词太少啦，请再多写点吧"),
        TOO_MANY_WORDS("118", false, "本文中英语单词太多啦，可以少写点呢"),
        OOV_HIGH("119", false, "有些内容hin奇怪，请再检查检查吧"),
        OTHER_CHARACTER("120", false, "有些内容hin奇怪，请再检查检查吧"),
        OCR_BAD_REQUEST("201", false, "诶？好像和基地失去了联系……"),
        OCR_FAILED("202", false, "诶？好像和基地失去了联系……"),
        ALI_FAILED("203", false, "诶？好像和基地失去了联系……"),
        INVALID_IMAGE_TYPE("211", false, "仅支持大小尺寸合理的bmp / png / jpg / jpeg图片哦"),
        TOO_SHORT_LENGTH("212", false, "仅支持大小尺寸合理的bmp / png / jpg / jpeg图片哦"),
        TOO_LONG_LENGTH("213", false, "仅支持大小尺寸合理的bmp / png / jpg / jpeg图片哦"),
        INVALID_IMAGE_SIZE("214", false, "仅支持大小尺寸合理的bmp / png / jpg / jpeg图片哦"),
        INVALID_IMAGE_CONTENT("215", false, "这张图片好像有点问题，换一张试试？"),
        UNKNOWN("99", false, "诶？好像和基地失去了联系…");
        public final String CODE;
        public final boolean SUCCESS;
        public final String TEXT;

        /**
         * 根据code获取枚举
         *
         * @param code code
         * @return 枚举
         */
        public static Code of(String code) {
            return Arrays.stream(Code.values())
                    .filter(i -> i.CODE.equals(code))
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }
}
