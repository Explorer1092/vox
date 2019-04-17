package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/4/5.
 * 线上表里有两套数据
 * 线上 -》 id: bookid
 * staging -》 id: bookid_staging
 * 给出的实体 bookId 都是处理过,没有 staging 后缀的
 */
@Named
public class TextBookManagementDao extends StaticCacheDimensionDocumentMongoDao<TextBookManagement, String> {


    public TextBookManagement load(String id, Mode mode) {
        return changeIdIfNecessary(load(runtimeModeId(id, mode)), mode);
    }

    public List<TextBookManagement> loadAll(Mode mode) {
        List<TextBookManagement> allList = super.query();
        if (mode == Mode.STAGING){
            allList = allList.stream().filter(TextBookManagement::isStagingData).collect(Collectors.toList());
        }
        if (mode == Mode.PRODUCTION){
            allList = allList.stream().filter(t -> !t.isStagingData()).collect(Collectors.toList());
        }
        return changeIdsIfNecessary(allList, mode);
    }


    public boolean remove(String id, Mode mode) {
        return super.remove(runtimeModeId(id, mode));
    }

    public TextBookManagement upsert(TextBookManagement textBookManagement, Mode mode) {
        textBookManagement.setBookId(runtimeModeId(textBookManagement.getBookId(), mode));
        return super.upsert(textBookManagement);
    }

    public List<TextBookManagement> loadAllIgnoreEnv(){
        return super.query();
    }

    private String runtimeModeId(String id,  Mode mode){
        if (mode == Mode.STAGING)
            id = id + "_staging";
        return id;
    }

    private List<TextBookManagement> changeIdsIfNecessary(Collection<TextBookManagement> textBookManagements, Mode mode){
        if (mode == Mode.STAGING){
            return textBookManagements.stream().map(t -> {
                if (t.isStagingData())
                    t.convertStagingId2OnlineId();
                return t;
            }).collect(Collectors.toList());
        }
        return textBookManagements.stream().collect(Collectors.toList());
    }

    private TextBookManagement changeIdIfNecessary(TextBookManagement textBookManagement, Mode mode){
        if (textBookManagement == null)
            return null;
        return changeIdsIfNecessary(Collections.singleton(textBookManagement), mode).get(0);
    }
}
