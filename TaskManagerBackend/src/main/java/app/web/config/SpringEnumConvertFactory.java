package app.web.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class SpringEnumConvertFactory implements ConverterFactory<String, Enum<?>> {
    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> {
            if (source == null || source.isBlank()) return null;
            for (T constant : targetType.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(source)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException();
        };
    }
}
