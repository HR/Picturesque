/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities.billing;

public class IabResult {
    int mResponse;
    String mMessage;

    public IabResult(int response, String message) {
        mResponse = response;
        if (message == null || message.trim()
                                      .length() == 0) {
            mMessage = IabHelper.getResponseDesc(response);
        } else {
            mMessage = message + " (response: " + IabHelper.getResponseDesc(response) + ")";
        }
    }

    public int getResponse() { return mResponse; }

    public String getMessage() { return mMessage; }

    // Successful if item was purchased or already owned
    public boolean isSuccess() {
        return mResponse == IabHelper.BILLING_RESPONSE_RESULT_OK || mResponse == IabHelper
                .BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED;
    }

    public boolean isCancel() {
        return mResponse == IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED || mResponse ==
                IabHelper.IABHELPER_USER_CANCELLED;
    }

    public boolean isFailure() { return !isSuccess(); }

    public String toString() { return "IabResult: " + getMessage(); }
}

