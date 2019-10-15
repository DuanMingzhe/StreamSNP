//存储ref上一个位点的信息
class RefUnit
{
    public int A_count;
    public int T_count;
    public int C_count;
    public int G_count;
    private char refBase;
    private int refPoint;
    public RefUnit(int refPoint,char refBase,char readb)
    {
        this.refPoint=refPoint;
        this.refBase = refBase;
        this.A_count = this.T_count = this.C_count = this.G_count = 0;
        this.addBase(readb);
    }
    public void addBase(char base)
    {
        switch (base) {
            case 'A':
                this.A_count++;
                break;
            case 'T':
                this.T_count++;
                break;
            case 'C':
                this.C_count++;
                break;
            case 'G':
                this.G_count++;
                break;
        }
    }

    public String summary()
    {
        double totalCount=this.A_count+this.T_count+this.C_count+this.G_count;
        double ARatio=this.A_count/totalCount;
        double TRatio=this.T_count/totalCount;
        double CRatio=this.C_count/totalCount;
        double GRatio=this.G_count/totalCount;
        return this.checkSNP(totalCount,ARatio,TRatio,CRatio,GRatio);
    }

    public char getRefBase()
    {
        return this.refBase;
    }
    public int getRefPoint()
    {
        return this.refPoint;
    }
    public String toString()
    {
        return "refPoint="+this.refPoint
                +"  refBase="+this.refBase
                +"  A_count="+this.A_count
                +"  T_count="+this.T_count
                +"  C_count="+this.C_count
                +"  G_count="+this.G_count;
    }
    public String checkSNP(double totalCount,double ARadio,double TRadio,double CRadio,double GRadio)
    {
        switch (this.refBase)
        {
            case 'A':
            {
                if(TRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: T"+"  TRadio is: "+TRadio;
                }
                else if(CRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: C"+"  CRadio is: "+CRadio;
                }
                else if(GRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: G"+"  GRadio is: "+GRadio;
                }
                break;
            }
            case 'T':
            {
                if(ARadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: A"+"  ARadio is: "+ARadio;
                }
                else if(CRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: C"+"  CRadio is: "+CRadio;
                }
                else if(GRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: G"+"  GRadio is: "+GRadio;
                }
                break;
            }
            case 'C':
            {
                if(ARadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: A"+"  ARadio is: "+ARadio;
                }
                else if(TRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: T"+"  TRadio is: "+TRadio;
                }
                else if(GRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: G"+"  GRadio is: "+GRadio;
                }
                break;
            }
            case 'G':
            {
                if(ARadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: A"+"  TRadio is: "+ARadio;
                }
                else if(TRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: T"+"  TRadio is: "+TRadio;
                }
                else if(CRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  SNP is: C"+"  TRadio is: "+CRadio;
                }
                break;
            }
        }
        return null;
    }
}
