package sample.bank.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.ExceptionMessage;
import com.lightbend.lagom.javadsl.api.transport.TransportErrorCode;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.bank.api.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class BankServiceImpl implements BankService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final CassandraSession db;

    @Inject
    public BankServiceImpl(PersistentEntityRegistry persistentEntityRegistry, CassandraReadSide readSide,
                           CassandraSession db) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(AccountPersistentEntity.class);
        // CassandraSession is Query Side API
        this.db = db;
        // CassandraReadSide is Data sharding API. When Event ocurre, Event Processor Insert or update tables to Query side.
        readSide.register(TransactionReadSideProcessor.class);
    }

    private <Response> Response handleException(Throwable e) {
        if (e.getCause() != null && e.getCause() instanceof PersistentEntity.InvalidCommandException) {
            throw new TransportException(TransportErrorCode.BadRequest,
                                         new ExceptionMessage("BadRequest", e.getCause().getMessage()));
        } else {
            // TODO : WIP
            throw new Error(e);
        }
    }

    /**
     * wrap invalid command to BadRequest
     */
    private <Request, Response> ServerServiceCall<Request, Response> validate(
            ServerServiceCall<Request, Response> serviceCall) {
        return HeaderServiceCall.compose(requestHeader ->
                                                 (req) -> serviceCall.invoke(req)
                                                                     .exceptionally(this::handleException)
        );
    }

    @Override
    public ServiceCall<Account, NotUsed> createAccount() {

        return validate((request) -> {

            // Look up the  account entity for the given ID.
            PersistentEntityRef<TransactionCommand> ref = persistentEntityRegistry.refFor(AccountPersistentEntity.class, request.id);

            // Ask the entity the CreateAccount command.
            return ref.ask(new TransactionCommand.CreateAccountCommand(request.id, request.name))
                      .thenApply(ack -> NotUsed.getInstance());
        });
    }

    @Override
    public ServiceCall<Money, NotUsed> deposit(String id) {
        return validate((request) -> {
            PersistentEntityRef<TransactionCommand> ref = persistentEntityRegistry.refFor(AccountPersistentEntity.class, id);

            return ref.ask(new TransactionCommand.DepositCommand(request.amount))
                      .thenApply(ack -> NotUsed.getInstance());
        });
    }

    @Override
    public ServiceCall<Money, NotUsed> withdrawal(String id) {
        return validate((request) -> {
            PersistentEntityRef<TransactionCommand> ref = persistentEntityRegistry.refFor(AccountPersistentEntity.class, id);
            return ref.ask(new TransactionCommand.WithdrawalCommand(request.amount))
                      .thenApply(ack -> NotUsed.getInstance());
        });
    }

    @Override
    public ServiceCall<Transfer, NotUsed> transfer(String id) {
        // TODO
        return null;
    }

    @Override
    public ServiceCall<NotUsed, Optional<Account>> getAccount(String id) {
        return (req) -> {
            CompletionStage<Optional<Account>> result = db.selectOne("SELECT * FROM account WHERE account_id = ?", id)
                                                          .thenApply(row ->
                                                                             row.map(r -> new Account(r.getString("account_id"), r.getString("name"), r.getLong("balance"), Optional.empty()))
                                                          );
            return result;
        };
    }

    @Override
    public ServiceCall<NotUsed, PSequence<MoneyTransaction>> getHistory(String id) {
        return (req) -> {
            CompletionStage<PSequence<MoneyTransaction>> result
                    = db.selectAll("SELECT * FROM transaction_history WHERE account_id = ? order by at DESC", id)
                        .thenApply(rows -> {
                            List<MoneyTransaction> list = rows.stream().map(r -> new MoneyTransaction(r.getString("type"), r.getLong("amount"),
                                                                                                      toLocalDateTime(r.getTimestamp("at")))).collect(Collectors.toList());
                            return TreePVector.from(list);
                        });

            return result;
        };
    }

    private LocalDateTime toLocalDateTime(Date ts) {
        return LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault());
    }
}
