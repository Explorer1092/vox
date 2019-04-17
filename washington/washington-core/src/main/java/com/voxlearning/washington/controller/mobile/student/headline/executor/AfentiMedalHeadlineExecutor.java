package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.AfentiMedalHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 阿分题勋章相关动态处理
 *
 * @author yuechen.wang
 * @since 2018/01/15
 */
@Named
public class AfentiMedalHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Arrays.asList(
                ClazzJournalType.AFENTI_NEW_MEDAL,
                ClazzJournalType.AFENTI_MEDAL_LEVEL_UP
        );
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;

        AfentiMedalHeadlineMapper mapper = new AfentiMedalHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, user, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setImgUrl(SafeConverter.toString(extInfo.get("imgUrl")));
        mapper.setMedalName(SafeConverter.toString(extInfo.get("medalName")));
        mapper.setLevel(SafeConverter.toString(extInfo.get("level")));
        mapper.setAppName(SafeConverter.toString(extInfo.get("appName")));
        if (clazzJournal.getJournalType() == ClazzJournalType.AFENTI_NEW_MEDAL) {
            mapper.setContentText(StringUtils.formatMessage("我在{}中获得了勋章，一起去看看吧", mapper.getAppName()));
        } else {
            mapper.setContentText(StringUtils.formatMessage("我在{}中的勋章升级了，不服来战", mapper.getAppName()));
        }
        return mapper;
    }
}
