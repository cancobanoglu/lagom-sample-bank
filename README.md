lagom sample application
-------------------------

## abstraction

This is lagom sample bank account application for my study.

## API

### create bank account

`curl -X POST -H 'Content-Type: application/json' -d '{"id":"<account id>", "name":"<account name>"}' http://localhost:9000/api/account`

+ POST /api/account
+ BODY `{"id":""<account id>"", "name":"<account name>"}`

### deposit money

`curl -X PUT -H 'Content-Type: application/json' -d '{"amount":<deposit MONEY>}' http://localhost:9000/api/account/:account_id/deposit`

+ PUT /api/account/:account_id/deposit
+ BODY `{"amount":<deposit MONEY>}`

### withdraw money

`curl -X PUT -H 'Content-Type: application/json' -d '{"amount":<withdrawal money>}' http://localhost:9000/api/account/:account_id/withdrawal`

+ PUT /api/account/:account_id/withdrawal
+ BODY `{"amount":<withdrawal money>}`

### get account information

`curl -X GET http://localhost:9000/api/account/:account_id`

+ GET /api/account/:account_id


### list account transaction history

`curl -X GET http://localhost:9000/api/account/:account_id/history`

+ GET /api/account/:account_id/history

## License

[Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0)
