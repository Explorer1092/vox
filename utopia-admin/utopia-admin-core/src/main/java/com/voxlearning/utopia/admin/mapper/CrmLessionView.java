package com.voxlearning.utopia.admin.mapper;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CrmLessionView {

    /**
     * 课程 ID
     */
    private String lessonId;

    /**
     * 课程真实名称
     */
    private String lessonRealName;

    /**
     * 课程别名
     */
    private String lessonAliasName;

    public static class Builder {
        public static List<CrmLessionView> build(List<NewBookCatalog> bookCatalogs) {
            List<CrmLessionView> viewList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(bookCatalogs)) {
                for (NewBookCatalog bookCatalog : bookCatalogs) {
                    CrmLessionView crmLessionView = new CrmLessionView();
                    crmLessionView.setLessonId(bookCatalog.getId());
                    crmLessionView.setLessonRealName(bookCatalog.getName());
                    crmLessionView.setLessonAliasName(bookCatalog.getAlias());
                    viewList.add(crmLessionView);
                }
            }
            return viewList;
        }
    }
}
