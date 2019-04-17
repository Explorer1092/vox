package com.voxlearning.utopia.admin.data;

import com.voxlearning.utopia.entity.ucenter.CertificationApplicationOperatingLog;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Sadi.Wan on 2014/12/4.
 */
@Getter
@Setter
public class P2pRecordMapper implements Serializable {
    private static final long serialVersionUID = -8608394144124025523L;
    private long teacherId;
    private String teacherName;
    private String teacherCell;
    private String adminName;
    private String latestHomeworkTimeString;
    private Date latestHomeworkTime;
    private int homeworkTime30d;
    private List<CertificationApplicationOperatingLog> opLogList = Collections.emptyList();
    private String note;
}
