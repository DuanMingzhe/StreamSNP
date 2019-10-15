import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SAMRecord;

import javafx.util.Pair;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StreamSNP
{
    public  Map<Integer,RefUnit> map;
    public PrintWriter pw;
    public StreamSNP()
    {
        this.map=new HashMap<Integer, RefUnit>();
        try
        {
            this.pw = new PrintWriter(new FileWriter("C:\\Users\\Duan\\Desktop\\output.txt"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {

        long startTime=System.currentTimeMillis();

        StreamSNP snp=new StreamSNP();
        StringBuilder refCache=HandleFasta.getChrRegion("E:\\data\\GRCh38_full_analysis_set_plus_decoy_hla.fa","chr20");
        String fileName="E:\\data\\DNA_ILM_LCL5_1_20170403_chr20.bam";
        File inputFile=new File(fileName);
        SamReaderFactory readerfactory=SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
        SamReader reader=readerfactory.open(inputFile);
        int i=0;
        for(SAMRecord samrecord:reader)
        {
//            if(i==100)
//                break;
            String rowSeq=new String(samrecord.getReadBases());
            String cigar=samrecord.getCigarString();
            int pos=samrecord.getAlignmentStart()-1;
            snp.handleOneLine(refCache,rowSeq,cigar,pos);
//            i=i+1;
        }
        try
        {
            reader.close();
            snp.pw.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        long endTime=System.currentTimeMillis();
        long usedTime=endTime-startTime;
        System.out.println("Execute Time is: "+usedTime);
    }

    public void handleOneLine(StringBuilder refCache,String rowSeq,String cigar,int pos)
    {

        this.updateMap(pos);
        StringBuilder seq=new StringBuilder(rowSeq);
        HandleCigar hc=new HandleCigar(cigar,pos);
        List<Pair<Integer,Integer>> mapReadPoint=hc.extractReadMapping();
        List<Pair<Integer, Integer>> mapRefPoint=hc.extractRefMapping();
        for(int i=0;i<mapRefPoint.size();i++)
        {
            int refst=mapRefPoint.get(i).getKey();
            int refed=mapRefPoint.get(i).getValue();
            int readst=mapReadPoint.get(i).getKey();
            for(;refst<=refed;refst++,readst++)
            {
                char refb=refCache.charAt(refst);
                char readb=seq.charAt(readst);
                if(this.map.containsKey(refst))
                {
                    this.map.get(refst).addBase(readb);
                }
                else
                {
                    this.map.put(refst,new RefUnit(refst,refb,readb));
                }
            }
        }
    }

    public void updateMap(int posStart)
    {
        Iterator<Map.Entry<Integer, RefUnit>> iterator = map.entrySet().iterator();
        while(iterator.hasNext())
        {
            Map.Entry<Integer, RefUnit> entry = iterator.next();
            int key=entry.getKey();
            if(key<posStart)
            {
                String info=entry.getValue().summary();
                if(info!=null)
                {
                    //System.out.println(info);
                    this.pw.println(info);
                }
                iterator.remove();
            }
            else
            {
                break;
            }
        }
    }


}
