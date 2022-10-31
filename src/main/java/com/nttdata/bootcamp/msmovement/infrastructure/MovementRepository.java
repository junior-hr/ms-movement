package com.nttdata.bootcamp.msmovement.infrastructure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.nttdata.bootcamp.msmovement.model.Movement;
import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MovementRepository extends ReactiveMongoRepository<Movement, String> {
    @Aggregation(pipeline = {"{ '$match': { 'accountNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    Mono<MovementDto> findLastMovementByAccount(String accountNumber) ;

    @Aggregation(pipeline = {"{ '$match': { 'accountNumber' : ?0, 'idMovement' : { $ne: ?1 } } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    Mono<MovementDto> findLastMovementByAccountExceptCurrentId(String accountNumber, String idMovement) ;

    @Aggregation(pipeline = {"{ '$match': { 'accountNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }"})
    Flux<MovementDto> findMovementsByAccount(String accountNumber) ;

    @Query(value = "{$and:[{'movementDate':{$gte:  { '$date' : ?0} }},{'movementDate': {$lte:  { '$date' : ?1} }}],'accountNumber':?2}")
    Flux<Movement> findMovementsByDateRange(String iniDate,String finalDate,String accountNumber);

    @Aggregation(pipeline = {"{ '$match': { 'credit.creditNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    public Mono<Movement> findByCreditNumber(Integer creditNumber);

    @Aggregation(pipeline = {"{ '$match': { 'loan.loanNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }"})
    Flux<MovementDto> findMovementsByLoanNumber(String loanNumber) ;

    @Aggregation(pipeline = {"{ '$match': { 'credit.creditNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }"})
    Flux<MovementDto> findMovementsByCreditNumber(String creditNumber) ;

}