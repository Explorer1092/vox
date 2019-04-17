package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/1/4
 */
@Getter
@Setter
public class ActiveServiceKnowledge implements Serializable {

    private static final long serialVersionUID = -5861210639258635090L;
    //    private List<Field> fieldList;
    private String key;
    private String value;
    private Type type;

    private ActiveServiceKnowledge() {
    }

    public static List<ActiveServiceKnowledge> build() {
        List<ActiveServiceKnowledge> list = new ArrayList<>();
        ActiveServiceKnowledge k1 = new ActiveServiceKnowledge();
        k1.setKey("知识加油站1");
        k1.setValue("");
        k1.setType(Type.text);
        list.add(k1);

        ActiveServiceKnowledge k2 = new ActiveServiceKnowledge();
        k2.setKey("知识加油站1音频");
        k2.setValue("");
        k2.setType(Type.audio);
        list.add(k2);

        ActiveServiceKnowledge k3 = new ActiveServiceKnowledge();
        k3.setKey("知识加油站2");
        k3.setValue("");
        k3.setType(Type.text);
        list.add(k3);

        ActiveServiceKnowledge k4 = new ActiveServiceKnowledge();
        k4.setKey("知识加油站2音频");
        k4.setValue("");
        k4.setType(Type.audio);
        list.add(k4);
        ActiveServiceKnowledge k5 = new ActiveServiceKnowledge();
        k5.setKey("知识加油站3");
        k5.setValue("");
        k5.setType(Type.text);
        list.add(k5);

        ActiveServiceKnowledge k6 = new ActiveServiceKnowledge();
        k6.setKey("知识加油站3音频");
        k6.setValue("");
        k6.setType(Type.audio);
        list.add(k6);

        return list;
    }

//    public static ActiveServiceKnowledge newInstance() {
//        ActiveServiceKnowledge knowledge = new ActiveServiceKnowledge();
//        List<Field> fieldList = new ArrayList<>();
//        Field f1 = new Field();
//        f1.setKey("知识加油站1");
//        f1.setValue("");
//        f1.setType(Type.text);
//        fieldList.add(f1);
//
//        Field f2 = new Field();
//        f2.setKey("知识加油站1音频");
//        f2.setValue("");
//        f2.setType(Type.audio);
//        fieldList.add(f2);
//
//        Field f3 = new Field();
//        f3.setKey("知识加油站2");
//        f3.setValue("");
//        f3.setType(Type.text);
//        fieldList.add(f3);
//
//        Field f4 = new Field();
//        f4.setKey("知识加油站2音频");
//        f4.setValue("");
//        f4.setType(Type.audio);
//        fieldList.add(f4);
//
//        Field f5 = new Field();
//        f5.setKey("知识加油站3");
//        f5.setValue("");
//        f5.setType(Type.text);
//        fieldList.add(f5);
//
//        Field f6 = new Field();
//        f6.setKey("知识加油站3音频");
//        f6.setValue("");
//        f6.setType(Type.audio);
//        fieldList.add(f6);
//
//        return knowledge;
//    }
//
//    @Getter
//    @Setter
//    public static class Field implements Serializable {
//        private static final long serialVersionUID = 5606187750312961528L;
//        private String key;
//        private String value;
//        private Type type;
//    }

    public enum Type {
        text,
        audio
    }

    @Override
    public String toString() {
        return "ActiveServiceKnowledge{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type=" + type +
                '}';
    }
}


