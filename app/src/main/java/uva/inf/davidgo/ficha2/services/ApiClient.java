package uva.inf.davidgo.ficha2.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uva.inf.davidgo.ficha2.utils.ServerURLs;

public class ApiClient {
    private static Retrofit.Builder retrofit = null;

    private static Retrofit.Builder getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create());
        }
        return retrofit;
    }

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = getClient().build();
        return retrofit.create(serviceClass);
    }
}
