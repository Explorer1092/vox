package com.voxlearning.enanalyze.aggregate.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.MessageFetcher;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.GroupAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.GroupView;
import com.voxlearning.utopia.enanalyze.api.GroupService;
import com.voxlearning.utopia.enanalyze.api.SentenceLikeService;
import com.voxlearning.utopia.enanalyze.model.GroupWithLike;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群聚合服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Service
public class GroupAggregatorImpl implements GroupAggregator {

    @ImportService(interfaceClass = GroupService.class)
    @ServiceVersion(version = "20180701")
    private GroupService groupService;

    @ImportService(interfaceClass = SentenceLikeService.class)
    @ServiceVersion(version = "20180701")
    private SentenceLikeService sentenceLikeService;

    @Override
    public List<GroupView> list(String openId) {
        MapMessage message = groupService.list(openId);
        List<GroupWithLike> groups = MessageFetcher.get(message, ArrayList.class);
        return groups.stream()
                .map(GroupView.Builder::build)
                .collect(Collectors.toList());
    }

    @Override
    public void remove(String openId, String openGroupId) {
        MapMessage message = groupService.remove(openId, openGroupId);
        Boolean result = MessageFetcher.get(message, Boolean.class);
        if (!result) {
            log.error("删除群时发生错误,openId = {}, openGroupId = {}", openId, openGroupId);
            throw new BusinessException(ViewCode.BIZ_ERROR, "删除群时发生错误");
        }
    }
}
