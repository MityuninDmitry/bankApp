package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should add notification successfully when valid request is sent"

    request {
        method 'POST'
        url '/api/addNotification'
        headers {
            contentType(applicationJson())
        }
        body([
                login: "testUser",
                message: "Подозрительная операция. Повторите снова."
        ])
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                success: true,
                message: "Успех добавления нотификации",
                data: null
        ])
    }
}