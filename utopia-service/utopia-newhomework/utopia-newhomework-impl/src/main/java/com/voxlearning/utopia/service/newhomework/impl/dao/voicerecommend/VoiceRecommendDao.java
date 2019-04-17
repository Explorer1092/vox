package com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.VoiceRecommendV2;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkCopyUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class VoiceRecommendDao {
    @Inject private VoiceRecommendV2Dao voiceRecommendV2Dao;

    public VoiceRecommend load(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        VoiceRecommendV2 voiceRecommendV2 = voiceRecommendV2Dao.load(id);
        if (voiceRecommendV2 != null) {
            VoiceRecommend voiceRecommend = new VoiceRecommend();
            HomeworkCopyUtils.copyProperties(voiceRecommend, voiceRecommendV2);
            return voiceRecommend;
        }
        return null;
    }

    public Map<String, VoiceRecommend> loads(Collection<String> ids) {
        Set<String> idSet = CollectionUtils.toLinkedHashSet(ids);
        if (CollectionUtils.isEmpty(idSet)) {
            return Collections.emptyMap();
        }
        Map<String, VoiceRecommendV2> v2Map = voiceRecommendV2Dao.loads(idSet);
        Map<String, VoiceRecommend> resultMap = new LinkedHashMap<>();
        for (String id : idSet) {
            VoiceRecommendV2 v2 = v2Map.get(id);
            if (v2 != null) {
                VoiceRecommend voiceRecommend = new VoiceRecommend();
                HomeworkCopyUtils.copyProperties(voiceRecommend, v2);
                resultMap.put(id, voiceRecommend);
            }
        }
        return resultMap;
    }

    public void upsert(VoiceRecommend voiceRecommend) {
        VoiceRecommendV2 v2 = new VoiceRecommendV2();
        HomeworkCopyUtils.copyProperties(v2, voiceRecommend);
        voiceRecommendV2Dao.upsert(v2);
    }
}
