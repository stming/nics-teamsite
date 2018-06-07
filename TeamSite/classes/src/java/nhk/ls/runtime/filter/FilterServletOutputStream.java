package nhk.ls.runtime.filter;

import javax.servlet.ServletOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: fish
 * Date: 22/06/2011
 * Time: 10:00 PM
 */
public class FilterServletOutputStream extends ServletOutputStream {

  private DataOutputStream stream;

  public FilterServletOutputStream(OutputStream output) {
    stream = new DataOutputStream(output);
  }

  public void write(int b) throws IOException  {
    stream.write(b);
  }

  public void write(byte[] b) throws IOException  {
    stream.write(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    stream.write(b,off,len);
  }

  public void flush() throws IOException {
      stream.flush();
  }

  public void close() throws IOException {
      stream.close();
  }

  
}
