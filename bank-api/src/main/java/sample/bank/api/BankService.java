package sample.bank.api;

import akka.NotUsed;

import org.pcollections.PSequence;

import java.util.Optional;


import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.namedCall;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

/**
 */
public interface BankService extends Service {

    // command
    ServiceCall<Account, NotUsed> createAccount();
    ServiceCall<Money, NotUsed> deposit(String id);
    ServiceCall<Money, NotUsed> withdrawal(String id);
    ServiceCall<Transfer, NotUsed> transfer(String id);

    // query
    ServiceCall<NotUsed, Optional<Account>> getAccount(String id);
    ServiceCall<NotUsed, PSequence<MoneyTransaction>> getHistory(String id);


    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("bankService").withCalls(
                restCall(Method.POST, "/api/account", this::createAccount),
                restCall(Method.GET, "/api/account/:id", this::getAccount),
                restCall(Method.GET, "/api/account/:id/history", this::getHistory),
                restCall(Method.PUT, "/api/account/:id/deposit", this::deposit),
                restCall(Method.PUT, "/api/account/:id/withdrawal", this::withdrawal),
                restCall(Method.PUT, "/api/account/:id/transfer", this::transfer)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
