package sort;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sort.parser.IntegerParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ExternalSortTests
{

    private static final String tempDir = "./tmp";

    Integer[] file1Content = new Integer[]{ 40 , 5, 8, 12, 11, 9, 10000, 200};

    Integer[] file2Content = new Integer[]{ 1, 7, 13, 9, 6, 600, 20000, 5};

    Integer[] file1SortedContent = new Integer[]{ 5, 8, 9 , 11, 12, 40, 200, 10000};

    Integer[] file2SortedContent = new Integer[]{ 1, 5, 6, 7, 9, 13, 600, 20000};

    Integer[] fileSortedContent = new Integer[]{ 1, 5, 5, 6, 7, 8, 9, 9, 11, 12, 13, 40, 200, 600, 10000, 20000};

    @BeforeAll
    public static void  before() throws IOException
    {
        Files.createDirectories(Paths.get(tempDir));
    }

    @AfterAll
    public static void cleanUp()
    {
        try
        {
            FileUtils.deleteDirectory(Paths.get(tempDir).toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    @BeforeEach
    public void clean(){

        try
        {
            FileUtils.cleanDirectory(new File(tempDir));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    @Test
    public void shouldSplitIn2SortedFiles_whenLimit8AndDataLengthIs16() throws IllegalAccessException, IOException, InstantiationException
    {
        Integer[] combined = Stream.concat(Arrays.stream(file1Content), Arrays.stream(file2Content))
            .toArray(Integer[]::new);

        Path fileIn = Paths.get(tempDir, "file_in.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");
        writeFIle(combined, fileIn);

        ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser(), 8);
        List<File> filesOut = externalSort.splitAndSort(fileIn.toFile());

        assertEquals(2, filesOut.size());

        Integer[] outputContent1 = readFile(filesOut.get(0).toPath());
        Integer[] outputContent2 = readFile(filesOut.get(1).toPath());
        assertArrayEquals( file1SortedContent, outputContent1);
        assertArrayEquals( file2SortedContent, outputContent2);
    }

    @Test
    public void shouldSort100000Int_400KBTextFile() throws IOException, InstantiationException, IllegalAccessException
    {
        Path fileIn = Paths.get(tempDir, "file_in.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");
        Integer[] counters = writeFile(fileIn, 42, 100000L, 200);

        ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser(), 10000);
        externalSort.sort(fileIn.toFile(), fileOut.toFile());

        Integer[] countersSorted = readFile(fileOut, 200);
        assertArrayEquals( counters, countersSorted);
    }

    @Test
    public void shouldSort10000000Int_50MBTextFile() throws IOException, InstantiationException, IllegalAccessException
    {
        Path fileIn = Paths.get(tempDir, "file_in.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");
        Integer[] counters = writeFile(fileIn, 42, 10000000L, 2000);

        ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser(), 100000);
        externalSort.sort(fileIn.toFile(), fileOut.toFile());

        Integer[] countersSorted = readFile(fileOut, 2000);
        assertArrayEquals( counters, countersSorted);
    }

//    @Test
//    public void shouldSortHugeFile_500MBTextFile() throws IOException, InstantiationException, IllegalAccessException
//    {
//        Path fileIn = Paths.get(tempDir, "file_in.txt");
//        Path fileOut = Paths.get(tempDir, "file_out.txt");
//        Integer[] counters = writeFile(fileIn, 42, 100000000L, 2000);
//
//        ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser(), 1000000);
//        externalSort.sort(fileIn.toFile(), fileOut.toFile());
//
//        Integer[] countersSorted = readFile(fileOut, 2000);
//        assertArrayEquals( counters, countersSorted);
//    }

    @Test
    public void shouldSortFile_whenUnsortedProvided() throws IllegalAccessException, IOException, InstantiationException
    {
        Integer[] combined = Stream.concat(Arrays.stream(file1Content), Arrays.stream(file2Content))
            .toArray(Integer[]::new);

        Path fileIn = Paths.get(tempDir, "file_in.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");
        writeFIle(combined, fileIn);

        ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser());
        externalSort.sort(fileIn.toFile(), fileOut.toFile());
       // ExternalSort_.sort(fileIn.toFile(), fileOut.toFile());


        Integer[] outputContent = readFile(fileOut);
        assertArrayEquals( fileSortedContent, outputContent);
    }

    @Test
    public void shouldGetSameContent_whenMergeOneFile() {
        Path fileIn = Paths.get(tempDir, "file_in.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");

        writeFIle(file1Content, fileIn);

        List<File> chunks = new ArrayList<>();
        chunks.add(fileIn.toFile());

        try
        {
            ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser());
            externalSort.merge(chunks, fileOut.toFile());
        }
        catch (IOException e)
        {
            fail( e.getMessage());
        }


        Integer[] outputContent = readFile(fileOut);
        assertArrayEquals( file1Content, outputContent);
    }

    @Test
    public void shouldGetSameContent_whenMergeTwoFileOneOfWhichIsempty() {
        Path fileIn1 = Paths.get(tempDir, "file_in1.txt");
        Path fileIn2 = Paths.get(tempDir, "file_in2.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");

        writeFIle(file1Content, fileIn1);
        writeFIle(new Integer[0], fileIn2);

        List<File> chunks = new ArrayList<>();
        chunks.add(fileIn1.toFile());
        chunks.add(fileIn2.toFile());

        try
        {
            ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser());
            externalSort.merge(chunks, fileOut.toFile());
        }
        catch (IOException e)
        {
            fail( e.getMessage());
        }


        Integer[] outputContent = readFile(fileOut);
        assertArrayEquals( file1Content, outputContent);
    }

    @Test
    public void shouldGetSortedContent_whenMergeTwoSortedFiles()
    {
        Path fileIn1 = Paths.get(tempDir, "file_in1.txt");
        Path fileIn2 = Paths.get(tempDir, "file_in2.txt");
        Path fileOut = Paths.get(tempDir, "file_out.txt");

        writeFIle(file1SortedContent, fileIn1);
        writeFIle(file2SortedContent, fileIn2);

        List<File> chunks = new ArrayList<>();
        chunks.add(fileIn1.toFile());
        chunks.add(fileIn2.toFile());

        try
        {
            ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser());
            externalSort.merge(chunks, fileOut.toFile());
        }
        catch (IOException e)
        {
            fail( e.getMessage());
        }

        Integer[] outputContent = readFile(fileOut);
        assertArrayEquals( fileSortedContent, outputContent);
    }


    private Integer[] writeFile(Path file, long seed, long numberOfIntegers, int maxInt) throws IOException
    {
        Random random = new Random(seed);
        Integer[] counters = new Integer[maxInt];
        Arrays.fill(counters, 0);

        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(file.toFile()), Charset.forName("UTF-8"))))
        {
            for (long i = 0; i < numberOfIntegers; i++)
            {
                int nextVal = random.nextInt(maxInt);
                bufferedWriter.write(String.valueOf(nextVal));
                bufferedWriter.newLine();
                counters[nextVal]++;
            }
        }
        return counters;
    }

    private Integer[] readFile(Path file, int maxInt) throws IOException
    {
        Integer[] counters = new Integer[maxInt];
        Arrays.fill(counters, 0);

        AtomicInteger previous = new AtomicInteger();

        try(BufferedReader br = new BufferedReader(new FileReader(file.toFile()))){
            br.lines().forEach(s -> {
                int val = Integer.parseInt(s);
                if(previous.get() <= val)
                {
                    previous.set(val);
                    counters[val]++;
                }
            });
        }
        return counters;
    }


    private void writeFIle(Integer[] array, Path file){

        try
        {
            Files.write(file, Arrays.asList(array).stream().map(i -> i.toString()).collect(Collectors.toList()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Can't write to a file");
        }

    }

    private Integer[] readFile(Path file) {
        try(Stream<String> stream = Files.lines(file))
        {
            return stream.map(s -> Integer.parseInt(s)).toArray(Integer[]::new);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Can't read from a file");
        }
        return null;
    }



}
