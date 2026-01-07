package com.simats.weekend;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    public static final String BASE_URL = "http://14.139.187.229:8081/destino/";

    // Keep a static instance of Retrofit to avoid recreating it unnecessarily
    private static Retrofit retrofit = null;
    private static Gson gsonInstance = null; // Keep Gson instance as well 

    public static Retrofit getClient(Context context) {
        // Only create instances if they don't exist
        if (gsonInstance == null) {
            // --- FIX IS HERE: Configure Gson date format ---
            gsonInstance = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss") // Tells Gson the date format from PHP
                    .setLenient() // Keep lenient for potential non-JSON server errors
                    .create();
            // --- END OF FIX ---
        }

        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Keep the timeout durations
            httpClient.connectTimeout(30, TimeUnit.SECONDS);
            httpClient.readTimeout(30, TimeUnit.SECONDS);
            httpClient.writeTimeout(30, TimeUnit.SECONDS);

            httpClient.addInterceptor(loggingInterceptor);
            httpClient.addInterceptor(chain -> {
                // Use application context to avoid memory leaks if context is an Activity
                SessionManager sessionManager = new SessionManager(context.getApplicationContext());
                String token = sessionManager.getAuthToken();
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // Use the configured Gson instance
                    .addConverterFactory(GsonConverterFactory.create(gsonInstance))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit; // Return the existing or newly created instance
    }
}