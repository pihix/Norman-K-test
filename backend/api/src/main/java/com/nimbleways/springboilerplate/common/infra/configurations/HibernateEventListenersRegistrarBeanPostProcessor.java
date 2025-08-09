package com.nimbleways.springboilerplate.common.infra.configurations;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HibernateEventListenersRegistrarBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(
        @SuppressWarnings("NullableProblems") Object bean,
        @SuppressWarnings("NullableProblems") String beanName
    ) {
        if (bean instanceof EntityManagerFactory entityManagerFactory) {
            EventListenerRegistry registry = getEventListenerRegistry(entityManagerFactory);
            registry.getEventListenerGroup(EventType.DELETE).appendListener(new FlushOnSoftDeleteEventListener());
        }
        return bean;
    }

    private static EventListenerRegistry getEventListenerRegistry(EntityManagerFactory entityManagerFactory) {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        return sessionFactory.getServiceRegistry().requireService(EventListenerRegistry.class);
    }

    static class FlushOnSoftDeleteEventListener implements DeleteEventListener {

        @Override
        public void onDelete(DeleteEvent deleteEvent, DeleteContext deleteContext) {
            onDelete(deleteEvent);
        }

        @Override
        public void onDelete(DeleteEvent deleteEvent) {
            if (!withinCascade(deleteEvent) && isSoftDeletable(deleteEvent)) {
                deleteEvent.getSession().getSession().flush();
            }
        }

        private static boolean withinCascade(DeleteEvent deleteEvent) {
            return deleteEvent.getSession().getSession().getPersistenceContext().getCascadeLevel() > 0;
        }

        private static boolean isSoftDeletable(DeleteEvent deleteEvent) {
            return deleteEvent.getObject().getClass().isAnnotationPresent(SQLDelete.class);
        }
    }
}
