package contracts.checkOperation


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should successfully process operation when not suspicious"
    request {
        method POST()
        url "/api/checkOperation"
        headers {
            contentType applicationJson()
        }
        body([
                login:  $(anyNonBlankString()),
                action: 'WITHDRAWN',
                money: 1000.0,
                accountNumber:  $(anyNonBlankString())
        ])
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
                success: true,
                message: "Все отлично. Работаем.",
                data: null
        ])
    }
}

