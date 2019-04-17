package com.voxlearning.utopia.service.crm.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class JPushTag implements Serializable {
    @Getter private List<String> orTag;
    @Getter private List<String> andTag;

    public JPushTag(List<String> orTag, List<String> andTag) {
        this.orTag = orTag;
        this.andTag = andTag;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(orTag) && CollectionUtils.isEmpty(andTag);
    }

    public boolean match(Set<String> userTag) {
        if (isEmpty()) return false;

        if (CollectionUtils.isEmpty(userTag)) {
            return false;
        }

        boolean orFlag = CollectionUtils.isEmpty(orTag) || orTag.stream().anyMatch(userTag::contains);
        boolean andFlag = CollectionUtils.isEmpty(andTag) || andTag.stream().allMatch(userTag::contains);

        return orFlag && andFlag;
    }
}