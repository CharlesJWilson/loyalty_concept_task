package com.flux.test

import com.flux.test.model.AccountId
import com.flux.test.model.Item
import com.flux.test.model.MerchantId
import com.flux.test.model.Receipt
import com.flux.test.model.Scheme
import com.flux.test.model.SchemeId
import io.kotlintest.IsolationMode
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.UUID

class LoyaltySpec : StringSpec() {

//  TODO("Create your instance here, if you are a Java person, drop the new keyword and the ;.  e.g. `new MyImpl();` becomes `MyImpl(singleScheme)`")
    val implementation: ImplementMe = MechantLoyalty(singleScheme)

    init {
        "Applies a stamp" {
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(Item("1", 100, 1)))

            val response = implementation.apply(receipt)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 1
            response.first().currentStampCount shouldBe 1
            response.first().paymentsGiven shouldHaveSize 0
        }

        "Triggers a redemption" {
            val receipt =
                Receipt(merchantId = merchantId, accountId = accountId, items = 1.rangeTo(5).map { Item("1", 100, 1) })
            val response = implementation.apply(receipt)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 4
            response.first().currentStampCount shouldBe 0
            response.first().paymentsGiven shouldHaveSize 1
            response.first().paymentsGiven.first() shouldBe 100
        }

        "Stores the current state for an account" {
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(Item("1", 100, 1)))

            implementation.apply(receipt)
            val response = implementation.state(accountId)

            response shouldHaveSize (1)
            response.first().currentStampCount shouldBe 1
            response.first().payments shouldHaveSize 0
        }

        "Stores multiple stamps across multiple items in single scheme" {
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(
                    Item("1", 100, 2),
                    Item("2", 50, 3)))

            val implementation: ImplementMe = MechantLoyalty(multiItemSingleScheme)
            val response = implementation.apply(receipt)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 4
            response.first().currentStampCount shouldBe 0
            response.first().paymentsGiven shouldHaveSize 1
        }

        "Stores multiple stamps across multiple items in single scheme with excess stamps" {
            val receipt = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(
                    Item("1", 100, 5),
                    Item("2", 50, 3)))

            val implementation: ImplementMe = MechantLoyalty(multiItemSingleScheme)
            val response = implementation.apply(receipt)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 7
            response.first().currentStampCount shouldBe 3
            response.first().paymentsGiven shouldHaveSize 1
        }

        "Stores multiple stamps across multiple items across multiple receipts" {
            val receipt1 = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(
                    Item("1", 100, 5),
                    Item("2", 50, 3)))
            val receipt2 = Receipt(merchantId = merchantId, accountId = accountId, items = listOf(
                    Item("1", 100, 5),
                    Item("2", 50, 3)))

            val implementation: ImplementMe = MechantLoyalty(multiItemSingleScheme)
            implementation.apply(receipt1)
            val response = implementation.apply(receipt2)

            response shouldHaveSize (1)
            response.first().stampsGiven shouldBe 6
            response.first().currentStampCount shouldBe 1
            response.first().paymentsGiven shouldHaveSize 2
            response[0].paymentsGiven[0] shouldBe 50
            response[0].paymentsGiven[1] shouldBe 50
        }

    }

    override fun isolationMode() = IsolationMode.InstancePerTest

    companion object {
        private val accountId: AccountId = UUID.randomUUID()
        private val merchantId: MerchantId = UUID.randomUUID()

        private val singleItemSingleSchemeId: SchemeId = UUID.randomUUID()
        private val multiItemSingleSchemeId: SchemeId = UUID.randomUUID()
        private val singleScheme = listOf(Scheme(singleItemSingleSchemeId, merchantId, 4, listOf("1")))
        private val multiItemSingleScheme = listOf(Scheme(multiItemSingleSchemeId, merchantId, 4, listOf("1", "2")))

    }

}