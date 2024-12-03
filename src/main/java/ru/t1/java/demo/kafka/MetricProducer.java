//package ru.t1.java.demo.kafka;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.kafka.support.SendResult;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MetricProducer {
//
//    private final KafkaTemplate<String, Object> template;
//
//    public CompletableFuture<SendResult<String, Object>> send(String topic, Object message, String headerKey, String headerValue) {
//        String messageKey = UUID.randomUUID().toString();
//
//        Message<Object> kafkaMessage = MessageBuilder.withPayload(message)
//                .setHeader(KafkaHeaders.TOPIC, topic)
//                .setHeader(KafkaHeaders.KEY, messageKey)
//                .setHeader(headerKey, headerValue)
//                .build();
//        try {
//            return template.send(kafkaMessage).toCompletableFuture();
//        }catch (Exception e){
//            log.error("Error sending message to Kafka: {}", e.getMessage(), e);
//            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
//            failedFuture.completeExceptionally(e);
//            return failedFuture;
//        }finally {
//            template.flush();
//        }
//    }
//}
