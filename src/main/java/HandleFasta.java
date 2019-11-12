import java.io.*;

public class HandleFasta
{
    //从fasta中提取相应的染色体序列
    public static StringBuilder getChrRegion(String refFile,String chr)
    {
        StringBuilder refCache=new StringBuilder();
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(refFile)));
            String tmp;
            while(   (tmp=br.readLine())!=null   )
            {
                if(tmp.charAt(0)=='>')
                {
                    String chrName=tmp.split(" ")[0];
                    chrName=chrName.substring(1,chrName.length());
                    if(chrName.equals(chr))
                    {
                        while(   (tmp=br.readLine())!=null   )
                        {
                            if(tmp.charAt(0)!='>')
                            {
                                refCache.append(tmp);
                            }
                            else
                            {
                                br.close();
                                return refCache;
                            }
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return refCache;
    }
}
