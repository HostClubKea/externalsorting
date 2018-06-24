package sort;

import sort.utils.FileWrapper;
import sort.parser.Parser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ExternalSort<T extends Comparable<T>, M extends Parser<T>>
{
    private static final String TMP_FILE_FORMAT = "temp_out_%s.tmp";

    private static int IN_MEMORY_ARRAY_SIZE = 100000;
    Random rnd = new Random();

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
        cleanTempFiles(files);

    }

    /**
     * Reads part of the file in the buffer, sort and writes to temp files
     * **/
    public List<File> splitAndSort(File input) throws IOException
    {
        List<File> files = new ArrayList<>();

        List<T> buffer = new ArrayList<>(bufferSize);

        FileWrapper<T, M> fw = new FileWrapper(input, parser);

        while(!fw.isEmpty()){
            readFilePartToBuffer(fw, buffer);

            Collections.sort(buffer);

            File sortedPart = writeFilePartFromBuffer(input.getParentFile(), buffer);
            files.add(sortedPart);
        }

        fw.close();

        return files;
    }

    /**
     * Merge sorted parts into bigger file
     * */
    public void merge(List<File> files, File output) throws IOException
    {
        List<FileWrapper<T, M>> wrappers = buildWrappers(files);

        PriorityQueue<FileWrapper<T, M>> pq = buildPriorityQueue(wrappers);

        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(output), Charset.forName("UTF-8"))))
        {
            while (!pq.isEmpty())
                writeNextLine(pq, bufferedWriter);

        }

        cleanWrappers(wrappers);
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


    private File writeFilePartFromBuffer(File folder, List<T> buffer) throws IOException
    {
        File file = getNewTempFile(folder);

        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(file), Charset.forName("UTF-8"))))
        {
            for (T val: buffer )
            {
                bufferedWriter.write(val.toString());
                bufferedWriter.newLine();
            }
        }
        return file;
    }


    private File getNewTempFile(File folder){
        File file = null;
        while(file == null || file.exists())
        {
            file = new File(folder, String.format(TMP_FILE_FORMAT, rnd.nextLong()));
        }
        return file;
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

    private void cleanTempFiles(List<File> files){
        files.forEach(f -> f.delete());
    }
}
