package uva.inf.davidgo.ficha2.services;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import uva.inf.davidgo.ficha2.pojos.LoginResponse;
import uva.inf.davidgo.ficha2.utils.ServerURLs;

public interface LoginService {

    @FormUrlEncoded
    @POST(ServerURLs.URL_LOGIN)
    Call<LoginResponse> login(@Field("login") String login, @Field("password") String password);
}
