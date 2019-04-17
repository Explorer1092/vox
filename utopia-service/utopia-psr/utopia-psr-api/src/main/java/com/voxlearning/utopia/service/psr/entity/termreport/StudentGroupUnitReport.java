package com.voxlearning.utopia.service.psr.entity.termreport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;


/**
 * Created by mingming.zhao on 2016/10/20.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class StudentGroupUnitReport implements Serializable {
    private static final long serialVersionUID = 7025638775590359616L;
    private String studentId;
    private Integer ontime_num;
    private Integer makeup_num;
    private Integer notdone_num;
    private Double avgscores;
    private Double do_homework_duration;
    private Double attendance_rate;
    private List<String> homeworkids;
}




