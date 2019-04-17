package com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class KnowledgeNameBO implements Serializable {
    private static final long serialVersionUID = 8314567811472611451L;
    private KpModule kpModule = new KpModule();
    private SmTmModule smTmModule = new SmTmModule();

    @Getter
    @Setter
    public static class KpModule implements Serializable {
        private List<String> kpIds = new LinkedList<>();
        private List<String> kpNames = new LinkedList<>();
        private List<String> kpfIds = new LinkedList<>();
        private List<String> kpfNames = new LinkedList<>();
        private String name;
    }

    @Getter
    @Setter
    public static class SmTmModule implements Serializable {
        private static final long serialVersionUID = -3804095163046594178L;
        private List<String> smIds = new LinkedList<>();
        private List<String> smNames = new LinkedList<>();
        private List<String> tmIds = new LinkedList<>();
        private List<String> tmNames = new LinkedList<>();
        private String name;
    }
}
