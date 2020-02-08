package com.hackthesouth2020;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ServerHandler {

    final String address;

    public ServerHandler(String address) {
        this.address = address;
    }

//    public void postRequest(String request) throws IOException {
//
//        Socket socket = new Socket(address, port);
//        PrintWriter out = new PrintWriter(socket.getOutputStream());
//        out.print(request);
//        Thread.sleep(1000);
//
//    }

    public String getRequest(String request) throws IOException {

        URL url = new URL(address + request);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String line, result = "";
        while ((line = in.readLine()) != null)
            result += line;

        return result;

    }


}
