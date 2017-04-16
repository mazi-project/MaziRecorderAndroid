package de.udk.drl.mazirecorderandroid.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.udk.drl.mazirecorderandroid.models.AttachmentModel;
import de.udk.drl.mazirecorderandroid.models.InterviewModel;
import de.udk.drl.mazirecorderandroid.utils.ObservableProperty;
import de.udk.drl.mazirecorderandroid.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by lutz on 04/11/16.
 */
public class InterviewUploader {

    public static final String API_BASE_URL = "http://192.168.1.2:8081/api/";

    public static final String API_DATE_FORMAT = "yyyy-MM-dd";

    public static class ServiceGenerator {

        private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        private static Retrofit.Builder builder =
                new Retrofit.Builder()
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .baseUrl(API_BASE_URL);

        public static <S> S createService(Class<S> serviceClass) {
            Retrofit retrofit = builder.client(httpClient.build()).build();
            return retrofit.create(serviceClass);
        }
    }

    public static class ConvertToJsonFunction implements Function<ResponseBody, JSONObject> {
        @Override
        public JSONObject apply(ResponseBody response) throws Exception {
            String string = response.string();
            return new JSONObject(string);
        }
    }

    public interface APIService {

        @POST("interviews")
        @FormUrlEncoded
        Observable<ResponseBody> postInterview(
                @Field("name") String title,
                @Field("role") String text,
                @Field("text") String date);

        @POST("attachments")
        @FormUrlEncoded
        Observable<ResponseBody> postAttachment(
                @Field("text") String text,
                @Field("tags") String[] tags,
                @Field("interview") String interview);

        @Multipart
        @POST("upload/image/{interviewId}")
        Observable<ResponseBody> uploadImage(
                @Path("interviewId") String interviewId,
                @Part MultipartBody.Part file
        );
    }

    public Context context;

    public InterviewUploader(Context context) {
        this.context = context;
    }

    public Observable<JSONObject> postInterview(final InterviewModel interview) {

        final APIService service = ServiceGenerator.createService(APIService.class);

        // first post interview
        return service.postInterview(interview.name, interview.role, interview.text).map(new ConvertToJsonFunction())
                .flatMap(new Function<JSONObject, Observable<ResponseBody>>() {
                    // upload image
                    @Override
                    public Observable<ResponseBody> apply(JSONObject json) throws Exception {
                        String id = json.getJSONObject("interview").getString("_id");
                        MultipartBody.Part image = MultipartBody.Part.createFormData("_","_");
                        if (interview.imageFile != null) {
                            File file = new File(interview.imageFile);
                            if (file.exists())
                                image = prepareFilePart("file", file);
                        }
                        return service.uploadImage(id,image);
                    }
                }).map(new ConvertToJsonFunction())
                .flatMapIterable(new Function<JSONObject, Iterable<AttachmentModel>>() {
                    @Override
                    // create list of attachments
                    public Iterable<AttachmentModel> apply(JSONObject json) throws Exception {
                        String id = json.getJSONObject("interview").getString("_id");
                        for (AttachmentModel attachments : interview.attachments)
                            attachments.interviewId = id;
                        return interview.attachments;
                    }
                })
                .flatMap(new Function<AttachmentModel, Observable<ResponseBody>>() {
                    //post attachments
                    @Override
                    public Observable<ResponseBody> apply(AttachmentModel model) throws Exception {
                        return service.postAttachment(model.text,model.tags,model.interviewId);
                    }
               }).toList().map(new Function<List<ResponseBody>, JSONObject>() {
                    @Override
                    public JSONObject apply(List<ResponseBody> responses) throws Exception {
                        JSONObject json = new JSONObject();
                        for (ResponseBody response : responses) {
                            Log.i("RESPONSE", response.string());
                        }
                        json.put("success",true);

                        return json;
                    }
                }).toObservable().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());

    }

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private static RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

    private MultipartBody.Part prepareFilePart(String partName, File file) {

        //String type = context.getContentResolver().getType(Uri.fromFile(file));
        String type = Utils.getMimeType(context, Uri.fromFile(file));

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(type), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

}
