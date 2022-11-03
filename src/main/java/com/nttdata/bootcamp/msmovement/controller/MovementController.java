package com.nttdata.bootcamp.msmovement.controller;

import com.nttdata.bootcamp.msmovement.application.MovementService;
import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import com.nttdata.bootcamp.msmovement.model.Movement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movements")
@Slf4j
public class MovementController {
    @Autowired
    private MovementService service;

    @GetMapping
    public Mono<ResponseEntity<Flux<Movement>>> listMovements() {
        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.findAll()));
    }

    @GetMapping("/{idMovement}")
    public Mono<ResponseEntity<Movement>> getMovementsDetails(@PathVariable("idMovement") String idMovement) {
        return service.findById(idMovement).map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> saveMovement(@Valid @RequestBody Mono<MovementDto> movementDto) {
        Map<String, Object> request = new HashMap<>();
        return movementDto.flatMap(mvDto -> {
            return service.save(mvDto).map(c -> {
                request.put("Movimiento", c);
                request.put("mensaje", "Movimiento de  guardado con exito");
                request.put("timestamp", new Date());
                return ResponseEntity.created(URI.create("/api/movements/".concat(c.getIdMovement())))
                        .contentType(MediaType.APPLICATION_JSON).body(request);
            });
        });
    }

    @PostMapping("/mobileWallet")
    public Mono<ResponseEntity<Map<String, Object>>> saveMovementMobileWallet(@Valid @RequestBody Mono<MovementDto> movementDto) {
        Map<String, Object> request = new HashMap<>();
        return movementDto.flatMap(mvDto ->
                service.saveMobileWallet(mvDto).map(c -> {
                    request.put("Movimiento", c);
                    request.put("mensaje", "Movimiento de  guardado con exito");
                    request.put("timestamp", new Date());
                    return ResponseEntity.created(URI.create("/api/movements/".concat(c.getIdMovement())))
                            .contentType(MediaType.APPLICATION_JSON).body(request);
                })
        );
    }

    @PostMapping("/creditCardAndLoan")
    public Mono<ResponseEntity<Map<String, Object>>> saveMovementCreditLoan(@Valid @RequestBody Mono<MovementDto> movementDto) {
        Map<String, Object> request = new HashMap<>();
        return movementDto.flatMap(mvDto -> {
            return service.saveCreditLoan(mvDto).map(c -> {
                request.put("Movimiento", c);
                request.put("mensaje", "Movimiento de Credito guardado con exito");
                request.put("timestamp", new Date());
                return ResponseEntity.created(URI.create("/api/movements/".concat(c.getIdMovement())))
                        .contentType(MediaType.APPLICATION_JSON).body(request);
            });
        });
    }

    @PutMapping("creditCardAndLoan/{idMovement}")
    public Mono<ResponseEntity<Movement>> editMovementCreditCardAndLoan(@Valid @RequestBody MovementDto movementDto, @PathVariable("idMovement") String idMovement) {
        return service.updateCreditCardLoan(movementDto, idMovement)
                .map(c -> ResponseEntity.created(URI.create("/api/movements/".concat(idMovement)))
                        .contentType(MediaType.APPLICATION_JSON).body(c));
    }

    @PutMapping("/{idMovement}")
    public Mono<ResponseEntity<Movement>> editMovement(@Valid @RequestBody MovementDto movementDto, @PathVariable("idMovement") String idMovement) {
        return service.update(movementDto, idMovement)
                .map(c -> ResponseEntity.created(URI.create("/api/movements/".concat(idMovement)))
                        .contentType(MediaType.APPLICATION_JSON).body(c));
    }

    @DeleteMapping("/{idMovement}")
    public Mono<ResponseEntity<Void>> deleteMovement(@PathVariable("idMovement") String idMovement) {
        return service.delete(idMovement).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    }


    @GetMapping("/last/accountNumber/{accountNumber}")
    public Mono<ResponseEntity<MovementDto>> getLastMovementsByAccountNumber(@PathVariable("accountNumber") String accountNumber) {
        return service.findLastMovementsByAccountNumber(accountNumber)
                .map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(c));
    }

    @GetMapping("/accountNumber/{accountNumber}")
    public Mono<ResponseEntity<List<MovementDto>>> getMovementsByAccountNumber(@PathVariable("accountNumber") String accountNumber) {
        return service.findMovementsByAccountNumber(accountNumber).flatMap(mm -> {
                    log.info("--getMovementsByAccountNumber-------: " + mm.toString());
                    return Mono.just(mm);
                })
                .collectList()
                .map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(c));
    }

    @GetMapping("creditNumber/{creditNumber}")
    public Mono<ResponseEntity<Movement>> creditByCreditNumber(@PathVariable("creditNumber") Integer creditNumber) {
        return service.creditByCreditNumber(creditNumber).map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(c));
    }

    @GetMapping("client/loanNumber/{loanNumber}")
    public Mono<ResponseEntity<List<MovementDto>>> getMovementsByLoanNumber(@PathVariable("loanNumber") String loanNumber) {
        return service.findMovementsByLoanNumber(loanNumber).flatMap(mm -> {
                    log.info("--getMovementsByLoanNumber-------: " + mm.toString());
                    return Mono.just(mm);
                })
                .collectList()
                .map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(c));
    }

    @GetMapping("client/creditNumber/{creditNumber}")
    public Mono<ResponseEntity<List<MovementDto>>> getMovementsByCreditNumber(@PathVariable("creditNumber") Integer creditNumber) {
        return service.findMovementsByCreditNumber(creditNumber).flatMap(mm -> {
                    log.info("--findMovementsByCreditNumber-------: " + mm.toString());
                    return Mono.just(mm);
                })
                .collectList()
                .map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(c));
    }

}
