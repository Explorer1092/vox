package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.AfentiRankHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿分题勋章相关动态处理
 *
 * @author yuechen.wang
 * @since 2018/01/15
 */
@Named
public class AfentiRankHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private VendorLoaderClient vendorLoaderClient;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.AFENTI_RANK_SHARE);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {
        StudentDetail student = studentLoaderClient.loadStudentDetail(clazzJournal.getRelevantUserId());
        if (null == student || null == student.getClazz()) return null;

        AfentiRankHeadlineMapper mapper = new AfentiRankHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, student, context);

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        mapper.setClassName(student.getClazz().formalizeClazzName());
        mapper.setProdName(SafeConverter.toString(extInfo.get("prodName")));
        mapper.setUnitName(SafeConverter.toString(extInfo.get("unitName")));
        mapper.setShareType(SafeConverter.toString(extInfo.get("shareType")));
        mapper.setOnList(SafeConverter.toBoolean(extInfo.get("onList")));
        mapper.setRank(SafeConverter.toInt(extInfo.get("rank")));
        mapper.setLinkUrl(SafeConverter.toString(extInfo.get("linkUrl")));

        // linkUrl需要特殊处理一下
        String url = SafeConverter.toString(extInfo.get("linkUrl"));
        if (StringUtils.isBlank(url) || !url.contains("?")) return null;

        String appKey = SafeConverter.toString(extInfo.get("appKey"));
        VendorAppsUserRef ref = vendorLoaderClient.loadVendorAppUserRef(appKey, student.getId());
        if (ref == null || StringUtils.isBlank(ref.getSessionKey())) return null;

        String baseUrl = url.substring(0, url.indexOf("?"));
        LinkedHashMap<String, String> params = UrlUtils.parseQueryString(url.substring(url.indexOf("?") + 1));
        params.put("session_key", ref.getSessionKey());

        String linkUrl = UrlUtils.buildUrlQuery(baseUrl, params);
        mapper.setLinkUrl(linkUrl);

        return mapper;
    }

}
