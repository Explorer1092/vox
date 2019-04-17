package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.HomelandPostCardHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 成长世界伙伴动态处理
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Named
public class HomelandHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Arrays.asList(
                ClazzJournalType.HOMELAND_SHARE_POSTCARD
        );
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        HomelandPostCardHeadlineMapper mapper = new HomelandPostCardHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setCardImg(SafeConverter.toString(extInfo.get("postcardPic")));
        mapper.setCardName(SafeConverter.toString(extInfo.get("postcardName")));
        mapper.setTypeImg(SafeConverter.toString(extInfo.get("levelPic")));
        mapper.setTypeText(SafeConverter.toString(extInfo.get("levelName")));
        mapper.setCount(SafeConverter.toInt(extInfo.get("times")));
        mapper.setLinkUrl(SafeConverter.toString(extInfo.get("statusUrl")));

        return mapper;
    }
}
