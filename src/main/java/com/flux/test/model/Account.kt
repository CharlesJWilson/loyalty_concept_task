package com.flux.test.model


data class Account(
        val id: AccountId,
        val stamps: Map<SchemeId, Int>,
        val allPayments: Map<SchemeId, List<Long>>
)

