package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.utopia.service.ai.data.AIUserInfoDetail;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author xuan.zhu
 * @date 2018/8/23 20:12
 * description,change me
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AiUserInfoDetailContext extends AbstractAIContext<AiUserInfoDetailContext> {

    // in
    private Long userId;

    // middle
    private ChipsEnglishUserExt chipsEnglishUserExt;
    private ChipsEnglishClassUserRef chipsEnglishClassUserRef;

    //out
    private AIUserInfoDetail aiUserInfoDetail = new AIUserInfoDetail();
}
