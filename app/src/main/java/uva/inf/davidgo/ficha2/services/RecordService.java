package uva.inf.davidgo.ficha2.services;

import java.util.Date;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import uva.inf.davidgo.ficha2.pojos.Record;
import uva.inf.davidgo.ficha2.utils.ServerURLs;

public interface RecordService {

    @POST(ServerURLs.URL_QUICK_ENTRY)
    Call< Record > quick_entry(@Header("Authorization") String token);

    @POST(ServerURLs.URL_QUICK_EXIT)
    Call< Record > quick_exit(@Header("Authorization") String token);

    @FormUrlEncoded
    @POST(ServerURLs.URL_NEW_RECORD)
    Call< Record > manual_record(@Header("Authorization") String token, @Field("entry") Date entry);

    @FormUrlEncoded
    @POST(ServerURLs.URL_NEW_RECORD)
    Call< Record > manual_record(@Header("Authorization") String token, @Field("entry") Date entry, @Field("exit") Date exit);
}
