package com.nttdata.bootcamp.msmovement.application;

import com.nttdata.bootcamp.msmovement.dto.MovementDto;
import com.nttdata.bootcamp.msmovement.exception.ResourceNotFoundException;
import com.nttdata.bootcamp.msmovement.infrastructure.BankAccountRepository;
import com.nttdata.bootcamp.msmovement.infrastructure.CreditRepository;
import com.nttdata.bootcamp.msmovement.infrastructure.MobileWalletRepository;
import com.nttdata.bootcamp.msmovement.infrastructure.MovementRepository;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
import com.nttdata.bootcamp.msmovement.model.MobileWallet;
import com.nttdata.bootcamp.msmovement.model.Movement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovementServiceImpl implements MovementService {

    @Autowired
    private MovementRepository movementRepository;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private MobileWalletRepository mobileWalletRepository;

    @Override
    public Flux<Movement> findAll() {
        return movementRepository.findAll();
    }

    @Override
    public Mono<Movement> findById(String idMovementCredit) {
        return Mono.just(idMovementCredit)
                .flatMap(movementRepository::findById)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovementCredit)));
    }

    @Override
    public Mono<Movement> save(MovementDto movementDto) {
        log.info("ini save------movementDto: " + movementDto.toString());
        return movementDto.validateMovementType()
                .flatMap(a -> {
                    log.info("sg validateMovementType-------a: " + a.toString());
                    if (movementDto.getDebitCardNumber() != null) {
                        return bankAccountRepository.findBankAccountByDebitCardNumber(movementDto.getDebitCardNumber())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tarjeta de débito", "DebitCardNumber", movementDto.getDebitCardNumber())));
                    } else {
                        return bankAccountRepository.findBankAccountByAccountNumber(movementDto.getAccountNumber())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Número de cuenta", "AccountNumber", movementDto.getAccountNumber())));
                    }
                })
                .flatMap(account -> {
                    log.info("ant validateMovementLimit-------account: " + account.toString());
                    return validateMovementLimit(account)
                            .flatMap(o -> {
                                log.info("sg validateMovementLimit-------0: " + o.toString());
                                if (o.equals(true)) {
                                    log.info("sg validateMovementLimit-------account.getCommissionTransaction(): " + account.getCommissionTransaction().toString());
                                    log.info("sg validateMovementLimit-------movementDto.getAmount(): " + movementDto.getAmount().toString());
                                    movementDto.setCommission(((account.getCommissionTransaction() / 100) * movementDto.getAmount()));
                                }
                                return Mono.just(account);
                            });
                })
                .flatMap(account ->
                        validateAvailableAmount(account, null, movementDto, "save", false)
                                .flatMap(a -> movementDto.MapperToMovement(null))
                                .flatMap(mvt -> validateTransfer(movementDto)
                                        .flatMap(ac -> {
                                            log.info("sg validateTransfer-------: ");
                                            log.info("sg validateTransfer-------mvt: " + mvt.toString());
                                            if (ac.getIdBankAccount() != null) {
                                                log.info("sg validateTransfer-------ac: " + ac.toString());
                                                mvt.setBankAccountTransfer(ac);
                                            }
                                            return Mono.just(mvt);
                                        }))
                                .flatMap(mvt -> movementRepository.save(mvt))
                                .flatMap(mvt -> bankAccountRepository.updateBalanceBankAccount(account.getIdBankAccount(), mvt.getBalance())
                                        .then(Mono.just(mvt)))
                );

    }

    @Override
    public Mono<Movement> saveMobileWallet(MovementDto movementDto) {
        log.info("ini save------movementDto: " + movementDto.toString());
        return movementDto.validateMovementType()
                .flatMap(a -> {
                    log.info("sg validateMovementType-------a: " + a.toString());
                    return mobileWalletRepository.findMobileWalletByAccountNumber(movementDto.getCellphone())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Monedero móvil", "Cellphone", movementDto.getCellphone())));
                })
                .flatMap(mw -> {
                    if (mw.getAccount() != null) {
                        movementDto.setAccountNumber(mw.getAccount().getAccountNumber());
                        log.info("ant validateMovementLimit-------account: " + mw.getAccount().toString());
                        return validateMovementLimit(mw.getAccount())
                                .flatMap(o -> {
                                    log.info("sg validateMovementLimit-------0: " + o.toString());
                                    if (o.equals(true)) {
                                        log.info("sg validateMovementLimit-------account.getCommissionTransaction(): " + mw.getAccount().getCommissionTransaction().toString());
                                        log.info("sg validateMovementLimit-------movementDto.getAmount(): " + movementDto.getAmount().toString());
                                        movementDto.setCommission(((mw.getAccount().getCommissionTransaction() / 100) * movementDto.getAmount()));
                                    }
                                    return Mono.just(mw);
                                });
                    } else {
                        return Mono.just(mw);
                    }
                })
                .flatMap(mw ->
                        validateAvailableAmount(null, mw, movementDto, "save", true)
                                .flatMap(a -> movementDto.MapperToMovement(null))
                                .flatMap(mvt -> movementRepository.save(mvt))
                                .flatMap(mvt -> {
                                    log.info("sg movementRepository.save-------mvt.toString(): " + mvt.toString());
                                    log.info("sg movementRepository.save-------mw.toString(): " + mw.toString());
                                    if (mw.getAccount() != null) {
                                        log.info("sg if ");
                                        return bankAccountRepository.updateBalanceBankAccount(mw.getAccount().getIdBankAccount(), mvt.getBalance())
                                                .then(Mono.just(mvt));
                                    } else {
                                        log.info("sg movementRepository.save-------mvt.toString(): " + mvt.toString());
                                        log.info("sg movementRepository.save-------mvt.getBalance().toString(): " + mvt.getBalance().toString());
                                        return mobileWalletRepository.updateBalanceMobilWallet(mw.getIdMobileWallet(), mvt.getBalance())
                                                .then(Mono.just(mvt));
                                    }
                                })
                );

    }


    public Mono<BankAccount> validateTransfer(MovementDto movementDto) {
        log.info("ini validateTransfer-------0: " + movementDto.toString());
        if (movementDto.getMovementType().equals("output-transfer")) { // transferencia de salida.
            log.info("1 validateTransfer-------output-transfer: ");
            return bankAccountRepository.findBankAccountByAccountNumber(movementDto.getAccountNumberForTransfer())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Número de cuenta para transferencia", "AccountNumberForTransfer", movementDto.getAccountNumberForTransfer())))
                    .flatMap(ac -> {
                        MovementDto mvDTO = MovementDto.builder()
                                .accountNumber(movementDto.getAccountNumberForTransfer())
                                .movementType("input-transfer")
                                .amount(movementDto.getAmount())
                                .currency(movementDto.getCurrency())
                                .accountNumberForTransfer(movementDto.getAccountNumber())
                                .build();
                        return save(mvDTO).then(Mono.just(ac));
                    });
        } else if (movementDto.getMovementType().equals("input-transfer")) {
            log.info("2 validateTransfer-------input-transfer: ");
            return bankAccountRepository.findBankAccountByAccountNumber(movementDto.getAccountNumberForTransfer())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Número de cuenta para transferencia", "AccountNumberForTransfer", movementDto.getAccountNumberForTransfer())));
        } else {
            log.info("3 validateTransfer------- : ");
            return Mono.just(new BankAccount());
        }
    }

    public Mono<Boolean> validateMovementLimit(BankAccount bankAccount) {
        log.info("ini validateMovementLimit-------: ");
        log.info("ini validateMovementLimit-------bankAccount: " + bankAccount.toString());


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        LocalDateTime dateNow = LocalDateTime.now();
        String dayValue = "" + dateNow.getDayOfMonth();
        String monthValue = (dateNow.getMonthValue() < 10 ? "0" : "") + dateNow.getMonthValue();

        String endDate = dateNow.getYear() + "-" + monthValue + "-" + calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + "T23:59:59.999Z";
        String startDate = dateNow.getYear() + "-" + monthValue + "-0" + 1 + "T00:00:00.000Z";

        log.info("ini validateMovementLimit-------bankAccount.getAccountNumber(): " + bankAccount.getAccountNumber());
        log.info("ini validateMovementLimit-------startDate: " + startDate);
        log.info("ini validateMovementLimit-------endDate: " + endDate);

        if (bankAccount.getAccountType().equals("FixedTerm-account")) {
            String movementDate = bankAccount.getMovementDate().toString();
            log.info("ini validateMovementLimit-------movementDate: " + movementDate);
            if (!dayValue.equals(movementDate)) {
                return Mono.error(new ResourceNotFoundException("Fecha movimientos", "movementDate", movementDate));
            }
        }

        log.info("------findMovementsByDateRange: ");

        return movementRepository.findMovementsByDateRange(startDate, endDate, bankAccount.getAccountNumber())
                .count()
                .flatMap(cnt -> {
                    log.info("ini validateMovementLimit-------cantidad: " + cnt);
                    log.info("ini validateMovementLimit-------cantidad: " + (bankAccount.getMaximumMovement() != null ? bankAccount.getMaximumMovement() : ""));

                    if (bankAccount.getMaximumMovement() != null) {
                        if (cnt >= bankAccount.getMaximumMovement()) {
                            return Mono.error(new ResourceNotFoundException("Máximo movimientos", "MaximumMovement", bankAccount.getMaximumMovement().toString()));
                        } else {
                            return validateTransactionLimit(bankAccount, cnt);
                        }
                    } else {
                        return validateTransactionLimit(bankAccount, cnt);
                    }
                });
    }

    public Mono<Boolean> validateTransactionLimit(BankAccount bankAccount, Long count) {
        log.info("ini validateTransactionLimit-------count: " + count);
        log.info("ini2 validateTransactionLimit-------bankAccount: " + bankAccount.toString());
        if (bankAccount.getTransactionLimit() != null) {
            if (count >= bankAccount.getTransactionLimit()) {
                return Mono.just(true);
            } else {
                return Mono.just(false);
            }
        } else {
            return Mono.just(false);
        }
    }

    @Override
    public Mono<Movement> saveCreditLoan(MovementDto movementDto) {
        return creditRepository.findCreditByCreditNumber(String.valueOf(movementDto.getCreditNumber()))
                .flatMap(credit -> movementDto.validateMovementTypeCreditLoan()
                        .flatMap(a -> movementDto.MapperToMovement(credit))
                        .flatMap(mvt -> movementRepository.save(mvt))
                );
    }

    public Mono<Boolean> validateAvailableAmount(BankAccount bankAccount, MobileWallet mobileWallet, MovementDto movementDto, String method, Boolean mainAccountOnly) {
        log.info("ini validateAvailableAmount-------: ");

        if (method.equals("save")) {
            return movementRepository.findLastMovementByAccount(movementDto.getAccountNumber())
                    .switchIfEmpty(Mono.defer(() -> {
                        log.info("----1 switchIfEmpty-------: ");
                        return Mono.just(movementDto);
                    }))
                    .flatMap(mvn -> movementDto.validateAvailableAmount(bankAccount, mobileWallet, mvn, mainAccountOnly))
                    .flatMap(rvt -> {
                        if (rvt > 0) {
                            if (mainAccountOnly.equals(false)) {
                                return bankAccountRepository.findBankAccountByDocumentNumberAndWithdrawalAmount(
                                                bankAccount.getClient().getDocumentNumber(), bankAccount.getDebitCard().getCardNumber(), rvt
                                        )
                                        .collectList()
                                        .flatMap(accs -> {
                                            Double totalBalance = accs.stream()
                                                    .map(mp -> mp.getBalance() != null ? mp.getBalance() : 0)
                                                    .reduce((accumulator, number) -> accumulator + number)
                                                    .get();
                                            if (totalBalance > rvt) {
                                                return Mono.just(accs);
                                            } else {
                                                return Mono.error(new ResourceNotFoundException("No tiene saldo disponible", "balance", ""));
                                            }
                                        })
                                        .flatMap(accs -> {
                                            AtomicReference<Double> missingOutflowAmount = new AtomicReference<>(rvt);
                                            List<BankAccount> bc = accs.stream()
                                                    .map(mp -> {
                                                        Double iniMissingOutflowAmount = missingOutflowAmount.get();
                                                        missingOutflowAmount.set(missingOutflowAmount.get() - mp.getBalance());
                                                        Double setBalance = missingOutflowAmount.get() < 0 ? iniMissingOutflowAmount : mp.getBalance();
                                                        mp.setBalance(setBalance);
                                                        return mp;
                                                    })
                                                    .collect(Collectors.toList());
                                            return Mono.just(bc);
                                        })
                                        .flatMapMany(Flux::fromIterable)
                                        .flatMap(acc -> {
                                            MovementDto movementDtoBalance = MovementDto.builder()
                                                    .accountNumber(acc.getAccountNumber())
                                                    .movementType(movementDto.getMovementType())
                                                    .amount(acc.getBalance())
                                                    .currency(movementDto.getCurrency())
                                                    .build();
                                            movementDtoBalance.setBalance(rvt);
                                            return save(movementDtoBalance).then(Mono.just(true));
                                        })
                                        .collectList()
                                        .then(Mono.just(true));

                            } else {
                                return Mono.error(new ResourceNotFoundException("Monto", "Amount", movementDto.getAmount().toString()));
                            }
                        } else {
                            return Mono.just(true);
                        }
                    });
        } else {
            log.info("ini validateAvailableAmount-------movementDto.getAccountNumber(): " + movementDto.getAccountNumber());
            log.info("ini validateAvailableAmount-------movementDto.getIdMovement(): " + movementDto.getIdMovement());
            return movementRepository.findLastMovementByAccountExceptCurrentId(movementDto.getAccountNumber(), movementDto.getIdMovement())
                    .switchIfEmpty(Mono.defer(() -> {
                        log.info("----2 switchIfEmpty-------: ");
                        return Mono.just(movementDto);
                    }))
                    .flatMap(mvn -> movementDto.validateAvailableAmount(bankAccount, mobileWallet, mvn, mainAccountOnly))
                    .flatMap(rvt -> {
                        if (rvt > 0) {

                            if (mainAccountOnly.equals(false)) {
                                return bankAccountRepository.findBankAccountByDocumentNumberAndWithdrawalAmount(
                                                bankAccount.getClient().getDocumentNumber(), bankAccount.getDebitCard().getCardNumber(), rvt
                                        )
                                        .collectList()
                                        .flatMap(accs -> {
                                            Double totalBalance = accs.stream()
                                                    .map(mp -> mp.getBalance() != null ? mp.getBalance() : 0)
                                                    .reduce((accumulator, number) -> accumulator + number)
                                                    .get();
                                            if (totalBalance > rvt) {
                                                return Mono.just(accs);
                                            } else {
                                                return Mono.error(new ResourceNotFoundException("No tiene saldo disponible", "balance", ""));
                                            }
                                        })
                                        .flatMap(accs -> {
                                            AtomicReference<Double> missingOutflowAmount = new AtomicReference<>(rvt);
                                            List<BankAccount> bc = accs.stream()
                                                    .map(mp -> {
                                                        Double iniMissingOutflowAmount = missingOutflowAmount.get();
                                                        missingOutflowAmount.set(missingOutflowAmount.get() - mp.getBalance());
                                                        Double setBalance = missingOutflowAmount.get() < 0 ? iniMissingOutflowAmount : mp.getBalance();
                                                        mp.setBalance(setBalance);
                                                        return mp;
                                                    })
                                                    .collect(Collectors.toList());
                                            return Mono.just(bc);
                                        })
                                        .flatMapMany(Flux::fromIterable)
                                        .flatMap(acc -> {
                                            MovementDto movementDtoBalance = MovementDto.builder()
                                                    .accountNumber(acc.getAccountNumber())
                                                    .movementType(movementDto.getMovementType())
                                                    .amount(acc.getBalance())
                                                    .currency(movementDto.getCurrency())
                                                    .build();
                                            movementDtoBalance.setBalance(rvt);
                                            return save(movementDtoBalance).then(Mono.just(true));
                                        })
                                        .collectList()
                                        .then(Mono.just(true));
                            } else {
                                return Mono.error(new ResourceNotFoundException("Monto", "Amount", movementDto.getAmount().toString()));
                            }
                        } else {
                            return Mono.just(true);
                        }
                    });
        }
    }

    @Override
    public Mono<Movement> update(MovementDto movementDto, String idMovement) {

        return movementDto.validateMovementType()
                .flatMap(at -> bankAccountRepository.findBankAccountByAccountNumber(movementDto.getAccountNumber()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cuenta", "AccountNumber", movementDto.getAccountNumber())))
                .flatMap(account -> validateAvailableAmount(account, null, movementDto, "update", false))
                .flatMap(a -> movementRepository.findById(idMovement)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                        .flatMap(c -> {
                            //c.setNumberMovement(movementDto.getNumberMovement());
                            c.setAccountNumber(movementDto.getAccountNumber());
                            c.setMovementType(movementDto.getMovementType());
                            c.setAmount(movementDto.getAmount());
                            c.setBalance(movementDto.getBalance());
                            c.setCurrency(movementDto.getCurrency());
                            c.setMovementDate(LocalDateTime.now());
                            //c.setIdCredit(movementDto.getIdCredit());
                            //c.setIdBankAccount(movementDto.getIdBankAccount());
                            //c.setIdLoan(movementDto.getIdLoan());
                            return movementRepository.save(c);
                        })
                );
    }

    @Override
    public Mono<Movement> updateCreditCardLoan(MovementDto movementDto, String idMovement) {
        return creditRepository.findCreditByCreditNumber(String.valueOf(movementDto.getCreditNumber()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Crédito", "CreditNumber", movementDto.getCreditNumber().toString())))
                .flatMap(credit -> {
                    return movementDto.validateMovementTypeCreditLoan()
                            .flatMap(a -> movementRepository.findById(idMovement)
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                                    .flatMap(c -> {
                                        c.setCredit(credit);
                                        c.setAccountNumber(movementDto.getAccountNumber());
                                        c.setMovementType(movementDto.getMovementType());
                                        c.setAmount(movementDto.getAmount());
                                        c.setBalance(movementDto.getBalance());
                                        c.setCurrency(movementDto.getCurrency());
                                        c.setMovementDate(LocalDateTime.now());
                                        return movementRepository.save(c);
                                    })
                            );
                });
    }

    @Override
    public Mono<Void> delete(String idMovement) {
        return movementRepository.findById(idMovement)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movement", "IdMovement", idMovement)))
                .flatMap(movementRepository::delete);
    }

    @Override
    public Mono<MovementDto> findLastMovementsByAccountNumber(String accountNumber) {
        log.info("Inicio findLastMovementsByAccountNumber-------accountNumber: " + accountNumber);
        return Mono.just(accountNumber)
                .flatMap(movementRepository::findLastMovementByAccount);
    }

    @Override
    public Flux<MovementDto> findMovementsByAccountNumber(String accountNumber) {
        log.info("Inicio findMovementsByAccountNumber-------accountNumber: " + accountNumber);
        return movementRepository.findMovementsByAccount(accountNumber);
    }

    @Override
    public Mono<Movement> creditByCreditNumber(Integer creditNumber) {
        return Mono.just(creditNumber)
                .flatMap(movementRepository::findByCreditNumber)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Movimientos", "creditNumber", creditNumber.toString())));
    }

    @Override
    public Flux<MovementDto> findMovementsByLoanNumber(String loanNumber) {
        log.info("Inicio findMovementsByLoanNumber-------loanNumber: " + loanNumber);
        return movementRepository.findMovementsByLoanNumber(loanNumber);
    }

    @Override
    public Flux<MovementDto> findMovementsByCreditNumber(Integer creditNumber) {
        log.info("Inicio findMovementsByCreditNumber-------creditNumber: " + creditNumber);
        return movementRepository.findMovementsByCreditNumber(String.valueOf(creditNumber));
    }

}
