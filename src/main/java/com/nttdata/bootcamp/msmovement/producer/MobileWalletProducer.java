package com.nttdata.bootcamp.msmovement.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.nttdata.bootcamp.msmovement.producer.mapper.BalanceMobileWalletModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class MobileWalletProducer.
 * Movement microservice class MobileWalletProducer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MobileWalletProducer {
    private final KafkaTemplate<String, BalanceMobileWalletModel> kafkaTemplate;

    @Value(value = "${spring.kafka.topic.mobile.name}")
    private String topic;

    public void sendMessage(BalanceMobileWalletModel balanceModel) {

        ListenableFuture<SendResult<String, BalanceMobileWalletModel>>
                future = kafkaTemplate.send(this.topic, balanceModel);

        future.addCallback(new ListenableFutureCallback<SendResult<String,
                BalanceMobileWalletModel>>() {
            @Override
            public void onSuccess(SendResult<String, BalanceMobileWalletModel> result) {
                log.info("Message {} has been sent ", balanceModel);
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Something went wrong with the balanceModel {} ", balanceModel);
            }
        });
    }
}
