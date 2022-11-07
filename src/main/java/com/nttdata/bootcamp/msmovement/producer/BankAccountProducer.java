package com.nttdata.bootcamp.msmovement.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.nttdata.bootcamp.msmovement.producer.mapper.BalanceBankAccountModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Class BankAccountProducer.
 * Movement microservice class BankAccountProducer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BankAccountProducer {
    private final KafkaTemplate<String, BalanceBankAccountModel> kafkaTemplate;

    @Value(value = "${spring.kafka.topic.bank.name}")
    private String topic;

    public void sendMessage(BalanceBankAccountModel balanceModel) {

        ListenableFuture<SendResult<String, BalanceBankAccountModel>>
                future = kafkaTemplate.send(this.topic, balanceModel);

        future.addCallback(new ListenableFutureCallback<SendResult<String,
                BalanceBankAccountModel>>() {
            @Override
            public void onSuccess(SendResult<String, BalanceBankAccountModel> result) {
                log.info("Message {} has been sent ", balanceModel);
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Something went wrong with the balanceModel {} ", balanceModel);
            }
        });
    }
}
