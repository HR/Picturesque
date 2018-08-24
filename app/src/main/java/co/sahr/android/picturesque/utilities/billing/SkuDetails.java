/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities.billing;

import org.json.JSONException;
import org.json.JSONObject;

public class SkuDetails {
    private final String mItemType;
    private final String mSku;
    private final String mType;
    private final String mPrice;
    private final long mPriceAmountMicros;
    private final String mPriceCurrencyCode;
    private final String mTitle;
    private final String mDescription;
    private final String mJson;

    public SkuDetails(String jsonSkuDetails) throws JSONException {
        this(IabHelper.ITEM_TYPE_INAPP, jsonSkuDetails);
    }

    public SkuDetails(String itemType, String jsonSkuDetails) throws JSONException {
        mItemType = itemType;
        mJson = jsonSkuDetails;
        JSONObject o = new JSONObject(mJson);
        mSku = o.optString("productId");
        mType = o.optString("type");
        mPrice = o.optString("price");
        mPriceAmountMicros = o.optLong("price_amount_micros");
        mPriceCurrencyCode = o.optString("price_currency_code");
        mTitle = o.optString("title");
        mDescription = o.optString("description");
    }

    public String getSku() { return mSku; }
    public String getType() { return mType; }
    public String getPrice() { return mPrice; }
    public long getPriceAmountMicros() { return mPriceAmountMicros; }
    public String getPriceCurrencyCode() { return mPriceCurrencyCode; }
    public String getTitle() { return mTitle; }
    public String getDescription() { return mDescription; }

    @Override
    public String toString() {
        return "SkuDetails:" + mJson;
    }
}
