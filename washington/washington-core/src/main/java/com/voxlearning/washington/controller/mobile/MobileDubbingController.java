package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.dubbing.api.DubbingHistoryLoader;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;
import com.voxlearning.utopia.service.question.api.DubbingLoader;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2017-9-15
 */
@Controller
@RequestMapping(value = "/dubbing/")
@Slf4j
public class MobileDubbingController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = DubbingHistoryLoader.class)
    private DubbingHistoryLoader dubbingHistoryLoader;
    @ImportService(interfaceClass = DubbingLoader.class)
    private DubbingLoader dubbingLoader;

    /**
     * 配音原声页
     */
    @RequestMapping(value = "dubbing_info_share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getOriginalDubbingInfo() {

        String dubbingId = getRequestString(REQ_DUBBING_ID);
        Dubbing dubbing = dubbingLoader.loadDubbingById(dubbingId);
        if (dubbing == null) {
            return MapMessage.errorMessage(RES_RESULT_DUBBING_NOT_EXIST_MSG);
        }
        Map<String, Object> dubbingInfoMap = new HashMap<>();
        dubbingInfoMap.put(RES_RESULT_DUBBING_ID, dubbing.getId());
        dubbingInfoMap.put(RES_RESULT_DUBBING_NAME, dubbing.getVideoName());
        dubbingInfoMap.put(RES_RESULT_DUBBING_COVER_IMG, dubbing.getCoverUrl());
        dubbingInfoMap.put(RES_RESULT_DUBBING_VIDEO_URL, dubbing.getVideoUrl());
        dubbingInfoMap.put(RES_RESULT_DUBBING_LEVEL, dubbing.getDifficult());
        dubbingInfoMap.put(RES_RESULT_DUBBING_SUMMARY, dubbing.getVideoSummary());
        dubbingInfoMap.put(RES_RESULT_DUBBING_CATEGORY_ID, dubbing.getCategoryId());
        //话题
        dubbingInfoMap.put(RES_RESULT_DUBBING_TOPIC_LIST, CollectionUtils.isEmpty(dubbing.getTopics()) ? Collections.emptyList() : dubbing.getTopics().stream().filter(p -> StringUtils.isNotBlank(p.getName())).map(Dubbing.DubbingTopic::getName).collect(Collectors.toList()));

        return MapMessage.successMessage().add(RES_RESULT_DUBBING_INFO, dubbingInfoMap);
    }

    @RequestMapping(value = "dubbing_history_info_share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDubbingHistoryInfo() {

        String historyId = getRequestString(REQ_DUBBING_HISTORY_ID);
        DubbingHistory dubbingHistory = dubbingHistoryLoader.getDubbingHistoryById(historyId);
        if (dubbingHistory == null) {
            return MapMessage.errorMessage(RES_RESULT_DUBBING_HISTORY_NOT_EXIST_MSG);
        }
        Dubbing dubbing = dubbingLoader.loadDubbingByIdIncludeDisabled(dubbingHistory.getDubbingId());
        User user = raikouSystem.loadUser(dubbingHistory.getUserId());
        Map<String, Object> info = new HashMap<>();
        info.put(RES_RESULT_DUBBING_ID, dubbingHistory.getDubbingId());
        info.put(RES_RESULT_DUBBING_NAME, dubbing == null ? "" : dubbing.getVideoName());
        info.put(RES_RESULT_DUBBING_LEVEL, dubbing == null ? Integer.valueOf(0) : dubbing.getDifficult());
        info.put(RES_RESULT_DUBBING_COVER_IMG, dubbing == null ? "" : dubbing.getCoverUrl());
        info.put(RES_RESULT_DUBBING_VIDEO_URL, dubbingHistory.getVideoUrl());
        info.put(RES_RESULT_DUBBING_SUMMARY, dubbing == null ? "" : dubbing.getVideoSummary());
        info.put(RES_RESULT_SENTENCE_COUNT, dubbing == null || CollectionUtils.isEmpty(dubbing.getSentences()) ? 0 : dubbing.getSentences().size());
        info.put(RES_RESULT_DUBBING_CATEGORY_ID, dubbing == null ? "" : dubbing.getCategoryId());
        if (dubbingHistory.getIsHomework()) {
            String createDate = DateUtils.dateToString(dubbingHistory.getCreateTime(), "MM-dd");
            String homeworkText = MessageFormat.format("{0}的配音作业", createDate);
            info.put(RES_RESULT_DUBBING_HOMEWORK_TEXT, homeworkText);
        }
        String userName;
        if (user == null || StringUtils.isBlank(user.fetchRealname())) {
            userName = "小学生";
        } else {
            userName = user.fetchRealname();
        }
        info.put(RES_STUDENT_NAME, userName);
        info.put(RES_AVATAR_URL, getUserAvatarImgUrl(user));
        //话题
        info.put(RES_RESULT_DUBBING_TOPIC_LIST, (dubbing == null || CollectionUtils.isEmpty(dubbing.getTopics())) ? Collections.emptyList() : dubbing.getTopics().stream().filter(p -> StringUtils.isNotBlank(p.getName())).map(Dubbing.DubbingTopic::getName).collect(Collectors.toList()));
        return MapMessage.successMessage().add(RES_RESULT_DUBBING_HISTORY_INFO, info);
    }
}
