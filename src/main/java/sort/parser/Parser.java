package sort.parser;

public interface Parser<T extends Comparable<T>>
{
    T parse(String val);
}
