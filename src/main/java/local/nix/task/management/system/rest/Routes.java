package local.nix.task.management.system.rest;


public final class Routes {

    private Routes() {
    }

    public static final String API_ROOT = "/api/v1";

    public static final String USERS =  API_ROOT + "/users";

    public static final String TASKS = API_ROOT + "/tasks";

    public static final String TOKEN = API_ROOT + "/token";

    public static String user(long id) {
        return USERS + '/' + id;
    }

    public static String task(long id) {
        return TASKS + '/' + id;
    }

}

