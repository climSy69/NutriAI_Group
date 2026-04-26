package com.example.advancedcomputersciencecn6008_1;

import com.google.gson.annotations.SerializedName;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    private static Retrofit retrofit = null;

    // --- DATA MODELS ---

    public static class AuthRequest {
        String email;
        String password;

        public AuthRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        @SerializedName("access_token")
        public String accessToken;
        public Session session;
        public User user;
        
        public static class Session {
            @SerializedName("access_token")
            public String accessToken;
        }

        public static class User {
            public String id; 
            public String email;
        }

        public String getAccessToken() {
            if (accessToken != null) return accessToken;
            if (session != null) return session.accessToken;
            return null;
        }
    }

    public static class Profile {
        public String id;
        @SerializedName("full_name")
        public String fullName;
        public String email;

        public Profile(String id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }
    }

    public static class Meal {
        @SerializedName("user_id")
        public String userId;
        @SerializedName("meal_name")
        public String mealName;
        public int calories;
        public int protein;
        public int carbs;
        public int fats;
        public String day;
        @SerializedName("meal_type")
        public String mealType;

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
        @POST("auth/v1/signup")
        Call<AuthResponse> signUp(@Body AuthRequest request);

        @POST("auth/v1/token?grant_type=password")
        Call<AuthResponse> signIn(@Body AuthRequest request);
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

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                        .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=minimal");

                    if (original.header("Authorization") == null) {
                        builder.addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
                    }

                    return chain.proceed(builder.build());
                })
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        }
        return retrofit;
    }
}
