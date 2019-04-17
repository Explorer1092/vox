package com.voxlearning.utopia.agent.mockexam.domain.validate;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 校验工具
 *
 * @author xiaolei.li
 * @version 2018/8/18
 * @see <a href="http://hibernate.org/validator/releases/6.0/">Hibernate Validator</a>
 */
public class ValidateUtil {

    /**
     * 约束性校验
     *
     * @param model 领域模型
     * @return 校验结果
     */
    public static ValidateResult validate(Object model) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> _rs = validator.validate(model);
        ValidateResult result = new ValidateResult();
        if (!_rs.isEmpty()) {
            result.setSuccess(false);
            result.setItems(_rs.stream().map(i -> {
                ValidateResult.Item item = new ValidateResult.Item();
                item.setPropertyName(i.getPropertyPath().toString());
                item.setPropertyValue(i.getInvalidValue());
                item.setMessage(i.getMessage());
                i.getMessage();
                return item;
            }).collect(Collectors.toList()));
        } else {
            result.setSuccess(true);
        }
        return result;
    }

}
