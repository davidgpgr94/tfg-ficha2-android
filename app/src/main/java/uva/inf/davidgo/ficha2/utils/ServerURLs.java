package uva.inf.davidgo.ficha2.utils;

public final class ServerURLs {

    public static final String ROOT_URL = "https://ficha2-api.herokuapp.com";
    public static final String REST_API = "/ficha/api/v1";

    public static final String URL_NEW_RECORD = ROOT_URL + REST_API + "/records";
    public static final String URL_MANUAL_EXIT = ROOT_URL + REST_API + "/manual_exit";
    public static final String URL_QUICK_ENTRY = ROOT_URL + REST_API + "/quick_entry";
    public static final String URL_QUICK_EXIT = ROOT_URL + REST_API + "/quick_exit";

    public static final String URL_GET_RECORDS = ROOT_URL + REST_API + "/records";
    public static final String URL_GET_REPORT = ROOT_URL + REST_API + "/report";

    public static final String URL_GET_INCOMPLETED_RECORD = ROOT_URL + REST_API + "/incompleted_record";

    public static final String URL_LOGIN = ROOT_URL + REST_API + "/login";
    public static final String URL_CREATE_EMPLOYEE = ROOT_URL + REST_API + "/register";

    public static final String URL_GET_EMPLOYEES = ROOT_URL + REST_API + "/employees";
    public static final String URL_CHANGE_PASSWORD = ROOT_URL + REST_API + "/change_password";
}
