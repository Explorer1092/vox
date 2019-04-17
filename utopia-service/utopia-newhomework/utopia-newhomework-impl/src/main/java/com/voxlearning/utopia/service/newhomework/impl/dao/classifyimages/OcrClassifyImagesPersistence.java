package com.voxlearning.utopia.service.newhomework.impl.dao.classifyimages;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.newhomework.api.entity.classifyimages.OcrClassifyImages;

import javax.inject.Named;
import java.util.Collection;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/10/25
 * \* Time: 5:32 PM
 * \* Description:纸质口算鉴黄替换图片记录数据
 * \
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.dao.classifyimages.OcrClassifyImagesPersistence")
@CacheBean(type = OcrClassifyImages.class)
public class OcrClassifyImagesPersistence extends AlpsStaticJdbcDao<OcrClassifyImages, Long> {
    @Override
    protected void calculateCacheDimensions(OcrClassifyImages document, Collection<String> dimensions) {
        dimensions.add(OcrClassifyImages.ck_id(document.getId()));
    }

    /**
     * 根据processId获取原始图片
     *
     * @param processId
     * @return
     */
    public String getOriginImageUrlByProcessId(String processId) {
        if (StringUtils.isEmpty(processId)) {
            return "";
        }
        Criteria criteria = Criteria.where("PROCESS_ID").is(processId);
        Query query = Query.query(criteria);
        query.field().includes("ORIGINAL_IMAGE_URL");
        query.limit(1);
        query.with(new Sort(Sort.Direction.ASC, "CREATE_DATETIME"));
        OcrClassifyImages ocrClassifyImages;
        ocrClassifyImages = query(query).stream().findFirst().orElse(null);
        if (ocrClassifyImages == null) {
            return "";
        }
        return ocrClassifyImages.getOriginalImageUrl();
    }
}
