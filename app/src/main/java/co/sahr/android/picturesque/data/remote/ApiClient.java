/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.remote;

import co.sahr.android.picturesque.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    /**
     * Api request base url (must end in trailing forward slash / )
     * A trailing / ensures that endpoints values which are relative paths will correctly append
     * themselves to a base which has path components
     */
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Retrofit.Builder builder = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
                                                             .addConverterFactory
                                                                     (GsonConverterFactory.create
                                                                             ());
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


            // Add request logging if not already
            if (BuildConfig.DEBUG) {
                httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel
                        (HttpLoggingInterceptor.Level.HEADERS));
            }

            retrofit = builder.client(httpClient.build())
                              .build();
        }

        return retrofit;
    }

    public static Api createService() {
        return ApiClient.getClient()
                        .create(Api.class);
    }
}
