package com.flux.test;

import com.flux.test.model.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MechantLoyalty implements ImplementMe {

    /**
     * List of all schemes run by merchant, TODO: replace with datastore
     */
    private List<Scheme> schemes;
    /**
     * List of all accounts for the merchant, TODO: replace with datastore
     */
    private List<Account> accounts;

    /**
     * The primary constructor called once at runtime to set the
     * available schemes for the merchant.
     * @param schemes the list of Scheme objects to set all schemes available for the merchant
     */
    public MechantLoyalty(final List<Scheme> schemes) {
        this.schemes = Objects.requireNonNull(schemes);
    }

    @Override
    public void setSchemes(final List<Scheme> schemes) {
        this.schemes = Objects.requireNonNull(schemes);
    }

    @Override
    public List<Scheme> getSchemes() {
        return schemes;
    }

    /**
     * Apply the receipt to all active schemes for the merchant, response should include one `ApplyResponse`
     * instance for each scheme belonging to the merchant - even if no stamps or payments where awarded for that scheme
     */
    @Override
    public List<ApplyResponse> apply(final Receipt receipt) {
        List<ApplyResponse> Responses;
        UUID accountId = receipt.getId();
        List<Item> items = receipt.getItems();

        Account customerAccount = getAccountFromReceipt(receipt);
        Map<String, Integer> customerPurchaes = customerAccount.getPurcahses();
        for (Scheme scheme: schemes) {

//            ApplyResponse response = new ApplyResponse(scheme.getId(),customerAccount.getPurcahses().getOrDefault() )
            ApplyResponse response;
            for (Item item: items) {
                if (scheme.getSkus().contains(item.getSku())) {
                    int purchases = item.getQuantity() + customerPurchaes.getOrDefault(item.getSku(), 0);
                    if (purchases > scheme.getSkus().get(item.getSku()).)
                }
            }
//            for (String sku: scheme.getSkus()) {
//                if (receipt.getItems().contains(sku)) {
//                    int purchaseNum = customerAccount.getPurcahses().getOrDefault(sku, 0);
//                    customerAccount.getPurcahses().put(sku, purchaseNum++);
//                    customerAccount.
//                }
//            }
        }

        return null;
    }

    @Override
    public List<StateResponse> state(final UUID accountId) {

//        List<StateResponse> customerState;
//        for (Scheme scheme : schemes) {
//            StateResponse schemeState = new StateResponse(scheme.getId(), scheme.);
//            schemeState.component1(scheme.getId());
//
//        }
        return null;
    }

    /**
     * Find the account specified in the receipt, or create a new account for customer if it's not found.
     * @param receipt the receipt for the customer.
     * @return the customers Account object.
     */
    private Account getAccountFromReceipt (final Receipt receipt) {

        for (Account account: accounts) {
            if (account.getId().equals(receipt.getAccountId())) {
                return account;
            }
        }
        return new Account(receipt.getAccountId(), receipt.getMerchantId(), null);
    }

}
