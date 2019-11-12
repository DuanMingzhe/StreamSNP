public class vcfCheckInfo
{
    public int pos;
    public char refBase;
    public char varBase;
    public int varConv;
    public int refBaseConv;
    public double  prSNPSjCJ;
    public double snpQual=0;
    public String genotype="";
    public vcfCheckInfo(){}
    public vcfCheckInfo(int pos,char refBase,char varBase,int varConv,int refBaseConv,double prSNPSjCJ)
    {
        this.pos=pos;
        this.refBase=refBase;
        this.varBase=varBase;
        this.varConv=varConv;
        this.refBaseConv=refBaseConv;
        this.prSNPSjCJ=prSNPSjCJ;
    }
    public String toString()
    {
        return(pos+"\t"
                +"."+"\t"
                +refBase+"\t"
                +varBase+"\t"
                +snpQual+"\t"
                +"PASS\t"
                +"GT:VR:RR:DP:GQ"+"\t"
                +genotype+":"
                +varConv+":"
                +refBaseConv+":"
                +(refBaseConv+varConv)+":\r\n");
    }

}
