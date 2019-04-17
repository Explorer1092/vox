package com.voxlearning.utopia.service.zone.data;

import com.voxlearning.utopia.service.zone.api.entity.UserMood;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class VersionedUserMoodData implements Serializable {
    private static final long serialVersionUID = 3064679493259035474L;

    private long version;                   // 0 means initial version
    private List<UserMood> userMoodList;
}
