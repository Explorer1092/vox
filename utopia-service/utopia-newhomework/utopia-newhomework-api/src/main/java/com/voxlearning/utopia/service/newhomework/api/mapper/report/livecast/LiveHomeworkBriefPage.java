package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;


import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class LiveHomeworkBriefPage implements Serializable {
    private static final long serialVersionUID = 3625038803384809560L;
    private List<LiveHomeworkBrief> content;
    private long totalElements;
    private long totalPages;
    private int size;
    private int number;

    public LiveHomeworkBriefPage(Page<LiveHomeworkBrief> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
    }

}
