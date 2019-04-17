package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.data.UserRecordSnapshot;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * AgentTaskDetailData
 *
 * @author song.wang
 * @date 2016/8/4
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentTaskDetailData implements Serializable {
    private AgentTaskDetail taskDetail;
    private List<UserRecordSnapshot> userRecords;
}
