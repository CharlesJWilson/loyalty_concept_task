package com.flux.test;

import com.flux.test.model.*;

import java.util.*;

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
     * The constructor for merchant loyalty when no current customer accounts exist.
     * @param schemes the list of Scheme objects to set all schemes available for the merchant
     */
    public MechantLoyalty(final List<Scheme> schemes) {
        MechantLoyalty(schemes, new ArrayList<Account>());
    }

    /**
     * The primary constructor called once at runtime to set the
     * available schemes for the merchant.
     * @param schemes the list of Scheme objects to set all schemes available for the merchant
     * @param accounts the list of accounts already associated with the merchant
     */
    public MechantLoyalty(final List<Scheme> schemes, final List<Account> accounts) {
        this.schemes = Objects.requireNonNull(schemes);
        this.accounts = accounts;
    }

    @Override
    public void setSchemes(final List<Scheme> schemes) {
        this.schemes = Objects.requireNonNull(schemes);
    }

    public void setAccounts(final  List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<Scheme> getSchemes() {
        return schemes;
    }

    public List<Account> getAccounts() { return accounts; }

    /**
     * Apply the receipt to all active schemes for the merchant, response should include one `ApplyResponse`
     * instance for each scheme belonging to the merchant - even if no stamps or payments where awarded for that scheme
     */
    @Override
    public List<ApplyResponse> apply(final Receipt receipt) {
        List<ApplyResponse> Responses = new ArrayList<>();
        List<Item> items = receipt.getItems();

        Account customerAccount = getAccountFromReceipt(receipt);
        for (Scheme scheme: schemes) {

            // For each scheme, calculate how many items were purchased in it, add to current stamp number.
            int stampsGained = getStampsGainedFromReceipt(receipt, scheme);

            // Get the current number of stamps the customer has, if none. return 0
            int currentStamps = customerAccount.getStamps().getOrDefault(scheme.getId(), 0);

            // Calculate number of payments customer is to receive, and payment item didn't count as a stamp.
            int totalStamps = stampsGained + currentStamps;
            int numberOfPayments = 0;
            while (totalStamps > scheme.getMaxStamps()) {
                ++numberOfPayments;
                // total stamps is the number of stamps minus stamps used for payment and item itself used for payment
                totalStamps = totalStamps - (scheme.getMaxStamps() + 1);
            }

            Responses.add(new ApplyResponse(
                    scheme.getId(),
                    totalStamps,
                    (stampsGained - currentStamps - numberOfPayments),
                    calculatePaymentsToMake(items, numberOfPayments)));
        }

        return Responses;
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

    /**
     * Gets the raw number of possible stamps gained by a customer in their receipt
     * @param receipt the customers receipt
     * @param scheme the scheme for which the receipt skus should be checked against
     * @return int the raw number of possible stamps gained.
     */
    private int getStampsGainedFromReceipt(final Receipt receipt, final Scheme scheme) {
        int stamps = 0;
        for (Item item: receipt.getItems()) {
            stamps = scheme.getSkus().contains(item.getSku()) ? stamps++ : stamps; //If the item is in the scheme, increment stamps
        }
        return stamps;
    }

    /**
     * Takes in a list of payments in arbitrarily order, sorts them and creates a list of payments to the length specified.
     * @param items the arbitrarily ordered list of items to make the payment from
     * @param paymentsToMake the number of payments to make
     * @return the list of item payments to make
     */
    private List<Long> calculatePaymentsToMake(final List<Item> items, final int paymentsToMake) {
        int paymentsLeft = paymentsToMake;
        List<Long> paymentsGiven = new ArrayList<>();
        if (paymentsLeft > 0) {
            List<Item> sortedItems = items;
            // Sort the items on their price ascending to ensure the first items are the cheapest
            sortedItems.sort(Comparator.comparing(Item::getPrice));
            while (paymentsLeft > 0) {
                paymentsGiven.add(sortedItems.get(0).getPrice());
                sortedItems.remove(0);
                paymentsLeft--;
            }
        }

        return paymentsGiven;
    }
}
