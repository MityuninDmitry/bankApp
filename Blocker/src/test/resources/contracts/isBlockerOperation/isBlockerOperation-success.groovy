package contracts.isBlockerOperation

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return success for isBlockerOperation"
    request {
        method POST()
        url "/api/isBlockerOperation"
        headers {
            contentType applicationJson()
        }
        body(5000.0)
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

