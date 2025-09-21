package contracts.processOperation

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return error for WITHDRAWN operation when insufficient funds"
    request {
        method POST()
        url "/api/processOperation"
        headers {
            contentType applicationJson()
        }
        body([
                login: "testUser",
                accountNumber: "ACC_INSUFFICIENT",  // ← счет с малым балансом
                action: "WITHDRAWN",
                money: 5000.00,  // ← пытаемся снять больше чем есть
        ])
    }
    response {
        status BAD_REQUEST()  // ← Обратите внимание: в контроллере при ошибке возвращается BAD_REQUEST
        headers {
            contentType applicationJson()
        }
        body([
                success: false,
                message: "Не достаточно денег. На балансе: 1000.00, списывается: 5000.00",  // ← Сообщение может немного отличаться
                data: null
        ])
    }
}