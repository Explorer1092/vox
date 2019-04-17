package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.parent.api.TalkShardLoader;
import com.voxlearning.utopia.service.parent.api.TalkShardService;
import com.voxlearning.utopia.service.parent.api.consumer.TalkShardLoaderClient;
import com.voxlearning.utopia.service.parent.api.entity.talking.*;
import com.voxlearning.utopia.service.parent.api.mapper.talk.TalkTopicTotalRequest;
import com.voxlearning.utopia.service.parent.api.mapper.talk.TalkTopicTotalResponse;
import com.voxlearning.utopia.service.parent.constant.TalkTopicPeriod;
import com.voxlearning.utopia.service.parent.constant.TalkTopicType;
import com.voxlearning.utopia.service.user.api.StudentLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

@Controller
@Slf4j
@RequestMapping(value = "/parentMobile/talk")
public class MobileParentTalkShardController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private TalkShardLoaderClient talkLoaderClient;

    @ImportService(interfaceClass = TalkShardLoader.class)
    private TalkShardLoader talkLoader;

    @ImportService(interfaceClass = TalkShardService.class)
    private TalkShardService talkService;

    @ImportService(interfaceClass = StudentLoader.class)
    private StudentLoader studentLoader;

    private static boolean checkTopicOnline(TalkTopic talkTopic, MapMessage message) {
        if (talkTopic == null) {
            message.put("online", false);
            return false;
        }

        Integer deleted = talkTopic.getDeleted();

        if (deleted != null && deleted == 1) {
            message.put("online", false);
            return false;
        }

        message.put("online", true);
        return true;
    }

    // region 话题信息

    private static String substring(String string) {
        if (StringUtils.isEmpty(string)) {
            return "";
        }

        if (string.length() <= 30) {
            return string;
        }

        return string.substring(0, 30) + "...";
    }

    /**
     * 获取话题
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getTopic.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTopic() {
        User user = currentUser();
        // 获取学校id
        Long schoolId = 0L;
        if (user != null) {
            Long studentId = getStudentId(user);
            if (studentId != 0) {
                schoolId = getSchoolId(studentId);
            }
        }

        // 判断
        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("话题id不为空");
        }

        // 获取数据
        TalkTopic topic = talkLoaderClient.getTalkTopicById(topicId);
        MapMessage map = MapMessage.successMessage();
        boolean online = checkTopicOnline(topic, map);

        if (!online) {
            return map;
        }

        // 数据绑定

        convertDetailTopic(map, topic, schoolId);

        map.put("reward", topic.getReward()); // 1.1增加 奖品

        map.put("guestAvatar", topic.getGuestAvatar()); // 嘉宾头像
        if (StringUtils.isNotEmpty(topic.getGuestProdIntro())) {
            map.put("guestProdIntro", topic.getGuestProdIntro()); // 嘉宾产品介绍
        }
        if (StringUtils.isNotEmpty(topic.getGuestProdUrl())) {
            map.put("guestProdUrl", topic.getGuestProdUrl()); // 嘉宾产品url
        }
        map.put("videoAvatar", topic.getVideoAvatar());
        map.put("currentTime", String.valueOf(System.currentTimeMillis()));
        if (null != topic.getVideoStartTime()) {
            map.put("startTime", String.valueOf(topic.getVideoStartTime().getTime()));
        }

        if (null != topic.getVideoEndTime()) {
            map.put("endTime", String.valueOf(topic.getVideoEndTime().getTime()));
        }

        map.put("videoPath", topic.getVideoAddress()); // 视频地址
        map.put("topicType", topic.getTopicType().getValue()); // 话题类型
        map.put("audioId", topic.getAudioId()); // 音频id

        if (topic.getTopicStartTime() != null) {
            map.put("topicStartTime", String.valueOf(topic.getTopicStartTime().getTime()));
        }
        if (topic.getTopicEndTime() != null) {
            map.put("topicEndTime", String.valueOf(topic.getTopicEndTime().getTime()));
        }

        if (topic.getViewStartTime() != null) {
            map.put("viewStartTime", String.valueOf(topic.getViewStartTime().getTime()));
        }
        if (topic.getViewStartTime() != null) {
            map.put("viewEndTime", String.valueOf(topic.getViewEndTime().getTime()));
        }

        // 是否参与投票
        putUserOption(map, user, topic);

        return map;
    }

    @RequestMapping(value = "/getCurrentTopic.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCurrentTopic() {
        List<TalkTopic> topics = talkLoaderClient.getAllTalkTopic();
        TalkTopic topic = topics.stream()
                .filter(x -> x.getPeriod() != null && x.getPeriod().equals(TalkTopicPeriod.current))
                .findFirst()
                .orElse(null);

        if (topic == null) {
            return MapMessage.errorMessage("不存在当前话题");
        }

        TalkTopicTotalRequest request = new TalkTopicTotalRequest();
        request.setTopicId(topic.getTopicId());
        request.setOptionIds(topic.getOptions().stream()
                .map(TalkTopic.TalkOption::getOptionId)
                .collect(Collectors.toList()));
        Map<String, TalkTopicTotalResponse> totalResponseMap =
                talkLoader.loadTotalList(Collections.singletonList(request));

        MapMessage map = MapMessage.successMessage();

        convertTopic(map, topic, totalResponseMap.get(topic.getTopicId()));

        return map;
    }

    /**
     * 获取话题
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getVideoTopic.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getVideoTopic() {
        User user = currentUser();
        // 判断
        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("话题id不为空");
        }

        // 获取数据
        TalkTopic topic = talkLoaderClient.getTalkTopicById(topicId);
        MapMessage map = MapMessage.successMessage();
        boolean online = checkTopicOnline(topic, map);

        if (!online) {
            return map;
        }
        map.put("topicId", topic.getTopicId()); // 话题id
        map.put("title", topic.getTitle()); // 标题
        String guestProdUrl = topic.getGuestProdUrl();
        if (StringUtils.isNotEmpty(guestProdUrl)) {
            map.put("guestProdUrl", topic.getGuestProdUrl()); // 嘉宾产品url
        }

        if (null != topic.getVideoStartTime()) {
            map.put("startTime", String.valueOf(topic.getVideoStartTime().getTime()));
        }

        if (null != topic.getVideoEndTime()) {
            map.put("endTime", String.valueOf(topic.getVideoEndTime().getTime()));
        }

        if (topic.getTopicStartTime() != null) {
            map.put("topicStartTime", String.valueOf(topic.getTopicStartTime().getTime()));
        }
        if (topic.getTopicEndTime() != null) {
            map.put("topicEndTime", String.valueOf(topic.getTopicEndTime().getTime()));
        }

        if (topic.getViewStartTime() != null) {
            map.put("viewStartTime", String.valueOf(topic.getViewStartTime().getTime()));
        }

        if (topic.getViewStartTime() != null) {
            map.put("viewEndTime", String.valueOf(topic.getViewEndTime().getTime()));
        }
        if (StringUtils.isNotEmpty(topic.getVideoAvatar())) {
            map.put("videoAvatar", topic.getVideoAvatar()); // 视频头图
        }
        if (StringUtils.isNotEmpty(topic.getVideoAddress())) {
            map.put("videoPath", topic.getVideoAddress()); // 视频地址
        }

        boolean voted = false;
        if (user != null) {
            TalkEnrollment enrollment = talkLoader.getTalkEnrollment(topicId, user.getId());
            if (enrollment != null) {
                voted = true;
            }
        }
        map.put("voted", voted);

        map.put("studentEnable", topic.studentEnable());

        Long total = talkLoader.getTopicOptionCount(topicId);
        map.put("total", total == null ? 0 : total);

        return map;
    }

    /**
     * 获取话题列表
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getTopicList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTopicList() {
        List<TalkTopic> topics = talkLoaderClient.getAllTalkTopic();

        Comparator<TalkTopic> c = ((o1, o2) -> (int) (o1.getPeriod().getValue() - o2.getPeriod().getValue()));
        c = c.thenComparing((TalkTopic o1, TalkTopic o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));

        topics = topics.stream()
                .filter(x -> x.getPeriod() != null && !x.getPeriod().equals(TalkTopicPeriod.test))
                .filter(x -> x.getDeleted() != null && x.getDeleted() == 0)
                .sorted(c)
                .collect(Collectors.toList());

        MapMessage mapMessage = MapMessage.successMessage();
        if (CollectionUtils.isEmpty(topics)) {
            return mapMessage;
        }

        List<TalkTopicTotalRequest> requests = new LinkedList<>();
        topics.forEach(x -> {
            TalkTopicTotalRequest request = new TalkTopicTotalRequest();
            request.setTopicId(x.getTopicId());
            request.setOptionIds(
                    x.getOptions()
                            .stream()
                            .map(TalkTopic.TalkOption::getOptionId)
                            .collect(Collectors.toList()));
            requests.add(request);
        });
        Map<String, TalkTopicTotalResponse> totalResponseMap = talkLoader.loadTotalList(requests);

        List<Map<String, Object>> list = new LinkedList<>();

        for (TalkTopic topic : topics) {
            Map<String, Object> map = new LinkedHashMap<>();
            convertTopic(map, topic, totalResponseMap.get(topic.getTopicId()));
            list.add(map);
        }

        mapMessage.put("topics", list);
        return mapMessage;
    }

    @RequestMapping(value = "/getSimpleTopic.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSimpleTopic() {
        User user = currentUser();
        // 获取学校id
        Long schoolId = 0L;
        if (user != null) {
            Long studentId = getStudentId(user);
            if (studentId != 0) {
                schoolId = getSchoolId(studentId);
            }
        }

        // 判断
        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("话题id不为空");
        }

        // 获取数据
        TalkTopic topic = talkLoaderClient.getTalkTopicById(topicId);
        MapMessage map = MapMessage.successMessage();
        boolean online = checkTopicOnline(topic, map);

        if (!online) {
            return map;
        }

        convertDetailTopic(map, topic, schoolId);

        if (user != null) {
            map.put("avatar", getUserAvatarImgUrl(user));
            map.put("isParent", user.isParent());
            if (user.isStudent()) {
                map.put("nickname", user.getProfile().getRealname());
            } else {
                User student = null;
                Long studentId = getStudentId(user);
                StudentParentRef ref = null;
                if (schoolId != null) {
                    student = userLoaderClient.loadUser(studentId, UserType.STUDENT);
                    List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
                    ref = studentParentRefs.stream()
                            .filter(x -> x.getStudentId().equals(studentId))
                            .findFirst()
                            .orElse(null);

                } else {
                    List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                    if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                        ref = studentParentRefs.get(0);
                        if (ref != null) {
                            student = userLoaderClient.loadUser(ref.getStudentId(), UserType.STUDENT);
                        }
                    }
                }
                map.put("nickname", generateNickName(user, student, ref));
            }

            List<TalkReplyShard> talkReplies = talkLoader.getUserTalkReply(topicId, user.getId());
            if (CollectionUtils.isNotEmpty(talkReplies)) {
                TalkReplyShard reply = talkReplies.get(0);
                String optionId = reply.getOptionId();
                if (CollectionUtils.isNotEmpty(topic.getOptions())) {
                    topic.getOptions()
                            .stream()
                            .filter(x -> StringUtils.equals(x.getOptionId(), optionId))
                            .findFirst()
                            .ifPresent(option -> map.put("optionText", option.getContent()));
                }
            }
        }

        return map;
    }

    private void convertDetailTopic(Map<String, Object> map, TalkTopic topic, Long schoolId) {
        map.put("topicId", topic.getTopicId()); // 话题id
        map.put("title", topic.getTitle()); // 标题
        map.put("titleImage", topic.getTitleImage()); // 1.1增加标题图片
        if (StringUtils.isNotEmpty(topic.getSubtitle())) {
            map.put("subTitle", topic.getSubtitle()); // 1.1增加 副标题
        }
        if (StringUtils.isNotEmpty(topic.getSubTitleImage())) {
            map.put("subTitleImage", topic.getSubTitleImage()); // 1.1增加副标题图片
        }

        map.put("no", topic.getTopicNumber()); // 1.1增加 期号

        List<TalkTopic.TalkOption> options = topic.getOptions();

        if (CollectionUtils.isNotEmpty(options)) {
            List<String> optionIds =
                    options.stream().map(TalkTopic.TalkOption::getOptionId).collect(Collectors.toList());
            TalkTopicTotalResponse totalResponse =
                    talkLoader.loadTotal(topic.getTopicId(), optionIds, schoolId);
            Map<String, Long> optionTotals =
                    MapUtils.isEmpty(totalResponse.getCountryVotes())
                            ? Collections.emptyMap()
                            : totalResponse.getCountryVotes();

            Map<String, Long> parentTotals =
                    MapUtils.isEmpty(totalResponse.getParentVotes())
                            ? Collections.emptyMap()
                            : totalResponse.getParentVotes();

            Map<String, Long> childrenTotals =
                    MapUtils.isEmpty(totalResponse.getChildrenVotes())
                            ? Collections.emptyMap()
                            : totalResponse.getChildrenVotes();

            List<Map<String, Object>> optionList = new LinkedList<>();
            for (TalkTopic.TalkOption option : options) {
                if (null == option) {
                    continue;
                }

                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("optionId", option.getOptionId()); // 立场id
                optionMap.put("title", option.getContent()); // 立场标题
                optionMap.put("support", optionTotals.get(option.getOptionId())); // 支持数
                optionMap.put("summary", option.getSummary()); // 1.1增加描述
                optionMap.put("shard", option.getShard()); // 1.1增加分享图片地址
                optionMap.put("studentSupport", childrenTotals.get(option.getOptionId()));
                optionMap.put("parentSupport", parentTotals.get(option.getOptionId()));
                optionList.add(optionMap);
            }
            map.put("options", optionList);
            if (MapUtils.isNotEmpty(totalResponse.getSchoolVotes())) {
                map.put("voteSchool", totalResponse.getSchoolVotes()); // 学校立场
            } else {
                map.put("voteSchool", null);
            }

            map.put("participant", totalResponse.getTotalVotes()); // 总投票数
        }

        map.put("background", topic.getBackground()); // 背景

        if (StringUtils.isNotEmpty(topic.getIntroduction())) {
            String intro = StringUtils.replace(topic.getIntroduction(), "\r\n", "<br/>");
            intro = StringUtils.replace(intro, "\n", "<br/>");
            map.put("introduce", intro); // 介绍
            map.put("introHeight", topic.getIntroHeight()); // 介绍高度
        }

        TalkTopic.TalkProduct product = topic.getProduct();
        if (product != null) {
            if (StringUtils.isNotEmpty(product.getProductImg())
                    && product.getDiscountPrice() != null
                    && StringUtils.isNotEmpty(product.getDiscountLink())
                    && product.getOriginalPrice() != null
                    && StringUtils.isNotEmpty(product.getOriginalLink())) {
                Map<String, Object> productMap = new LinkedHashMap<>();
                productMap.put("productImg", product.getProductImg());
                productMap.put("discountPrice", product.getDiscountPrice());
                productMap.put("discountLink", product.getDiscountLink());
                productMap.put("originalLink", product.getOriginalLink());
                productMap.put("originalPrice", product.getOriginalPrice());

                String result = calculatePriceDifference(product.getOriginalPrice(), product.getDiscountPrice());

                productMap.put("discountValue", result);

                map.put("product", productMap);
            }
        }
    }

    private void putUserOption(Map<String, Object> map, User user, TalkTopic talkTopic) {
        if (user != null) {
            List<TalkReplyShard> replies = talkLoader.getUserTalkReply(talkTopic.getTopicId(), user.getId());
            map.put("avatar", getUserAvatarImgUrl(user)); // 当前用户头像
            if (CollectionUtils.isNotEmpty(replies)) {
                map.put("optionId", replies.get(0).getOptionId()); // 当前用户立场
                map.put("voted", true); // 当前用户投票状态
            } else {
                map.put("optionId", "");
                map.put("voted", false);
            }
            map.put("isLogin", true);
            boolean sharded =
                    talkTopic.haveProduct()
                            && CollectionUtils.isNotEmpty(
                            talkLoader.shardedTopic(talkTopic.getTopicId(), user.getId()));
            map.put("sharded", sharded);
        } else {
            map.put("avatar", null);
            map.put("optionId", "");
            map.put("voted", false);
            map.put("isLogin", false);
            map.put("sharded", false);
        }
    }
    // endregion

    // region 公共评论

    private void convertTopic(
            Map<String, Object> map, TalkTopic topic, TalkTopicTotalResponse totalResponse) {
        map.put("topicId", topic.getTopicId());
        map.put("number", topic.getTopicNumber());
        map.put("title", topic.getTitle());
        map.put("titleImg", topic.getCoverTitle());
        map.put("pastTitleImg", topic.getCoverPastTitle());
        map.put("backgroundImg", topic.getCoverBack());
        map.put("optionImg", topic.getCoverOption());
        map.put("period", topic.getPeriod().getValue());
        map.put("videoFlagImg", topic.getCoverFlag());
        map.put("studentEnable", topic.studentEnable());
        List<Map<String, Object>> options = new LinkedList<>();

        if (totalResponse != null) {
            map.put("participant", String.valueOf(totalResponse.getTotalVotes()));

            Map<String, Long> votes = totalResponse.getCountryVotes();
            for (TalkTopic.TalkOption option : topic.getOptions()) {
                Long vote = votes.get(option.getOptionId());
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("title", option.getContent()); // 立场标题
                optionMap.put("support", vote == null ? "0" : String.valueOf(vote)); // 支持数

                options.add(optionMap);
            }
        } else {
            map.put("participant", "0");

            for (TalkTopic.TalkOption option : topic.getOptions()) {
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("title", option.getContent()); // 立场标题
                optionMap.put("support", "0"); // 支持数
                options.add(optionMap);
            }
        }

        map.put("options", options);
    }

    /**
     * 获取话题观点（评论）
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getReply.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage getReplyList() {
        Long userId = 0L;

        User user = currentUser();
        if (user != null) {
            userId = user.getId();
        }

        int replierType = getRequestInt("userType");
        if (replierType != UserType.STUDENT.getType()) {
            replierType = UserType.PARENT.getType();
        }

        // 话题id
        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("话题id不为空");
        }
        // 第一次请求时间
        Long firstTime = getRequestLong("firstTime");
        // 最后一次点赞数
        int voted = getRequestInt("voted");
        if (firstTime != 0 && voted == 0) {
            return MapMessage.successMessage();
        }
        if (firstTime == 0) {
            voted = Integer.MAX_VALUE;
        } else {
            voted -= 1;
        }

        // 当前请求时间
        Long current = System.currentTimeMillis();
        boolean firstLoad = false;
        if (firstTime == 0) {
            firstTime = current;
            firstLoad = true;
        }

        Long currentTime = current;

        MapMessage mapMessage = MapMessage.successMessage();

        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        boolean online = checkTopicOnline(talkTopic, mapMessage);
        if (!online) {
            return mapMessage;
        }

        // 拉取数据
        // 评论点赞排序结果
        Map<String, Long> replyPraiseCount = talkLoader.getCountryTalkReplyIds(topicId, replierType, userId, 30L, firstLoad, voted, firstTime, currentTime);

        if (MapUtils.isEmpty(replyPraiseCount)) {
            mapMessage.put("min", 0);
            return mapMessage;
        }
        // 最小点赞数
        Optional<Long> minOptional = replyPraiseCount.values().stream().min(Comparator.naturalOrder());
        Long min = minOptional.orElse(0L);
        mapMessage.put("min", min);

        // 获取评论
        List<String> replyIds = new ArrayList<>(replyPraiseCount.keySet());

        Map<String, TalkReplyShard> replyMap = talkLoader.getReplies(replyIds);
        if (MapUtils.isEmpty(replyMap)) {
            return mapMessage;
        }

        // 获取精选评论的id
        List<String> choiceReplyIds = getChoiceReplyIds(topicId, replierType);

        // final Long userId = userId;
        final Long uid = userId;
        List<TalkReplyShard> replies = replyMap.values().stream()
                .filter(reply -> reply.pass()
                        && !choiceReplyIds.contains(reply.getId())
                        && !reply.getUserId().equals(uid)
                        && (!reply.isDelete() || (reply.isDelete() && reply.replyQuoted())))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(replies)) {
            return mapMessage;
        }

        Collection<Map<String, Object>> list = convertReplyWithQuote(talkTopic, replies, userId);

        current = replies.get(replies.size() - 1).getCreateTime().getTime();

        mapMessage.put("replies", list);
        mapMessage.put("firstTime", String.valueOf(firstTime));
        mapMessage.put("currentTime", String.valueOf(current));

        return mapMessage;
    }

    /**
     * 评论列表
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getquotedReply.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuotedReplyList() {
        String topicId = getRequestString("topicId");
        String replyId = getRequestString("replyId");
        if (StringUtils.isEmpty(topicId) || StringUtils.isEmpty(replyId)) {
            return MapMessage.errorMessage("参数错误");
        }

        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        if (talkTopic == null) {
            return MapMessage.errorMessage("话题不存在");
        }
        MapMessage message = MapMessage.successMessage();

        long startTime = getRequestLong("startTime", System.currentTimeMillis());

        if (startTime <= 0) {
            return message;
        }

        long lastTime = 0;

        TalkReplyShard reply = talkLoader.getReplyById(replyId);
        if (reply == null || !reply.replyQuoted()) {
            return MapMessage.successMessage();
        }

        LinkedList<TalkReplyShard> replies = new LinkedList<>();

        // 当前用户在评论下的回复，并且过滤出所有未审核的
        User user = currentUser();
        Long userId = 0L;
        if (user != null) {
            userId = user.getId();
            List<TalkReplyShard> quotedReplies = getQuotedReplies(topicId, userId);
            if (CollectionUtils.isNotEmpty(quotedReplies)) {
                quotedReplies = quotedReplies.stream()
                        .filter(x -> StringUtils.equals(x.getRootReplyId(), replyId))
                        .filter(x -> x.getCreateTime().getTime() <= startTime - 1)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(quotedReplies)) {
                    replies.addAll(quotedReplies);
                }
            }
        }

        List<String> replyIds = reply.getSubIds();
        Map<String, TalkReplyShard> talkReplyMap = talkLoader.getReplies(replyIds);
        if (MapUtils.isNotEmpty(talkReplyMap)) {
            List<TalkReplyShard> list = talkReplyMap.values().stream()
                    .filter(x -> x.getCreateTime().getTime() <= startTime - 1)
                    .collect(Collectors.toList());
            replies.addAll(list);
        }

        if (CollectionUtils.isEmpty(replies)) {
            message.put("startTime", 0);
            return message;
        }

        replies = replies.stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .limit(30)
                .collect(Collectors.toCollection(LinkedList::new));

        lastTime = replies.getLast().getCreateTime().getTime();

        message.put("startTime", lastTime);

        Map<String, Map<String, Object>> map = convertReply(talkTopic, replies, userId);
        message.put("replies", map.values());

        return message;
    }

    /**
     * 获取精选评论ID
     *
     * @param topicId 话题ID
     * @return 话题id list
     */
    private List<String> getChoiceReplyIds(String topicId, Integer userType) {
        List<TalkReplyChoice> choiceReply = talkLoader.getChoiceReply(topicId, userType);
        if (CollectionUtils.isEmpty(choiceReply)) {
            return Collections.emptyList();
        }
        List<String> replyIds = choiceReply.stream().map(TalkReplyChoice::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(replyIds)) {
            return Collections.emptyList();
        }
        if (replyIds.size() > 5) {
            replyIds = replyIds.subList(0, 5);
        }

        return replyIds;
    }

    @RequestMapping(value = "/getChoiceReply.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage getChoiceReply() {
        Long userId = 0L;

        User user = currentUser();
        if (user != null) {
            userId = user.getId();
        }

        int userType = getRequestInt("userType");
        if (userType != UserType.STUDENT.getType()) {
            userType = UserType.PARENT.getType();
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("话题id不为空");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        boolean online = checkTopicOnline(talkTopic, mapMessage);
        if (!online) {
            return mapMessage;
        }

        // 读取精选id
        List<String> replyIds = getChoiceReplyIds(topicId, userType);
        if (CollectionUtils.isEmpty(replyIds)) {
            return mapMessage;
        }

        // 读取精选评论
        Map<String, TalkReplyShard> replyMap = talkLoader.getReplies(replyIds);
        if (MapUtils.isEmpty(replyMap)) {
            return mapMessage;
        }

        Long currentUserId = userId;

        List<TalkReplyShard> replies = replyMap.values().stream()
                .filter(x -> x.pass() && !x.isDelete() && !x.getUserId().equals(currentUserId))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(replies)) {
            return mapMessage;
        }

        replies = replies.size() >= 5 ? replies.subList(0, 5) : replies;
        Collection<Map<String, Object>> list = convertReplyWithQuote(talkTopic, replies, userId);

        mapMessage.put("replies", list);

        return mapMessage;
    }

    /**
     * 获取评论
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/loadReply.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadReplyById() {
        User user = currentUser();

        String replyId = getRequestString("replyId");
        if (StringUtils.isEmpty(replyId)) {
            return MapMessage.errorMessage("参数错误");
        }
        TalkReplyShard reply = talkLoader.getReplyById(replyId);
        if (reply == null) {
            return MapMessage.successMessage("评论不存在");
        }

        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(reply.getTopicId());

        MapMessage mapMessage = MapMessage.successMessage();

        boolean online = checkTopicOnline(talkTopic, mapMessage);
        if (!online) {
            return mapMessage;
        }

        Map<String, Map<String, Object>> map = convertReply(talkTopic, Collections.singletonList(reply), user != null ? user.getId() : 0L);
        Map<String, Object> replyMap = map.get(replyId);
        replyMap.put("optionId", reply.getOptionId());

        int quoteCount = CollectionUtils.isEmpty(reply.getSubIds()) ? 0 : reply.getSubIds().size();

        if (user != null) {
            List<TalkReplyShard> replies = getQuotedReplies(reply.getTopicId(), user.getId());
            long count = replies.stream().filter(x -> StringUtils.equals(x.getRootReplyId(), replyId)).count();
            quoteCount += count;
        }

        replyMap.put("quoteCount", String.valueOf(quoteCount));

        mapMessage.put("reply", replyMap);

        Map<String, Object> topicMap = new LinkedHashMap<>();
        topicMap.put("topicId", talkTopic.getTopicId());
        topicMap.put("title", talkTopic.getTitle());
        topicMap.put("topicType", talkTopic.getTopicType().getValue());
        List<Map<String, Object>> optionMapList = new LinkedList<>();
        for (TalkTopic.TalkOption option : talkTopic.getOptions()) {
            Map<String, Object> optionMap = new LinkedHashMap<>();
            optionMap.put("title", option.getContent());
            optionMap.put("optionId", option.getOptionId());
            optionMapList.add(optionMap);
        }
        topicMap.put("options", optionMapList);

        topicMap.put("studentEnable", talkTopic.studentEnable());
        // 是否参与投票

        putUserOption(topicMap, user, talkTopic);

        topicMap.put("topicType", talkTopic.getTopicType().getValue()); // 话题类型

        if (talkTopic.getTopicStartTime() != null) {
            topicMap.put("topicStartTime", String.valueOf(talkTopic.getTopicStartTime().getTime()));
        }
        if (talkTopic.getTopicEndTime() != null) {
            topicMap.put("topicEndTime", String.valueOf(talkTopic.getTopicEndTime().getTime()));
        }

        mapMessage.put("topic", topicMap);

        mapMessage.put("isLogin", user != null);

        return mapMessage;
    }

    // endregion

    // region 评论辅助方法
    private Collection<Map<String, Object>> convertReplyWithQuote(TalkTopic talkTopic, List<TalkReplyShard> replies, Long currentUserId) {
        if (talkTopic == null || CollectionUtils.isEmpty(replies)) {
            return Collections.emptyList();
        }

        // 格式化评论
        Map<String, Map<String, Object>> convertReplyMap =
                convertReply(talkTopic, replies, currentUserId);

        // 获取有回复的评论
        /*List<String> haveQuoteReplyIds = replies.stream()
                .filter(TalkReplyShard::replyQuoted)
                .map(TalkReplyShard::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(haveQuoteReplyIds)) {
            return convertReplyMap.values();
        }*/

        // 评论的数量
        Map<String, Long> quoteCountMap = replies.stream().collect(Collectors.toMap(TalkReplyShard::getId, reply -> CollectionUtils.isEmpty(reply.getSubIds()) ? 0L : reply.getSubIds().size()));

        // 评论和回复关联
        Map<String, List<String>> quoteReplyMap = replies.stream()
                .filter(reply -> CollectionUtils.isNotEmpty(reply.getSubIds()))
                .collect(Collectors.toMap(TalkReplyShard::getId, reply -> reply.getSubIds().stream().limit(2).collect(Collectors.toList())));

        // 没有获取到回复直接返回
        if (MapUtils.isNotEmpty(quoteReplyMap)) {

            // 回复id列表
            List<String> quoteIds = new LinkedList<>();
            quoteReplyMap.values().forEach(quoteIds::addAll);

            // 回复的评论
            Map<String, TalkReplyShard> replyMap = talkLoader.getReplies(quoteIds);

            if (MapUtils.isNotEmpty(replyMap)) {
                replies = replyMap.values().stream()
                        .filter(reply -> reply != null
                                && reply.getAudit() != null
                                && reply.getAudit().equals(TalkReply.AuditType.pass.getValue()) // 删除未审核
                                && reply.getDeleted() != null
                                && reply.getDeleted().equals(0)) // 删除已删除
                        .collect(Collectors.toList());
            }
        }

        // 当前用户的回复
        List<TalkReplyShard> userTalkReplies = getQuotedReplies(talkTopic.getTopicId(), currentUserId);

        if (CollectionUtils.isNotEmpty(userTalkReplies)) {
            // 合并回复
            replies.addAll(userTalkReplies);
            // 重新排序
            replies = replies.stream()
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toList());
            // 每个评论取前两条回复
            Set<String> keySet = convertReplyMap.keySet();
            List<TalkReplyShard> list = new LinkedList<>();
            for (String key : keySet) {

                long unAuditCount = userTalkReplies.stream()
                        .filter(x -> StringUtils.equals(x.getRootReplyId(), key))
                        .count();
                if (unAuditCount > 0) {
                    Long auditCount = quoteCountMap.get(key);
                    quoteCountMap.put(key, auditCount == null ? unAuditCount : auditCount + unAuditCount);
                }

                List<TalkReplyShard> item = replies.stream()
                        .filter(x -> StringUtils.equals(x.getRootReplyId(), key))
                        .limit(2)
                        .collect(Collectors.toList());
                list.addAll(item);
                quoteReplyMap.put(key, item.stream().map(TalkReplyShard::getId).collect(Collectors.toList()));
            }
            replies = list;
        }

        Map<String, Map<String, Object>> quotedConvertReplyMap =
                convertReply(talkTopic, replies, currentUserId);

        quoteReplyMap.keySet().forEach(key -> {
            List<String> quotedReplyIdList = quoteReplyMap.get(key);
            if (CollectionUtils.isNotEmpty(quotedReplyIdList)) {
                Map<String, Object> map = convertReplyMap.get(key);
                List<Map<String, Object>> list = new LinkedList<>();
                quotedReplyIdList.forEach(
                        id -> {
                            Map<String, Object> objectMap = quotedConvertReplyMap.get(id);
                            if (objectMap != null) {
                                list.add(objectMap);
                            }
                        });
                if (CollectionUtils.isNotEmpty(list)) {
                    map.put("quoteReplies", list);
                    map.put("quoted", quoteCountMap.getOrDefault(key, 0L));
                }
            }
        });

        return convertReplyMap.values();
    }

    private Map<String, Map<String, Object>> convertReply(
            TalkTopic talkTopic, List<TalkReplyShard> replies, Long currentUserId) {
        if (talkTopic == null || CollectionUtils.isEmpty(replies)) {
            return Collections.emptyMap();
        }

        // 评论id列表
        List<String> replyIds =
                replies.stream().map(TalkReplyShard::getId).collect(Collectors.toList());
        // 真实点赞数
        Map<String, Long> replyVoteCount =
                talkLoader.getReplyVoteCount(talkTopic.getTopicId(), replyIds);

        // 评论的学生
        Set<Long> studentIds =
                replies.stream().map(TalkReplyShard::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> students = studentLoader.loadStudents(studentIds);

        // 评论的家长
        Set<Long> parentIds = replies.stream().map(TalkReplyShard::getUserId).collect(Collectors.toSet());
        Map<Long, User> parentMap = userLoaderClient.loadUsers(parentIds);

        // 学生家长关联
        Map<Long, List<StudentParentRef>> studentParentRefs =
                studentLoaderClient.loadStudentParentRefs(studentIds);


        // 当前用户评论投票记录
        Map<String, Boolean> replyVoteRecord = replyVoteRecord(replyIds, currentUserId);

        Map<String, Map<String, Object>> map = new LinkedHashMap<>();

        // 数据转换
        for (TalkReplyShard reply : replies) {
            Map<String, Object> convertReply = convertReply(talkTopic, reply, parentMap, students, studentParentRefs, currentUserId, replyVoteCount, replyVoteRecord);
            map.put(reply.getId(), convertReply);
        }

        return map;
    }

    private Map<String, Object> convertReply(
            TalkTopic talkTopic,
            TalkReplyShard reply,
            Map<Long, User> parentMap,
            Map<Long, Student> students,
            Map<Long, List<StudentParentRef>> studentParentRefs,
            Long userId,
            Map<String, Long> replyVoteCount,
            Map<String, Boolean> replyVoteRecord) {
        if (reply == null) {
            return Collections.emptyMap();
        }

        // 家长（评论用户）
        User parent = parentMap.get(reply.getUserId());
        // 学生（评论时家长的孩子）
        Student student = students.get(reply.getStudentId());
        // 家长和孩子的关系
        List<StudentParentRef> parentList = studentParentRefs.get(reply.getStudentId());
        StudentParentRef relation = null;
        if (CollectionUtils.isNotEmpty(parentList)) {
            List<StudentParentRef> collect = parentList.stream()
                    .filter(x -> x.getParentId().equals(reply.getUserId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                relation = collect.get(0);
            }
        }
        // 评论投票数量
        Long voteCount = replyVoteCount.get(reply.getId());
        // 当前用户是否投票
        Boolean isVoted = replyVoteRecord.get(reply.getId());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("replyId", reply.getId()); // 评论id
        map.put("avatar", getUserAvatarImgUrl(parent)); // 用户头像
        map.put("title", generateNickName(parent, student, relation)); // 名称
        map.put("deleted", reply.getDeleted());
        TalkTopic.TalkOption option = talkTopic.getOptions().stream()
                .filter(x -> StringUtils.equals(x.getOptionId(), reply.getOptionId()))
                .findFirst()
                .orElse(null);
        map.put("option", option == null ? "" : option.getContent()); // 立场
        map.put("parseTime", DateFormat.parse(reply.getCreateTime())); // 时间
        map.put(
                "content",
                reply.getDeleted() != null && reply.getDeleted() == 1 ? "已删除" : reply.getConcept());
        map.put("voted", isVoted == null ? false : isVoted); // 是否投票
        map.put("praise", voteCount == null ? 0 : voteCount); // 点赞数量
        map.put("mine", userId.equals(reply.getUserId())); // 是否我的
        map.put("quoted", 0); // 被引用次数
        map.put("isParent", reply.isParentReplied());

        return map;
    }

    private Map<String, Boolean> replyVoteRecord(List<String> replyIds, Long userId) {
        if (CollectionUtils.isEmpty(replyIds) || null == userId || 0 >= userId) {
            return Collections.emptyMap();
        }

        List<String> voteIds = replyIds.stream()
                .map(replyId -> TalkReplyVoteShard.generateId(replyId, userId))
                .collect(Collectors.toList());
        Map<String, TalkReplyVoteShard> map = talkLoader.loadReplyVoted(voteIds);
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }

        Map<String, Boolean> collect = map.values().stream()
                .filter(vote -> !vote.isDelete())
                .collect(Collectors.toMap(TalkReplyVoteShard::getReplyId, vote -> !vote.isDelete()));
        return collect;
    }
    // endregion

    // region 我的评论

    private List<TalkReplyShard> getQuotedReplies(String topicId, Long userId) {
        if (StringUtils.isEmpty(topicId) || userId == null || userId <= 0) {
            return Collections.emptyList();
        }
        List<TalkReplyShard> userTalkReplies = talkLoader.getUserTalkReply(topicId, userId);
        // 过滤出用户未审核的回复

        Stream<TalkReplyShard> stream = userTalkReplies.stream()
                .filter(reply -> reply.getAudit() == null || !reply.getAudit().equals(TalkReply.AuditType.pass.getValue()))
                .filter(reply -> StringUtils.isNotEmpty(reply.getRootReplyId()));

        stream = stream.filter(reply -> reply.getDeleted() != null && reply.getDeleted().equals(0));

        return stream.collect(Collectors.toList());
    }

    /**
     * 我的评论（全部评论）
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getMyAuditReply.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMyAuditReplyList() {
        User user = currentUser();

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }

        if (user == null) {
            return MapMessage.successMessage(); // 未登录返回空
        }

        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        MapMessage mapMessage = MapMessage.successMessage();
        boolean online = checkTopicOnline(talkTopic, mapMessage);
        if (!online) {
            return mapMessage;
        }

        Set<Long> userIds = getFamilyUserId(user);

        // 读取评论数据
        List<TalkReplyShard> replies = new LinkedList<>();
        for (Long userId : userIds) {
            replies.addAll(talkLoader.getUserTalkReply(talkTopic.getTopicId(), userId));
        }

        if (CollectionUtils.isEmpty(replies)) {
            return mapMessage;
        }

        replies = replies.stream()
                .filter(reply -> StringUtils.isEmpty(reply.getRootReplyId()))
                .filter(x -> StringUtils.isNotEmpty(x.getConcept()))
                .filter(x -> x.getDeleted() != null && x.getDeleted().equals(0))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(replies)) {
            Long noticeTime = talkLoader.getFamilyNoticeTime(topicId, user.getId());
            TalkReplyShard reply = replies.stream()
                    .filter((TalkReplyShard talkReply) -> talkReply.getCreateTime().getTime() > noticeTime
                            && ((user.isParent() && !talkReply.isParentReplied()) || (!user.isParent() && talkReply.isParentReplied())))
                    .findFirst()
                    .orElse(null);
            if (reply != null) {
                if (user.isParent()) {
                    mapMessage.put("topNotice", "你的孩子有新的观点");
                } else {
                    mapMessage.put("topNotice", "你的家长有新的观点");
                }
                talkService.resetFamilyNoticeTime(topicId, user.getId());
            }
        }

        // 数据转换
        Collection<Map<String, Object>> list = convertReplyWithQuote(talkTopic, replies, user.getId());

        mapMessage.put("replies", list);

        return mapMessage;
    }

    @RequestMapping(
            value = "/setFamilyNoticeTime.vpage",
            method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage setFamilyNoticeTime() {
        User user = currentUser();

        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }

        talkService.resetFamilyNoticeTime(topicId, user.getId());

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/shard.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getShardReplyList() {
        long uid = getRequestLong("uid");
        if (uid <= 0) {
            return MapMessage.errorMessage("参数错误");
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }

        User user = raikouSystem.loadUser(uid);
        if (user == null) {
            return MapMessage.errorMessage("参数错误");
        }
        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        if (talkTopic == null) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        boolean online = checkTopicOnline(talkTopic, mapMessage);
        if (!online) {
            return mapMessage;
        }

        Set<Long> userIds = getFamilyUserId(user);

        // 读取评论数据
        List<TalkReplyShard> replies = new LinkedList<>();
        for (Long userId : userIds) {
            replies.addAll(talkLoader.getUserTalkReply(talkTopic.getTopicId(), userId));
        }

        if (CollectionUtils.isEmpty(replies)) {
            return mapMessage;
        }

        replies = replies.stream()
                .filter(reply -> StringUtils.isEmpty(reply.getRootReplyId()))
                .filter(x -> StringUtils.isNotEmpty(x.getConcept()))
                .filter(x -> x.getDeleted() != null && x.getDeleted().equals(0))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        if (user.isParent()) {
            TalkReplyShard reply =
                    replies.stream().filter(x -> x.getUserId().equals(uid)).findFirst().orElse(null);
            String parentOpinion = reply == null ? "" : reply.getOptionId();
            Set<String> opinionList = replies.stream()
                    .filter(x -> !x.isParentReplied())
                    .map(TalkReplyShard::getOptionId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(opinionList)) {
                mapMessage.put("topNotice", "");
            } else {
                if ("".equals(parentOpinion)) {
                    mapMessage.put("topNotice", "");
                } else if (opinionList.size() > 1) {
                    mapMessage.put("topNotice", "我和孩子观点竟然不一致？！要多听听孩子真实的想法了！");
                } else if (opinionList.size() == 1) {
                    String next = opinionList.iterator().next();
                    mapMessage.put(
                            "topNotice",
                            StringUtils.equals(next, parentOpinion)
                                    ? "我和孩子观点一致，默契十足！"
                                    : "我和孩子观点竟然不一致？！要多听听孩子真实的想法了！");
                }
            }
        }

        // 数据转换
        Collection<Map<String, Object>> list = convertReplyWithQuote(talkTopic, replies, user.getId());

        mapMessage.put("replies", list);

        if (user.isParent()) {
            Long cid = getRequestLong("cid");
            if (cid == 0L) {
                cid = parentLoaderClient.loadParentStudentRefs(uid).stream()
                        .map(StudentParentRef::getStudentId)
                        .findFirst()
                        .orElse(0L);
            }
            User student = studentLoaderClient.loadStudent(cid);
            String nickname;
            if (student == null) {
                nickname = user.getProfile().getRealname();
            } else {
                nickname = student.getProfile().getRealname();
                if (StringUtils.isEmpty(nickname)) {
                    nickname = sensitiveUserDataServiceClient.loadUserMobileObscured(user.getId());
                }
            }

            mapMessage.put("nickname", nickname);
            mapMessage.put("avatar", getUserAvatarImgUrl(user));
            mapMessage.put("isParent", true);
        } else {
            mapMessage.put("nickname", user.getProfile().getRealname());
            mapMessage.put("avatar", getUserAvatarImgUrl(user));
            mapMessage.put("isParent", false);
        }

        return mapMessage;
    }

    private Set<Long> getFamilyUserId(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        Set<Long> userIds = new LinkedHashSet<>();
        userIds.add(user.getId());

        if (user.isStudent()) {
            Map<Long, List<StudentParentRef>> refs =
                    studentLoaderClient.loadStudentParentRefs(Collections.singleton(user.getId()));
            List<StudentParentRef> parentRefs = refs.get(user.getId());
            if (CollectionUtils.isNotEmpty(parentRefs)) {
                userIds.addAll(
                        parentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet()));
            }
        } else {
            Map<Long, List<StudentParentRef>> studentRefs =
                    parentLoaderClient.loadParentStudentRefs(Collections.singleton(user.getId()));
            if (MapUtils.isNotEmpty(studentRefs)) {
                List<StudentParentRef> parentRefs = studentRefs.get(user.getId());
                if (CollectionUtils.isNotEmpty(parentRefs)) {
                    List<Long> studentIds =
                            parentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
                    userIds.addAll(studentIds);
                    Map<Long, List<StudentParentRef>> map =
                            studentLoaderClient.loadStudentParentRefs(studentIds);
                    if (MapUtils.isNotEmpty(map)) {
                        map.values().forEach(x -> {
                            if (CollectionUtils.isNotEmpty(x)) {
                                userIds.addAll(
                                        x.stream()
                                                .map(StudentParentRef::getParentId)
                                                .collect(Collectors.toSet()));
                            }
                        });
                    }
                }
            }
        }

        return userIds;
    }

    /**
     * 我的评论（不包括已经审核通过的评论，包括部分话题信息）
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getMyReply.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage getMyReply() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }

        MapMessage mapMessage = MapMessage.successMessage();

        // 话题信息
        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        boolean online = checkTopicOnline(talkTopic, mapMessage);
        if (!online) {
            return mapMessage;
        }

        mapMessage.put("topicId", topicId);
        mapMessage.put("topicTitle", talkTopic.getTitle());
        mapMessage.put("topicType", talkTopic.getTopicType().getValue());

        // 话题观念
        List<TalkTopic.TalkOption> options = talkTopic.getOptions();
        if (CollectionUtils.isNotEmpty(options)) {
            List<Map<String, Object>> maps = new LinkedList<>();
            for (TalkTopic.TalkOption option : options) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("optionId", option.getOptionId());
                map.put("title", option.getContent());
                map.put("summary", option.getSummary());
                map.put("shard", option.getShard());
                maps.add(map);
            }
            mapMessage.put("options", maps);
        }

        // 加载用户的观念
        loadUserReply(mapMessage, talkTopic, user);

        return mapMessage;
    }
    // endregion

    // region 评论处理 发表、删除、点赞以及视频课程报名

    private void loadUserReply(MapMessage mapMessage, TalkTopic talkTopic, User parent) {

        List<TalkReplyShard> replies = talkLoader.getUserTalkReply(talkTopic.getTopicId(), parent.getId());

        if (CollectionUtils.isEmpty(replies)) {
            return;
        }

        // 投票页面观念处理
        String optionId = replies.get(0).getOptionId();
        List<TalkTopic.TalkOption> optionList = talkTopic.getOptions()
                .stream()
                .filter(x -> StringUtils.equals(optionId, x.getOptionId()))
                .collect(Collectors.toList());
        if (optionList.size() > 0) {
            TalkTopic.TalkOption option = optionList.get(0);
            mapMessage.put("optionId", option.getOptionId());
            mapMessage.put("optionTitle", option.getContent());
        }

        // 数据筛选，去掉空评论内容
        replies = replies.stream()
                .filter(x -> StringUtils.isNotEmpty(x.getConcept()))
                .collect(Collectors.toList());
        // 投票页面只显示未审核
        replies = replies.stream()
                .filter(
                        x ->
                                x.getAudit() != null
                                        && x.getAudit().equals(TalkReply.AuditType.unAudit.getValue()))
                .collect(Collectors.toList());

        // 排序
        replies = replies.stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());

        Collection<Map<String, Object>> list = convertReply(talkTopic, replies, parent.getId()).values();
        mapMessage.put("replies", list);
    }

    /**
     * 发布观念
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/publishReply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publishReply() {
        User user = currentUser();

        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String topicId = getRequestString("topicId");
        String optionId = getRequestString("optionId");
        String content = getRequestString("content");
        String quoteReplyId = getRequestString("quoteReplyId"); // v2.5新增
        if (StringUtils.isEmpty(topicId) || StringUtils.isEmpty(optionId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            MapMessage expiry = validTopic(topicId);
            if (expiry != null) {
                return expiry;
            }

            Long userId = user.getId();
            Long studentId = getStudentId(user);
            Long schoolId = getSchoolId(studentId);

            TalkReplyShard reply = new TalkReplyShard();
            reply.setUserId(userId);
            reply.setStudentId(studentId);
            reply.setConcept(content);
            reply.setTopicId(topicId);
            reply.setOptionId(optionId);
            reply.setSchoolId(schoolId);
            reply.setReplierType(user.getUserType());
            reply.setQuoteReplyId(quoteReplyId); // 引用评论id v2.5新增

            return talkService.replyTopic(reply);
        } catch (Exception ex) {
            log.error("topicId:{},optionId:{},content:{},quoteReplyId:{}", topicId, optionId, content, quoteReplyId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 删除观念
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/deleteReply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteReply() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String topicId = getRequestString("topicId");

        MapMessage expiry = validTopic(topicId);
        if (expiry != null) {
            return expiry;
        }

        String replyId = getRequestString("replyId");
        if (StringUtils.isEmpty(topicId) || StringUtils.isEmpty(replyId)) {
            return MapMessage.errorMessage("参数错误");
        }

        return talkService.deleteReply(user.getId(), topicId, replyId);
    }

    /**
     * 点赞评论
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/praiseReply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage praiseReply() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long studentId = getStudentId(user.getId());

        Long schoolId = getSchoolId(studentId);

        String topicId = getRequestString("topicId");

        MapMessage expiry = validTopic(topicId);
        if (expiry != null) {
            return expiry;
        }

        String replyId = getRequestString("replyId");
        if (StringUtils.isEmpty(topicId) || StringUtils.isEmpty(replyId)) {
            return MapMessage.errorMessage("参数错误");
        }

        talkService.processReplyVote(topicId, replyId, schoolId, user.getId());

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/joinVideo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinVideo() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }

        return talkService.joinVideo(topicId, user.getId());
    }
    // endregion

    // region 音频和音频点赞

    /**
     * 验证话题的有效性
     *
     * @param topicId 话题id
     * @return 无效话题返回MapMessage，否则返回NULL
     */
    private MapMessage validTopic(String topicId) {
        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        if (talkTopic == null) {
            return MapMessage.errorMessage("话题不存在");
        }

        if (talkTopic.getDeleted() != null && talkTopic.getDeleted() == 1) {
            return MapMessage.errorMessage("话题被删除");
        }

        return null;
    }

    /**
     * 获取音频
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/getAudio.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAudio() {
        return MapMessage.successMessage();
        /*User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage message = MapMessage.successMessage();
        TalkTopic topic = talkLoaderClient.getTalkTopicById(topicId);
        boolean online = checkTopicOnline(topic, message);
        if (!online) {
            return message;
        }

        if (StringUtils.isEmpty(topic.getAudioId())) {
            return MapMessage.errorMessage("数据不存在");
        }

        TalkAudio audio = talkLoaderClient.getTalkAudioById(topic.getAudioId());
        if (audio == null) {
            return MapMessage.errorMessage("数据不存在");
        }

        message.put("topicId", topic.getTopicId());
        message.put("topicTitle", topic.getTitle());
        message.put("guestAvatar", topic.getGuestAvatar());
        if (topic.getVideoStartTime() != null) {
            message.put("startTime", String.valueOf(topic.getVideoStartTime().getTime()));
        }
        if (topic.getVideoEndTime() != null) {
            message.put("endTime", String.valueOf(topic.getVideoEndTime().getTime()));
        }

        message.put("currentTime", String.valueOf(System.currentTimeMillis()));

        // 点赞人数
        Long voteUserCount = talkLoader.loadAudioVoteUserCount(topicId);
        message.put("support", voteUserCount);

        // 自己是否点过赞
        Map<String, Boolean> vote =
                talkLoader.loadVote(
                        Collections.singletonList(topicId), TalkVote.VoteType.audio, parent.getId());
        message.put("supported", vote.get(topicId));

        List<TalkAudio.AudioContent> contents = audio.getContents();
        if (CollectionUtils.isNotEmpty(contents)) {
            List<Map<String, Object>> list = new LinkedList<>();
            for (TalkAudio.AudioContent content : contents) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("content", StringUtils.replace(content.getContent(), "\r\n", "<br/>"));
                map.put("duration", content.getDuration());
                map.put("audioType", content.getAudioType().getValue());
                list.add(map);
            }
            message.put("content", list);
        }

        return message;*/
    }
    // endregion

    // region 学生和学校默认信息补偿

    /**
     * 音频点赞
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/praiseAudio.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage praiseAudio() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }

        TalkTopic talkTopic = talkLoaderClient.getTalkTopicById(topicId);
        if (talkTopic == null
                || talkTopic.getTopicType() != TalkTopicType.audio
                || StringUtils.isEmpty(talkTopic.getAudioId())) {
            return MapMessage.errorMessage("参数错误");
        }

        talkService.processAudioVote(topicId, parent.getId());

        return MapMessage.successMessage();
    }

    /**
     * 根据用户id获取学生
     *
     * @param parentId 家长id
     * @return 孩子id，无孩子返回0
     */
    private Long getStudentId(Long parentId) {
        // 获取学生id
        long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            // 没有传学生id，获取家长第一个孩子
            List<StudentParentRef> parentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
            StudentParentRef ref = parentRefs.stream()
                    .min(Comparator.comparing(StudentParentRef::getStudentId))
                    .orElse(null);
            return ref == null ? 0L : ref.getStudentId();
        } else {
            return studentId;
        }
    }

    /**
     * 获取学生id
     *
     * @param user 当前用户
     * @return long 学生id
     */
    private Long getStudentId(User user) {
        // 当前用户为NULL，返回0L;
        if (user == null) {
            return 0L;
        }

        // 当前用户是学生，返回学生id
        if (user.isStudent()) {
            return user.getId();
        }

        // 当前用户是家长，如果参数有孩子，返回孩子id，如果没有则返回第一个孩子，没有孩子，则返回0L;
        Long studentId = getRequestLong("sid");
        if (studentId == 0L) {
            List<StudentParentRef> parentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
            // 没有传学生id，获取家长第一个孩子
            if (CollectionUtils.isNotEmpty(parentRefs)) {
                parentRefs =
                        parentRefs
                                .stream()
                                .sorted(Comparator.comparing(StudentParentRef::getStudentId))
                                .collect(Collectors.toList());
                studentId = parentRefs.get(0).getStudentId();
            }
        }
        return studentId;
    }
    // endregion

    /**
     * 根据学生id获取学校id
     *
     * @param studentId 学生id
     * @return 学校id
     */
    private Long getSchoolId(Long studentId) {
        if (studentId == null || studentId == 0L) {
            return 0L;
        }

        // 获取学校明细
        StudentDetail detail = studentLoaderClient.loadStudentDetail(studentId);
        if (detail != null) {
            Clazz clazz = detail.getClazz();
            if (clazz != null) {
                return clazz.getSchoolId();
            }
        }

        return 0L;
    }

    // region 直播链接和回放链接
    @RequestMapping(value = "/liveUrl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLiveUrl() {
        // 直播id
        String liveId = getRequestString("liveId");
        if (StringUtils.isEmpty(liveId)) {
            return MapMessage.errorMessage("直播id不为空");
        }

        // 当前家长信息
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        // 获取当前学生信息，并根据学生信息以及学生和家长关系，计算nickname;
        Long sid = getStudentId(user.getId());

        StudentParentRef parentRef = null;
        Student student = null;
        if (user.isParent() && sid > 0) {
            student = studentLoaderClient.loadStudent(sid);
            List<StudentParentRef> parentRefs = studentLoaderClient.loadStudentParentRefs(sid);
            if (CollectionUtils.isNotEmpty(parentRefs)) {
                parentRefs = parentRefs.stream()
                        .filter(x -> user.getId().equals(x.getParentId()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(parentRefs)) {
                    parentRef = parentRefs.get(0);
                }
            }
        }
        String nickName = generateNickName(user, student, parentRef);

        String urlTpl;
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            // 生产环境
            urlTpl = "https://livecdn.17zuoye.com/zylive/index.html";
        } else {
            // 测试环境
            urlTpl = "https://activity.test.17zuoye.net/index.html";
        }

        // 封装参数
        Map<String, String> parameter = new HashMap<>();
        parameter.put("nickname", nickName);
        parameter.put("user_id", "zjt" + String.valueOf(user.getId()));

        String avatarUrl = super.getUserAvatarImgUrl(user);
        if (!avatarUrl.endsWith("avatar_default.png")) {
            avatarUrl += "?x-oss-process=image/resize,w_30,h_30";
        }

        parameter.put("avatar_url", avatarUrl);
        parameter.put("user_type", "1"); // 固定位学生身份
        parameter.put("live_id", liveId);
        parameter.put("room_index", "666"); // 暂时不做区分，影响弹幕
        parameter.put("timestamp", String.valueOf(System.currentTimeMillis()));

        sign(parameter);

        // 可选参数
        parameter.put("ticket", null);
        parameter.put("recommend_text", null);
        parameter.put("recommend_url", null);

        String query = UrlUtils.buildUrlQuery(urlTpl, parameter);
        return MapMessage.successMessage().add("liveUrl", query);
    }

    @RequestMapping(value = "/playbackUrl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPlayBackUrl() {

        User user = currentUser();

        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        String liveId = getRequestString("liveId");
        if (StringUtils.isEmpty(liveId)) {
            return MapMessage.errorMessage("直播id不存在");
        }

        String remote;
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            // 生产环境
            remote = "https://dfchain.17zuoye.com/v1/get-mp4";
        } else {
            // 测试环境
            remote = "http://10.200.3.140:8888/v1/get-mp4";
        }

        // 参数签名
        Map<String, String> parameter = new HashMap<>();
        parameter.put("uid", String.valueOf(user.getId()));
        parameter.put("live_id", liveId);
        parameter.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000 + 3600 * 24 * 9));
        sign(parameter);

        // 请求m3u8
        String query = UrlUtils.buildUrlQuery(remote, parameter);

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(query).execute();
        String responseString = response.getResponseString();
        Map<String, Object> json = JsonUtils.fromJson(responseString);
        Object code = json.get("code");
        if (StringUtils.equals(SafeConverter.toString(code), "0")) {
            Object url = json.get("url");
            return MapMessage.successMessage().add("playback", SafeConverter.toString(url));
        } else {
            return MapMessage.errorMessage("请求失败");
        }
    }

    private void sign(Map<String, String> parameter) {
        // 根据环境选择appId和appSecret
        String appId;
        String appSecret;
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            // 生产环境
            appId = "59a91c3237d3d8d28516801c";
            appSecret = "ea4958b53cd9da924e1223252d5d215b";
        } else {
            // 测试环境
            appId = "58eee6ac19b005fec0d848ce";
            appSecret = "4911898908f9d03ae7bf913f2ae16cb1";
        }

        parameter.put("app_id", appId);

        // key排序
        List<String> keys = new ArrayList<>(parameter.keySet());
        Collections.sort(keys);

        // 拼接参数
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            builder.append(key);
            builder.append("=");
            builder.append(parameter.get(key));
            builder.append("&");
        }
        builder.setLength(builder.length() - 1);
        // 秘钥算法
        SecretKeySpec signingKey = new SecretKeySpec(appSecret.getBytes(), "HmacSHA256");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            return;
        }
        try {
            mac.init(signingKey);
        } catch (InvalidKeyException e) {
            return;
        }
        // 签名
        String sign;
        sign = Hex.encodeHexString(mac.doFinal(builder.toString().getBytes(StandardCharsets.UTF_8)));
        parameter.put("sign", sign);
    }

    @RequestMapping(value = "/liveJson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLiveJson() {
        // 当前家长信息
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        // 当前话题&话题判断
        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty("topicId")) {
            return MapMessage.errorMessage("参数错误");
        }

        TalkTopic talkTopic = this.talkLoaderClient.getTalkTopicById(topicId);
        if (talkTopic == null) {
            return MapMessage.errorMessage("话题不存在");
        }

        if (talkTopic.getTopicType() != TalkTopicType.video
                || StringUtils.isEmpty(talkTopic.getVideoAddress())) {
            return MapMessage.errorMessage("当前话题不是直播话题");
        }

        if (talkTopic.getTopicEndTime().getTime() < System.currentTimeMillis()) {
            return MapMessage.errorMessage("当前直播已经结束");
        }

        // 获取当前学生信息，并根据学生信息以及学生和家长关系，计算nickname;
        Long sid = getStudentId(user.getId());

        StudentParentRef parentRef = null;
        Student student = null;
        if (user.isParent() && sid > 0) {
            student = studentLoaderClient.loadStudent(sid);
            List<StudentParentRef> parentRefs = studentLoaderClient.loadStudentParentRefs(sid);
            if (CollectionUtils.isNotEmpty(parentRefs)) {
                parentRef = parentRefs.stream()
                        .filter(x -> user.getId().equals(x.getParentId()))
                        .findFirst()
                        .orElse(null);
            }
        }
        String nickName = generateNickName(user, student, parentRef);

        MapMessage message = MapMessage.successMessage();

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "live_yqzy");
        content.put("play_mode", "1");
        Map<String, String> data = new LinkedHashMap<>();
        data.put("nickname", nickName);
        data.put("user_id", "zjt" + String.valueOf(user.getId()));

        String avatarUrl = super.getUserAvatarImgUrl(user);
        if (!avatarUrl.endsWith("avatar_default.png")) {
            avatarUrl += "?x-oss-process=image/resize,w_30,h_30";
        }
        data.put("avatar_url", avatarUrl);
        data.put("user_type", "1");
        data.put("live_id", talkTopic.getVideoAddress());
        data.put("room_index", "666");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        sign(data);
        data.put("class_type", "4");
        data.put("course_type", "3");

        content.put("data", data);

        message.set("content", content);
        return message;
    }
    // endregion

    // region 用户信息辅助方法

    /**
     * 生成用户昵称
     *
     * @param parent    家长
     * @param student   学生
     * @param parentRef 家长学生关系
     * @return 昵称
     */
    private String generateNickName(User parent, User student, StudentParentRef parentRef) {
        String call = null;
        if (student != null && student.getProfile() != null && parentRef != null) {
            call = student.getProfile().getRealname() + parentRef.getCallName();
        }

        if (StringUtils.isEmpty(call)) {
            if (parent.getProfile() != null) {
                call = parent.getProfile().getRealname();
            }
            if (StringUtils.isEmpty(call)) {
                call = sensitiveUserDataServiceClient.loadUserMobileObscured(parent.getId());
            }
        }

        return call;
    }
    // endregion

    @RequestMapping(value = "/shardTopic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shardTopic() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录不记录分享");
        }

        String topicId = getRequestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("参数错误");
        }

        TalkTopic topic = talkLoaderClient.getTalkTopicById(topicId);
        if (topic == null) {
            return MapMessage.errorMessage("话题不存在");
        }

        if (!topic.haveProduct()) {
            return MapMessage.errorMessage("没有商品");
        }

        talkService.shardTopic(topicId, user.getId());
        return MapMessage.successMessage();
    }

    /**
     * 获取用户头像
     *
     * @param user 用户
     * @return 当用户不存在或用户没有头像时，返回NULL
     */
    @Override
    protected String getUserAvatarImgUrl(User user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.isNotEmpty(user.fetchImageUrl())) {
            return getCdnBaseUrlAvatarWithSep() + "gridfs/" + user.fetchImageUrl();
        } else {
            return null;
        }
    }

    // region 通知
    @ResponseBody
    @RequestMapping(value = "/getNewNotice.vpage", method = RequestMethod.GET)
    public MapMessage getNewNotice() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage();
        }

        TalkTopic topic = talkLoaderClient
                .getAllTalkTopic()
                .stream()
                .filter(x -> x.getPeriod() != null && x.getPeriod().equals(TalkTopicPeriod.current))
                .findFirst()
                .orElse(null);

        if (topic == null
                || topic.getPeriod() == null
                || !topic.getPeriod().equals(TalkTopicPeriod.current)) {
            return MapMessage.errorMessage();
        }

        String topicId = topic.getTopicId();

        MapMessage message = MapMessage.successMessage();

        long min = talkLoader.getLastNoticeTime(topicId, user.getId());
        /*if (min == 0) {
            return message;
        }*/

        List<String> noticeIds = talkLoader.getNewNoticeIds(user.getId(), System.currentTimeMillis(), min, 10000L);
        Map<String, TalkNotice> noticeMap = talkLoader.getNotices(noticeIds);
        if (MapUtils.isEmpty(noticeMap)) {
            return message;
        }
        List<TalkNotice> notices = new LinkedList<>(noticeMap.values());
        notices = notices.stream().filter(x -> StringUtils.equals(x.getTopicId(), topicId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(notices)) {
            return message;
        }

        Set<Integer> noticeCodes =
                notices.stream().map(x -> x.getNoticeType().getCode()).collect(Collectors.toSet());
        int count = noticeCodes.size();
        if (count > 1) {
            message.put("message",
                    StringUtils.formatMessage(TalkNotice.NoticeType.normal.getText(), notices.size()));
        } else if (count == 1) {
            TalkNotice.NoticeType type =
                    TalkNotice.NoticeType.valueOf(noticeCodes.stream().findFirst().get());
            if (type.equals(TalkNotice.NoticeType.voted)) {
                Set<String> set =
                        notices.stream().map(TalkNotice::getTargetId).collect(Collectors.toSet());
                Map<String, Long> voteCountMap =
                        talkLoader.getReplyVoteCount(topicId, new ArrayList<>(set));
                long total = 0L;
                for (Long value : voteCountMap.values()) {
                    if (value != null) {
                        total += value;
                    }
                }
                message.put("message", StringUtils.formatMessage(type.getText(), notices.size(), total));
            } else {
                message.put("message", StringUtils.formatMessage(type.getText(), notices.size()));
            }
        }

        return message;
    }

    @RequestMapping(value = "/getNoticeList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNoticeList() {
        User user = currentUser();

        if (user == null) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        long current = System.currentTimeMillis();

        long timestamp = getRequestLong("timestamp", current);

        if (timestamp == 0) {
            return MapMessage.successMessage();
        }

        if (current == timestamp) {
            resetNoticeTime(user.getId());
        }

        List<String> noticeIds = talkLoader.getNewNoticeIds(user.getId(), timestamp, 0, 30);
        Map<String, TalkNotice> noticeMap = talkLoader.getNotices(noticeIds);
        if (MapUtils.isEmpty(noticeMap)) {
            return MapMessage.successMessage();
        }
        List<TalkNotice> list = new LinkedList<>(noticeMap.values());
        MapMessage message = MapMessage.successMessage();

        if (CollectionUtils.isEmpty(list)) {
            message.put("timestamp", "0");
            return message;
        }
        Map<String, TalkReplyShard> replyMap = Collections.emptyMap();
        Set<String> replyIds = list.stream()
                .filter(x -> x.getNoticeType().equals(TalkNotice.NoticeType.reply)
                        || x.getNoticeType().equals(TalkNotice.NoticeType.voted)
                        || x.getNoticeType().equals(TalkNotice.NoticeType.audit))
                .map(TalkNotice::getTargetId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(replyIds)) {
            replyMap = talkLoader.getReplies(new LinkedList<>(replyIds));
            replyIds = replyMap.values()
                    .stream()
                    .filter(x -> StringUtils.isNotEmpty(x.getQuoteReplyId()))
                    .map(TalkReplyShard::getQuoteReplyId)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isNotEmpty(replyIds)) {
                replyMap.putAll(talkLoader.getReplies(new LinkedList<>(replyIds)));
            }
        }

        Collection<TalkReplyShard> replies = replyMap.values();

        Map<String, Long> voteCountMap = new LinkedHashMap<>();
        Set<String> topicIds = replies.stream().map(TalkReplyShard::getTopicId).collect(Collectors.toSet());
        for (String topicId : topicIds) {
            Set<String> ids = replies.stream()
                    .filter(x -> StringUtils.equals(x.getTopicId(), topicId))
                    .map(TalkReplyShard::getId)
                    .collect(Collectors.toSet());
            Map<String, Long> count = talkLoader.getReplyVoteCount(topicId, new LinkedList<>(ids));
            if (MapUtils.isNotEmpty(count)) {
                voteCountMap.putAll(count);
            }
        }

        // 评论的学生
        Set<Long> studentIds =
                replies.stream().map(TalkReplyShard::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> students = studentLoader.loadStudents(studentIds);

        // 评论的家长
        Set<Long> parentIds = replies.stream().map(TalkReplyShard::getUserId).collect(Collectors.toSet());
        Map<Long, User> parentMap = userLoaderClient.loadUsers(parentIds);

        // 学生家长关联
        Map<Long, List<StudentParentRef>> studentParentRefs =
                studentLoaderClient.loadStudentParentRefs(studentIds);

        List<Map<String, Object>> mapList = new LinkedList<>();

        List<TalkTopic> topics = this.talkLoaderClient.getAllTalkTopic();

        for (TalkNotice notice : list) {
            Map<String, Object> map = new LinkedHashMap<>();

            TalkTopic topic = topics.stream()
                    .filter(x -> StringUtils.equals(x.getTopicId(), notice.getTopicId()))
                    .findFirst()
                    .orElse(null);
            if (topic == null) {
                continue;
            }

            map.put("topicId", notice.getTopicId()); // 话题id
            // map.put("noticeType", notice.getNoticeType()); //评论类型
            map.put("icon", notice.getNoticeType().getIcon()); // 通知图标
            map.put("title", notice.getNoticeType().getTitle()); // 通知标题
            map.put("time", DateUtils.dateToString(notice.getUpdateTime(), "yyyy-MM-dd"));

            if (notice.getNoticeType().equals(TalkNotice.NoticeType.award)) {
                map.put("content", "您参与的话题“" + topic.getTitle() + "”已中奖");
                mapList.add(map);
                continue;
            }

            TalkReplyShard reply = replyMap.get(notice.getTargetId());
            if (reply == null) {
                continue;
            }
            map.put("replyId",
                    StringUtils.isEmpty(reply.getRootReplyId())
                            ? reply.getId()
                            : reply.getRootReplyId());

            if (notice.getNoticeType().equals(TalkNotice.NoticeType.audit)) {
                map.put("content", "您的评论“" + substring(reply.getConcept()) + "”");
            } else if (notice.getNoticeType().equals(TalkNotice.NoticeType.reply)) {
                // 家长（评论用户）
                User parent = parentMap.get(reply.getUserId());
                // 学生（评论时家长的孩子）
                Student student = students.get(reply.getStudentId());
                // 家长和孩子的关系
                List<StudentParentRef> parentList = studentParentRefs.get(reply.getStudentId());
                StudentParentRef relation = null;
                if (CollectionUtils.isNotEmpty(parentList)) {
                    List<StudentParentRef> collect = parentList.stream()
                            .filter(x -> x.getParentId().equals(reply.getUserId()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        relation = collect.get(0);
                    }
                }
                String nickname = generateNickName(parent, student, relation);

                TalkReplyShard quoted = replyMap.get(reply.getQuoteReplyId());
                if (quoted == null) {
                    continue;
                }
                String builder = String.format("%s回复了你的评论“%s”", nickname, substring(quoted.getConcept()));
                map.put("content", builder);
                map.put("quoted", reply.getConcept());
                // map.put("id", reply.getRootReplyId());
            } else if (notice.getNoticeType().equals(TalkNotice.NoticeType.voted)) {
                String builder =
                        String.format(
                                "有%d个人赞同了你的评论“%s”",
                                voteCountMap.get(reply.getId()), substring(reply.getConcept()));
                map.put("content", builder);
            }

            mapList.add(map);
        }
        message.put("list", mapList);
        return message;
    }

    private void resetNoticeTime(Long userId) {
        TalkTopic topic = talkLoaderClient.getAllTalkTopic().stream()
                .filter(x -> x.getDeleted() != null && x.getDeleted() == 0)
                .filter(x -> x.getPeriod() != null && x.getPeriod().equals(TalkTopicPeriod.current))
                .findFirst()
                .orElse(null);

        if (topic == null) {
            return;
        }

        talkService.resetNoticeTime(topic.getTopicId(), userId);
    }
    // endregion

    /**
     * 重写当前用户，过滤出是家长和孩子的用户
     *
     * @return user
     */
    @Override
    protected User currentUser() {
        User user;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            long uid = getRequestLong("pid");
            if (uid > 0) {
                user = raikouSystem.loadUser(uid);
                return user;
            }
        }
        user = super.currentUser();
        if (user == null) {
            return null;
        }
        return user.isParent() || user.isStudent() ? user : null;
    }

    /**
     * 时间格式化工具类
     */
    private static class DateFormat {
        private static final long ONE_MIN = 60000L;
        private static final long ONE_HOR = 3600000L;

        public static String parse(Date createTime) {
            long current = System.currentTimeMillis();
            long create = createTime.getTime();

            long times = current - create;
            if (times <= ONE_MIN) {
                return "刚刚";
            } else if (times < ONE_HOR) {
                return (times / ONE_MIN) + "分钟前";
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(create);
                StringBuilder builder = new StringBuilder();
                DayRange range = DayRange.newInstance(current);
                if (range.getStartTime() <= create) {
                    builder.append("今天");
                } else {
                    range = range.previous();
                    if (range.getStartTime() <= create) {
                        builder.append("昨天");
                    } else {
                        range = range.previous();
                        if (range.getStartTime() <= create) {
                            builder.append("前天");
                        } else {
                            return DateUtils.dateToString(createTime, FORMAT_SQL_DATE);
                        }
                    }
                }
                builder.append(calendar.get(Calendar.HOUR_OF_DAY));
                builder.append(":");
                String min = String.valueOf(calendar.get(Calendar.MINUTE));
                builder.append(min.length() == 1 ? "0" + min : min);
                return builder.toString();
            }
        }
    }

    private static String calculatePriceDifference(String price1, String price2) {
        BigDecimal p1 = new BigDecimal(SafeConverter.toDouble(price1));
        BigDecimal p2 = new BigDecimal(SafeConverter.toDouble(price2));
        return p1.subtract(p2)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}