package contracts.checkOperation

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return error when operation is suspicious"
    request {
        method POST()
        url "/api/checkOperation"
        headers {
            contentType applicationJson()
        }
        body([
                login:  $(anyNonBlankString()),
                action: "WITHDRAWN",
                money: 50000.0,
                accountNumber:  $(anyNonBlankString())
        ])
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
                success: false,
                message: "Подозрительная операция. Отклонено!",
                data: null
        ])
    }
}

