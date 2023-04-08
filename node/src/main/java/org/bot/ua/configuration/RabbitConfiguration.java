package org.bot.ua.configuration;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    //Method convert update in json format, then transfer it to rabbitMQ
    @Bean
    public MessageConverter jsonMassageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
