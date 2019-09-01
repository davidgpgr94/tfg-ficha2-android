package uva.inf.davidgo.ficha2.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import uva.inf.davidgo.ficha2.pojos.Employee;
import uva.inf.davidgo.ficha2.utils.ServerURLs;

public interface EmployeeService {

    @GET(ServerURLs.URL_GET_EMPLOYEES)
    Call<List<Employee>> getEmployees(@Header("Authorization") String token);

}
