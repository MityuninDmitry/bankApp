package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return error when notification service fails"

    request {
        method 'POST'
        url '/api/addNotification'
        headers {
            contentType(applicationJson())
        }
        body([
                login: "testUser",
                message: ""
        ])
    }

    response {
        status 500
        headers {
            contentType(applicationJson())
        }
        body([
                success: false,
                message: anyNonBlankString(),
                data: null
        ])
    }
}