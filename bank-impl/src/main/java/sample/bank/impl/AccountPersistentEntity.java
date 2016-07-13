package sample.bank.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import akka.Done;
import sample.bank.api.Account;
import sample.bank.api.MoneyTransaction;

/**
 */
public class AccountPersistentEntity extends PersistentEntity<TransactionCommand, TransactionEvent, AccountState> {

  public static enum TransactonType{DEPOSIT, WITHDRAWAL};

  /**
   * An entity can define different behaviours for different states, but it will
   * always start with an initial behaviour. This entity only has one behaviour.
   */
  @Override
  public Behavior initialBehavior(Optional<AccountState> snapshotState) {

    BehaviorBuilder b = newBehaviorBuilder(
        snapshotState.orElse(new AccountState(Optional.empty())));

    // acount creation
    b.setCommandHandler(TransactionCommand.CreateAccountCommand.class, (cmd, ctx) -> {
      if (state().account.isPresent()) {
        // already created;
        ctx.invalidCommand("Account " + cmd.id + " is already created");
        return ctx.done();
      } else {
        return ctx.thenPersist(new TransactionEvent.AccountCreatedEvent(cmd.id, cmd.name),
                evt -> ctx.reply(Done.getInstance()));
      }

    });


    b.setEventHandler(TransactionEvent.AccountCreatedEvent.class,
        evt -> new AccountState(Optional.of(new Account(evt.id, evt.name))));

    // Money deposit
    b.setCommandHandler(TransactionCommand.DepositCommand.class, (cmd, ctx) -> {
      if (!state().account.isPresent()) {
        ctx.invalidCommand("Account  does not exists.");
        return ctx.done();
      } else if (cmd.amount <= 0) {

        ctx.invalidCommand("Deposit amount is invalid.");
        return ctx.done();
      }
      Account current = state().account.get();
      return ctx.thenPersist(new TransactionEvent.MoneyDepositedEvent(current.id, cmd.amount, current.balance),
              evt -> ctx.reply(Done.getInstance()));

    });

    b.setEventHandler(TransactionEvent.MoneyDepositedEvent.class,
            evt -> state().addTransaction(
                    new MoneyTransaction(TransactonType.DEPOSIT.toString(), evt.amount, LocalDateTime.now())));

    // Money withdrawal
    b.setCommandHandler(TransactionCommand.WithdrawalCommand.class, (cmd, ctx) -> {
      if (!state().account.isPresent()) {
        ctx.invalidCommand("Account  does not exists.");
        return ctx.done();
      } else if (cmd.amount <= 0) {
        ctx.invalidCommand("Withdrawal amount is invalid.");
        return ctx.done();
      } else if (state().account.get().balance < cmd.amount) {
        ctx.invalidCommand("This account does not have enough balance");
        return ctx.done();
      }
      Account current = state().account.get();
      return ctx.thenPersist(new TransactionEvent.MoneyWithdrawnEvent(current.id, cmd.amount, current.balance),
              evt -> ctx.reply(Done.getInstance()));

    });

    b.setEventHandler(TransactionEvent.MoneyWithdrawnEvent.class,
            evt -> state().addTransaction(
                    new MoneyTransaction(TransactonType.WITHDRAWAL.toString(), evt.amount, LocalDateTime.now())));


    return b.build();
  }

}
