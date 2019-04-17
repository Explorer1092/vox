package com.voxlearning.utopia.service.newhomework.api.util;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/10/28
 */
public class NewHomeworkUtils {
    /**
     * 处理作业单题完成用时
     */
    public static Long processDuration(Long originalDuration) {
        if (originalDuration == null || originalDuration <= 0) {
            return NewHomeworkConstants.DEFAULT_DURATION_MILLISECONDS;
        }
        if (originalDuration > NewHomeworkConstants.MAX_DURATION_MILLISECONDS) {
            return NewHomeworkConstants.MAX_DURATION_MILLISECONDS;
        }
        return originalDuration;
    }

    public static String getUserAvatarImgUrl(String cdnBaseUrl, String imgFile) {
        String imgUrl;
        if (!StringUtils.isEmpty(imgFile)) {
            imgUrl = "gridfs/" + imgFile;
        } else {
            imgUrl = "upload/images/avatar/avatar_normal.gif";
        }
        return cdnBaseUrl + imgUrl;
    }


    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {

            URL realUrl = new URL(url);
            // 打开和URL之间的连接

            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static AppOralScoreLevel handleAppOralScoreLevel(double score) {
        AppOralScoreLevel appOralScoreLevel;
        if (score > AppOralScoreLevel.B.getScore()) {
            appOralScoreLevel = AppOralScoreLevel.A;
        } else if (score > AppOralScoreLevel.C.getScore()) {
            appOralScoreLevel = AppOralScoreLevel.B;
        } else if (score > AppOralScoreLevel.D.getScore()) {
            appOralScoreLevel = AppOralScoreLevel.C;
        } else {
            appOralScoreLevel = AppOralScoreLevel.D;
        }
        return appOralScoreLevel;
    }

    /**
     * 按照每份多少个元素，切分数组
     *
     * @param list      要切分的数组
     * @param splitSize 切分后每份元素的个数
     * @return List
     */
    public static <T> List<List<T>> splitList(List<T> list, int splitSize) {
        if (list == null || splitSize < 1) {
            return null;
        }
        int totalSize = list.size();
        int count = (totalSize % splitSize == 0) ? (totalSize / splitSize) : (totalSize / splitSize + 1);
        List<List<T>> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            List<T> cols = list.subList(i * splitSize, (i == count - 1) ? totalSize : splitSize * (i + 1));
            rows.add(cols);
        }
        return rows;
    }

    /**
     * 根据数字字符串转化为相应的第几自然段
     * 数字返回在1000内，如果超过一千内然后返回无内容字符串
     *
     * @param name 数字字符串
     * @return 根据数字字符串转化为相应的第几自然段
     */
    public static String transferToChinese(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        int length = name.length();
        StringBuilder cName = new StringBuilder();
        if (length <= 3) {
            if (name.equals("10")) {
                cName.append("十");
            } else {
                switch (length) {
                    case 3: {
                        cName.append(numberMap.get(name.charAt(length - 3) + "")).append("百");
                    }
                    case 2: {
                        String c = name.charAt(length - 2) + "";
                        if (c.equals("0")) {
                            String b = name.charAt(length - 1) + "";
                            if (b.equals("0")) {
                                break;
                            }
                        } else {
                            cName.append(numberMap.get(name.charAt(length - 2) + "")).append("十");
                        }
                    }
                    case 1: {
                        if (!(length > 1 && name.charAt(length - 1) == '0')) {
                            cName.append(numberMap.get(name.charAt(length - 1) + ""));
                        }

                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
        return cName.toString();
    }

    /**
     * numberMap 因为多次调用transferToChinese方法，避免重复构造，所以放在static内
     * 修改方式可以在调用transferToChinese方法处，形成一个这样的Map，transferToChinese增加一个参数
     */
    private static Map<String, String> numberMap;

    static {
        numberMap = new LinkedHashMap<>();
        String[] a = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        for (int i = 9; i >= 0; i--) {
            numberMap.put(i + "", a[i]);
        }
    }

    public static boolean isSubHomework(String id) {
        return StringUtils.endsWith(id, "_1");
    }

    public static boolean isShardHomework(String id) {
        return StringUtils.endsWith(id, "_2");
    }

    /**
     * 校验作业id基本格式
     * 可以继续分段校验
     *
     * @param homeworkId 作业id
     * @return 格式是否合法
     */
    public static boolean isHomeworkId(String homeworkId) {
        homeworkId = StringUtils.trim(homeworkId);
        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }

        String[] segments = StringUtils.split(homeworkId, "_");

        // 只有1位的时候
        if (segments.length == 1 && homeworkId.length() == 24) {
            try {
                new ObjectId(homeworkId);
            } catch (Exception ex) {
                return false;
            }
            return true;
        } else {
            // 新老作业校验，SubHomework和NewHomeworkV1
            if (segments.length == 2 || segments.length == 3) {
                String month = segments[0]; // yyyyMM
                if (StringUtils.length(month) != 6) {
                    return false;
                }
                try {
                    new ObjectId(segments[1]);
                } catch (Exception ex) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public static Collection<String> filterIllegalHomeworkIds(Collection<String> homeworkIds) {
        homeworkIds = CollectionUtils.toLinkedList(homeworkIds);
        if (CollectionUtils.isEmpty(homeworkIds)) {
            return Collections.emptyList();
        }
        return homeworkIds.stream().filter(NewHomeworkUtils::isHomeworkId).collect(Collectors.toList());
    }

    public static boolean isSelfStudyHomeworkId(String homeworkId) {
        homeworkId = StringUtils.trim(homeworkId);
        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }
        String[] segments = StringUtils.split(homeworkId, "_");
        try {
            new ObjectId(segments[1]);
        } catch (Exception e) {
            return false;
        }
        return segments.length == 3;
    }

    public static Collection<String> filterIllegalSelfStudyHomeworkIds(Collection<String> homeworkIds) {
        homeworkIds = CollectionUtils.toLinkedList(homeworkIds);
        if (CollectionUtils.isEmpty(homeworkIds)) {
            return Collections.emptyList();
        }
        return homeworkIds.stream().filter(NewHomeworkUtils::isSelfStudyHomeworkId).collect(Collectors.toList());
    }

    public static String formatDuring(long ms) {
        long days = ms / (1000 * 60 * 60 * 24);
        long hours = (ms % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (ms % (1000 * 60)) / 1000;
        return days + "天" + hours + "小时" + minutes + "分钟"
                + seconds + "秒";
    }

    public static boolean isSmallPaymentHomeworkId(String homeworkId) {
        homeworkId = StringUtils.trim(homeworkId);
        if (StringUtils.isBlank(homeworkId)) {
            return false;
        }
        String[] segments = StringUtils.split(homeworkId, "_");
        if (segments.length == 3) {
            // 旧黄金
            WeekRange currentWeekRange = WeekRange.current();
            WeekRange paramWeekRange = WeekRange.parse(segments[0]);
            return paramWeekRange.getEndTime() <= currentWeekRange.getEndTime();
        } else if (segments.length == 4) {
            // 白金+新黄金
            String date = segments[0];
            if (date.length() == 6) {
                // 白金任务
                WeekRange currentWeekRange = WeekRange.current();
                WeekRange paramWeekRange = WeekRange.parse(segments[0]);
                return paramWeekRange.getEndTime() <= currentWeekRange.getEndTime();
            } else if (date.length() == 8) {
                // 第二版黄金任务
                DayRange currentDayRange = DayRange.current();
                DayRange paramDayRange = DayRange.parse(segments[0]);
                return paramDayRange.getEndTime() <= currentDayRange.getEndTime();
            } else {
                return false;
            }
        } else if (segments.length == 5) {
            // 第三版黄金任务
            DayRange currentDayRange = DayRange.current();
            DayRange paramDayRange = DayRange.parse(segments[0]);
            return paramDayRange.getEndTime() <= currentDayRange.getEndTime();
        } else {
            return false;
        }
    }

    public static Collection<String> filterIllegalSmallPaymentHomeworkIds(Collection<String> homeworkIds) {
        homeworkIds = CollectionUtils.toLinkedList(homeworkIds);
        if (CollectionUtils.isEmpty(homeworkIds)) {
            return Collections.emptyList();
        }
        return homeworkIds.stream().filter(NewHomeworkUtils::isSmallPaymentHomeworkId).collect(Collectors.toList());
    }

    public static String processScoreLevel(List<Map<String, Object>> scoreLevels, double score) {
        if (CollectionUtils.isNotEmpty(scoreLevels)) {
            for (Map<String, Object> mapper : scoreLevels) {
                String level = SafeConverter.toString(mapper.get("level"));
                int minScore = SafeConverter.toInt(mapper.get("minScore"));
                int maxScore = SafeConverter.toInt(mapper.get("maxScore"));
                if (score >= minScore && score <= maxScore) {
                    return level;
                }
            }
        }
        return null;
    }

    public static String getLevelByStar(List<Map<String, Object>> scoreLevels, int star) {
        if (CollectionUtils.isEmpty(scoreLevels)) {
            return null;
        }
        for (Map<String, Object> mapper : scoreLevels) {
            String level = SafeConverter.toString(mapper.get("level"));
            int itemStar = SafeConverter.toInt(mapper.get("star"));
            if (star == itemStar) {
                return level;
            }
        }
        return null;
    }

    public static Map<String, String> handleLessonIdToUnitId(Map<String, NewBookCatalog> ms) {
        Map<String, String> data = new LinkedHashMap<>();
        for (NewBookCatalog newBookCatalog : ms.values()) {
            if (newBookCatalog != null) {
                String unitId = null;
                for (NewBookCatalogAncestor ancestor : newBookCatalog.getAncestors()) {
                    if (Objects.equals(ancestor.getNodeType(), "UNIT")) {
                        unitId = ancestor.getId();
                    }
                }
                //兼容没有unit的错误数据
                if (unitId == null) {
                    unitId = "-1";
                }
                data.put(newBookCatalog.getId(), unitId);
            }
        }
        return data;
    }

    //处理时间转化为分秒中文表示
    public static String handlerTime(int mentalDuration) {
        int minutes = mentalDuration / 60;
        int second = mentalDuration % 60;
        if (minutes == 0) {
            return second + "秒";
        } else {
            return minutes + "分" + second + "秒";
        }
    }

    //处理时间转化为分秒中文表示
    public static String handlerEnTime(int time) {
        String duration;
        int minutes = time / 60;
        int second = time % 60;
        if (minutes == 0) {
            duration = second + "\"";
        } else {
            duration = minutes + "'" + second + "\"";
        }
        return duration;
    }


    public static String pressAnswer(List<NewQuestionsSubContents> qscs, List<List<String>> userAnswerList) {
        String answer = "暂时无法查看答案";
        List<String> answers = new ArrayList<>();
        int i = 0;
        for (NewQuestionsSubContents qsc : qscs) {
            // 主观题的userAnswer是没有值的
            if (CollectionUtils.isNotEmpty(userAnswerList)) {
                List<String> subContentAnswers = userAnswerList.get(i);
                i++;
                if (qsc.getSubContentTypeId() != QuestionConstants.LianXianTi
                        && qsc.getSubContentTypeId() != QuestionConstants.GuiLeiTi
                        && qsc.getSubContentTypeId() != QuestionConstants.LianXianTi_V2
                        && qsc.getSubContentTypeId() != QuestionConstants.GuiLeiTi_V2) {
                    if (qsc.getSubContentTypeId() == QuestionConstants.XuanZe_DanXuan
                            || qsc.getSubContentTypeId() == QuestionConstants.XuanZe_DuoXuan
                            || qsc.getSubContentTypeId() == QuestionConstants.XuanZe_BuDingXiang
                            || qsc.getSubContentTypeId() == QuestionConstants.PanDuanTi) {
                        List<String> ans = subContentAnswers
                                .stream()
                                .map(an -> StringUtils.isNotBlank(an) ?
                                        Character.valueOf((char) (SafeConverter.toInt(an) + 65)).toString() :
                                        "未答")
                                .collect(Collectors.toList());
                        answer = StringUtils.join(ans, ",");
                    } else if (qsc.getSubContentTypeId() == QuestionConstants.XuanCiTianKong) {
                        // 排序题和一级题型为选词填空，答案的处理
                        List<String> ans = new ArrayList<>();
                        for (String an : subContentAnswers) {
                            List<String> as = new ArrayList<>();
                            for (String a : StringUtils.split(an, ",")) {
                                if (StringUtils.isNumeric(a)) {
                                    as.add(SafeConverter.toString(SafeConverter.toInt(a) + 1));
                                }
                            }
                            ans.add(StringUtils.join(as, ","));
                        }
                        answer = StringUtils.join(ans, ",");
                    } else if (qsc.getSubContentTypeId() == QuestionConstants.PaiXuTi) {
                        List<String> ans = new ArrayList<>();
                        for (String an : subContentAnswers) {
                            List<String> as = new ArrayList<>();

                            Map<Integer, Integer> map = new LinkedHashMap<>();
                            int value = 0;
                            String[] split = StringUtils.isNotBlank(an) ? an.split(",") : new String[0];
                            for (String a : split) {
                                if (StringUtils.isNumeric(a)) {
                                    int ann = SafeConverter.toInt(a);
                                    map.put(ann, value);
                                }
                                value++;
                            }
                            for (int p = 0; p < split.length; p++) {
                                if (map.containsKey(p)) {
                                    as.add((map.get(p) + 1) + "");
                                } else {
                                    as.add("");
                                }
                            }
                            ans.add(StringUtils.join(as, ","));
                        }
                        answer = StringUtils.join(ans, ",");
                    } else {
                        answer = StringUtils.join(subContentAnswers, ",");
                    }
                }
            }
            answers.add(answer);
        }
        if (CollectionUtils.isNotEmpty(answers)) {
            answer = StringUtils.join(answers, ";");
        }
        return answer;
    }

    public static String processChinesePictureBookClazzLevel(String levelName) {
        switch (levelName) {
            case "L1A":
                return "一级";
            case "L2A":
                return "二级";
            case "L3A":
                return "三级";
            default:
                return levelName;
        }
    }

    public static String compressBookImg(String imgUrl) {
        if (StringUtils.isBlank(imgUrl)) {
            return "";
        }
        return "https://cdn-mirror.17zuoye.cn" + imgUrl + "?x-oss-process=image/quality,q_75/resize,w_240";
    }

    /**
     * 根据class 属性去 distinct
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 将中文汉字转为拼音
     * @param chinese
     * @return
     */
    public static String getFullSpell(String chinese) {
        if (StringUtils.isBlank(chinese)){
            return null;
        }
        StringBuffer sb = new StringBuffer();

        char[] arr = chinese.toCharArray();

        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

}
