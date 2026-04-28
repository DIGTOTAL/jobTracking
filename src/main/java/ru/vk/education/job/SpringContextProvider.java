package ru.vk.education.job;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Утилита для доступа к Spring ApplicationContext из статических/устаревших классов.
 * Используется только как transitional adapter для удаления singletons в корневом пакете.
 */
@Component
public class SpringContextProvider implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (CONTEXT == null) {
            throw new IllegalStateException("Spring ApplicationContext is not initialized");
        }
        return CONTEXT.getBean(beanClass);
    }
}
