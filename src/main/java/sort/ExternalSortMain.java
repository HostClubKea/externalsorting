package sort;

import sort.parser.IntegerParser;

import java.io.File;
import java.io.IOException;

public class ExternalSortMain
{

    public static void main(final String[] args) throws IOException
    {
        if(args.length != 2){
            System.out.println("Please specify input and ouput file");
        }

        String inpuFilename = args[0];
        String outputFilename = args[1];

        ExternalSort<Integer, IntegerParser> externalSort = new ExternalSort<>(new IntegerParser());
        externalSort.sort(new File(inpuFilename), new File(outputFilename));

    }
}
