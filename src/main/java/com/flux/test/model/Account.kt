package com.flux.test.model


data class Account(
        val id: AccountId,
        val merchantId: MerchantId,
        val stamps: Map<SchemeId, Int>
)

