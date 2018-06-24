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


    public void sort(File input, File output) throws IllegalAccessException, IOException, InstantiationException
    {

        List<File> files = splitAndSort(input);
        //List<Path> sortedChunks = splitAndSort(input);
        merge(files, output);

    }

    public void readChunk(FileWrapper<T, M> fw, List<T> buffer) throws IOException
    {
        buffer.clear();
        int cnt = 0;

        while(!fw.isEmpty() && cnt < bufferSize){
            buffer.add((T) fw.pop());
            cnt ++;
        }

    }

    public List<File> splitAndSort(File input) throws IllegalAccessException, IOException, InstantiationException
    {
        List<T> buffer = new ArrayList<>(bufferSize);

        FileWrapper<T, M> fw = new FileWrapper(input, parser);

        int i = 0;
        int j = 0;
        List<File> files = new ArrayList<>();
        while(!fw.isEmpty()){
            if(i == bufferSize){
                Collections.sort(buffer);
                File outPut = new File(input.getParentFile() + "\\output" + j + ".tmp");
                writeFile(outPut, buffer);
                j++;
                i = 0;
                buffer.clear();
                files.add(outPut);
            } else {
                buffer.add(fw.pop());
                i++;
            }
        }

        if(buffer.size() != 0){
            Collections.sort(buffer);
            File outPut = new File(input.getParentFile() + "\\output" + j + ".tmp");
            writeFile(outPut, buffer);
            files.add(outPut);
        }
        fw.close();
return files;
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

    public void merge(List<File> chunks, File output) throws IOException, InstantiationException, IllegalAccessException
    {
        List<FileWrapper<T, M>> wrappers = buildWrappers(chunks);

        PriorityQueue<FileWrapper<T, M>> pq = new PriorityQueue<>(chunks.size());

        for (File chunk: chunks)
        {
            FileWrapper<T, M> fw = new FileWrapper(chunk, parser);
            if(!fw.isEmpty())
                pq.add(fw);
            else
                fw.close();
        }

        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(output), Charset.forName("UTF-8"))))
        {

            while (!pq.isEmpty())
            {
                FileWrapper<T, M> fw = pq.poll();
                bufferedWriter.write(fw.pop().toString());
                bufferedWriter.newLine();

                if (!fw.isEmpty())
                    pq.add(fw);
                else
                    fw.close();

            }

        }

    }

    private List<FileWrapper<T, M>> buildWrappers(List<File> files) throws IOException
    {
        List<FileWrapper<T, M>> wrappers = new ArrayList<>();

        for(File file: files){
            wrappers.add(new FileWrapper<>(file, parser));
        }

        return wrappers;
    }

    private PriorityQueue<FileWrapper<T, M>> buildPriorityQueue(List<FileWrapper<T, M>> wrappers){
        PriorityQueue<FileWrapper<T, M>> pq = new PriorityQueue<>(wrappers.size());

        for (FileWrapper<T, M> fw: wrappers)
        {
            if(!fw.isEmpty())
                pq.add(fw);
        }

        return pq;
    }
}
