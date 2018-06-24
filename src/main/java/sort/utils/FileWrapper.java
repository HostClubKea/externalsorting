package sort.utils;

import sort.parser.Parser;

import java.io.*;

public class FileWrapper<T extends Comparable<T>, M extends Parser<T>> implements Comparable<FileWrapper>
{
    private T val;
    private final BufferedReader br;
    M parser;

    public FileWrapper(File file, M parser) throws IOException
    {
        this.parser = parser;
        this.br = new BufferedReader(new FileReader(file));
        read();
    }

    private void read() throws IOException
    {
        val = parser.parse(br.readLine());
    }

    public T peek(){
        return val;
    }

    public T pop() throws IOException
    {
        T currentVal = val;
        read();
        return currentVal;
    }

    public boolean isEmpty(){
        return val == null;
    }


    @Override public int compareTo(FileWrapper o)
    {
        return val.compareTo((T) o.val);
    }

    public void close(){
        try
        {
            this.br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
