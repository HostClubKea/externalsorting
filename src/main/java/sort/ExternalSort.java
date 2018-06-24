package sort;

import sort.utils.FileWrapper;
import sort.parser.Parser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class ExternalSort<T extends Comparable<T>, M extends Parser<T>>
{
    private static int IN_MEMORY_ARRAY_SIZE = 10;

    private int bufferSize;

    M parser;

    public ExternalSort(M parser) {
        this(parser, null);
    }

    public ExternalSort(M parser, Integer bufferSize)
    {
        this.parser = parser;
        if(bufferSize == null)
            this.bufferSize = IN_MEMORY_ARRAY_SIZE;
        else
            this.bufferSize = bufferSize;

    }


    public void sort(File input, File output) throws IOException
    {

        List<File> files = splitAndSort(input);
        merge(files, output);

    }

    public List<File> splitAndSort(File input) throws IOException
    {
        List<File> files = new ArrayList<>();

        List<T> buffer = new ArrayList<>(bufferSize);

        FileWrapper<T, M> fw = new FileWrapper(input, parser);

        int j = 0;

        while(!fw.isEmpty()){
            readFilePartToBuffer(fw, buffer);

            Collections.sort(buffer);

            File outPut = new File(input.getParentFile() + "\\output" + j + ".tmp");
            writeFile(outPut, buffer);
            files.add(outPut);
            j++;

        }

        fw.close();

        return files;
    }


    private void readFilePartToBuffer(FileWrapper<T, M> fw, List<T> buffer) throws IOException
    {
        buffer.clear();
        int cnt = 0;

        while(!fw.isEmpty() && cnt < bufferSize){
            buffer.add((T) fw.pop());
            cnt ++;
        }
    }

    private void writeFile(File file, List<T> content) throws IOException
    {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(file), Charset.forName("UTF-8"))))
        {
            for (T val: content )
            {
                bufferedWriter.write(val.toString());
                bufferedWriter.newLine();
            }
        }
    }

    private List<Path> splitAndSort(Path input){
        throw new UnsupportedOperationException();
    }

    public void merge(List<File> chunks, File output) throws IOException
    {
        List<FileWrapper<T, M>> wrappers = buildWrappers(chunks);

        PriorityQueue<FileWrapper<T, M>> pq = buildPriorityQueue(wrappers);

        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(output), Charset.forName("UTF-8"))))
        {
            while (!pq.isEmpty())
                writeNextLine(pq, bufferedWriter);

        }

        cleanWrappers(wrappers);
    }


    private void writeNextLine(PriorityQueue<FileWrapper<T, M>> pq, BufferedWriter bufferedWriter) throws IOException
    {
        FileWrapper<T, M> fw = pq.poll();
        bufferedWriter.write(fw.pop().toString());
        bufferedWriter.newLine();

        //add File back to priority queue if it still contains data
        if (!fw.isEmpty())
            pq.add(fw);
    }


    private List<FileWrapper<T, M>> buildWrappers(List<File> files) throws IOException
    {
        List<FileWrapper<T, M>> wrappers = new ArrayList<>();

        for(File file: files){
            wrappers.add(new FileWrapper<>(file, parser));
        }

        return wrappers;
    }

    private void cleanWrappers(List<FileWrapper<T, M>> wrappers){
        wrappers.forEach(w -> w.close());
    }


    private PriorityQueue<FileWrapper<T, M>> buildPriorityQueue(List<FileWrapper<T, M>> wrappers){
        PriorityQueue<FileWrapper<T, M>> pq = new PriorityQueue<>(wrappers.size());

        wrappers.stream()
            .filter(fw -> !fw.isEmpty())
            .forEach(pq::add);

        return pq;
    }
}
