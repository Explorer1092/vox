package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TalkResultInfo implements Serializable {
    private static final long serialVersionUID = 4592975817859948942L;
    private List<TalkResultInfoData> data;
}
