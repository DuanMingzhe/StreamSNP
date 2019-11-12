public class ExtractInfo
{
    public String readName;
    public int flag;
    public String ref;
    public int pos;
    public int mapq;
    public String cigar;
    public String rowSeq;
    public String rowQuality;
    public String text;
    public ExtractInfo(){}
    public ExtractInfo(String readName,int flag,String ref,int pos,int mapq,String cigar,
                       String rowSeq,String rowQuality,String text)
    {
        this.readName=readName;
        this.flag=flag;
        this.ref=ref;
        this.pos=pos;
        this.mapq=mapq;
        this.cigar=cigar;
        this.rowSeq=rowSeq;
        this.rowQuality=rowQuality;
        this.text=text;
    }
    public String toString()
    {
        return this.readName+" "+this.flag+" "+this.ref+" "+this.pos+" "+this.mapq+" "+this.cigar+" "+this.rowSeq+" "+this.rowQuality+" "+this.text+"\n";
    }
}
