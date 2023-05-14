import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;
class ClientWorker extends Thread{
    Socket socket;
    String path ;

    public ClientWorker(Socket s, String path){
        this.socket = s;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            OutputStream out = this.socket.getOutputStream();
            FileInputStream data_stream = new FileInputStream(this.path);
            String file_name = this.path.split("/")[this.path.split("/").length-1];
            out.write(("UPLOAD "+file_name+"\r\n").getBytes());

            if (util.isValidUpload(this.path)) {
                int count;
                byte[] buffer = new byte[8192];
                while ((count = data_stream.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
                System.out.println("Upload complete: "+file_name);


            }
            else {
                System.out.println("Unsupported file type for upload: "+this.path);
            }

            out.close();
            data_stream.close();
            socket.close();


        } catch (Exception e) {
            System.out.println(e);
            System.out.println("File not found");
        }
    }
}

//UPLOAD Faces2.txt
//UPLOAD Mini2.txt

public class Client {
    static final int PORT = 5053;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        while (true) {

            Socket socket = new Socket("localhost", PORT);

            System.out.println("Connection established on port : " + PORT + " ...\n");
            String[] str_arr = scanner.nextLine().split(" ");

            if(str_arr.length!=2 || !Objects.equals(str_arr[0], "UPLOAD")){
                System.out.println("Only upload operations supported.");
                continue;
            }

            ClientWorker worker = new ClientWorker(socket, str_arr[1]);
            worker.start();

        }
    }
}
