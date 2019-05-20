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
    private List<Account> accounts = new ArrayList<>();

    /**
     * The primary constructor called once at runtime to set the
     * available schemes for the merchant.
     * @param schemes the list of Scheme objects to set all schemes available for the merchant
     */
    public MechantLoyalty(final List<Scheme> schemes) {
        this.schemes = schemes;
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

        Account customerAccount = getAccountFromId(receipt.getAccountId());
        Map<UUID, Integer> allStamps = customerAccount.getStamps();
        Map<UUID, List<Long>> schemePayments = customerAccount.getAllPayments();
        for (Scheme scheme: schemes) {
            UUID schemeId = scheme.getId();

            // For each scheme, calculate how many items were purchased in it, add to current stamp number.
            int stampsGained = getStampsGainedFromReceipt(receipt, scheme);

            // Get the current number of stamps the customer has, if none. return 0
            int currentStamps = customerAccount.getStamps().getOrDefault(schemeId, 0);

            // Calculate number of payments customer is to receive, and payment item didn't count as a stamp.
            int totalStamps = stampsGained + currentStamps;
            int numberOfPayments = 0;
            while (totalStamps > scheme.getMaxStamps()) {
                ++numberOfPayments;
                // total stamps is the number of stamps minus stamps used for payment and item itself used for payment
                totalStamps = totalStamps - (scheme.getMaxStamps() + 1);
            }

            List<Long> newPayments = calculatePaymentsToMake(items, numberOfPayments);
            List<Long> allPayments = new ArrayList<>();
            List<Long> existingPayments = schemePayments.get(schemeId);
            Optional.ofNullable(newPayments).ifPresent(allPayments::addAll);
            Optional.ofNullable(existingPayments).ifPresent(allPayments::addAll);

            schemePayments.put(schemeId, allPayments);
            allStamps.put(schemeId, totalStamps);

            Responses.add(new ApplyResponse(
                    scheme.getId(),
                    totalStamps,
                    (stampsGained - numberOfPayments),
                    newPayments));
        }

        // Create new account as unable to call accounts.get(index).setAllPayments() as kotlin models do not appears to have standard setters.
        Account newAccount = new Account(customerAccount.getId(), allStamps, schemePayments);
        try {
            accounts.set(accounts.indexOf(customerAccount), newAccount);
        } catch (IndexOutOfBoundsException e) {
            accounts.add(newAccount);
        }

        return Responses;
    }

    /**
     * Retrieve and return the current state for an account for all the active schemes.  If they have never used a
     * scheme before then a zero state should be returned (ie 0 stamps given, 0 payments, current stamp as 0.
     *
     * Should return one `StateResponse` instance for each scheme
     */
    @Override
    public List<StateResponse> state(final UUID accountId) {

        List<StateResponse> customerState = new ArrayList<>();
        Account customerAccount = getAccountFromId(accountId);

        for (Scheme scheme: schemes) {
            UUID schemeId = scheme.getId();
            StateResponse response = new StateResponse(
                    schemeId,
                    customerAccount.getStamps().getOrDefault(schemeId, 0),
                    customerAccount.getAllPayments().getOrDefault(schemeId, new ArrayList<Long>()));
            customerState.add(response);
        }

        return customerState;
    }

    /**
     * Find the account by the specified Id, or create a new account for the given customer if it's not found.
     * @param accountId the accountId for the customer.
     * @return the customers Account object.
     */
    private Account getAccountFromId (final UUID accountId) {

        for (Account account: accounts) {
            if (account.getId().equals(accountId)) {
                return account;
            }
        }
        return new Account(accountId, new HashMap<>(), new HashMap<>());
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
            int quantity = item.getQuantity();
            //If the item is in the scheme, increment stamps
            if (scheme.getSkus().stream().anyMatch(str -> str.trim().equals(item.getSku()))) {
                stamps = stamps + quantity;
            }
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
            List<Item> sortedItems = new ArrayList<>(items);
            // Sort the items on their price ascending to ensure the first items are the cheapest
            sortedItems.sort(Comparator.comparing(Item::getPrice));
            while (paymentsLeft > 0) {
                Item firstItem = sortedItems.get(0);
                paymentsGiven.add(firstItem.getPrice());
                if (firstItem.getQuantity() <=1) {
                    sortedItems.remove(0);
                } else {
                    Item newItem = new Item(firstItem.getSku(), firstItem.getPrice(), (firstItem.getQuantity() - 1));
                    sortedItems.set(0, newItem);
                }
                paymentsLeft--;
            }
        }

        return paymentsGiven;
    }
}
