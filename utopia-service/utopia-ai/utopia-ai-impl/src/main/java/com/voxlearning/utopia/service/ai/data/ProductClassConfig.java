package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ProductClassConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String teacher;
    private List<Integer> localCode;

//    public static void main(String[] args) {
//        List<ProductClassConfig> configs = new ArrayList<>();
//        ProductClassConfig config = new ProductClassConfig();
//        config.setTeacher(ChipsEnglishTeacher.David.name());
//        config.setLocalCode(Arrays.asList(110100, 310100, 440300));
//        configs.add(config);
//        ProductClassConfig config1 = new ProductClassConfig();
//        config1.setTeacher(ChipsEnglishTeacher.Winston.name());
//        configs.add(config1);
//
//        System.out.println(JsonUtils.toJson(configs));
//    }
}
