package edu.brown.cs.cocker.application;


import java.io.*;
import java.net.Socket;

public class PingTester
{
    public static void main(String[] args) throws Throwable {
       try (Socket socket = new Socket("bdognom.cs.brown.edu", 10274)) {
          OutputStream os = socket.getOutputStream();
          os.write("<COMMAND CMD='STATUS' />\n".getBytes());
          os.flush();
          InputStream is = socket.getInputStream();
          byte [] buf = new byte[8192];
          int ln = is.read(buf);
          if (ln == 0) System.out.println("NOTHING RETURNED");
          else {
             String s = new String(buf,0,ln);
             System.out.println(s);
           }
        }
    }
}
