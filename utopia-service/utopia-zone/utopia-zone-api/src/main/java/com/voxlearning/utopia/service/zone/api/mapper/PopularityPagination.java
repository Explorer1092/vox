package com.voxlearning.utopia.service.zone.api.mapper;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RuiBao
 * @since 11/16/2015
 */
public class PopularityPagination extends PageImpl<PopularityPagination.PopularityMapper> {
    private static final long serialVersionUID = -6544713939852977571L;

    public PopularityPagination() {
        this(Collections.emptyList());
    }

    public PopularityPagination(List<PopularityMapper> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PopularityPagination(List<PopularityMapper> content) {
        super(content);
    }

    @Data
    public static class PopularityMapper implements Serializable {
        private static final long serialVersionUID = -5216094415203760283L;

        private Long journalId;
        private Long userId;
        private String userName;
        private String userImg;
        private ClazzJournalType journalType;
        private String date;
        private Long commentImg;
        private String type; // COMMENT OR LIKE
        private Map<String, Object> param = new HashMap<>();
    }
}
