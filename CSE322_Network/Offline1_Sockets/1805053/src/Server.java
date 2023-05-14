import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

class util {
    public static final String start = "\n" +
            "<html>\n" +
            "\t<head>\n" +
            "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "\t</head>\n" +
            "\t<body>\n" +
            "\t\t<h1> My Server </h1><ul>\n";

    public static final String end = "\t</ul></body></html>";

    public static boolean isDirectory(String path) {
        return path.split("\\.").length == 1;
    }
    public static boolean isImage(String path) {
        return Objects.equals(path.split("\\.")[1], "jpeg")
                || Objects.equals(path.split("\\.")[1], "jpg")
                || Objects.equals(path.split("\\.")[1], "png");
    }
    public static String imageExtension(String path) {
        return path.split("\\.")[1];
    }
    public static boolean isText(String path) {
        return Objects.equals(path.split("\\.")[1], "txt");
    }
    public static boolean isValidUpload(String path) {
        String extension = path.split("\\.")[path.split("\\.").length-1];
        String[] valid_extensions = new String[]{"jpg","txt","png","mp4"};

        for (var v: valid_extensions){
            if (v.equals(extension))
                return true;
        }
        return false;
    }
}


class Worker extends Thread {
    Socket socket;
    static int count = 0;
    int id;
    public static synchronized void writeToFile(String input, boolean OK) throws IOException {

        FileWriter out = new FileWriter("server.log", true);
        out.write("----------------------------------------------------\r\n");
        out.write(("Date: " + new Date() + "\r\n"));
        out.write((input+"\r\n"));
        if (OK)
            out.write("HTTP/1.1 200 OK\r\n");
        else
            out.write("HTTP/1.1 404 Not Found\r\n");

        out.write("----------------------------------------------------\r\n");
        out.flush();
        out.close();
    }
    public static void writeHeader(OutputStream out, String content_type, long content_length) throws IOException {

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Server: Java HTTP Server: 1.0\r\n".getBytes());
        out.write(("Date: " + new Date() + "\r\n").getBytes());
//                            out.write("Content-Disposition: attachment\r\n".getBytes());
        out.write(("Content-Type:" + content_type + "\r\n").getBytes());
        out.write(("Content-Length: " + content_length + "\r\n").getBytes());
        out.write("\r\n".getBytes());

    }
    public static void writeHeader404(OutputStream out) throws IOException {

        String content = "404 File Not Found";
        out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        out.write("Server: Java HTTP Server: 1.0\r\n".getBytes());
        out.write(("Date: " + new Date() + "\r\n").getBytes());
//                            out.write("Content-Disposition: attachment\r\n".getBytes());
        out.write(("Content-Type:text/html \r\n").getBytes());
        out.write(("Content-Length: " + content.length() + "\r\n").getBytes());
        out.write("\r\n".getBytes());

        out.write(content.getBytes());

    }
    public static void writeHeaderWithContentDisposition(OutputStream out, String content_type, long content_length, String content_disp) throws IOException {

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Server: Java HTTP Server: 1.0\r\n".getBytes());
        out.write(("Date: " + new Date() + "\r\n").getBytes());
        out.write(("Content-Disposition: "+content_disp+"\r\n").getBytes());
        out.write(("Content-Type:" + content_type + "\r\n").getBytes());
        out.write(("Content-Length: " + content_length + "\r\n").getBytes());
        out.write("\r\n".getBytes());

    }

    public Worker(Socket socket) {
        this.socket = socket;
        this.id = count;
        count++;
    }

    public void run() {
        try {
            OutputStream out = socket.getOutputStream();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String input = in.readLine();

            if (input != null) {
                System.out.println(input);
                if (input.startsWith("GET")) {
                    System.out.println("Input received: " + input);
                    String middle = input.split(" ")[1];

                    if (middle.equals("/")) {

                        StringBuilder stringBuilder = new StringBuilder();
                        File file = new File("root");

                        for (var f : Objects.requireNonNull(file.listFiles())) {
                            String f_path = f.getPath().replace("\\", "/");
                            String f_name = f.getName();
                            if (f.getName().split("\\.").length == 0)
                                stringBuilder.append("<li><a href=\"" + f_path + "\"><i>" + f_name + "</i></a></li>");
                            else
                                stringBuilder.append("<li><a href=\"" + f_path + "\">" + f_name + "</a></li>");
                        }
                        stringBuilder.append("<li><a href=\"doesnt_exist\"> DOES NOT EXIST </a></li>");
                        String content = util.start + stringBuilder + util.end;
                        writeHeader(out, "text/html", content.length());
                        writeToFile(input, true);
                        out.write(content.getBytes());

                    }
                    else {
                        String path = middle.substring(1);
                        System.out.println("PATH> " + path);
                        if (util.isDirectory(path)) {

                            File folder = new File(path);
                            try {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (var f : Objects.requireNonNull(folder.listFiles())) {
                                    String f_path = f.getPath().replace("\\", "/");
                                    String f_name = f.getName();
                                    if (f.getName().split("\\.").length == 0)
                                        stringBuilder.append("<li><a href=\"/" + f_path + "\"><i>" + f_name + "</i></a></li>");
                                    else
                                        stringBuilder.append("<li><a href=\"/" + f_path + "\">" + f_name + "</a></li>");
                                }
                                String content = util.start + stringBuilder + util.end;
                                writeHeader(out, "text/html", content.length());
                                writeToFile(input, true);
                                out.write(content.getBytes());
                            } catch (Exception e) {
                                System.out.println("Directory Not Found: " + path);
                                writeHeader404(out);
                                writeToFile(input, false);
                            }
                            //                    stringBuilder.append("<li><a href=\"www.google.com\">GOOGLE</a></li>");
                        } else if (util.isImage(path)) {
                            File image = new File(path);

                            try {
                                FileInputStream data_stream = new FileInputStream(image);

                                writeHeader(out, "image/" + util.imageExtension(path), image.length());
                                writeToFile(input, true);
                                int count;
                                byte[] buffer = new byte[8192];
                                while ((count = data_stream.read(buffer)) > 0) {
                                    out.write(buffer, 0, count);
                                    sleep(100);
                                }
                            } catch (Exception e) {
                                System.out.println("Image Not Found: " + path);
                                writeHeader404(out);
                                writeToFile(input, false);
                            }
//                        out.write(data_stream.readAllBytes());
                        } else if (util.isText(path)) {
                            File txt = new File(path);

                            try {
                                FileInputStream data_stream = new FileInputStream(txt);
                                writeHeader(out, "text/plain", txt.length());
                                writeToFile(input, true);

                                int count;
                                byte[] buffer = new byte[8192];
                                while ((count = data_stream.read(buffer)) > 0) {
                                    out.write(buffer, 0, count);
                                }
                            } catch (Exception e) {
                                System.out.println("Text Not Found: " + path);
                                writeHeader404(out);
                                writeToFile(input, false);
                            }


                        } else {
                            File txt = new File(path);
                            try {
                                FileInputStream data_stream = new FileInputStream(txt);
                                writeHeaderWithContentDisposition(out, "application/misc", txt.length(), "attachment");
                                writeToFile(input, true);
                                int count;
                                byte[] buffer = new byte[8192];
                                while ((count = data_stream.read(buffer)) > 0) {
                                    out.write(buffer, 0, count);
//                                    sleep(100);
                                }

                            } catch (Exception e) {
                                System.out.println("No file with path: " + path);
                                writeHeader404(out);
                                writeToFile(input, false);
                            }
                        }
                    }
                }

                else if (input.startsWith("UPLOAD")){


                    String file_name = input.split(" ")[1];

                    if(util.isValidUpload(file_name)){
                        FileOutputStream fileOutputStream = new FileOutputStream("root/uploads/"+file_name);


                        int BUF_SIZE = 65536;
                        byte[] buffer = new byte[BUF_SIZE];

                        while (in.available()>0){
                            if (in.available()==BUF_SIZE){
                                in.read(buffer, 0,BUF_SIZE);
                                fileOutputStream.write(buffer, 0, BUF_SIZE);
                                continue;
                            }
                            fileOutputStream.write(in.read());
                        }
                        System.out.println("Upload complete: "+file_name);
                        fileOutputStream.close();

                    }
                    else {
                        System.out.println("Unsupported file type for upload: "+file_name);
                    }

                }
            }
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Exception in ServerWorker run");
        }


    }
}


public class Server {
    static final int PORT = 5053;

    public static void main(String[] args) throws IOException {

        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        while (true) {

            Socket s = serverConnect.accept();
            Thread worker = new Worker(s);
            worker.start();
        }

    }

}
