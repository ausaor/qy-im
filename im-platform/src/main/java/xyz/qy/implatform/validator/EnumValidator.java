package xyz.qy.implatform.validator;

import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author aurora
 */
public class EnumValidator implements ConstraintValidator<ValidEnum, Object> {

    private Class<? extends Enum<?>> enumClass;
    private String property;
    private boolean ignoreCase;
    private Method propertyMethod;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.property = constraintAnnotation.property();
        this.ignoreCase = constraintAnnotation.ignoreCase();

        try {
            // 查找枚举类中指定属性的 getter 方法
            this.propertyMethod = findPropertyMethod();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("枚举类 " + enumClass.getName() +
                    " 中未找到属性 '" + property + "' 的 getter 方法", e);
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 允许空值
        }

        // 获取所有枚举值
        Enum<?>[] enumConstants = enumClass.getEnumConstants();

        // 尝试匹配枚举值
        for (Enum<?> enumConstant : enumConstants) {
            try {
                Object propertyValue = propertyMethod.invoke(enumConstant);
                if (matches(value, propertyValue)) {
                    return true;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                // 忽略异常，继续匹配下一个
            }
        }

        return false;
    }

    private boolean matches(Object inputValue, Object enumPropertyValue) {
        if (inputValue == null || enumPropertyValue == null) {
            return false;
        }

        if (ignoreCase) {
            return inputValue.toString().equalsIgnoreCase(enumPropertyValue.toString());
        }

        return inputValue.toString().equals(enumPropertyValue.toString());
    }

    private Method findPropertyMethod() throws NoSuchMethodException {
        // 尝试查找 getProperty() 方法
        String getterName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
        try {
            return enumClass.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            // 尝试查找 property() 方法
            try {
                return enumClass.getMethod(property);
            } catch (NoSuchMethodException ex) {
                // 尝试查找 isProperty() 方法（用于布尔属性）
                String isGetterName = "is" + property.substring(0, 1).toUpperCase() + property.substring(1);
                return enumClass.getMethod(isGetterName);
            }
        }
    }
}
