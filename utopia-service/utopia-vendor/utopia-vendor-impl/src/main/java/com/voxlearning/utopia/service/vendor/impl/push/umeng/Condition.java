package com.voxlearning.utopia.service.vendor.impl.push.umeng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 11/11/2016
 */
public class Condition {
    private Operator operator;
    private Map<String, Object> value;
    private List<Condition> conditions;

    public enum Operator {
        AND,
        OR,
        NOT,
        NONE
    }

    private Condition(Operator operator, Map<String, Object> value) {
        this.operator = operator;
        this.value = value;
    }

    private Condition(Operator operator, Condition condition) {
        this.operator = operator;
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        this.conditions =conditions;
    }

    public static Condition none(Map<String, Object> value) {
        return new Condition(Operator.NONE, value);
    }

    public static Condition and(Map<String, Object> value) {
        return new Condition(Operator.AND, new Condition(Operator.NONE, value));
    }

    public static Condition or(Map<String, Object> value) {
        return new Condition(Operator.OR, new Condition(Operator.NONE, value));
    }

    public static Condition not(Map<String, Object> value) {
        return new Condition(Operator.NOT, value);
    }

    public Condition and(Condition addCondition) {
        if (this.operator == Operator.NONE && addCondition.operator == Operator.NONE) {
            Condition condition = new Condition(Operator.AND, addCondition);
            condition.conditions.add(this);
            return condition;
        }

        if (this.operator == Operator.AND && addCondition.operator == Operator.AND) {
            this.conditions.addAll(addCondition.conditions);
            return this;
        }

        if (this.operator == Operator.NONE && addCondition.operator == Operator.AND) {
            addCondition.conditions.add(this);
            return addCondition;
        }

        if (addCondition.operator == Operator.NONE && this.operator == Operator.AND) {
            this.conditions.add(addCondition);
            return this;
        }

        Condition condition = new Condition(Operator.AND, addCondition);
        condition.conditions.add(this);
        return condition;
    }

    public Condition or(Condition orCondition) {
        if (this.operator == Operator.NONE && orCondition.operator == Operator.NONE) {
            Condition condition = new Condition(Operator.OR, orCondition);
            condition.conditions.add(this);
            return condition;
        }

        if (this.operator == Operator.OR && orCondition.operator == Operator.OR) {
            this.conditions.addAll(orCondition.conditions);
            return this;
        }

        if (this.operator == Operator.NONE && orCondition.operator == Operator.OR) {
            orCondition.conditions.add(this);
            return orCondition;
        }

        if (this.operator == Operator.OR && orCondition.operator == Operator.NONE) {
            this.conditions.add(orCondition);
            return this;
        }

        Condition condition = new Condition(Operator.OR, orCondition);
        condition.conditions.add(this);
        return condition;
    }

    public Map<String, Object> toMap() {
        if (this.operator == Operator.NONE) {
            return this.value;
        }

        Map<String, Object> result = new HashMap<>();

        if (this.operator == Operator.NOT) {
            result.put("not", value);
        } else if (this.operator == Operator.AND) {
            List<Map<String, Object>> conditions = new ArrayList<>();
            for (Condition c : this.conditions) {
                conditions.add(c.toMap());
            }
            result.put("and", conditions);
        } else if (this.operator == Operator.OR) {
            List<Map<String, Object>> conditions = new ArrayList<>();
            for (Condition c : this.conditions) {
                conditions.add(c.toMap());
            }
            result.put("or", conditions);
        }

        return result;
    }
}
