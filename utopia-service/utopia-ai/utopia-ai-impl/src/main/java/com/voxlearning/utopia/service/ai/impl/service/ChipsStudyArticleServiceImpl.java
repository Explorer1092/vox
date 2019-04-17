package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsStudyArticleService;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.entity.ChipsStudyArticle;
import com.voxlearning.utopia.service.ai.entity.ChipsUserPageViewLog;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsStudyArticleDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserPageViewLogDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Named
@ExposeService(interfaceClass = ChipsStudyArticleService.class)
public class ChipsStudyArticleServiceImpl implements ChipsStudyArticleService {

    @Inject
    private ChipsStudyArticleDao chipsStudyArticleDao;
    @Inject
    private ChipsUserPageViewLogDao userPageViewLogDao;

    @Override
    public MapMessage loadArticleListForCrm() {
        List<ChipsStudyArticle> list = chipsStudyArticleDao.query();
        List<Map<String, Object>> mapList = new ArrayList<>();
        list.forEach(article -> {
            List<ChipsUserPageViewLog> logList = userPageViewLogDao.loadByUniqueKeyAndType(PageViewType.STUDY_INFORMATION, article.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", article.getId());
            map.put("title", article.getTitle());
            map.put("num", logList.size());
            map.put("updateTime", DateUtils.dateToString(article.getUpdateDate(), DateUtils.FORMAT_SQL_DATETIME));
            mapList.add(map);
        });
        MapMessage message = MapMessage.successMessage();
        message.add("articleList", mapList);
        return message;
    }

    @Override
    public MapMessage upsertStudyArticle(ChipsStudyArticle article) {
        ChipsStudyArticle upsert = chipsStudyArticleDao.upsert(article);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadArticleForCrm(String articleId) {
        if (StringUtils.isBlank(articleId)) {
            return MapMessage.successMessage();
        }
        ChipsStudyArticle article = chipsStudyArticleDao.load(articleId);
        MapMessage message = MapMessage.successMessage();
        message.add("article", article);
        return message;
    }
}
