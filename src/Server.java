public class Server implements Runnable {
    private final ApiService apiService;

    Server(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void run() {
        apiService.registerData("test", "something");
        apiService.readData("test");
        apiService.readData("unknown");
    }
}
