package com.example.advancedcomputersciencecn6008_1;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class SupabaseClient {
    private static final String TAG = "SUPABASE_CLIENT";
    private static Retrofit retrofit = null;

    // --- DATA MODELS ---
    public static class AuthRequest {
        @SerializedName("email") public String email;
        @SerializedName("password") public String password;

        public AuthRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        @Nullable @SerializedName("access_token") public String accessToken;
        @Nullable @SerializedName("token_type") public String tokenType;
        @Nullable @SerializedName("expires_in") public Long expiresIn;
        @Nullable @SerializedName("refresh_token") public String refreshToken;
        @Nullable @SerializedName("user") public User user;
        @Nullable @SerializedName("session") public Session session;

        public static class Session {
            @Nullable @SerializedName("access_token") public String accessToken;
            @Nullable @SerializedName("user") public User user;
        }

        public static class User {
            @Nullable @SerializedName("id") public String id; 
            @Nullable @SerializedName("email") public String email;
        }

        @Nullable
        public String getAccessToken() {
            if (accessToken != null) return accessToken;
            if (session != null) return session.accessToken;
            return null;
        }

        @Nullable
        public String getUserId() {
            if (user != null) return user.id;
            if (session != null && session.user != null) return session.user.id;
            return null;
        }

        @NonNull
        @Override
        public String toString() {
            return "AuthResponse{hasToken=" + (getAccessToken() != null) + ", userId=" + getUserId() + "}";
        }
    }

    public static class Profile {
        @SerializedName("id") public String id;
        @SerializedName("full_name") public String fullName;
        @SerializedName("email") public String email;

        public Profile(String id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }
    }

    public static class Meal {
        @SerializedName("id") public String id;
        @SerializedName("user_id") public String userId;
        @SerializedName("meal_name") public String mealName; 
        @SerializedName("calories") public int calories;
        @SerializedName("protein") public int protein;
        @SerializedName("carbs") public int carbs;
        @SerializedName("fats") public int fats;
        @SerializedName("day") public String day;
        @SerializedName("meal_type") public String mealType;
        @SerializedName("created_at") public String createdAt;

        public Meal(String userId, String mealName, int calories, int protein, int carbs, int fats, String day, String mealType) {
            this.userId = userId;
            this.mealName = mealName;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.day = day;
            this.mealType = mealType;
        }
    }

    // --- API INTERFACES ---
    public interface AuthService {
        // Corrected to use typed AuthResponse for both calls
        @POST("auth/v1/token?grant_type=password")
        Call<AuthResponse> signIn(@Body AuthRequest request);

        @POST("auth/v1/signup")
        Call<AuthResponse> signUp(@Body AuthRequest request);
    }

    public interface DatabaseService {
        @POST("rest/v1/profiles")
        Call<Void> createProfile(
            @Header("Authorization") String token,
            @Body Profile profile
        );

        @POST("rest/v1/meals")
        Call<Void> insertMeal(
            @Header("Authorization") String token,
            @Body Meal meal
        );

        @GET("rest/v1/meals")
        Call<java.util.List<Meal>> getMeals(
            @Header("Authorization") String token,
            @Query("user_id") String userIdFilter
        );
    }

    // --- RETROFIT CLIENT ---
    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "getClient: Initializing Retrofit with mandatory Supabase headers...");

            String baseUrl = SupabaseConfig.SUPABASE_URL;
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                Log.e(TAG, "CRITICAL ERROR: SUPABASE_URL is null or empty!");
                baseUrl = "https://invalid.url/";
            }

            // 1. Interceptor for mandatory Supabase headers
            Interceptor headerInterceptor = new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    
                    // Supabase requires 'apikey' and 'Content-Type' for all requests
                    builder.header("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                    builder.header("Content-Type", "application/json");

                    // Add default Bearer token if none provided (e.g. for Auth or public REST)
                    if (original.header("Authorization") == null) {
                        builder.header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
                    }

                    return chain.proceed(builder.build());
                }
            };

            // 2. Logging Interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d("OKHTTP_LOG", message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 3. OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

            try {
                retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create()) // GSON Factory ensured
                    .client(client)
                    .build();
                Log.d(TAG, "getClient: Retrofit initialized successfully.");
            } catch (Exception e) {
                Log.e(TAG, "getClient: Failed to build Retrofit", e);
            }
        }
        return retrofit;
    }
}
