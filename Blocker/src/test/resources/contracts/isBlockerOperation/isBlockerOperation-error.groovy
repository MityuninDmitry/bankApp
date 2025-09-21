package contracts.isBlockerOperation

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return error for isBlockerOperation when suspicious"
    request {
        method POST()
        url "/api/isBlockerOperation"
        headers {
            contentType applicationJson()
        }
        body(15000.0)
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

