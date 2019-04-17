package com.voxlearning.utopia.service.reward.mapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(of = {"parentId","childMap"})
public class PGParentChildRef implements Serializable{

    private static final long serialVersionUID = -2959640527841184576L;

    private Long parentId;
    private Map<Long,ChildRef> childMap;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"childId","finishCollectIds"})
    public static class ChildRef implements Serializable{
        private static final long serialVersionUID = 841654065233414517L;
        private Long childId;
        private Set<String> finishCollectIds; // 完成的教室ID列表

        boolean addCollectId(String collectId){
            if(finishCollectIds == null)
                finishCollectIds = new HashSet<>();

            return finishCollectIds.add(collectId);
        }
    }

    public boolean addFinishCollectId(Long childId,String collcetId){
        if(childMap == null)
            childMap = new HashMap<>();

        ChildRef childRef = childMap.computeIfAbsent(childId,cId -> {
            ChildRef newRef = new ChildRef();
            newRef.childId = cId;
            return newRef;
        });

        return childRef.addCollectId(collcetId);
    }

    public boolean hadChildData(){
        return childMap != null && childMap.size() > 0;
    }
}
