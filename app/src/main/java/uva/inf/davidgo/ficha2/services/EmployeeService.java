package uva.inf.davidgo.ficha2.services;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import uva.inf.davidgo.ficha2.pojos.Employee;
import uva.inf.davidgo.ficha2.utils.ServerURLs;

public interface EmployeeService {

    @GET(ServerURLs.URL_GET_EMPLOYEES)
    Call<List<Employee>> getEmployees(@Header("Authorization") String token);

    @FormUrlEncoded
    @POST(ServerURLs.URL_CREATE_EMPLOYEE)
    Call<Employee> createEmployee(@Header("Authorization") String token, @Field("name") String name, @Field("surname") String surname, @Field("login") String login, @Field("password") String password, @Field("is_admin") boolean isAdmin);

    @FormUrlEncoded
    @PUT(ServerURLs.URL_CHANGE_PASSWORD)
    Call<ResponseBody> changePassword(@Header("Authorization") String token, @Field("old_password") String oldPassword, @Field("new_password") String newPassword, @Field("repeat_password") String repeatPassword);

}
