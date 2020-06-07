import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.listener.DataListener;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class BrowserSourceServer {

    private StaticServer staticServer;
    private SocketIOServer socketServer;

    private void start()
    {
        staticServer = new StaticServer("browser-source", "localhost", 7777);
        staticServer.startServer();

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(7778);
        config.setTransports(Transport.byName("websocket"));

        final SocketIOServer socketServer = new SocketIOServer(config);
        socketServer.addEventListener("card_image", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackRequest) {
                socketServer.getBroadcastOperations().sendEvent("card_image", data);
                System.out.println("data get: "+data.toString());
            }
        });

        socketServer.start();
    }

    public void stop()
    {
        staticServer.stop();
        socketServer.stop();
    }


    public static void main(String[] args) throws InterruptedException{
        BrowserSourceServer bss = new BrowserSourceServer();
        bss.start();
        Thread.sleep(Integer.MAX_VALUE);
        bss.stop();
    }
}

class StaticServer extends NanoHTTPD {
    private ArrayList<String> allowFiles;
    private String path;

    public StaticServer(String path, String address, int port) {
        super(address, port);
        this.path = path;
        allowFiles = new ArrayList<>();
        try {
            Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach(f -> {
                allowFiles.add(f.toAbsolutePath().toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean startServer() {
        try {
            super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void stop() {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {

        Path p = Paths.get(path, session.getUri());
        File f = p.toFile();
        if (allowFiles.contains(f.getAbsolutePath()) && f.exists() && f.isFile()) {
            FileInputStream fin;
            try {
                fin = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "");
            }
            String mime = "text/html";
            if(f.getName().endsWith(".js"))
            {
                mime = "application/javascript";
            }
            return newChunkedResponse(Status.OK, mime, fin);
        }
        else
        {
            return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "");
        }
    }
}