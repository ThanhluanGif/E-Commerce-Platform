package com.ecommerce.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotificationRabbitConfig {

    public static final String INVOICE_EMAIL_QUEUE = "notification.invoice.email.queue";
    public static final String INVOICE_EMAIL_DLQ = "notification.invoice.email.dlq";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_DLX = "notification.dlx";
    public static final String INVOICE_EMAIL_ROUTING_KEY = "notification.invoice.email";
    public static final String INVOICE_EMAIL_DLQ_ROUTING_KEY = "notification.invoice.email.dlq";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    @Bean
    public Queue invoiceEmailQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATION_DLX);
        args.put("x-dead-letter-routing-key", INVOICE_EMAIL_DLQ_ROUTING_KEY);
        return QueueBuilder.durable(INVOICE_EMAIL_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue invoiceEmailDlq() {
        return QueueBuilder.durable(INVOICE_EMAIL_DLQ).build();
    }

    @Bean
    public Binding invoiceEmailBinding(Queue invoiceEmailQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(invoiceEmailQueue).to(notificationExchange).with(INVOICE_EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding invoiceEmailDlqBinding(Queue invoiceEmailDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(invoiceEmailDlq).to(notificationDlx).with(INVOICE_EMAIL_DLQ_ROUTING_KEY);
    }
}
