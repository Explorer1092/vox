
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.cache.UserInvitationRankCache;
import com.voxlearning.utopia.service.ai.data.ChipsRank;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语邀请排行榜",
        jobDescription = "每天02:00执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 2 * * ?"
)
@ProgressTotalWork(100)
public class AutoGenChipsInvitationRankJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        String sql = "SELECT INVITER, COUNT(*) AS NUM FROM VOX_CHIP_ENGLISH_INVITATION WHERE DISABLED = '0' AND SEND = 1 GROUP BY INVITER HAVING NUM >=3 ORDER BY NUM DESC LIMIT 20";
        List<ChipsRank> chipsRanks = new ArrayList<>();
        utopiaSql.withSql(sql).queryAll((rs, rowNum) -> {
            int num = rs.getInt("NUM");
            long uId = rs.getLong("INVITER");
            ChipsRank chipsRank = new ChipsRank();
            chipsRank.setNumber(num);
            chipsRank.setUserId(uId);
            chipsRanks.add(chipsRank);
            return null;
        });

        List<Long> userIds = chipsRanks.stream().map(ChipsRank::getUserId).collect(Collectors.toList());
        Map<Long, ParentExtAttribute> extMap = getUserExtInfo(userIds);

        Map<Long, UserBean> userInfoMap = new HashMap<>();
        userIds.forEach(e -> {
            if (MapUtils.isNotEmpty(extMap)) {
                UserBean userBean = new UserBean();
                userBean.setImage(Optional.ofNullable(extMap.get(e)).filter(e1 -> StringUtils.isNotBlank(e1.getWechatImage())).map(ParentExtAttribute::getWechatImage).orElse(""));
                userBean.setName(Optional.ofNullable(extMap.get(e)).filter(e1 -> StringUtils.isNotBlank(e1.getWechatNick())).map(ParentExtAttribute::getWechatNick).orElse("**"));
                userInfoMap.put(e, userBean);
            }
        });

        int rank = 1;
        for(int i = 0; i < chipsRanks.size(); i ++) {
            chipsRanks.get(i).setRank(rank);
            chipsRanks.get(i).setUserName(Optional.ofNullable(userInfoMap.get(chipsRanks.get(i).getUserId())).map(UserBean::getName).orElse("**"));
            chipsRanks.get(i).setImage(Optional.ofNullable(userInfoMap.get(chipsRanks.get(i).getUserId())).map(UserBean::getImage).orElse(""));
            if (i == 0) {
                rank ++;
                continue;
            }
            if (chipsRanks.get(i - 1).getNumber().compareTo(chipsRanks.get(i).getNumber()) == 0) {
                chipsRanks.get(i).setRank(chipsRanks.get(i - 1).getRank());
                continue;
            }
            rank++;
            if (rank > 10) {
                break;
            }
        }

        UserInvitationRankCache.save(chipsRanks.stream().filter(e -> e.getRank() != null && e.getRank() > 0).collect(Collectors.toList()));
    }


    private Map<Long, ParentExtAttribute> getUserExtInfo(List<Long> userIds) {
        Map<Long, ParentExtAttribute> map = new HashMap<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return map;
        }
        for(int i = 0; i < userIds.size(); i += 200) {
            Map<Long, ParentExtAttribute> extMap = parentLoaderClient.loadParentExtAttributes(userIds.subList(i, Math.min(userIds.size(), i + 200)));
            if (MapUtils.isNotEmpty(extMap)) {
                map.putAll(extMap);
            }
        }
        return map;
    }

    @Getter
    @Setter
    private class UserBean {
        private String name;
        private String image;
    }
}
