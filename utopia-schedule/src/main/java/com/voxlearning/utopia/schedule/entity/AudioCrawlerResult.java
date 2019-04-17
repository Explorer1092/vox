package com.voxlearning.utopia.schedule.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang wei on 2017/7/10.
 */
@Data
public class AudioCrawlerResult implements Serializable {

    private static final long serialVersionUID = 6173765547863903890L;
    private String id;
    private String title;
    private List<String> real_url;
    private String real_url_32;
    private String real_url_64;
    private String file_url;
}
