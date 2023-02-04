public class Main {
    public static void main(String args[]) {
        final byte[] key = new byte[]{
            0,1,2,3,4,5,6,7,
            8,9,10,11,12,13,14,15,
            16,17,18,19,20,21,22,23,
            24,25,26,27,28,29,30,31,
        };

        final DataCryptor dataCryptor = new DataCryptor(key);
        final ApiService apiService = new ApiService(dataCryptor);
        final Server server = new Server(apiService);

        final Thread registerThread = new Thread(server);

        System.out.println("Start thread");
        registerThread.start();
    }
}
