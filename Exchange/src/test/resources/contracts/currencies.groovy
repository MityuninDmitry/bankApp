package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("External currency service should return exchange rates")
    request {
        method GET()
        url "/api/currencies"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
                success: true,
                message: "Актуальные курсы валют",
                data: [
                        [
                                localDateTime: "2024-01-15T10:30:00",
                                currencyFrom: "RUB",
                                currencyTo: "CNY",
                                value: 67
                        ],
                        [
                                localDateTime: "2024-01-15T10:30:00",
                                currencyFrom: "RUB",
                                currencyTo: "USD",
                                value: 70
                        ],
                        [
                                localDateTime: "2024-01-15T10:30:00",
                                currencyFrom: "CNY",
                                currencyTo: "RUB",
                                value: 62
                        ],
                        [
                                localDateTime: "2024-01-15T10:30:00",
                                currencyFrom: "USD",
                                currencyTo: "RUB",
                                value: 100
                        ],
                        [
                                localDateTime: "2024-01-15T10:30:00",
                                currencyFrom: "RUB",
                                currencyTo: "RUB",
                                value: 1
                        ]
                ]
        ])
    }
}