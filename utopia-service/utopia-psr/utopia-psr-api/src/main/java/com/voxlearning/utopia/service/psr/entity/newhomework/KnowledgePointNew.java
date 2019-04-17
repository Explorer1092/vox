package com.voxlearning.utopia.service.psr.entity.newhomework;

import com.voxlearning.alps.annotation.dao.DocumentField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/18
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class KnowledgePointNew implements Serializable {

    private static final long serialVersionUID = -1213327959207441715L;
    @DocumentField("tag_id")
    private Integer tag_id;
    @DocumentField("main")
    private Integer main;
    @DocumentField("id")
    private String id;
    @DocumentField("feature_ids")
    private List<String> feature_ids;
    @DocumentField("kpf_id")
    private String kpf_id;

}
