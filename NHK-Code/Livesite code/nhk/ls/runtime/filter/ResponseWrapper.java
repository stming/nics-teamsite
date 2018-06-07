package nhk.ls.runtime.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import nhk.ls.runtime.filter.PrintWriterWrapper;


/**
 * User: fish
 * Date: 22/06/2011
 * Time: 6:20 PM
 */
public class ResponseWrapper extends HttpServletResponseWrapper {


    private ByteArrayOutputStream output;
    private PrintWriterWrapper printer;
    private ServletOutputStream sOutputStream;
    private int contentLength;


    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
        sOutputStream = new FilterServletOutputStream(output);
        printer = new PrintWriterWrapper(sOutputStream, "", true);
    }


    public byte[] getData() {
        return output.toByteArray();
    }


    public String getStringData() {
        return printer.getStringData();
    }






    public ServletOutputStream getOutputStream() {
        return sOutputStream;
    }


    public PrintWriter getWriter() throws IOException {
        return printer;
    }


    public int getContentLength() {
        return contentLength;
    }


    public void flushBuffer() {
        try {
            printer.flush();
            sOutputStream.flush();
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}