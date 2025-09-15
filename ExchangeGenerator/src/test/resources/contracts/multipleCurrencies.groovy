package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return multiple currencies"

    request {
        method GET()
        url "/api/currencies"
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                success: true,
                message: "Актуальные курсы валют",
                data: [
                        [
                                localDateTime: $(anyIso8601WithOffset()),
                                currencyFrom: $(anyOf("RUB", "USD", "CNY")),
                                currencyTo: $(anyOf("RUB", "USD", "CNY")),
                                value: $(anyPositiveInt())
                        ],
                        [
                                localDateTime: $(anyIso8601WithOffset()),
                                currencyFrom: $(anyOf("RUB", "USD", "CNY")),
                                currencyTo: $(anyOf("RUB", "USD", "CNY")),
                                value: $(anyPositiveInt())
                        ],
                        [
                                localDateTime: $(anyIso8601WithOffset()),
                                currencyFrom: $(anyOf("RUB", "USD", "CNY")),
                                currencyTo: $(anyOf("RUB", "USD", "CNY")),
                                value: $(anyPositiveInt())
                        ]
                ]
        )
        bodyMatchers {
            jsonPath('$.success', byType())
            jsonPath('$.message', byType())
            jsonPath('$.data', byType {
                // Проверяем что data является массивом с минимум 3 элементами
                minOccurrence(3)
            })
            jsonPath('$.data[*].localDateTime', byRegex(".+"))
            jsonPath('$.data[*].currencyFrom', byRegex("RUB|USD|CNY"))
            jsonPath('$.data[*].currencyTo', byRegex("RUB|USD|CNY"))
            jsonPath('$.data[*].value', byType())
        }
    }
}