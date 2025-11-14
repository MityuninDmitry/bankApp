package contracts.processOperation

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully process WITHDRAWN operation when enough balance"
    request {
        method POST()
        url "/api/processOperation"
        headers {
            contentType applicationJson()
        }
        body([
                login: "testUser",
                accountNumber: "ACC_SUCCESS",  // ← счет с достаточным балансом
                action: "WITHDRAWN",
                money: 5000.00,
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