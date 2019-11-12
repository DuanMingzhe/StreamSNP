public class SNPInfo
{
    public char allele;
    public int qual;
    public int dist;
    public boolean nqs;
    public int readLen;
    public SNPInfo(){}
    public SNPInfo(char allele,int qual,int dist,boolean nqs,int readLen)
    {
        this.allele=allele;
        this.qual=qual;
        this.dist=dist;
        this.nqs=nqs;
        this.readLen=readLen;
    }
}
