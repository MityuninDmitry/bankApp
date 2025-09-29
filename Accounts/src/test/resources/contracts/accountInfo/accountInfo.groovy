package contracts.accountInfo

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return account info when account exists and not deleted"
    request {
        method 'POST'
        url '/api/accountInfo'
        body("ACC123456")
        headers {
            contentType('application/json')
        }
    }
    response {
        status 200
        body([
                success: true,
                message: "There is account number info",
                data: [
                        accountNumber: "ACC123456",
                        currency: "RUB",
                        balance: 10000.00,
                        isDeleted: false
                ]
        ])
        headers {
            contentType('application/json')
        }
    }
}