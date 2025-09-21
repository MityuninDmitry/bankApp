package contracts.processOperation

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully process DEPOSIT operation"
    request {
        method POST()
        url "/api/processOperation"
        headers {
            contentType applicationJson()
        }
        body([
                login:  "testUser",
                accountNumber:  "ACC789012",
                action: "DEPOSIT",
                money: 1000.00
        ])
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
                success: true,
                message: "Успех",
                data: null
        ])
    }
}