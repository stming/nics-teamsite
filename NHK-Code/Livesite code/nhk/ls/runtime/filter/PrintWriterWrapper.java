/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nhk.ls.runtime.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 *
 * @author smukherj
 */
public class PrintWriterWrapper extends PrintWriter {

    private String data;

    public PrintWriterWrapper(Writer writer) {
        super(writer);
    }


    public PrintWriterWrapper(Writer writer, boolean b) {
        super(writer, b);
    }


    public PrintWriterWrapper(OutputStream outputStream) {
        super(outputStream);
    }


    public PrintWriterWrapper(OutputStream outputStream, boolean b) {
        super(outputStream, b);
    }


    public PrintWriterWrapper(OutputStream outputStream, String data, boolean b) {
        super(outputStream, b);
        this.data = data;
    }


    public PrintWriterWrapper(String s) throws FileNotFoundException {
        super(s);
    }


    public PrintWriterWrapper(String s, String s1) throws FileNotFoundException, UnsupportedEncodingException {
        super(s, s1);
    }


    public PrintWriterWrapper(File file) throws FileNotFoundException {
        super(file);
    }


    public PrintWriterWrapper(File file, String s) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, s);
    }


    public void print(String s){
        data = data + s;
        super.print(s);
    }


    public String getStringData() {
        return this.data;
    }


    public void write(String s){
        super.write(s);
    }


    public void write(String s, int off, int len){
        super.write(s, off, len);
    }



}

