package com.flux.test.model

import java.util.UUID

data class Account(
        val id: AccountId,
        val merchantId: MerchantId,
        val purcahses: Map<String, Int>
)
//
//data class Purchase(
//        /**
//         * The unique ID of the item - only unique for one merchant and may be duplicated across merchants
//         */
//        val sku: String,
//
//        /**
//         * Quantity of this item purchased, if an item has a quantity of 2 then it can generate 2 stamps
//         */
//        val quantity: Int
//
//)
