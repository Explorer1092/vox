package com.voxlearning.utopia.service.ai.support;

import com.voxlearning.alps.lang.convert.SafeConverter;

import java.util.*;

/**
 * @author songtao
 * @since 2018/6/27
 */
public class MessageConfig {
    public static String unit_grade_A = "太棒啦，Bravo!";
    public static String unit_grade_B = "今天完成得很不错呢！";
    public static String unit_grade_C = "诶，要加油了呢！强烈建议陪孩子再学一遍";
    public static String[] unit_plan_production = {"点击气泡，让孩子听听自己的录音，再对比外教示范", "回到热身环节，通过跟读再练练发音哟"};
    public static String[] unit_plan_express = {"在每次对话结束时，认真查看知识总结", "学完一课后点击“对话实录”，里面有增强表达的秘籍哦"};
    public static String[] unit_plan_fluency = {"提示孩子点击“帮助”按钮，通过跟读、复述来提高流利度", "在热身环节，提示孩子这些单词、句子都是通关秘籍，千万不要有口无心地跟读"};
    public static String[] unit_plan_listening = {"点击外教气泡，结合画面猜猜意思，不要急着点“译”哦", "知道意思后，再点击外教的气泡，多听几遍"};
    public static String[] unit_plan_independent = {"让孩子多多尝试根据自己的理解完成对话，不要直接点击帮助哦", "让孩子多多尝试根据自己的理解完成对话，不要直接点击“译”哦"};

    public static String book_result_level_1_en = "{0} is a diligent student who finishes tasks on time every day, but needs more fundamental practices.";
    public static String book_result_level_2_en = "{0} is generally an effective interlocutor, partially due to the hard-working spirits.";
    public static String book_result_level_3_en = "{0} has demonstrated a set of highly effective conversational skills after 10-day's persistence in learning.";
    public static String book_result_level_1_cn = "{0}是一个努力的学生，每天都坚持完成任务；同时，也还有很大的进步空间。";
    public static String book_result_level_2_cn = "{0}非常认真、努力，每天都坚持完成任务，也取得了不错的成果。";
    public static String book_result_level_3_cn = "{0}的认真、努力得到了回报，在每天都坚持完成任务之后，学习成果非常显著。";

    public static String book_result_task_en = "{0} is able to finish {1} and {2}, but he/she has a long way to go in {3}. In conversations, {4} has shown a mastery of {5} or {6}. However," +
            " he/she could benefit from learning {7} and {8}. We suggest {9} continue to take Level {10} Course to enhance his/her spoken English.";
    public static String book_result_task_cn = "{0}可以完成{1}、{2}任务，但在{3}方面的完成度还有待提高。在交流时，" +
            "{4}可以使用{5}或者{6}等语句进行沟通，然而，对于{7}和{8}的表达掌握还可以更好。因此，我们建议{9}继续学习{10}级的正式课程，以继续夯实其口语交际水平。";

    public static String book_result_task_cn_common = "{0}非常出色的完成了这段时间的学习，取得了很大的进步。希望{1}可以在未来继续努力，进一步提升学习的效果。";
    public static String book_result_task_en_common = "{0} did a pretty good job in the recent study and made huge progress. We hope {1} can work even harder and obtain even better result in the future.";

    private static String [] unit_ext_info = {
            "BKC_10300227051151;今天的任务是学会打包行李、询问物品在哪，在薯条英语第2级第1单元有拓展哦~",
            "BKC_10300227052789;今天的任务是在飞机上换座位",
            "BKC_10300227053519;今天的任务是解释入境目的以通过海关",
            "BKC_10300227054242;今天的任务是和新朋友交谈，在薯条英语第1级第1单元有拓展哦~",
            "BKC_10300227055751;今天的任务是高效问路和指路，在第3级第2、3单元都有延伸哦~",
            "BKC_10300227056259;今天的任务是像当地人一样点餐",
            "BKC_10300227057319;今天的任务是在超市高效购物，在第2级第2单元有拓展哦~",
            "BKC_10300227058648;今天的任务是询问并回答别人的发现",
            "BKC_10300227059808;今天的任务是描述小伙伴的外貌特征，在第2级第5单元都有延续哦~",
            "BKC_10300227060017;今天的任务是进行工作分配、学会描述农活"
    };
    
    private static String [] unit_summary_info = {
            "BKC_10300227051151;打包行李;\"Packing\";\"Where is your passport?\";\"It's in my blue luggage.\"",
            "BKC_10300227052789;在飞机上换座位;\"Sending request to switch seats\";\"Can I sit here?\"",
            "BKC_10300227053519;回答海关问题;\"Going through the customs\";\"What brings you here?\";\"I'm here to travel.\"",
            "BKC_10300227054242;认识新朋友;\"Making new friends\";\"What do you like to do?\";\"I like to play football.\"",
            "BKC_10300227055751;问路与指路;\"Asking and giving directions\";\"How can I go to Central Park?\";\"Just go straight and turn right.\"",
            "BKC_10300227056259;点餐;\"Making orders at restaurant\";\"Our special is the beef burger.\";\"I'd like a small French fries, please.\"",
            "BKC_10300227057319;询问商品;\"Smart shopping\";\"Do you have toothbrushes?\"",
            "BKC_10300227058648;询问并回答他人的发现;\"Asking about other's findings\";\"What did you find?\"",
            "BKC_10300227059808;描述他人外貌特征;\"Describing people's appearances\";\"You are taller than Joe.\"",
            "BKC_10300227060017;分配工作;\"Delegating work\";\"Who is going to feed the chickens?\";\"My dad is going to feed the chickens.\"",
            "SD_10300001252370;跟小伙伴打招呼;\"Greeting\";\"How are you?\";\"I am doing well.\";",
            "SD_10300001254684;学习常见学习物品词汇;\"School things\";\"What's that?\";\"This is a new eraser.\";",
            "SD_10300001255186;学习身体部位词汇;\"Body parts\";\"Touch your nose.\";\"This is my nose.\";",
            "SD_10300001256982;跟小伙伴告别;\"Farewell\";\"Good bye.\";\"See you.\";",
            "SD_10300001257880;跟别人提要求;\"Talk about needs\";\"We need computers.\";\"Ok, we can buy computers.\""
    };

    private static Map<String, String> unitExtMap = new HashMap<>();

    private static Map<String, String> unitCnSumMap = new HashMap<>();
    private static Map<String, String> unitEnSumMap = new HashMap<>();
    private static Map<String, List<String>> unitSentenceMap = new HashMap<>();
    static {
        for(String string : unit_ext_info) {
            String[] sts = string.split(";");
            if (sts == null || sts.length < 2) {
                continue;
            }
            unitExtMap.put(sts[0], sts[1]);
        }
        for(String string : unit_summary_info) {
            String[] sts = string.split(";");
            if (sts == null || sts.length < 4) {
                continue;
            }

            unitCnSumMap.put(sts[0], sts[1]);
            unitEnSumMap.put(sts[0], sts[2]);
            List<String> sens = new ArrayList<>();
            for(int i = 3; i < sts.length; i ++) {
                sens.add(sts[i]);
            }
            unitSentenceMap.put(sts[0], sens);
        }
    }

    public static String getUnitExtInfo(String unit) {
        return SafeConverter.toString(unitExtMap.get(unit), "");
    }

    public static String getUnitCnInfo(String unit) {
        return unitCnSumMap.get(unit);
    }

    public static String getUnitEnInfo(String unit) {
        return SafeConverter.toString(unitEnSumMap.get(unit), "");
    }

    public static List<String> getUnitSentence(String unit) {
        return unitSentenceMap.get(unit);
    }
}
