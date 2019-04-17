package com.voxlearning.utopia.api.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * Created by jiangpeng on 16/7/10.
 */
@Getter
@AllArgsConstructor
public enum SelfStudyType {

    WALKMAN_ENGLISH(1, Subject.ENGLISH, "英语随身听", "BK_10300000265057",
            "/public/skin/parentMobile/images/app_icon/icon_zixue_suishenting.png",
            true, false, true, true, true,
            OrderProductServiceType.WalkerMan.name(),
            "","", "", "", ""),

    PICLISTEN_ENGLISH(2, Subject.ENGLISH, "课本点读", "BK_10300000423854",
            "/public/skin/parentMobile/images/app_icon/icon_zixue_diandu2.png",
            true, false, true, false, true,
            OrderProductServiceType.PicListenBook.name(),
            "/view/mobile/parent/choice_book/index.vpage?useNewCore=wk&rel=practice",
            "/public/skin/parentMobile/images/app_icon/icon_zixue_diandu2.png",
            "课本点读",  "点读机", "课本原文同步点读"),

    DUBBING(2, Subject.ENGLISH, "一起配音", "",
            "/public/skin/parentMobile/images/app_icon/icon_dubbing.png",
            true, false, true, false, true,
            null,
            "",
            "/public/skin/parentMobile/images/app_icon/icon_dubbing.png",
            "专项练习", "一起配音", "跟着视频练口语"),

    TEXTREAD_CHINESE(3,Subject.CHINESE, "语文课文朗读", "BK_10100000013407",
            "/public/skin/parentMobile/images/app_icon/icon_zixue_yuwenlangdu.png",
            true, false, true, false, true,
            null,
            "","", "", "", ""),

    READING_ENGLISH(11, Subject.ENGLISH, "绘本馆", "",
            "/public/skin/parentMobile/images/app_icon/icon_englishhuiben.png",
            true, false, true, false, false,
            null,
            "/view/mobile/parent/picture_books/index.vpage?rel=xxyy&useNewCore=wk",
            "/public/skin/parentMobile/images/app_icon/icon_englishhuiben.png",
            "", "", ""),

    AFENTI_ENGLISH(4,Subject.ENGLISH, "小U英语", "",
            "/public/skin/parentMobile/images/app_icon/icon_afenti_jzen.png",
            false, true, true, true, true,
            OrderProductServiceType.AfentiExam.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "智能教辅", "小U英语", "个性化同步学习好伙伴"),

    AFENTI_ENGLISH_IMPROVE(4,Subject.ENGLISH, "小U英语提高版", "",
            "/public/skin/parentMobile/images/app_icon/icon_afenti_jzen.png",
            false, true, true, true, true,
            OrderProductServiceType.AfentiExamImproved.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    AFENTI_MATH(5,Subject.MATH, "小U数学", "",
            "/public/skin/parentMobile/images/app_icon/icon_afenti_jzmath.png",
            false, true, true, true, true,
            OrderProductServiceType.AfentiMath.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "智能教辅", "小U数学", "个性化同步学习好伙伴"),

    AFENTI_MATH_IMPROVE(5,Subject.MATH, "小U数学提高版", "",
            "/public/skin/parentMobile/images/app_icon/icon_afenti_jzmath.png",
            false, true, true, true, true,
            OrderProductServiceType.AfentiMathImproved.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    KUPAOWORD_ENGLISH(6, Subject.ENGLISH, "酷跑学单词", "",
            "/public/skin/parentMobile/images/app_icon/kupaoxuedanci.png",
            false, false, false, true, false,
            OrderProductServiceType.GreatAdventure.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    ZOUMEI_ENGLISH(7,Subject.ENGLISH, "走遍美国学英语", "",
            "/public/skin/parentMobile/images/app_icon/icon_usa_app.png",
            false, false, true, true, true,
            OrderProductServiceType.UsaAdventure.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "趣味拓展", "走遍美国学英语", "趣味场景学单词"),

    AFENTI_CHINESE(8, Subject.CHINESE, "小U语文", "",
            "/public/skin/parentMobile/images/app_icon/icon_afenti_jzcn.png",
            false, true, false, true, true,
            OrderProductServiceType.AfentiChinese.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "智能教辅", "小U语文", "个性化同步学习好伙伴"),

    AFENTI_CHINESE_IMPROVE(8, Subject.CHINESE, "小U语文提高版", "",
            "/public/skin/parentMobile/images/app_icon/icon_afenti_jzcn.png",
            false, true, false, true, true,
            OrderProductServiceType.AfentiChineseImproved.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    SYN_PRACTICE_CHINESE(9, Subject.CHINESE, "语文同步练","",
            "/public/skin/parentMobile/images/app_icon/chinesetblx.png",
            false, true, false, true, false,
            OrderProductServiceType.ChineseSynPractice.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    ENCYCLOPEDIA_CHALLENGE_UNKUNOW(10, Subject.UNKNOWN, "百科大挑战","",
            "/public/skin/parentMobile/images/app_icon/baikedatiaozhan.png",
            false, true, false, true, true,
            OrderProductServiceType.EncyclopediaChallenge.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "趣味拓展", "百科大挑战", "选自权威儿童百科全书"),

    FEE_COURSE_UNKUNOW(12, Subject.UNKNOWN, "错题精讲","",
            "/public/skin/parentMobile/images/app_icon/feecourse.png",
            false, true, true, true, true,
            OrderProductServiceType.FeeCourse.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    ARITHMETIC(13, Subject.MATH, "速算脑力王","",
            "/public/skin/parentMobile/images/app_icon/icon_arithmetic.png",
            false, false, false, true, true,
            OrderProductServiceType.Arithmetic.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    CHINESE_HERO(14, Subject.CHINESE, "字词英雄","",
            "/public/skin/parentMobile/images/app_icon/icon_arithmetic.png",
            false, false, false, true, true,
            OrderProductServiceType.ChineseHero.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    LIVECAST(15, Subject.CHINESE, "直播课","",
            "/public/skin/parentMobile/images/app_icon/icon_zhiboke.png",
            false, false, false, true, true,
            null,
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    ALBUM(16, Subject.CHINESE, "趣味音视频","", //原名我的订阅
            "/public/skin/parentMobile/images/app_icon/icon_album_new.png",
            false, false, false, true, true,
            null,
            "/view/mobile/parent/album/index.vpage",
            "/public/skin/parentMobile/images/app_icon/icon_album_new.png",
            "趣味拓展", "趣味音视频", "超千集学习资源"),

    ZUOYECUOITI(17, Subject.CHINESE, "错题本","",
            "/public/skin/parentMobile/images/app_icon/icon_mistaken_new.png",
            false, false, false, true, true,
            null,
            "/view/mobile/student/wrong_question/index.vpage?from=parents","",
            "", "", ""),

    Sudoku(18, Subject.CHINESE, "天天爱数独","",
            "/public/skin/parentMobile/images/app_icon/icon_arithmetic.png",
            false, false, false, true, true,
            OrderProductServiceType.Sudoku.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    SMALL_CLASS(19, Subject.UNKNOWN, "优外教小课堂","",
            "/public/skin/parentMobile/images/app_icon/icon_smallclass.jpg",
            false, false, false, true, true,
            null,
            "/view/mobile/parent/afenti/afenti_report?subject=ENGLISH","",
            "", "", ""),

    REFINED_LESSON(19, Subject.UNKNOWN, "精品课", "",
            "/public/skin/parentMobile/images/app_icon/icon_17xue.png",
            false, false, false, true, true,
            null,
            "/view/mobile/parent/afenti/afenti_report?subject=ENGLISH","",
            "", "", ""),

    CHIPS_ENGLISH(19, Subject.ENGLISH, "薯条英语", "",
            "/public/skin/parentMobile/images/app_icon/chips_english_new.png",
            false, false, false, true, true,
            OrderProductServiceType.ChipsEnglish.name(),
            "/view/mobile/parent/afenti/afenti_report?subject=ENGLISH","",
            "", "", ""),

    XUEBA_VIDEO(20, Subject.ENGLISH, "学霸视频", "",
            "/public/skin/parentMobile/images/app_icon/icon_xuebavideo.png",
            false, false, false, true, true,
            "XuebaVideo",
            "/view/mobile/parent/afenti/afenti_report?subject=ENGLISH","",
            "", "", ""),

    SYNCHRONOUS_EXERCISE(21, Subject.ENGLISH, "家长布置练习", "",
            "/study/planning/2019/01/10/20190110160820294276.png",
            true, false, true, false, false,
            ProductIdentifyType.parent_EXAM.name(),
            "/view/mobile/parent/homework_parent/homework_list.vpage", "",
            "同步练习", "家长布置练习", "精选名校同步习题"),

    MATH_PRACTICE(22, Subject.MATH, "数学重难点辅导", "",
            "/shuxuezhongnandianfudao/2019/02/21/20190221162700101715.png", false, false,
            true, true, true,
            ProductIdentifyType.parent_INTELLIGENT_TEACHING.name(),
            "/view/mobile/parent/curx_in_math/index.vpage?referrer=1&useNewCore=wk", "",
            "同步练习", "数学重难点辅导", "趣味课程和单元自测"),

    QUICK_ORAL_ARITHMETIC(23, Subject.MATH, "在线口算速算", "",
            "/xianshangkousuanfangxing/2019/03/22/20190322141924344289.png",
            true, false, true, false, false,
            ProductIdentifyType.parent_MENTAL_ARITHMETIC.name(),
            "/view/mobile/parent/homework_parent/homework_list.vpage", "",
            "同步练习", "在线口算速算", "支持手写和键盘"),

    ORAL_ARITHMETIC_EXERCISE(24, Subject.MATH, "纸质口算练习", "",
            "/zhizhikousuanfangxing/2019/03/22/20190322141808741118.png",
            true, false, true, false, false,
            ProductIdentifyType.parent_OCR_MENTAL_ARITHMETIC.name(),
            "/view/mobile/parent/print_oral/index.vpage", "",
            "同步练习", "纸质口算练习", "一键打印练习册"),

    LISTEN_WORLD(25, Subject.ENGLISH, "配音100分", "",
            "/peiyin100/2019/01/21/20190121121005412578.jpg", false, false,
            true, true, true, OrderProductServiceType.ListenWorld.name(),
            "", "", "专项练习", "配音100分", "轻松学习地道伦敦腔"),

    ELEVEL_READING(26, Subject.ENGLISH, "小U绘本", "",
            "/uhuiben/2019/01/21/20190121121041292160.png", false, false,
            true, true, true, OrderProductServiceType.ELevelReading.name(),
            "", "", "趣味拓展", "小U绘本", "阅读中练习语感"),

    CLEVEL_READING(27, Subject.CHINESE, "小U语文绘本", "",
            "/uyuwenhuiben/2019/01/21/20190121121120687509.jpg", false, false,
            true, true, true, OrderProductServiceType.CLevelReading.name(),
            "", "", "趣味拓展", "小U语文绘本", "孩子天生的最佳读本"),

    /////////////////////////////////////////////////////////////////////////////////
    @Deprecated
    ZOUMEI_PC_ENGLISH(109, Subject.ENGLISH, "精品网络课程", "",
            "/public/skin/parentMobile/images/app_icon/icon_usa_pc.jpg",
            false, true , false, false, false,
            OrderProductServiceType.TravelAmerica.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    WOKEWORD_ENGLISH(110, Subject.ENGLISH, "沃克单词冒险", "",
            "/public/skin/parentMobile/images/app_icon/wokedancimaoxian.jpg",
            false, true , false, false, false,
            OrderProductServiceType.Walker.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    @Deprecated
    CHONGWU_ENGLISH(111, Subject.ENGLISH, "宠物大乱斗", "",
            "/public/skin/parentMobile/images/app_icon/chongwuluandou.png",
            false, true , false, false, false,
            OrderProductServiceType.PetsWar.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    QUWEIXUNLIANYING_MATH(112, Subject.MATH, "趣味数学训练营", "",
            "/public/skin/parentMobile/images/app_icon/quweixunlianying.jpg",
            false, true , false, false, false,
            OrderProductServiceType.Stem101.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    DONGHUAHUIBEN_ENGLISH(113, Subject.ENGLISH, "英语动画绘本", "",
            "/public/skin/parentMobile/images/app_icon/yingyudonghuahuihen.jpg",
            false, true , false, false, false,
            OrderProductServiceType.WalkerElf.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    WUKONGSHIZI_CHINESE(114, Subject.CHINESE, "悟空识字", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_shizi.png",
            false, true, false, false, false,
            OrderProductServiceType.WukongShizi.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    WUKONGPINYIN_CHINESE(115, Subject.CHINESE, "悟空拼音", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.WukongPinyin.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    ANIMAL_LAND(116, Subject.UNKNOWN, "动物大冒险", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.AnimalLand.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    DINOSAUR_LAND(117, Subject.UNKNOWN, "恐龙时代", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.DinosaurLand.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "趣味拓展", "恐龙时代", "轻松学习恐龙知识"),

    EAGLETSINOLOGY_CLASSROOM(118, Subject.UNKNOWN, "小鹰学堂", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.EagletSinologyClassRoom.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    SCIENCE_LAND(119, Subject.UNKNOWN, "魔力科技", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.ScienceLand.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "", "", ""),

    MATH_GARDEN(120, Subject.MATH, "速算100分", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.MathGarden.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "专项练习", "速算100分", "丰富题量任意练"),

    WORD_BUILDER(121, Subject.ENGLISH, "单词100分", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.WordBuilder.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "专项练习", "单词100分", "趣味学单词"),

    CHINESE_PILOT(122, Subject.CHINESE, "字词100分", "",
            "/public/skin/parentMobile/images/app_icon/icon_wukong_pinyin.png",
            false, true, false, false, false,
            OrderProductServiceType.ChinesePilot.name(),
            "/parentMobile/ucenter/shoppinginfo.vpage","",
            "专项练习", "字词100分", "一词全面练习"),

    READING_ENGLISH_PLUS(123, Subject.ENGLISH, "英文绘本", "",
            "",true, false, true, false, false,
            null,"","", "", "", ""),
    /////////////////////////////////////////////////////////
    @Deprecated
    AOSHU_MATH(201, Subject.MATH, "一起学奥数", "",
            "/public/skin/parentMobile/images/app_icon/icon_aoshu1.png",
            false, true, false, false, false,
            OrderProductServiceType.WukongPinyin.name(),
            "","", "", "", ""),

    UNKNOWN(9999,null,"未知","",
            "",
            false, false, false, false, false ,
            OrderProductServiceType.Unknown.name(), "","", "", "", "");

    private int type;

    private Subject subject;

    private String desc;

    private String defaultBookId;

    private String iconUrl;


    private Boolean isFree; //是否免费

    private Boolean pcSupport;  //pc端是否支持

    private Boolean parentAppSupport;  //家长端是否支持

    private Boolean studentAppSupport; //学生端是否支持

    private Boolean isNative;  //是否为原生功能

    private String orderProductServiceType;  //对应的订单产品类型

    private String h5Url;

    private String backImgUrl;

    private String applicationType;//应用类型

    private String applicationName;//应用名称

    private String subheading;//应用副标题


    private static Map<OrderProductServiceType, SelfStudyType> orderType2SelfStudyTypeMap = new HashMap<>();

    public static List<OrderProductServiceType> orderProductServiceTypes = new ArrayList<>();

    public static List<SelfStudyType> upperList = new ArrayList<>();
    public static List<SelfStudyType> lowerList = new ArrayList<>();

    public boolean isAfenti(){
        return this == AFENTI_CHINESE
                || this == AFENTI_ENGLISH
                || this == AFENTI_MATH;
    }

    static {
        Arrays.stream(values()).forEach(st -> {
            if(st.getOrderProductServiceType() != null)
                orderType2SelfStudyTypeMap.put(OrderProductServiceType.safeParse(st.getOrderProductServiceType()), st);
            if (st.getType()<100)
                upperList.add(st);
            else if (st.getType() >= 100 && st.getType() < 200) {
                if (st != ZOUMEI_PC_ENGLISH && st != CHONGWU_ENGLISH)
                    lowerList.add(st);
            }
            if (st != PICLISTEN_ENGLISH && st != TEXTREAD_CHINESE && st != WALKMAN_ENGLISH && st.getOrderProductServiceType() != null
                    && st != AFENTI_CHINESE_IMPROVE && st != AFENTI_ENGLISH_IMPROVE && st != AFENTI_MATH_IMPROVE)
                orderProductServiceTypes.add(OrderProductServiceType.safeParse(st.getOrderProductServiceType()));
        });
    }

    public static SelfStudyType of(String name){
        try{
            SelfStudyType selfStudyType = valueOf(name);
            if(selfStudyType == null)
                return UNKNOWN;
            else
                return selfStudyType;
        }catch (Exception e){
            return UNKNOWN;
        }
    }

    public static SelfStudyType fromOrderType(OrderProductServiceType orderProductServiceType){
        if (orderProductServiceType == null)
            return null;
        return orderType2SelfStudyTypeMap.get(orderProductServiceType);
    }

}

