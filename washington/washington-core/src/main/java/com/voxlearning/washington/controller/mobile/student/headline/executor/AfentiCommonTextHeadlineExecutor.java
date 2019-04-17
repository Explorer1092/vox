package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.AfentiCommonTextHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 阿分题通用分享处理
 *
 * @author 陈司南
 * @since 2018/11/30
 */
@Named
public class AfentiCommonTextHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.AFENTI_COMMON_TEXT);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        AfentiCommonTextHeadlineMapper mapper = new AfentiCommonTextHeadlineMapper();
        fillInteractiveMapper(mapper, clazzJournal, user, context);
        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());
        mapper.setContentText(SafeConverter.toString(extInfo.get("contentText")));
        mapper.setAppName(SafeConverter.toString(extInfo.get("appName")));
        mapper.setTargetType(SafeConverter.toInt(extInfo.get("targetType")));
        mapper.setTargetUrl(SafeConverter.toString(extInfo.get("targetUrl")));
        return mapper;
    }


}
