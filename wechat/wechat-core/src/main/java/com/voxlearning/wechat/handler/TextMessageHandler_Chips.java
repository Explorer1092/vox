
package com.voxlearning.wechat.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.client.ChipsEnglishContentLoaderClient;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.BooKConst;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.pojo.ChipsAutoReplayKeyWord;
import com.voxlearning.wechat.support.utils.MessageFields;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 先占坑，防止报错
 */
public class TextMessageHandler_Chips extends AbstractHandler {

    private ChipsEnglishContentLoaderClient chipsEnglishContentLoaderClient;

    //全匹配
    private static final String TODAY_STUDAY = "今日学习内容";
    private static final String GRADING_REPORT = "定级报告";
    private static final String ELECTRONIC_TEXTBOOK = "电子教材";
    private static final String GRADUATION_CERTIFICATE = "毕业证书";

    //半匹配
    private static final String TEACHER = "老师";
    private static final String[] REFUND_ARR = {"退费", "退款"};

    private static final String PATTERN = "(?<=^第)\\d+(?=天$)";

    private static final String REFUND_TEXT = "联系客服“4001601717”说明情况就可以退费哦，已经加班主任微信的可以直接告诉班主任哦";
    private static final String TEACHER_TEXT = "已经购买的家长，可以向你的专属班主任老师咨询~\n" +
            "--------------------------------------------------------------\n" +
            "注：在右下角【个人中心】-【我的老师】中可以添加我的老师";

    private static final Map<String, Integer> chineseDayMap = new HashMap<>();

    static {
        chineseDayMap.put("第一天", 1);
        chineseDayMap.put("第二天", 2);
        chineseDayMap.put("第三天", 3);
        chineseDayMap.put("第四天", 4);
        chineseDayMap.put("第五天", 5);
        chineseDayMap.put("第六天", 6);
        chineseDayMap.put("第七天", 7);
        chineseDayMap.put("第八天", 8);
        chineseDayMap.put("第九天", 9);
        chineseDayMap.put("第十天", 10);
        chineseDayMap.put("第十一天", 11);
        chineseDayMap.put("第十二天", 12);
    }

    @Override
    public String getFingerprint() {
        return WechatType.CHIPS.name() + ":" + MessageFields.FIELD_CONTENT;
    }

    public String defaultReply(MessageContext context) {
        return "success";
    }

    private String beforeEightText(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);
        rb.buildContent("非常抱歉，二十点之后才可以查看今日学习内容");
        return rb.toString();
    }

    private String notBeginLessonText(MessageContext context, int n) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);
        rb.buildContent("非常抱歉，第" + n + "天的课程还未开课");
        return rb.toString();
    }

    private ChipsAutoReplayKeyWord switchKeyWord(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        if (content.equals(TODAY_STUDAY)) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.TODAYSTUDY, -1);//匹配到今日学习内容
        }
        if (content.equals(GRADING_REPORT)) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.GRADINGREPORT);//匹配到定级报告
        }
        if (content.equals(ELECTRONIC_TEXTBOOK)) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.ELECTRONICTEXTBOOK);//匹配到电子教材
        }
        if (content.equals(GRADUATION_CERTIFICATE)) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.GRADUATIONCERTIFICATE);//匹配到毕业证书
        }
        for (String refund : REFUND_ARR) {
            if (content.contains(refund)) {
                return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.REFUND);//半匹配退费, 退款
            }
        }
        if (content.contains(TEACHER)) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.TEACHER);//半匹配老师
        }
        Integer n = chineseDayMap.get(content);//中文的第几天，如第二天
        if (n != null) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.TODAYSTUDY, n);// 匹配到第几天
        }
        String nStr = matchN(content);
        if (StringUtils.isNotBlank(nStr)) {
            return new ChipsAutoReplayKeyWord(ChipsAutoReplayKeyWord.KeyWordType.TODAYSTUDY, SafeConverter.toInt(nStr));// 匹配到第N天
        }
        return null;
    }

    private String handleTodayStudySummary(MessageContext context) {
        OrderProduct orderProduct = aiOrderProductServiceClient.getRemoteReference().loadCurrentValidPaidShortProduct(getUserIdByOpenId(context.getFromUserName()));
        if (orderProduct == null) {
            return defaultReply(context);
        }
        StoneUnitData unit = chipsEnglishContentLoaderClient.getRemoteReference().loadTodayStudyUnit(orderProduct.getId());
        if (unit == null) {
            return defaultReply(context);
        }
        if (!isAfterEightPm()) {
            return beforeEightText(context);
        }
        return sendStudyTemplateMessage(context, unit, orderProduct);
    }

    private String handleOtherDayStudySummary(MessageContext context, int day) {
        OrderProduct orderProduct = aiOrderProductServiceClient.getRemoteReference().loadBeginPaidShortProduct(getUserIdByOpenId(context.getFromUserName()));
        if (orderProduct == null) {
            return defaultReply(context);
        }
        Date endDate = parseEndDate(orderProduct);
        if (endDate == null) {
            return defaultReply(context);
        }
        if (endDate.after(new Date())) {
            return handleNotFinished(context, day, orderProduct);
        } else {
            return handleFinished(context, day, orderProduct);
        }
    }

    private String handleFinished(MessageContext context, int day, OrderProduct orderProduct) {
        List<StoneUnitData> unitList = chipsEnglishContentLoaderClient.getRemoteReference().fetchUnitListExcludeTrialV2(orderProduct.getId());
        if (CollectionUtils.isEmpty(unitList)) {
            return defaultReply(context);
        }
        StoneUnitData unit = getUnitN(unitList, day);
        return sendStudyTemplateMessage(context, unit, orderProduct);
    }

    private String handleNotFinished(MessageContext context, int day, OrderProduct orderProduct) {
        List<StoneUnitData> unitList = chipsEnglishContentLoaderClient.getRemoteReference().loadValidBeginUnitByBookIdSortWithRank(orderProduct, BooKConst.CHIPS_ENGLISH_BOOK_ID);
        if (CollectionUtils.isEmpty(unitList)) {
            return defaultReply(context);
        }
        if (unitList.size() == day && !isAfterEightPm()) {
            return beforeEightText(context);
        }
        if (day > unitList.size()) {
            return notBeginLessonText(context, day);
        }
        StoneUnitData unit = getUnitN(unitList, day);
        return sendStudyTemplateMessage(context, unit, orderProduct);
    }

    private Date parseEndDate(OrderProduct product) {
        if (product == null || StringUtils.isBlank(product.getAttributes())) {
            return null;
        }
        ChipsEnglishProductTimetable timetable = chipsEnglishContentLoaderClient.getRemoteReference().loadChipsEnglishProductTimetableById(product.getId());
        return Optional.ofNullable(timetable).map(t -> timetable.getBeginDate()).orElse(null);
    }

    /**
     * @param context
     * @param keyword value -1代表匹配到今日学习内容，非负整数代表匹配到 第N天
     * @return
     */
    private String handleStudySummary(MessageContext context, ChipsAutoReplayKeyWord keyword) {
        Integer day = keyword.getValue();
        if (day == null) {
            return defaultReply(context);
        }
        if (day == -1) {
            return handleTodayStudySummary(context);
        } else {
            return handleOtherDayStudySummary(context, day);
        }
    }

    /**
     * 根据unit处理发送模板消息
     *
     * @param context
     * @param unit
     * @return
     */
    private String sendStudyTemplateMessage(MessageContext context, StoneUnitData unit, OrderProduct product) {
        String unitId = (unit == null ? null : unit.getId());
        if (StringUtils.isBlank(unitId)) {
            return defaultReply(context);
        }
        Long userId = getUserIdByOpenId(context.getFromUserName());
        if (userId == null || userId == 0l) {
            return defaultReply(context);
        }
        aiLoaderClient.getRemoteReference().sendDailyLessonTemplateMessage(userId, unit, product, BooKConst.CHIPS_ENGLISH_BOOK_ID);
        return defaultReply(context);
    }

    @Override
    public String handle(MessageContext context) {
        String content = context.getContent();
        if (StringUtils.isBlank(content)) {
            return defaultReply(context);
        }
        ChipsAutoReplayKeyWord keyWord = switchKeyWord(content);
        if (keyWord == null) {
            return defaultReply(context);
        }
        switch (keyWord.getType()) {
            case TODAYSTUDY:
                return handleStudySummary(context, keyWord);
            case GRADINGREPORT:
                return handleGradingReport(context);
            case GRADUATIONCERTIFICATE:
                return handleGraduationCertificate(context);
            case REFUND:
                return handleRefund(context);
            case TEACHER:
                return handleTeacher(context);
            case ELECTRONICTEXTBOOK:
                return handleElectronictTextBook(context);
            default:
                return defaultReply(context);
        }
    }

    private Long getUserIdByOpenId(String openId) {
        if (StringUtils.isBlank(openId)) {
            return null;
        }
        User user = wechatLoaderClient.loadWechatUser(openId);
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    private boolean isAfterEightPm() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date eight = cal.getTime();
        if (eight.before(now)) {
            return true;
        }
        return false;
    }

    private String matchN(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=39717861
     *
     * @return
     */

    private StoneUnitData getUnitN(List<StoneUnitData> unitList, int n) {
        if (CollectionUtils.isEmpty(unitList)) {
            return null;
        }
        if (n <= 0) {
            n = 1;
        }
        if (n > unitList.size()) {
            n = unitList.size();
        }
        if (n <= unitList.size()) {
            return unitList.get(n - 1);
        }
        return null;
    }

    private String handleGradingReport(MessageContext context) {
        Long userId = getUserIdByOpenId(context.getFromUserName());
        if (userId == null || userId == 0l) {
            return defaultReply(context);
        }
        aiLoaderClient.getRemoteReference().sendGradingReportTemplateMessage(userId, BooKConst.CHIPS_ENGLISH_BOOK_ID);
        return defaultReply(context);
    }

    private String handleGraduationCertificate(MessageContext context) {
        Long userId = getUserIdByOpenId(context.getFromUserName());
        if (userId == null || userId == 0l) {
            return defaultReply(context);
        }
        aiLoaderClient.getRemoteReference().sendGraduationCertificateTemplateMessage(userId);
        return defaultReply(context);
    }

    private String handleRefund(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);
        rb.buildContent(REFUND_TEXT);
        return rb.toString();
    }

    private String handleTeacher(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);
        rb.buildContent(TEACHER_TEXT);
        return rb.toString();
    }

    private String handleElectronictTextBook(MessageContext context) {
        Long userId = getUserIdByOpenId(context.getFromUserName());
        if (userId == null || userId == 0l) {
            return defaultReply(context);
        }
        MapMessage mapMessage = aiLoaderClient.getRemoteReference().sendElectronictTextBookTextMessage(userId);
        if (!mapMessage.isSuccess()) {
            return defaultReply(context);
        }
        Object urlObj = mapMessage.get("url");
        if (urlObj == null || StringUtils.isBlank((String) urlObj)) {
            return defaultReply(context);
        }
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.NEWS);
        rb.buildArticle("电子教材", "", "http://cdn.17zuoye.com/fs-resource/5b682954e8ddcac020e84255.png", (String) urlObj);
        return rb.toString();
    }

}
