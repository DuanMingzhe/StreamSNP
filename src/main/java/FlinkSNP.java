import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlinkSNP
{
    public static double maxSub=5.0;
    public static double maxIndel=5.0;

    public static void main(String[] args) throws Exception
    {
        int sum=0;
        System.out.println("Start read char20");
        final StringBuilder refCache=HandleFasta.getChrRegion("E:\\data\\human_g1k_v37.fasta","20");
        System.out.println("Finish read char20");
        StreamExecutionEnvironment env=StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        DataStream<String> text=env.socketTextStream("localhost",9000,"\n");
        DataStream<vcfCheckInfo> filterVcfInfo=text.flatMap(new FlatMapFunction<String, ExtractInfo>()
        {
            public void flatMap(String s, Collector<ExtractInfo> collector) throws Exception
            {
                String[] array = s.split("\\s+");
                if(array.length>=11)
                {
                    String readName = array[0];
                    int flag = Integer.valueOf(array[1]);
                    String ref = array[2];
                    int pos = Integer.valueOf(array[3]);
                    int mapq = Integer.valueOf(array[4]);
                    String cigar = array[5];
                    String rowSeq = array[9];
                    String rowQuality = array[10];
                    String text = s;
                    collector.collect(new ExtractInfo(readName, flag, ref, pos, mapq, cigar, rowSeq, rowQuality, text));
                }
            }
        }).setParallelism(1).filter(new FilterFunction<ExtractInfo>()
        {
            public boolean filter(ExtractInfo extractInfo) throws Exception
            {
                Pattern pattern=Pattern.compile("NM:i:(\\d+)");
                Matcher m=pattern.matcher(extractInfo.text);
                int NMInfo=0;
                if(m.find())
                {
                    NMInfo=Integer.parseInt(m.group(1));
                }
                else
                {
                    return false;
                }
                if(extractInfo.rowSeq.length()!=extractInfo.rowQuality.length())
                {
                    return false;
                }
                if((!extractInfo.cigar.contains("M"))&&(!extractInfo.cigar.contains("I"))&&(!extractInfo.cigar.contains("D"))&&(!extractInfo.cigar.contains("S")))
                {
                    return false;
                }
                if(  (extractInfo.flag & 1024) >0 || (extractInfo.flag & 512) >0 || (extractInfo.flag & 256) >0 ||(extractInfo.flag & 4) >0)
                {
                    return false;
                }
                if(extractInfo.mapq<1)
                {
                    return false;
                }
                int queryLen=extractInfo.rowSeq.length();
                int num_snp=0,num_ins=0,num_del=0,num_clip=0;
                int gap=0,num_sub=0;
                double sub_perc=0,gap_perc=0;
                HandleCigar hc=new HandleCigar(extractInfo.cigar,extractInfo.pos);
                for(int i=0;i<hc.tag.length;i++)
                {
                    int num=hc.digit[i];
                    char c=hc.tag[i];
                    if(c=='I')
                        num_ins+=num;
                    else if(c=='D')
                        num_del+=num;
                    else if(c=='S')
                        num_clip+=num;
                }
                gap=num_ins+num_del;
                num_sub=NMInfo-gap;
                sub_perc=Math.round(Double.valueOf(num_sub)/(queryLen-num_clip)*10000)/100.0;
                gap_perc=Math.round(Double.valueOf(gap)/(queryLen-num_clip)*10000)/100.0;
                //超过最大阈值，直接返回
                if((sub_perc>FlinkSNP.maxSub) || (gap_perc>FlinkSNP.maxIndel))
                {
                    return false;
                }
                return true;

            }
        }).setParallelism(1).flatMap(new FlatMapFunction<ExtractInfo, BaseUnit>()
        {
            public void flatMap(ExtractInfo extractInfo, Collector<BaseUnit> collector) throws Exception
            {
                char dir;
                int queryLen=extractInfo.rowSeq.length();
                if((extractInfo.flag&16)>0)
                {
                    dir='-';
                }
                else
                {
                    dir='+';
                }
                int tPlace=extractInfo.pos,qPlace=1,span=0,cigarQueryLength=0;
                HandleCigar hc=new HandleCigar(extractInfo.cigar,extractInfo.pos);
                for(int i=0;i<hc.tag.length;i++)
                {
                    int num=hc.digit[i];
                    char c=hc.tag[i];
                    if(c=='M')
                    {
                        for(int j=0;j<num;j++)
                        {
                            char refBase=refCache.charAt(tPlace-1);
                            char queryBase=extractInfo.rowSeq.charAt(qPlace-1);
                            if(refBase==queryBase)
                            {
                                collector.collect(new BaseUnit(refBase,tPlace));
                            }
                            else
                            {
                                int qual=extractInfo.rowQuality.charAt(qPlace-1);
                                int dist_3=0;
                                if(dir=='-')
                                {
                                    dist_3=qPlace-1;
                                }
                                else
                                {
                                    dist_3=queryLen-qPlace;
                                }
                                boolean nqs=FlinkSNP.calculateNQS(qPlace-1,dist_3,extractInfo.rowQuality,queryLen);
                                collector.collect(new BaseUnit(refBase,tPlace,queryBase,qual,dist_3,nqs,queryLen));
                            }
                            tPlace++;
                            qPlace++;
                        }
                    }
                    else if(c=='I')
                    {
                        qPlace+=num;
                    }
                    else if(c=='D')
                    {
                        for(int a=0;a<num;a++)
                        {
                            char refBase=refCache.charAt(tPlace-1);
                            collector.collect(new BaseUnit(refBase,tPlace,true));
                            tPlace++;
                        }
                    }
                    else if(c=='S')
                    {
                        qPlace+=num;
                    }
                }

            }
        }).setParallelism(1).keyBy("refPoint").timeWindow(Time.seconds(10),Time.seconds(10)).reduce(new ReduceFunction<BaseUnit>()
        {
            public BaseUnit reduce(BaseUnit t1, BaseUnit t2) throws Exception
            {

                BaseUnit temp=t1;
                temp.Acount+=t2.Acount;
                temp.Tcount+=t2.Tcount;
                temp.Ccount+=t2.Ccount;
                temp.Gcount+=t2.Gcount;
                temp.snpList.addAll(t2.snpList);
                temp.isAllele=temp.isAllele||t2.isAllele;
                return temp;
            }
        }).setParallelism(1).filter(new FilterFunction<BaseUnit>()
        {
            public boolean filter(BaseUnit baseUnit) throws Exception
            {
                if(baseUnit.isAllele)
                {
                    //System.out.println(baseUnit.refPoint+" "+baseUnit.refBase+" "+baseUnit.Acount+" "+baseUnit.Tcount+" "+baseUnit.Ccount+" "+baseUnit.Gcount);
                    return true;
                }
                else
                    return false;
            }
        }).setParallelism(1).flatMap(new FlatMapFunction<BaseUnit, vcfCheckInfo>()
         {
             public void flatMap(BaseUnit baseUnit, Collector<vcfCheckInfo> collector) throws Exception
             {
                 char varBase=baseUnit.getVarBase();
                 int varConv=baseUnit.getConv(varBase);
                 int refBaseConv=baseUnit.getConv(baseUnit.refBase);
                 double prSNPSjCJ=baseUnit.getStatistic(varBase);
                 //System.out.println(baseUnit.refPoint+" "+baseUnit.refBase+" "+varBase+" "+varConv+" "+refBaseConv+" "+baseUnit.snpList.size());
                 collector.collect(new vcfCheckInfo(baseUnit.refPoint,baseUnit.refBase,varBase,varConv,refBaseConv,prSNPSjCJ));

             }
         }).setParallelism(1).filter(new FilterFunction<vcfCheckInfo>()
        {
            public boolean filter(vcfCheckInfo vcfcheckInfo) throws Exception
            {
                double prSNPSjCJ=vcfcheckInfo.prSNPSjCJ;
                int refBaseCov=vcfcheckInfo.refBaseConv;
                int varCov=vcfcheckInfo.varConv;
                //System.out.println(vcfcheckInfo.pos+"  "+vcfcheckInfo.refBase+" "+vcfcheckInfo.varBase+" "+vcfcheckInfo.snpQual+"  "+vcfcheckInfo.varConv+" "+vcfcheckInfo.refBaseConv+" "+prSNPSjCJ);
                if(prSNPSjCJ<0.109)
                {
                    return false;
                }
                double n=refBaseCov+varCov;
                String filter="";
                double snpQual=0;
                String genotype;
                if(prSNPSjCJ<0.95)
                {
                    filter+="low_snpqual;";
                }
                if(n>1024)
                {
                    filter+="high_coverage;";
                }
                if(n<6)
                {
                    filter+="low_coverage;";
                }
                if(varCov<3 && varCov>0)
                {
                    filter+="low_VariantReads";
                }
                if(varCov>0)
                {
                    snpQual=Math.round(-10*Math.log10(1-prSNPSjCJ+0.000001));
                }
                if(n==0)
                {
                    genotype=".";
                    filter="No_data";
                }
                else
                {
                    double tmpQual=varCov/n;
                    if(tmpQual<=0.1)
                    {
                        genotype="0/0";
                        filter+="low_VariantRatio";
                    }
                    else if(tmpQual>0.1 &&tmpQual<0.9)
                    {
                        genotype="0/1";
                    }
                    else
                    {
                        genotype="1/1";
                    }
                }
                if(n>0&&varCov==0)
                {
                    filter="No_var";
                }
                if(filter=="")
                {
                    filter="PASS";
                }
                else
                {
                    return false;
                }
                vcfcheckInfo.genotype=genotype;
                vcfcheckInfo.snpQual=snpQual;
                return true;
            }
        }).setParallelism(1);

        filterVcfInfo.writeAsText("C:\\Users\\Duan\\Desktop\\flink_output.txt",FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        env.execute();
    }
    public static boolean calculateNQS(int pos,int dist_3,String rowQuality,int queryLen)
    {
        boolean pStatus=false;
        if(queryLen>10)
        {
            if(dist_3>queryLen-6)
            {
                pStatus=true;
            }
            else if(pos>=5 && queryLen-pos>6
                    && rowQuality.charAt(pos)>=53
                    && rowQuality.charAt(pos-1)>=48
                    && rowQuality.charAt(pos-2)>=48
                    && rowQuality.charAt(pos-3)>=48
                    && rowQuality.charAt(pos-4)>=48
                    && rowQuality.charAt(pos-5)>=48
                    && rowQuality.charAt(pos+1)>=48
                    && rowQuality.charAt(pos+2)>=48
                    && rowQuality.charAt(pos+3)>=48
                    && rowQuality.charAt(pos+4)>=48
                    && rowQuality.charAt(pos+5)>=48)
            {
                pStatus=true;
            }
        }
        return pStatus;
    }
}
