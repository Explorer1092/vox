package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author songtao
 * @since 2018/4/1
 */
@Getter
@Setter
public class AIQuestion implements Serializable {
    private static final long serialVersionUID = -7016745392131794262L;
    private String id;
    private Integer type;  // 1 热身单词 2 热身句子 3 情景对话  4 任务
    private String audio;
    private List<String> answer;
    private List<Preload> preloads;
    private String image;
    private String content;
    private String description;
    private List<Role> roles;
    private boolean finished;

    @Getter
    @Setter
    public static class Preload implements Serializable {
        private static final long serialVersionUID = 8394674479994546763L;
        private String description;
        private String audio;
        private List<SubPreload> sentences;
    }

    @Getter
    @Setter
    public static class SubPreload  implements Serializable {

        private static final long serialVersionUID = -6657314135119637183L;
        private String image;
        private String audio;
        private String content;
        private String role;
    }

    @Getter
    @Setter
    public static class Role implements Serializable {
        private static final long serialVersionUID = -6657314135119637183L;
        private String image;
        private String name;
        private String background;
    }

}
