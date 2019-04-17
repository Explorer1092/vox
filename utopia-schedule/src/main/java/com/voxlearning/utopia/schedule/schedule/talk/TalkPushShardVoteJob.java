package com.voxlearning.utopia.schedule.schedule.talk;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.JobTalkShardLoader;
import com.voxlearning.utopia.service.parent.api.TalkShardLoader;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkReplyShard;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkReplyVoteShard;
import com.voxlearning.utopia.service.parent.api.entity.talking.TalkTopic;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ScheduledJobDefinition(
        jobName = "17说评论点赞推送",
        jobDescription = "17说评论点赞推送，每天12点和20点执行执行。",
        disabled = {Mode.STAGING, Mode.TEST},
        cronExpression = "0 0 12,20 * * ?",
        ENABLED = true
)
public class TalkPushShardVoteJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = JobTalkShardLoader.class)
    private JobTalkShardLoader jobTalkShardLoader;

    @ImportService(interfaceClass = TalkShardLoader.class)
    private TalkShardLoader talkShardLoader;

    @Inject
    protected AppMessageServiceClient appMessageServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        logger.info("17说点赞推送");

        TalkTopic topic = jobTalkShardLoader.findCurrentTopic();
        if (topic == null) {
            logger.info("17说点赞推送失败，没有当前话题");
            return;
        }

        //计算时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR);

        long startTime = 0;
        long endTime = 0;

        if (hour == 12) {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DATE, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 20);
            startTime = calendar.getTimeInMillis();
        } else if (hour == 20) {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            endTime = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            startTime = calendar.getTimeInMillis();
        } else if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            endTime = System.currentTimeMillis();
            startTime = System.currentTimeMillis() - 86400000;
        } else {
            return;
        }

        String topicId = topic.getTopicId();

        Set<String> sets = new HashSet<>();

        List<TalkReplyVoteShard> votes = jobTalkShardLoader.findNewVotes(topicId, startTime, endTime, 1000);
        while (CollectionUtils.isNotEmpty(votes)) {
            Set<String> strings = votes.stream().map(TalkReplyVoteShard::getReplyId).collect(Collectors.toSet());
            sets.addAll(strings);
            Long max = votes.stream().map(reply -> reply.getCreateTime().getTime()).max(Long::compareTo).orElse(null);
            if (max == null || max.equals(0L)) {
                break;
            }
            startTime = max;
            votes = jobTalkShardLoader.findNewVotes(topicId, startTime, endTime, 1000);
        }

        if (CollectionUtils.isEmpty(sets)) {
            return;
        }

        LinkedList<String> replyIds = new LinkedList<>(sets);
        Map<String, Long> replyVoteCount = talkShardLoader.getReplyVoteCount(topic.getTopicId(), replyIds);
        Map<String, TalkReplyShard> replies = talkShardLoader.getReplies(replyIds);
        if (MapUtils.isEmpty(replies)) {
            return;
        }

        Map<Long, TalkReplyShard> sendMap = new HashMap<>();
        replies.values().stream().forEach(reply -> {
            Long count = replyVoteCount.get(reply.getId());
            if (count == null) {
                return;
            }
            Long userId = reply.getUserId();
            if (userId == null) {
                return;
            }
            TalkReplyShard prove = sendMap.get(userId);

            if (prove != null) {
                Long currentCount = replyVoteCount.get(reply.getId());
                if (currentCount == null) {
                    return;
                }

                Long proveCount = replyVoteCount.get(prove.getId());
                if (currentCount > proveCount) {
                    sendMap.put(userId, reply);
                }
            } else {
                sendMap.put(reply.getUserId(), reply);
            }
        });

        String url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17shuo/index.vpage?from=app&topicId=" + topicId + "&rel=push3#comment_anchors";

        sendMap.values().forEach(reply -> {
            String message = "你发布的评论“" + StringUtils.substring(reply.getConcept(), 0, 14) +
                    "”已获得" + replyVoteCount.get(reply.getId()) + "个赞";
            appMessageServiceClient.sendAppJpushMessageByIds(message,
                    AppMessageSource.PARENT,
                    Collections.singletonList(reply.getUserId()),
                    MapUtils.m("url", url));
        });
    }
}
