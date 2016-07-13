package sample.bank.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 *
 */
public interface TransactionEvent extends Jsonable, AggregateEvent<TransactionEvent> {

    /**
     * For query
     */
    @Override
    default AggregateEventTag<TransactionEvent> aggregateTag() {
        return TransactionEventTag.INSTANCE;
    }

    @SuppressWarnings("serial")
    @Immutable
    @JsonDeserialize
    public final class AccountCreatedEvent implements TransactionEvent {
        public final String id;
        public final String name;

        @JsonCreator
        public AccountCreatedEvent(String id, String name) {

            this.id = Preconditions.checkNotNull(id, "id");
            this.name = Preconditions.checkNotNull(name, "name");
        }

        @Override
        public boolean equals(@Nullable Object another) {
            if (this == another)
                return true;
            return another instanceof AccountCreatedEvent && equalTo((AccountCreatedEvent) another);
        }

        private boolean equalTo(AccountCreatedEvent another) {
            return id.equals(another.id);
        }

        @Override
        public int hashCode() {
            int h = 31;
            h = h * 17 + id.hashCode();
            return h;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper("AccountCreatedEvent").add("id", id).toString();
        }
    }

    @SuppressWarnings("serial")
    @Immutable
    @JsonDeserialize
    public final class MoneyDepositedEvent implements TransactionEvent {
        public final String id;
        public final Long amount;
        public final Long balance;

        @JsonCreator
        public MoneyDepositedEvent(String id, Long amount, Long balance) {

            this.id = Preconditions.checkNotNull(id, "id");
            this.amount = Preconditions.checkNotNull(amount, "amount");
            this.balance = Preconditions.checkNotNull(balance, "balance");
        }

        @Override
        public boolean equals(@Nullable Object another) {
            if (this == another)
                return true;
            return another instanceof MoneyDepositedEvent && equalTo((MoneyDepositedEvent) another);
        }

        private boolean equalTo(MoneyDepositedEvent another) {
            return id.equals(id) && amount.equals(amount) && balance.equals(balance);
        }

        @Override
        public int hashCode() {
            int h = 31;
            h = h * 17 + id.hashCode();
            h = h * 17 + amount.hashCode();
            h = h * 17 + balance.hashCode();
            return h;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper("MoneyDepositedEvent").add("amount", amount).toString();
        }
    }


    @SuppressWarnings("serial")
    @Immutable
    @JsonDeserialize
    public final class MoneyWithdrawnEvent implements TransactionEvent {
        public final String id;
        public final Long amount;
        public final Long balance;

        @JsonCreator
        public MoneyWithdrawnEvent(String id, Long amount, Long balance) {

            this.id = Preconditions.checkNotNull(id, "id");
            this.amount = Preconditions.checkNotNull(amount, "amount");
            this.balance = Preconditions.checkNotNull(balance, "balance");
        }

        @Override
        public boolean equals(@Nullable Object another) {
            if (this == another)
                return true;
            return another instanceof MoneyWithdrawnEvent && equalTo((MoneyWithdrawnEvent) another);
        }

        private boolean equalTo(MoneyWithdrawnEvent another) {
            return id.equals(id) && amount.equals(amount) && balance.equals(balance);
        }

        @Override
        public int hashCode() {
            int h = 31;
            h = h * 17 + id.hashCode();
            h = h * 17 + amount.hashCode();
            h = h * 17 + balance.hashCode();
            return h;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper("MoneyWithdrawnEvent").add("amount", amount).toString();
        }
    }
}
