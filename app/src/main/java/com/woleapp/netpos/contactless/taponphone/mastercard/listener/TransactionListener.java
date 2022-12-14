package com.woleapp.netpos.contactless.taponphone.mastercard.listener;


import com.danbamitale.epmslib.entities.CardData;

public interface TransactionListener {

    /**
     * Invoked for offline apprroval
     */
    void onTransactionSuccessful();

    /**
     * Invoked when requires online approval
     */
    void onOnlineReferral(CardData cardData, String pan);

    /**
     * Invoked when transaction is declined
     */
    void onTransactionDeclined();

    /**
     * Invoked when SDK ends due to error
     */
    void onApplicationEnded();

    /**
     * Invoked when transaction is cancelled
     */
    void onTransactionCancelled();

    void logToScreen(String s);

    void onTransactionError(String message);
}
