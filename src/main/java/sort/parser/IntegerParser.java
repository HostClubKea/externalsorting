package sort.parser;

public class IntegerParser implements Parser<Integer>
{
    @Override public Integer parse(String val)
    {
        if(val == null || val.isEmpty())
            return null;
        return Integer.parseInt(val);
    }
}
