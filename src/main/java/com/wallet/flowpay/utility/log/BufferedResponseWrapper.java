package com.wallet.flowpay.utility.log;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BufferedResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream copy = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public BufferedResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new TeeServletOutputStream(super.getOutputStream(), copy);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(getOutputStream(), true);
        }
        return writer;
    }

    public byte[] getCopy() {
        return copy.toByteArray();
    }
}

class TeeServletOutputStream extends ServletOutputStream {

    private final ServletOutputStream original;
    private final ByteArrayOutputStream copy;

    public TeeServletOutputStream(ServletOutputStream original, ByteArrayOutputStream copy) {
        this.original = original;
        this.copy = copy;
    }

    @Override
    public boolean isReady() {
        return original.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        original.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException {
        original.write(b);
        copy.write(b);
    }

    @Override
    public void flush() throws IOException {
        original.flush();
        copy.flush();
    }

    @Override
    public void close() throws IOException {
        original.close();
        copy.close();
    }
}
