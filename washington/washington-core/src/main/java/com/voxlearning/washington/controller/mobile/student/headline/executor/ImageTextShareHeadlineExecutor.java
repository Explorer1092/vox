package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.ImageTextShareHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 图文分享动态处理
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Named
public class ImageTextShareHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.IMAGE_TEXT_SHARE);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        ImageTextShareHeadlineMapper mapper = new ImageTextShareHeadlineMapper();
        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());
        mapper.setImageUrl(SafeConverter.toString(extInfo.get("imageUrl")));
        mapper.setContent(SafeConverter.toString(extInfo.get("content")));
        mapper.setLinkUrl(SafeConverter.toString(extInfo.get("linkUrl")));
        mapper.setTitle(SafeConverter.toString(extInfo.get("title")));

        return mapper;
    }
}
