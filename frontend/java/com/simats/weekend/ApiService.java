package com.simats.weekend;

import com.simats.weekend.models.AdminReviewListResponse;
import com.simats.weekend.models.AdminTripListResponse;
import com.simats.weekend.models.AnalysisGraphResponse;
import com.simats.weekend.models.AnalysisStatsResponse;
import com.simats.weekend.models.FlightSearchResponse;
import com.simats.weekend.models.HomeDataResponse;
import com.simats.weekend.models.LoginResponse;
import com.simats.weekend.models.NearbyPlacesResponse;
import com.simats.weekend.models.PlaceDetailsResponse;
import com.simats.weekend.models.PlacesResponse;
import com.simats.weekend.models.PopularPlaceStatsResponse;
import com.simats.weekend.models.SavedTripsResponse;
import com.simats.weekend.models.SignUpResponse;
import com.simats.weekend.models.SingleTripResponse;
import com.simats.weekend.models.StatusResponse;
import com.simats.weekend.models.TipsResponse;
import com.simats.weekend.models.TrainSearchResponse;
import com.simats.weekend.models.TripDataPayload;
import com.simats.weekend.models.TripResponse;
import com.simats.weekend.models.TripReviewResponse;
import com.simats.weekend.models.TripStatsResponse;
import com.simats.weekend.models.UPlaceDetailsResponse;
import com.simats.weekend.models.UserListResponse;
import com.simats.weekend.models.UserProfileResponse;
import com.simats.weekend.models.UserStatsResponse;
import com.simats.weekend.models.UserTripCountsResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("signup.php")
    Call<SignUpResponse> signupUser(
            @Field("fullname") String fullname,
            @Field("username") String username, // <-- ADD THIS LINE
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> loginUser(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("google_auth.php")
    Call<LoginResponse> authWithGoogle(@Field("idToken") String idToken);

    // CORRECTED: The return type is now Call<StatusResponse>
    @Multipart
    @POST("add_place.php")
    Call<StatusResponse> addPlace(
            @PartMap Map<String, RequestBody> fields,
            @Part List<MultipartBody.Part> images
    );

    @FormUrlEncoded
    @POST("forgot_password.php")
    Call<StatusResponse> forgotPassword(@Field("email") String email);

    @FormUrlEncoded
    @POST("reset_password.php")
    Call<StatusResponse> resetPassword(@Field("email") String email, @Field("otp") String otp, @Field("new_password") String newPassword);

    @FormUrlEncoded
    @POST("verify_otp.php")
    Call<StatusResponse> verifyOtp(@Field("email") String email, @Field("otp") String otp);

    @GET("get_places.php")
    Call<HomeDataResponse> getPlaces(@Query("filter_type") String filterType, @Query("month") String month, @Query("user_lat") Double userLat, @Query("user_lng") Double userLng);

    @GET("get_place_details.php")
    Call<PlaceDetailsResponse> getPlaceDetails(@Query("place_id") int placeId);

    @GET("get_place_details.php")
    Call<PlaceDetailsResponse> getPlaceDetailsWithDistance(@Query("place_id") int placeId, @Query("user_lat") double userLatitude, @Query("user_lng") double userLongitude);

    // In ApiService.java

    // Remove the old addTrip method and replace it with this one
// AFTER
    @POST("add_trip.php")
    Call<StatusResponse> addTrip(@Body TripDataPayload payload);

    @GET("get_trips.php")
    Call<TripResponse> getTrips(
            @Query("user_id") int userId,
            @Query("status") String status
    );

    @GET("get_trip_details.php")
    Call<SingleTripResponse> getTripDetails(@Query("trip_id") int tripId);

    @FormUrlEncoded
    @POST("update_trip_status.php")
    Call<StatusResponse> updateTripStatus(
            @Field("trip_id") int tripId,
            @Field("status") String newStatus
    );

    @FormUrlEncoded
    @POST("cancel_trip.php")
    Call<StatusResponse> cancelTrip(@Field("trip_id") int tripId);

    @FormUrlEncoded
    @POST("update_user_finished_trips.php") // Change the filename here
    Call<StatusResponse> updateFinishedTrips(@Field("user_id") int userId);

    @FormUrlEncoded
    @POST("delete_trips.php")
    Call<StatusResponse> deleteTrips(@Field("user_id") int userId, @Field("trip_ids_json") String tripIdsJson);

    @FormUrlEncoded
    @POST("get_nearby_places.php")
    Call<NearbyPlacesResponse> getNearbyPlaces(@Field("latitude") double latitude, @Field("longitude") double longitude, @Field("type") String type);

    @FormUrlEncoded
    @POST("add_review.php")
    Call<StatusResponse> addReview(@Field("user_id") int userId, @Field("place_id") int placeId, @Field("rating") float rating, @Field("review_text") String reviewText);
    @GET("api/v1/searchTrain")
    Call<TrainSearchResponse> searchTrains(
            @Query("fromStationCode") String fromCode,
            @Query("toStationCode") String toCode,
            @Query("date") String date // e.g., "2025-10-09"
    );

    /**
     * Searches for flights.
     * NOTE: The path and Query names depend on the API you chose.
     */
    @GET("flights/search")
    Call<FlightSearchResponse> searchFlights(
            @Query("from") String fromCode,
            @Query("to") String toCode,
            @Query("date") String date, // e.g., "2025-10-09"
            @Query("adults") int adults
    );
    // ADD THIS NEW METHOD FOR THE "CAPTURE MOMENTS" FEATURE
    @FormUrlEncoded
    @POST("save_media_folder.php")
    Call<StatusResponse> saveMediaFolder(
            @Field("trip_id") int tripId,
            @Field("folder_name") String folderName
    );
    @GET("get_user_stats.php")
    Call<UserStatsResponse> getUserStats();

    @GET("get_all_users.php")
    Call<UserListResponse> getAllUsers();

    @FormUrlEncoded
    @POST("update_user_status.php")
    Call<StatusResponse> updateUserStatus(
            @Field("user_id") int userId,
            @Field("status") String status
    );

    @GET("get_user_trip_counts.php")
    Call<UserTripCountsResponse> getUserTripCounts(@Query("user_id") int userId);
    @GET("get_trip_stats.php")
    Call<TripStatsResponse> getTripStats();

    @GET("get_all_trips_admin.php")
    Call<AdminTripListResponse> getAllTripsAdmin();
    // Add this method for the automatic trip status update
    @POST("update_all_finished_trips.php")
    Call<StatusResponse> updateAllFinishedTrips();
    // Add these new methods for the review system

    @POST("add_reviews.php")
    Call<StatusResponse> addReviews(@Body RequestBody reviewPayload);

    @GET("get_reviews_admin.php")
    Call<AdminReviewListResponse> getReviewsAdmin();

    @GET("get_trip_reviews.php")
    Call<TripReviewResponse> getTripReviews(@Query("trip_id") int tripId);
    @GET("get_popular_places_stats.php")
    Call<PopularPlaceStatsResponse> getPopularPlaceStats();
    @GET("get_latest_reviews.php")
    Call<AdminReviewListResponse> getLatestReviews();
    @Multipart
    @POST("update_place.php")
    Call<StatusResponse> updatePlace(@PartMap Map<String, RequestBody> placeData, @Part List<MultipartBody.Part> images);
    @GET("get_all_places.php")
    Call<PlacesResponse> getAllPlaces();

    @POST("delete_place.php")
    Call<StatusResponse> deletePlace(@Body Map<String, Integer> body);
    @GET("get_place_details.php")
    Call<UPlaceDetailsResponse> getUserPlaceDetails(@Query("place_id") int placeId);
    @GET("get_user_profile.php")
    Call<UserProfileResponse> getUserProfile(@Query("user_id") int userId);

    @Multipart
    @POST("update_user_profile.php")
    Call<StatusResponse> updateUserProfile(
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part profileImage
    );

    // A version for when no new image is uploaded
    @Multipart
    @POST("update_user_profile.php")
    Call<StatusResponse> updateUserProfile(@PartMap Map<String, RequestBody> fields);
    @POST("change_password.php")
    Call<StatusResponse> changePassword(@Body Map<String, Object> body);
    @POST("delete_account.php")
    Call<StatusResponse> deleteAccount(@Body Map<String, Integer> body);
    @GET("get_tips.php")
    Call<TipsResponse> getTravelTips();

    @POST("manage_tips.php")
    Call<StatusResponse> manageTravelTip(@Body Map<String, Object> body);
    @GET("get_analysis_stats.php")
    Call<AnalysisStatsResponse> getAnalysisStats();

    @GET("get_analysis_graphs.php")
    Call<AnalysisGraphResponse> getAnalysisGraphData(
            @Query("chart_type") String chartType,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );
    @POST("save_trip.php")
    Call<StatusResponse> saveTrip(@Body Map<String, Integer> body);

    @GET("get_saved_trips.php")
    Call<SavedTripsResponse> getSavedTrips(@Query("user_id") int userId);

    @POST("remove_saved_trip.php")
    Call<StatusResponse> removeSavedTrip(@Body Map<String, Integer> body);
}