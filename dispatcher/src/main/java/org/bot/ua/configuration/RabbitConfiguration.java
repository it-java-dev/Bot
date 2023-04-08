package org.bot.ua.configuration;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bot.ua.model.RabbitQueue.*;

@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter jsonMassageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public Queue textMassageQueue() {
        return new Queue(TEXT_MASSAGE_UPDATE);
    }


    @Bean
    public Queue fileMassageQueue() {
        return new Queue(FILE_MASSAGE_UPDATE);
    }

    @Bean
    public Queue filesGroupMassageQueue() {
        return new Queue(FILES_MASSAGE_UPDATE);
    }
    @Bean
    public Queue documentMassageQueue() {
        return new Queue(DOCUMENT_MASSAGE_UPDATE);
    }
    @Bean
    public Queue photoMassageQueue() {
        return new Queue(PHOTO_MASSAGE_UPDATE);
    }
    @Bean
    public Queue answerMassageQueue() {
        return new Queue(ANSWER_MESSAGE);
    }

}
