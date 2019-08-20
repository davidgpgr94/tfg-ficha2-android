package uva.inf.davidgo.ficha2.utils;

public final class ServerURLs {

    public static final String ROOT_URL = "http://192.168.56.1:3977";
    public static final String REST_API = "/ficha/api/v1";

    public static final String URL_NEW_RECORD = ROOT_URL + REST_API + "/records";
    public static final String URL_MANUAL_EXIT = ROOT_URL + REST_API + "/manual_exit";
    public static final String URL_QUICK_ENTRY = ROOT_URL + REST_API + "/quick_entry";
    public static final String URL_QUICK_EXIT = ROOT_URL + REST_API + "/quick_exit";

    public static final String DUMMY_EMPLOYEE_1 = "5d3a1257899c8612f42b7cea";
    public static final String URL_LOGIN = ROOT_URL + REST_API + "/login";;
}
