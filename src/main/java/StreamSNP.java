import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SAMRecord;
import javafx.util.Pair;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
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
    public static void main(String[] args) throws Exception
    {

        long startTime=System.currentTimeMillis();

        StreamSNP snp=new StreamSNP();
        final StringBuilder refCache=HandleFasta.getChrRegion("E:\\data\\human_g1k_v37.fasta","20");
        System.out.println("Finish read fasta");
        StreamExecutionEnvironment env=StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> text=env.socketTextStream("localhost",9000,"\n");
        DataStream<ExtractInfo> rowInfo=text.flatMap(new FlatMapFunction<String, ExtractInfo>()
        {
            public void flatMap(String s, Collector<ExtractInfo> collector) throws Exception
            {
                String[] array=s.split(" ");
                collector.collect(new ExtractInfo(Integer.parseInt(array[3]),array[5],array[9],array[10]));
            }
        });
        DataStream<RefUnit> refInfo=rowInfo.flatMap(new FlatMapFunction<ExtractInfo, RefUnit>()
        {
            public void flatMap(ExtractInfo extractInfo, Collector<RefUnit> collector) throws Exception
            {
                StringBuilder seq=new StringBuilder(extractInfo.seq);
                StringBuilder quality=new StringBuilder(extractInfo.rowQuality);
                HandleCigar hc=new HandleCigar(extractInfo.cigar,extractInfo.pos);
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
                        int readq=quality.charAt(readst)-33;
                        collector.collect(new RefUnit(refst,refb,readb,readq));
                    }
                }
            }
        }).keyBy("refPoint").timeWindow(Time.seconds(1)).reduce(new ReduceFunction<RefUnit>() {
        public RefUnit reduce(RefUnit u1, RefUnit u2) throws Exception
        {
            RefUnit temp=new RefUnit(u1,u2);
            return temp;
        }
    });
        DataStream<RefUnit> outputInfo=refInfo.filter(new FilterFunction<RefUnit>() {
            public boolean filter(RefUnit refUnit) throws Exception
            {
                String summary=refUnit.summary();
                if(summary!=null)
                    return true;
                else
                    return false;
            }
        }).setParallelism(1);
        outputInfo.writeAsText("C:\\Users\\Duan\\Desktop\\output.txt",FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        env.execute(" call SNP");






//        String fileName="E:\\data\\NA12878_chr20.bam";
//        File inputFile=new File(fileName);
//        SamReaderFactory readerfactory=SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
//        SamReader reader=readerfactory.open(inputFile);
//        int i=0;
//        for(SAMRecord samrecord:reader)
//        {
//
//            String rowSeq=new String(samrecord.getReadBases());
//            String cigar=samrecord.getCigarString();
//            String rowQuality=samrecord.getBaseQualityString();
//            int pos=samrecord.getAlignmentStart()-1;
//            snp.handleOneLine(refCache,rowSeq,rowQuality,cigar,pos);
//        }
//        try
//        {
//            reader.close();
//            snp.pw.close();
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }
        long endTime=System.currentTimeMillis();
        long usedTime=endTime-startTime;
        System.out.println("Execute Time is: "+usedTime);
    }

//    public void handleOneLine(StringBuilder refCache,String rowSeq,String rowQuality,String cigar,int pos)
//    {
//
//        this.updateMap(pos);
//        StringBuilder seq=new StringBuilder(rowSeq);
//        StringBuilder quality=new StringBuilder(rowQuality);
//        HandleCigar hc=new HandleCigar(cigar,pos);
//        List<Pair<Integer,Integer>> mapReadPoint=hc.extractReadMapping();
//        List<Pair<Integer, Integer>> mapRefPoint=hc.extractRefMapping();
//        for(int i=0;i<mapRefPoint.size();i++)
//        {
//            int refst=mapRefPoint.get(i).getKey();
//            int refed=mapRefPoint.get(i).getValue();
//            int readst=mapReadPoint.get(i).getKey();
//            for(;refst<=refed;refst++,readst++)
//            {
//                char refb=refCache.charAt(refst);
//                char readb=seq.charAt(readst);
//                int readq=quality.charAt(readst)-33;
//                if(this.map.containsKey(refst))
//                {
//                    this.map.get(refst).addBase(readb,readq);
//                }
//                else
//                {
//                    this.map.put(refst,new RefUnit(refst,refb,readb,readq));
//                }
//            }
//        }
//    }
//
//    public void updateMap(int posStart)
//    {
//        Iterator<Map.Entry<Integer, RefUnit>> iterator = map.entrySet().iterator();
//        while(iterator.hasNext())
//        {
//            Map.Entry<Integer, RefUnit> entry = iterator.next();
//            int key=entry.getKey();
//            if(key<posStart)
//            {
//                String info=entry.getValue().summary();
//                if(info!=null)
//                {
//                    //System.out.println(info);
//                    this.pw.println(info);
//                }
//                iterator.remove();
//            }
//            else
//            {
//                break;
//            }
//        }
//    }


}
