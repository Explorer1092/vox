package com.voxlearning.washington.mapper;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by jiang wei on 2017/5/15.
 */
@Data
public class AlbumAbilityTagConfig {
    private Integer subjectId;
    private Map<String, List<Long>> dataTags;
    
}
