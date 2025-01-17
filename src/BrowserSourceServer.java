import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BrowserSourceServer
{

    private HttpServer httpServer;
    private SocketIOServer socketServer;
    private boolean isUp = false;
    private String address;
    private int webPort;

    public void update(DescContainer dc, boolean auto)
    {
        if (isUp)
        {
            String imageUrl = String.format("http://%s:%d/card-image?id=%s", address, webPort, dc.getID());
            CardImageMessage msg = new CardImageMessage();
            msg.auto = auto;
            msg.src = imageUrl;
            socketServer.getBroadcastOperations().sendEvent("card_image", msg);
        }
    }

    public boolean start(String address, int webPort)
    {
        if (!isUp)
        {
            this.address = address;
            this.webPort = webPort;
            int socketPort = -1;

            try
            {
                ServerSocket s = new ServerSocket(0);
                socketPort = s.getLocalPort();
                s.close();
            } catch (IOException e)
            {
                return false;
            }

            httpServer = new HttpServer("browser-source", address, webPort, socketPort);
            if (!httpServer.startServer())
            {
                return false;
            }

            Configuration config = new Configuration();
            config.setHostname(address);
            config.setPort(socketPort);
            config.setTransports(Transport.WEBSOCKET);

            socketServer = new SocketIOServer(config);
            socketServer.addEventListener("card_image", Object.class, new DataListener<Object>()
            {
                @Override
                public void onData(SocketIOClient client, Object data, AckRequest ackRequest)
                {
                    socketServer.getBroadcastOperations().sendEvent("card_image", data);
                }
            });
            try
            {
                socketServer.start();
            } catch (Exception e)
            {
                return false;
            }
            isUp = true;
            return true;
        }
        return false;
    }

    public void stop()
    {
        if (isUp)
        {
            httpServer.stop();
            socketServer.stop();
            isUp = false;
        }
    }
}

class CardImageMessage
{
    public boolean auto;
    public String src;
}

class HttpServer extends NanoHTTPD
{
    private ArrayList<String> allowFiles;
    private String path;
    private int wsPort;
    private String address;

    public HttpServer(String path, String address, int port, int wsPort)
    {
        super(address, port);
        this.address = address;
        this.path = path;
        this.wsPort = wsPort;
        allowFiles = new ArrayList<>();
        try
        {
            Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach(f ->
            {
                allowFiles.add(f.toAbsolutePath().toString());
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean startServer()
    {
        try
        {
            super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e)
        {
            return false;
        }
        return true;
    }

    public void stop()
    {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        String uri = session.getUri();
        if (uri.equalsIgnoreCase("/address-config.js"))
        {
            String content = String.format("var socket_server_addr = \"http://%s:%d\";\n", address, wsPort);
            return newFixedLengthResponse(Status.OK, "application/javascript", content);
        }
        if (uri.equalsIgnoreCase("/card-image"))
        {
            try
            {
                String cardId = session.getParameters().get("id").get(0);
                BufferedImage cardImage = RecogApp.INSTANCE.getCardImageFromID(cardId);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(cardImage, "png", baos);
                byte[] buf = baos.toByteArray();
                ByteArrayInputStream bain = new ByteArrayInputStream(buf);
                baos.close();
                return newChunkedResponse(Status.OK, "image/png", bain);
            } catch (Exception e)
            {
                return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "");
            }
        }
        Path p = Paths.get(path, uri);
        File f = p.toFile();
        if (allowFiles.contains(f.getAbsolutePath()) && f.exists() && f.isFile())
        {
            FileInputStream fin;
            try
            {
                fin = new FileInputStream(f);
            } catch (FileNotFoundException e)
            {
                return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "");
            }
            String mime = "text/html";
            if (f.getName().endsWith(".js"))
            {
                mime = "application/javascript";
            }
            return newChunkedResponse(Status.OK, mime, fin);
        }
        return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "");
    }
}
