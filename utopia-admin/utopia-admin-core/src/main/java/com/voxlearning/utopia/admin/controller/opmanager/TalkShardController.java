package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.parent.api.CrmTalkShardLoader;
import com.voxlearning.utopia.service.parent.api.CrmTalkShardService;
import com.voxlearning.utopia.service.parent.api.TalkShardService;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkAudio;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkReply;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkReplyShard;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkTopic;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.talk.ScoreTalkContext;
import com.voxlearning.utopia.service.parent.api.mapper.talk.TalkReplyQueryRequest;
import com.voxlearning.utopia.service.parent.api.mapper.talk.TopicTotalValue;
import com.voxlearning.utopia.service.parent.constant.TalkTopicPeriod;
import com.voxlearning.utopia.service.parent.constant.TalkTopicType;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 17TALK crm controller
 */

/**
 * 17TALK 话题
 */
@Controller
@Slf4j
@RequestMapping(value = "/opmanager/talk")
public class TalkShardController extends OpManagerAbstractController {
    
    @ImportService(interfaceClass = CrmTalkShardLoader.class)
    private CrmTalkShardLoader crmTalkLoader;
    @ImportService(interfaceClass = CrmTalkShardService.class)
    private CrmTalkShardService crmTalkService;
    @ImportService(interfaceClass = TalkShardService.class)
    private TalkShardService talkService;

    //region topic


    @RequestMapping(value = "/topicpublish.vpage", method = RequestMethod.GET)
    public String topicPublish(Model model) {
        String topicId = requestString("topicId");

        Map<String, Object> map = new HashMap<>();
        Map<String, String> audioMap = new HashMap<>();

        List<TalkAudio> audioList = crmTalkLoader.getAllAudio();
        if (CollectionUtils.isNotEmpty(audioList)) {
            for (TalkAudio audio : audioList) {
                audioMap.put(audio.getAudioId(), audio.getTitle());
            }
        }

        if (StringUtils.isNotEmpty(topicId)) {
            TalkTopic topic = crmTalkLoader.getTopicById(topicId);
            if (topic != null) {
                map.put("topicId", topic.getTopicId());
                map.put("title", topic.getTitle());
                map.put("titleImage", topic.getTitleImage());
                map.put("subtitle", topic.getSubtitle());
                map.put("subtitleImage", topic.getSubTitleImage());
                map.put("topicNumber", topic.getTopicNumber());
                if (topic.getTopicStartTime() != null) {
                    map.put("topicStartTime", DateUtils.dateToString(topic.getTopicStartTime(), "yyyy-MM-dd HH:mm"));
                } else {
                    map.put("topicStartTime", "");
                }
                if (topic.getTopicEndTime() != null) {
                    map.put("topicEndTime", DateUtils.dateToString(topic.getTopicEndTime(), "yyyy-MM-dd HH:mm"));
                } else {
                    map.put("topicEndTime", "");
                }
                map.put("reward", topic.getReward());

                map.put("type", topic.getTopicType().getValue());
                String intro = StringUtils.isEmpty(topic.getIntroduction()) ? "" : HtmlUtils.htmlEscape(topic.getIntroduction());

                map.put("intro", intro);
                map.put("introHeight", topic.getIntroHeight());
                map.put("guestIntro", topic.getGuestIntroduction());
                map.put("guestAvatar", topic.getGuestAvatar());
                map.put("guestName", topic.getGuestName());
                map.put("guestProdIntro", topic.getGuestProdIntro());
                map.put("guestProdUrl", topic.getGuestProdUrl());
                map.put("scope", topic.studentEnable() ? "3" : "1");

                if (topic.getVideoEndTime() != null) {
                    map.put("endTime", DateUtils.dateToString(topic.getVideoEndTime(), "yyyy-MM-dd HH:mm"));
                } else {
                    map.put("endTime", "");
                }
                if (topic.getViewStartTime() != null) {
                    map.put("startTime", DateUtils.dateToString(topic.getVideoStartTime(), "yyyy-MM-dd HH:mm"));
                } else {
                    map.put("startTime", "");
                }
                if (topic.getViewEndTime() != null) {
                    map.put("viewEndTime", DateUtils.dateToString(topic.getViewEndTime(), "yyyy-MM-dd HH:mm"));
                } else {
                    map.put("viewEndTime", "");
                }
                if (topic.getViewStartTime() != null) {
                    map.put("viewStartTime", DateUtils.dateToString(topic.getViewStartTime(), "yyyy-MM-dd HH:mm"));
                } else {
                    map.put("viewStartTime", "");
                }
                map.put("videoAddr", topic.getVideoAddress());
                map.put("videoAvatar", topic.getVideoAvatar());
                map.put("background", topic.getBackground());
                map.put("audioId", topic.getAudioId());
                List<TalkTopic.TalkOption> options = topic.getOptions();
                if (CollectionUtils.isNotEmpty(options)) {
                    int count = options.size();
                    for (int i = 0; i < count; i++) {
                        map.put("optionId_" + i, options.get(i).getOptionId());
                        map.put("optionText_" + i, options.get(i).getContent());
                        map.put("optionSummary_" + i, options.get(i).getSummary());
                        map.put("optionShard_" + i, options.get(i).getShard());
                    }
                }

                map.put("period", topic.getPeriod() == null ? 3 : topic.getPeriod().getValue());
                map.put("coverBack", topic.getCoverBack());
                map.put("coverFlag", topic.getCoverFlag());
                map.put("coverTitle", topic.getCoverTitle());
                map.put("coverOption", topic.getCoverOption());
                map.put("coverPastTitle", topic.getCoverPastTitle());

                if (topic.getProduct() != null) {
                    map.put("productImg", topic.getProduct().getProductImg());
                    map.put("originalPrice", topic.getProduct().getOriginalPrice());
                    map.put("originalPrice", topic.getProduct().getOriginalPrice());
                    map.put("originalLink", topic.getProduct().getOriginalLink());
                    map.put("discountPrice", topic.getProduct().getDiscountPrice());
                    map.put("discountLink", topic.getProduct().getDiscountLink());
                }
            }
        }

        if (map.size() == 0) {
            map.put("type", TalkTopicType.common.getValue());
        }

        model.addAttribute("topic", map).addAttribute("audioMap", audioMap);

        return "opmanager/talk/topicPublish";
    }

    @RequestMapping(value = "/savetopic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTopic() {
        try {
            //用户部分
            TalkTopic topic = new TalkTopic();
            topic.setTopicId(requestString("topicId"));
            topic.setTitle(requestString("title"));
            topic.setTitleImage(requestString("titleImage"));
            String subTitle = requestString("subtitle");
            topic.setSubtitle(StringUtils.isEmpty(subTitle) ? "" : subTitle);
            String subtitleImage = requestString("subtitleImage");
            topic.setSubTitleImage(StringUtils.isEmpty(subtitleImage) ? "" : subtitleImage);

            topic.setTopicNumber(requestString("topicNumber"));
            topic.setTopicStartTime(requestDate("topicStartTime", "yyyy-MM-dd hh:mm", null));
            topic.setTopicEndTime(requestDate("topicEndTime", "yyyy-MM-dd hh:mm", null));
            topic.setReward(requestString("reward"));

            topic.setBackground(requestString("background"));
            topic.setChoiceCount(requestInteger("choiceCount"));
            topic.setIntroduction(requestString("introduction"));
            topic.setIntroHeight(requestInteger("introHeight"));
            Integer topicType = requestInteger("topicType");
            topic.setScope(getRequestInt("scope", 1));

            //选项部分
            List<TalkTopic.TalkOption> options = new ArrayList<>();
            topic.setOptions(options);
            TalkTopic.TalkOption option0 = new TalkTopic.TalkOption();
            option0.setContent(requestString("optionText_0"));
            option0.setShard(requestString("optionShard_0"));
            option0.setOptionId(requestString("optionId_0"));
            option0.setSummary(requestString("optionSummary_0"));
            option0.setShard(requestString("optionShard_0"));
            options.add(option0);

            TalkTopic.TalkOption option1 = new TalkTopic.TalkOption();
            option1.setOptionId(requestString("optionId_1"));
            option1.setShard(requestString("optionShard_1"));
            option1.setContent(requestString("optionText_1"));
            option1.setSummary(requestString("optionSummary_1"));
            option1.setShard(requestString("optionShard_1"));
            options.add(option1);


            TalkTopicType talkTopicType = TalkTopicType.valueOf(topicType);
            topic.setTopicType(talkTopicType);

            if (talkTopicType != TalkTopicType.common) {
                topic.setVideoStartTime(requestDate("videoStartTime", "yyyy-MM-dd hh:mm", null));
                topic.setVideoEndTime(requestDate("videoEndTime", "yyyy-MM-dd hh:mm", null));

                topic.setViewStartTime(requestDate("viewStartTime", "yyyy-MM-dd hh:mm", null));
                topic.setViewEndTime(requestDate("viewEndTime", "yyyy-MM-dd hh:mm", null));

                topic.setGuestAvatar(requestString("guestAvatar"));
                topic.setGuestName(requestString("guestName"));
                topic.setGuestIntroduction(requestString("guestIntroduction"));
                String guestProdIntro = requestString("guestProdIntro");
                topic.setGuestProdIntro(StringUtils.isEmpty(guestProdIntro) ? "" : guestProdIntro);
                String guestProdUrl = requestString("guestProdUrl");
                topic.setGuestProdUrl(StringUtils.isEmpty(guestProdUrl) ? "" : guestProdUrl);

                if (talkTopicType == TalkTopicType.video) {
                    topic.setVideoAddress(requestString("videoAddress"));
                    topic.setVideoAvatar(requestString("videoAvatar"));
                } else {
                    topic.setAudioId(requestString("audioId"));
                }
            }

            topic.setPublish(getCurrentAdminUser().getRealName());

            //当期往期
            topic.setPeriod(TalkTopicPeriod.valueOf(getRequestInt("period")));
            //封面背景
            topic.setCoverBack(requestString("coverBack"));
            //封面视频标识
            topic.setCoverFlag(requestString("coverFlag"));
            //封面当期标题
            topic.setCoverTitle(requestString("coverTitle"));
            //封面当期观念
            topic.setCoverOption(requestString("coverOption"));
            //封面往期标题
            topic.setCoverPastTitle(requestString("coverPastTitle"));

            String productImg = requestString("productImg");
            String originalPrice = requestString("originalPrice", "0");
            String originalLink = requestString("originalLink");
            String discountPrice = requestString("discountPrice", "0");
            String discountLink = requestString("discountLink");

            TalkTopic.TalkProduct product = new TalkTopic.TalkProduct();
            product.setDiscountLink(discountLink);
            product.setDiscountPrice(discountPrice);
            product.setOriginalLink(originalLink);
            product.setOriginalPrice(originalPrice);

            product.setProductImg(productImg);
            topic.setProduct(product);

            crmTalkService.saveTalkTopic(topic);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "/deletetopic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTopic() {
        String topicId = requestString("topicId");
        if (StringUtils.isEmpty(topicId)) {
            return MapMessage.errorMessage("主题id不为空");
        }

        crmTalkService.deleteTalkTopic(topicId);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/topiclist.vpage", method = RequestMethod.GET)
    public String topicList(Model model) {
        //接受参数
        String title = getRequestString("title");
        Date startTime = requestDate("startTime", "yyyy-MM-dd hh:mm:ss", null);
        Date endTime = requestDate("endTime", "yyyy-MM-dd hh:mm:ss", null);
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("title", title);
        parameters.put("startTime", getRequestString("startTime"));
        parameters.put("endTime", getRequestString("endTime"));
        String query = UrlUtils.buildUrlQuery("?", parameters);
        model.addAttribute("query", query + "&pageIndex=");


        int pageSize = 25;

        int pageIndex = getRequestInt("pageIndex");
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("title", title);
        model.addAttribute("startTime", startTime != null ? DateUtils.dateToString(startTime) : "");
        model.addAttribute("endTime", endTime != null ? DateUtils.dateToString(endTime) : "");

        List<TalkTopic> topics = crmTalkLoader.getAllTopic();

        Stream<TalkTopic> stream = topics.stream();
        if (StringUtils.isNotEmpty(title)) {
            stream = stream.filter(x -> StringUtils.contains(x.getTitle(), title));
        }
        if (null != startTime) {
            stream = stream.filter(x -> x.getCreateTime().getTime() > startTime.getTime());
        }

        if (null != endTime) {
            stream = stream.filter(x -> x.getCreateTime().getTime() < endTime.getTime());
        }

        topics = stream.collect(Collectors.toList());

        long count = topics.size();
        long pageCount = (count + pageSize - 1) / pageSize;
        model.addAttribute("pageCount", pageCount);

        stream = topics.stream()
                .sorted((t1, t2) -> t2.getCreateTime().compareTo(t1.getCreateTime()))
                .skip((pageIndex - 1) * pageSize).limit(pageSize);

        topics = stream.collect(Collectors.toList());

        List<String> ids = topics.stream().map(TalkTopic::getTopicId).collect(Collectors.toList());
        Map<String, TopicTotalValue> total = crmTalkLoader.getTotalByIds(ids);
        List<Map<String, Object>> list = new LinkedList<>();
        for (TalkTopic topic : topics) {
            Map<String, Object> map = new HashMap<>();
            map.put("topicId", topic.getTopicId());
            map.put("title", topic.getTitle());
            map.put("intro", StringUtils.substring(topic.getIntroduction(), 0, 6) + "...");
            map.put("type", topic.getTopicType().getName());
            map.put("publish", topic.getPublish());
            map.put("time", DateUtils.dateToString(topic.getCreateTime()));
            TopicTotalValue value = total.get(topic.getTopicId());
            if (value != null) {
                map.put("reply", value.getReplyCount());
                map.put("option", value.getOptionCount());
                map.put("vote", value.getVotedCount());
            } else {
                map.put("reply", 0);
                map.put("option", 0);
                map.put("vote", 0);
            }
            list.add(map);
        }
        model.addAttribute("topics", list);

        return "opmanager/talk/topicList";
    }

    @RequestMapping(value = "/topictotal.vpage", method = RequestMethod.GET)
    public String topicTotal(Model model) {

        List<TalkTopic> topics = crmTalkLoader.getAllTopic()
                .stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());

        List<String> ids = topics.stream().map(TalkTopic::getTopicId).collect(Collectors.toList());
        Map<String, TopicTotalValue> total = crmTalkLoader.getTotalByIds(ids);
        //todo load total count

        List<Map<String, Object>> list = new LinkedList<>();
        for (TalkTopic topic : topics) {
            Map<String, Object> map = new HashMap<>();
            map.put("topicId", topic.getTopicId());
            map.put("title", topic.getTitle());
            map.put("publish", topic.getPublish());
            map.put("time", DateUtils.dateToString(topic.getCreateTime()));
            map.put("statisticsTime", topic.getStatisticsTime() == null ? "" : DateUtils.dateToString(topic.getStatisticsTime()));
            TopicTotalValue value = total.get(topic.getTopicId());
            if (value != null) {
                map.put("reply", value.getReplyCount());
                map.put("replier", value.getReplyUserCount());
                map.put("option", value.getOptionCount());
                map.put("vote", value.getVotedCount());
                map.put("video", value.getVideoCount());
                map.put("quoteReply", value.getQuoteReplyCount());
                map.put("quoteReplyUser", value.getQuoteReplyUserCount());
            } else {
                map.put("reply", 0);
                map.put("replier", 0);
                map.put("option", 0);
                map.put("vote", 0);
                map.put("video", 0);
                map.put("quoteReply", 0);
                map.put("quoteReplyUser", 0);
            }
            list.add(map);
        }
        model.addAttribute("topics", list);

        return "opmanager/talk/topicTotal";
    }

    private List<TalkTopic> loadTopics(Model model) {
        //获取话题列表
        List<TalkTopic> topics = crmTalkLoader.getAllTopic();

        topics = topics.stream()
                .sorted((TalkTopic t0, TalkTopic t1) -> t1.getCreateTime().compareTo(t0.getCreateTime()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(topics)) {
            List<Map<String, Object>> topices = new LinkedList<>();
            for (TalkTopic topic : topics) {
                if (null == topic || CollectionUtils.isEmpty(topic.getOptions())) {
                    continue;
                }
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("topicId", topic.getTopicId());
                map.put("title", topic.getTitle());
                topices.add(map);
            }
            model.addAttribute("topics", topices);
        }

        return topics;
    }

    //endregion

    //region audio

    @RequestMapping(value = "/audiolist.vpage", method = RequestMethod.GET)
    public String audioList(Model model) {
        String title = getRequestString("title");
        String topicId = getRequestString("topicId");
        loadTopics(model);
        model.addAttribute("title", title);
        model.addAttribute("topicId", topicId);

        int pageSize = 20;

        int pageIndex = getRequestInt("pageIndex");
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        model.addAttribute("pageIndex", pageIndex);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("title", title);
        parameters.put("topicId", topicId);
        String query = UrlUtils.buildUrlQuery("?", parameters);
        model.addAttribute("query", query + "&pageIndex=");

        List<TalkAudio> talkAudios = null;
        if (StringUtils.isNotEmpty(topicId)) {
            TalkTopic topic = crmTalkLoader.getTopicById(topicId);
            if (topic != null && StringUtils.isNotEmpty(topic.getAudioId())) {
                TalkAudio audio = crmTalkLoader.getAudioById(topic.getAudioId());
                if (audio != null) {
                    talkAudios = Collections.singletonList(audio);
                }
            }
        } else {
            talkAudios = crmTalkLoader.getAllAudio();
            if (StringUtils.isNotEmpty(title)) {
                talkAudios = talkAudios.stream()
                        .filter(x -> x != null && StringUtils.contains(x.getTitle(), title))
                        .collect(Collectors.toList());
            }
        }


        List<TalkTopic> talkTopics = crmTalkLoader.getAllTopic();
        Map<String, TalkTopic> maps = new LinkedHashMap<>();
        talkTopics.forEach(topic -> {
            if (null == topic
                    || StringUtils.isEmpty(topic.getAudioId())
                    || topic.getTopicType() != TalkTopicType.audio) {
                return;
            }
            maps.put(topic.getAudioId(), topic);
        });
        if (CollectionUtils.isNotEmpty(talkAudios)) {

            long count = talkAudios.size();
            long pageCount = (count + pageSize - 1) / pageSize;

            talkAudios = talkAudios.stream()
                    .skip((pageSize * (pageIndex - 1)))
                    .limit(pageSize)
                    .collect(Collectors.toList());

            model.addAttribute("pageCount", pageCount);

            List<Map<String, Object>> list = new LinkedList<>();
            Long current = System.currentTimeMillis();
            for (TalkAudio audio : talkAudios) {
                if (null == audio) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("audioId", audio.getAudioId());
                map.put("title", audio.getTitle());
                map.put("createTime", DateUtils.dateToString(audio.getCreateTime()));
                map.put("publisher", audio.getPublisher());
                TalkTopic topic = maps.get(audio.getAudioId());
                if (null != topic) {
                    map.put("topic", topic.getTitle());
                    Long startTime = topic.getVideoStartTime().getTime() - 1000 * 60 * 5;
                    Long endTime = topic.getVideoEndTime().getTime();
                    String editFlag = (startTime <= current && endTime >= current) ? "cancel" : "ok";
                    map.put("flag", editFlag);
                    map.put("publishTime", topic.getVideoStartTime() == null ? "" : topic.getVideoStartTime());
                }
                list.add(map);
            }
            model.addAttribute("list", list);
        } else {
            model.addAttribute("pageCount", 0);
        }
        return "opmanager/talk/audioList";
    }

    @RequestMapping(value = "/audiopublish.vpage", method = RequestMethod.GET)
    public String audioPublish(Model model) {
        String audioId = requestString("audioId");
        if (StringUtils.isNotEmpty(audioId)) {
            TalkAudio audio = crmTalkLoader.getAudioById(audioId);
            if (null != audio) {

                boolean enableAudioEdit = enableAudioEdit(audioId);
                model.addAttribute("enable", enableAudioEdit ? "ok" : "cancel");

                model.addAttribute("title", audio.getTitle());
                model.addAttribute("audioId", audio.getAudioId());
                List<Map<String, Object>> list = new LinkedList<>();
                List<TalkAudio.AudioContent> contents = audio.getContents();
                for (TalkAudio.AudioContent content : contents) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("content", content.getContent());
                    map.put("type", content.getAudioType().getValue());
                    map.put("duration", content.getDuration());
                    list.add(map);
                }
                model.addAttribute("contentList", list);
            }
        }
        return "opmanager/talk/audioPublish";
    }

    private boolean enableAudioEdit(String audioId) {
        List<TalkTopic> topics = this.crmTalkLoader.getAllTopic();
        if (CollectionUtils.isNotEmpty(topics)) {
            topics = topics.stream()
                    .filter(x -> StringUtils.equals(x.getAudioId(), audioId))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(topics)) {
                long current = System.currentTimeMillis();
                long five = 5 * 60 * 1000;
                for (TalkTopic topic : topics) {
                    if (topic.getVideoStartTime() == null || topic.getVideoEndTime() == null) {
                        continue;
                    }
                    if (topic.getVideoStartTime().getTime() - five <= current || current <= topic.getVideoEndTime().getTime()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @RequestMapping(value = "/audiosave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage audioSave() {
        try {
            String audioId = requestString("audioId");

            MapMessage message = checkAudioStatus(audioId);
            if (message != null) {
                return message;
            }

            String title = requestString("title");
            if (StringUtils.isEmpty(title)) {
                return MapMessage.errorMessage("请输入文稿名称");
            }

            String[] contents = requestArray("content");
            String[] durations = requestArray("duration");
            String[] audioTypes = requestArray("audioType");

            if (ArrayUtils.isEmpty(contents)
                    || ArrayUtils.isEmpty(durations)
                    || ArrayUtils.isEmpty(audioTypes)
                    || contents.length != durations.length
                    || durations.length != audioTypes.length) {
                return MapMessage.errorMessage("没有文稿数据");
            }

            int length = contents.length;

            List<TalkAudio.AudioContent> list = new LinkedList<>();
            for (int i = 0; i < length; i++) {
                TalkAudio.AudioContent content = new TalkAudio.AudioContent();
                content.setAudioType(TalkAudio.AudioType.valueOf(NumberUtils.toInt(audioTypes[i])));
                content.setDuration(NumberUtils.toInt(durations[i]));
                content.setContent(contents[i]);
                list.add(content);
            }

            TalkAudio audio = new TalkAudio();
            audio.setAudioId(audioId);
            audio.setTitle(title);
            audio.setContents(list);

            audio.setPublisher(getCurrentAdminUser().getAdminUserName());

            crmTalkService.saveTalkAudio(audio);

            return MapMessage.successMessage().setInfo("成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage().setInfo("服务器异常");
        }
    }

    @RequestMapping(value = "/deleteautio.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteAudio() {
        String audioId = requestString("audioId");
        if (StringUtils.isEmpty(audioId)) {
            return MapMessage.errorMessage("参数错误");
        }


        MapMessage message = checkAudioStatus(audioId);
        if (message != null) {
            return message;
        }

        crmTalkService.deleteTalkAudio(audioId);

        return MapMessage.successMessage();
    }

    private MapMessage checkAudioStatus(String audioId) {
        List<TalkTopic> topics = crmTalkLoader.getAllTopic();
        long current = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(topics)) {
            for (TalkTopic topic : topics) {
                if (topic == null || topic.getAudioId() == null || !StringUtils.equals(topic.getAudioId(), audioId)) {
                    continue;
                }

                if (topic.getVideoStartTime() == null || topic.getVideoEndTime() == null) {
                    continue;
                }
                long start = topic.getVideoStartTime().getTime();
                long end = topic.getVideoEndTime().getTime();

                if (start - 5 * 60 * 1000 <= current && current <= end) {
                    return MapMessage.errorMessage("直播中的视频不能删除");
                }
            }
        }

        return null;
    }
    //endregion

    //region reply

    @RequestMapping(value = "/replypublish.vpage", method = RequestMethod.GET)
    public String replyPublish(Model model) {

        List<TalkTopic> topics = crmTalkLoader.getAllTopic();
        if (CollectionUtils.isNotEmpty(topics)) {
            List<Map<String, Object>> topices = new LinkedList<>();
            List<Map<String, Object>> options = new LinkedList<>();
            for (TalkTopic topic : topics) {
                if (null == topic || CollectionUtils.isEmpty(topic.getOptions())) {
                    continue;
                }

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("topicId", topic.getTopicId());
                map.put("title", topic.getTitle());
                topices.add(map);
                List<TalkTopic.TalkOption> optionList = topic.getOptions();
                for (TalkTopic.TalkOption option : optionList) {
                    Map<String, Object> optionMap = new LinkedHashMap<>();
                    optionMap.put("topicId", topic.getTopicId());
                    optionMap.put("optionId", option.getOptionId());
                    optionMap.put("title", option.getContent());
                    options.add(optionMap);
                }
            }

            model.addAttribute("topics", topices)
                    .addAttribute("options", options);
        }

        Mode mode = RuntimeMode.current();
        if (mode == Mode.PRODUCTION || mode == Mode.STAGING) {
            model.addAttribute("parents", loadProd());
        } else {
            model.addAttribute("parents", loadTest());
        }

        return "opmanager/talk/replyPublish";
    }

    @RequestMapping(value = "/savereply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveReply() {

        TalkReplyShard talkReply = new TalkReplyShard();
        talkReply.setTopicId(requestString("topicId"));
        talkReply.setOptionId(requestString("optionId"));
        talkReply.setConcept(requestString("concept"));
        talkReply.setAudit(0);

        talkReply.setUserId(getRequestLong("majia"));

        talkService.replyTopic(talkReply);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/replylist.vpage", method = RequestMethod.GET)
    public String replyList(Model model) {
        //加载话题
        List<TalkTopic> topics = loadTopics(model);
        //参数处理
        TalkReplyQueryRequest request = new TalkReplyQueryRequest();
        replyParameter(request, model);
        request.setIsRoot(true);
        request.setRootReplyId("0");

        //读取评论，转换数据
        List<Map<String, Object>> replies = loadReplies(request, topics);

        model.addAttribute("replies", replies);
        model.addAttribute("pageCount", request.getPageCount());

        int pageIndex = getRequestInt("pageIndex");
        if (pageIndex <= 0) {
            pageIndex = 1;
        }

        if (pageIndex > (request.getPageCount() / 10) * 10) {
            model.addAttribute("end", request.getPageCount());
        }

        return "opmanager/talk/replyList";
    }

    @RequestMapping(value = "/subReplyList.vpage", method = RequestMethod.GET)
    public String subReplyList(Model model) {
        //参数处理
        TalkReplyQueryRequest request = new TalkReplyQueryRequest();
        replyParameter(request, model);

        //读取评论，转换数据
        List<Map<String, Object>> replies = loadReplies(request, loadTopics(model));

        model.addAttribute("replies", replies);
        model.addAttribute("pageCount", request.getPageCount());

        int pageIndex = getRequestInt("pageIndex");
        if (pageIndex <= 0) {
            pageIndex = 1;
        }

        if (pageIndex > (request.getPageCount() / 10) * 10) {
            model.addAttribute("end", request.getPageCount());
        }
        return "opmanager/talk/subReplyList";
    }

    @RequestMapping(value = "/hidereply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hideReply() {
        String idStr = requestString("replyIds");
        if (StringUtils.isEmpty(idStr)) {
            return MapMessage.successMessage();
        }

        String[] split = StringUtils.split(idStr, ",");

        List<String> replyIds = Arrays.stream(split)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(replyIds)) {
            return MapMessage.successMessage();
        }

        return crmTalkService.hideReplies(replyIds);
    }


    private void replyParameter(TalkReplyQueryRequest request, Model model) {
        Map<String, String> query = new LinkedHashMap<>();

        Integer auditType = requestInteger("auditType");
        request.setStatus(auditType);
        model.addAttribute("auditType", auditType);
        query.put("auditType", getRequestString("auditType"));

        Date startTime = requestDate("startTime", "yyyy-MM-dd hh:mm:ss", null);
        request.setStartTime(startTime == null ? null : startTime.getTime());
        model.addAttribute("startTime", getRequestString("startTime"));
        query.put("startTime", getRequestString("startTime"));

        Date endTime = requestDate("endTime", "yyyy-MM-dd hh:mm:ss", null);
        request.setEndTime(endTime == null ? null : endTime.getTime());
        model.addAttribute("endTime", getRequestString("endTime"));
        query.put("endTime", getRequestString("endTime"));

        Long userId = requestLong("userId");
        String userIdStr = getRequestString("userId");
        if (StringUtils.isNotEmpty(userIdStr)) {
            if (userId != null && userId > 0) {
                request.setUserId(userId);
                model.addAttribute("userId", userId);
                query.put("userId", userIdStr);
            } else {
                request.setUserId(0L);
                model.addAttribute("userId", userIdStr);
                query.put("userId", userIdStr);
                getAlertMessageManager().addMessageError("评论用户id是长整数");
            }
        }
        String topicId = requestString("topicId");
        request.setTopicId(topicId);
        model.addAttribute("topicId", topicId);
        query.put("topicId", getRequestString("topicId"));


        Integer pageIndex = getRequestInt("pageIndex");
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        model.addAttribute("pageIndex", pageIndex);
        request.setPageIndex(pageIndex);
        request.setPageSize(20);

        model.addAttribute("pageSize", 20);

        //精选参数
        request.setChoice(getRequestInt("choice") == 1);
        model.addAttribute("choice", request.getChoice());
        if (request.getChoice()) {
            query.put("choice", "1");
        }

        //存在回复
        request.setQuoted(getRequestInt("quoted") == 1);
        model.addAttribute("quoted", request.getQuoted());
        if (request.getQuoted()) {
            query.put("quoted", "1");
        }

        //根评论id
        String replyId = getRequestString("id");
        request.setRootReplyId(replyId);
        model.addAttribute("replyId", replyId);
        query.put("replyId", replyId);

        String queryString = UrlUtils.buildUrlQuery("?", query);
        model.addAttribute("query", queryString + "&pageIndex=");


        int start = ((pageIndex - 1) / 10) * 10 + 1;
        int end = ((pageIndex - 1) / 10 + 1) * 10;

        model.addAttribute("start", start);
        model.addAttribute("end", end);
    }

    private List<Map<String, Object>> loadReplies(TalkReplyQueryRequest request, List<TalkTopic> topics) {

        Page<String> page = crmTalkLoader.getReplyIds(request);

        int pageCount = (int) ((page.getTotalElements() + request.getPageSize() - 1) / request.getPageSize());
        request.setPageCount(pageCount);

        List<TalkReplyShard> replies = crmTalkLoader.getReplyByIds(page.getContent());
        if (CollectionUtils.isEmpty(replies)) {
            return Collections.emptyList();
        }

        Map<String, Long> quotes = crmTalkLoader.loadQuoteCount(page.getContent());

        replies = replies.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        List<Map<String, Object>> list = new LinkedList<>();
        for (TalkReplyShard reply : replies) {
            Map<String, Object> map = parseReply(reply, topics, quotes);
            List<Map<String, Object>> subReplies = loadSubReplies(reply.getId(), topics, quotes);
            map.put("list", subReplies);
            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> loadSubReplies(String replyId, List<TalkTopic> topics, Map<String, Long> quotes) {

        TalkReplyQueryRequest replyQueryRequest = new TalkReplyQueryRequest();
        replyQueryRequest.setStatus(TalkReply.AuditType.unAudit.getValue());
        replyQueryRequest.setRootReplyId(replyId);
        replyQueryRequest.setPageSize(10);
        replyQueryRequest.setPageIndex(1);


        Page<String> page = crmTalkLoader.getReplyIds(replyQueryRequest);

        List<String> ids = page.getContent();
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<TalkReplyShard> replies = crmTalkLoader.getReplyByIds(ids);
        if (CollectionUtils.isEmpty(replies)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new LinkedList<>();
        for (TalkReplyShard reply : replies) {
            list.add(parseReply(reply, topics, quotes));
        }
        return list;
    }

    private Map<String, Object> parseReply(TalkReplyShard reply, List<TalkTopic> topics, Map<String, Long> quotes) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("replyId", reply.getId());
        map.put("concept", reply.getConcept());
        map.put("createTime", DateUtils.dateToString(reply.getCreateTime(), "MM-dd HH:mm"));
        map.put("userType", reply.isParentReplied() ? "家长" : "孩子");
        map.put("userId", reply.getUserId());
        int audit = reply.getAudit() == null ? 0 : reply.getAudit();
        String auditStatus = "未知";
        //1：未审核；2、通过；3、拒绝
        switch (audit) {
            case 1:
                auditStatus = "未审核";
                break;
            case 2:
                auditStatus = "通过";
                break;
            case 3:
                auditStatus = "拒绝";
                break;
            case 4:
                auditStatus = "隐藏";
                break;
        }
        map.put("auditStatus", auditStatus);
        map.put("reason", reply.getReason());
        map.put("auditTime", reply.getAuditDateTime() == null ? "" : DateUtils.dateToString(reply.getAuditDateTime(), "MM-dd HH:mm"));
        map.put("auditName", reply.getAuditUserName());
        map.put("choice", reply.getChoice() == null || reply.getChoice() != 1 ? 0 : 1);
        map.put("quote", quotes.get(reply.getId()));

        Optional<TalkTopic> optional = topics.stream()
                .filter(t -> StringUtils.equals(t.getTopicId(), reply.getTopicId()))
                .findFirst();

        if (optional.isPresent()) {
            TalkTopic topic = optional.get();
            map.put("topicTitle", topic.getTitle());
            List<TalkTopic.TalkOption> options = topic.getOptions().stream()
                    .filter(option -> StringUtils.equals(option.getOptionId(), reply.getOptionId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(options)) {
                TalkTopic.TalkOption talkOption = options.get(0);
                map.put("optionTitle", talkOption.getContent());
            }
        }

        return map;
    }

    @RequestMapping(value = "/auditReply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage auditReply() {

        boolean pass = getRequestBool("pass");
        String reason = getRequestString("reason");
        String replyId = getRequestString("replyId");
        Long userId = getCurrentAdminUser().getFakeUserId();
        String userName = getCurrentAdminUser().getAdminUserName();

        if (StringUtils.isEmpty(replyId)) {
            return MapMessage.errorMessage("参数错误");
        }

        MapMessage result = crmTalkService.auditReply(replyId, userId, userName, pass, reason);
        if (!result.isSuccess()) {
            return result;
        }

        if (pass) {
            List<TalkReplyShard> replies = crmTalkLoader.getReplyByIds(Collections.singletonList(replyId));
            if (CollectionUtils.isNotEmpty(replies)) {
                TalkReplyShard reply = replies.get(0);
                String message = "恭喜你！你发布的评论“" + StringUtils.substring(reply.getConcept(), 0, 14) +
                        "”已通过审核，能被大家点赞和分享哦；";

                String url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17shuo/index.vpage?from=app&topicId=" + reply.getTopicId() + "&rel=push2#comment_anchors";

                appMessageServiceClient.sendAppJpushMessageByIds(message,
                        AppMessageSource.PARENT,
                        Collections.singletonList(reply.getUserId()),
                        MapUtils.m("url", url));
            }
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/choiceReply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage choiceReply() {
        String replyId = getRequestString("replyId");
        if (StringUtils.isEmpty(replyId)) {
            return MapMessage.errorMessage("参数错误");
        }

        List<TalkReplyShard> replies = crmTalkLoader.getReplyByIds(Collections.singletonList(replyId));
        if (CollectionUtils.isEmpty(replies)) {
            return MapMessage.errorMessage("评论不存在");
        }

        TalkReplyShard reply = replies.get(0);
        if (reply.getDeleted() != null && reply.getDeleted() == 1) {
            return MapMessage.errorMessage("评论已删除");
        }

        if (reply.getAudit() == null || reply.getAudit() != TalkReply.AuditType.pass.getValue()) {
            return MapMessage.errorMessage("评论未审核通过");
        }

        return crmTalkService.choiceReply(replyId);
    }

    //endregion

    //region 其他功能

    @RequestMapping(value = "rankList.vpage", method = RequestMethod.GET)
    public String getRankList(Model model) {
        Double maxVoteCount = getRequestDouble("max", -1d);
        String topicId = getRequestString("topicId");
        long limit = getRequestLong("limit");
        int userType = getRequestInt("userType");
        if (userType != UserType.STUDENT.getType()) {
            userType = UserType.PARENT.getType();
        }
        model.addAttribute("max", maxVoteCount);
        model.addAttribute("topicId", topicId);
        model.addAttribute("limit", limit);
        model.addAttribute("userType", userType);
        if (maxVoteCount == -1 || StringUtils.isBlank(topicId) || limit <= 0) {
            return "opmanager/talk/rankList";
        }
        ScoreTalkContext context = new ScoreTalkContext();
        context.setTopicId(topicId);
        context.setScore(maxVoteCount);
        List<ScoreTalkContext> list = crmTalkLoader.loadForCRM(context, limit, userType);
        model.addAttribute("rank_list", list);
        return "opmanager/talk/rankList";
    }

    @RequestMapping(value = "force_reload_rank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage forceReloadRankList() {
        String topicId = getRequestString("topicId");
        if (StringUtils.isBlank(topicId)) {
            return MapMessage.errorMessage("话题ID不能为空");
        }

        int userType = getRequestInt("userType");
        if (userType != UserType.STUDENT.getType()) {
            userType = UserType.PARENT.getType();
        }
        return crmTalkService.forceReloadRankList(topicId, userType);
    }

    //endregion

    //region 辅助方法
    @RequestMapping(value = "/upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBackground(MultipartFile inputFile) {
        try {
            String path = AdminOssManageUtils.upload(inputFile, "17shuo");
            return MapMessage.successMessage().add("path", path);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        MapMessage mapMessage = new MapMessage();

        switch (action) {
            case "config":
                mapMessage.set("imageActionName", "uploadimage");
                mapMessage.set("imageFieldName", "upfile");
                mapMessage.set("imageInsertAlign", "none");
                mapMessage.set("imageMaxSize", 2048000);
                mapMessage.set("imageUrlPrefix", "");
                mapMessage.set("imageAllowFiles", new String[]{".png", ".jpg", ".jpeg", ".gif"});
                mapMessage.set("videoActionName", "uploadvideo");
                mapMessage.set("videoFieldName", "upfile");
                mapMessage.set("videoUrlPrefix", "");
                mapMessage.set("videoMaxSize", 20971520);
                mapMessage.set("videoAllowFiles", new String[]{".flv", ".swf", ".mkv", ".avi", ".rm", ".rmvb", ".mpeg", ".mpg", ".ogg", ".ogv", ".mov", ".wmv", ".mp4", ".webm", ".mp3", ".wav", ".mid"});
                mapMessage.setSuccess(true);
                break;
            case "uploadimage":
            case "uploadvideo":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");

                if (imgFile.isEmpty()) {
                    mapMessage.setSuccess(false);
                    mapMessage.setInfo("没有文件上传");
                } else {
                    try {
                        String filename = imgFile.getOriginalFilename();
                        String path = AdminOssManageUtils.upload(imgFile, "17shuo/editor");
                        mapMessage.add("url", path)
                                .add("title", filename)
                                .add("state", "SUCCESS")
                                .add("original", filename);
                        mapMessage.setSuccess(true);
                    } catch (Exception e) {
                        mapMessage.setSuccess(false);
                        mapMessage.setInfo("文件上传异常");
                    }
                }
                break;
        }

        return mapMessage;
    }


    private static Map<String, String> loadTest() {
        String str = "钟舒妈妈,263173,朱丽爸爸,263174,包薇爸爸,263175,蔡小青妈妈,263176,陈丽爸爸,263177,杜丽爸爸,263178,李兰芽妈妈,263179,李三妈妈,263180,璐娜爸爸,263181,吕丽红妈妈,263182,梦研爸爸,263183,张丽妈妈,263184,赵玉妈妈,263185,白逸凤爸爸,263186,凌月青爸爸,263187,沙雅爸爸,263188,陈贺妈妈,263189,金鑫妈妈,263190,李国爸爸,263191,李丽贡爸爸,263192,李中青爸爸,263193,刘国强爸爸,263194,吕刚爸爸,263195,吕四妈妈,263196,王安强妈妈,263197,王牌妈妈,263198,文小刚妈妈,263199,闫学成妈妈,263200,张家力妈妈,263201,白家力妈妈,263202,白松爸爸,263203,程鑫爸爸,263204,高力国妈妈,263205,李磊爸爸,263206,李力国妈妈,263207,刘力昆妈妈,263208,石磊爸爸,263209,李晶妈妈,263210,毛丽爸爸,263211,晓丽妈妈,263212,张宏妈妈,263213,张娜妈妈,263214,骆凉倩妈妈,263215,钟滢晶爸爸,263216,涂丛岚妈妈,263217,蔡妮玥妈妈,263218,林菁蝶爸爸,263219,郑洋影妈妈,263220,严凌爸爸,263221,钟艳梦爸爸,263222";
        String[] strings = StringUtils.split(str, ",");
        int length = strings.length / 2;
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < length; i++) {
            map.put(strings[i * 2], strings[i * 2 + 1]);
        }
        return map;
    }

    private static Map<String, String> loadProd() {
        String str = "钟舒妈妈,223786801,朱丽爸爸,223786802,包薇爸爸,223786803,蔡小青妈妈,223786804,陈丽爸爸,223786805,杜丽爸爸,223786806,李兰芽妈妈,223786807,李三妈妈,223786808,璐娜爸爸,223786809,吕丽红妈妈,223786810,梦研爸爸,223786811,张丽妈妈,223786813,赵玉妈妈,223786814,白逸凤爸爸,223786815,凌月青爸爸,223786816,沙雅爸爸,223786817,陈贺妈妈,223786818,金鑫妈妈,223786819,李国爸爸,223786820,李丽贡爸爸,223786821,李中青爸爸,223786822,刘国强爸爸,223786823,吕刚爸爸,223786824,吕四妈妈,223786825,王安强妈妈,223786826,王牌妈妈,223786827,文小刚妈妈,223786828,闫学成妈妈,223786829,张家力妈妈,223786830,白家力妈妈,223786831,白松爸爸,223786832,程鑫爸爸,223786833,高力国妈妈,223786834,李磊爸爸,223786835,李力国妈妈,223786836,刘力昆妈妈,223786837,石磊爸爸,223786838,李晶妈妈,223786839,毛丽爸爸,223786840,晓丽妈妈,223786841,张宏妈妈,223786842,张娜妈妈,223786843,骆凉倩妈妈,223786844,钟滢晶爸爸,223786845,涂丛岚妈妈,223786846,蔡妮玥妈妈,223786847,林菁蝶爸爸,223786848,郑洋影妈妈,223786849,严凌爸爸,223786850,钟艳梦爸爸,223786851";
        String[] strings = StringUtils.split(str, ",");
        int length = strings.length / 2;
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < length; i++) {
            map.put(strings[i * 2], strings[i * 2 + 1]);
        }
        return map;
    }

    //endregion
}