import org.apache.flink.runtime.messages.RegistrationMessages;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

//存储ref上一个位点的信息
public class RefUnit
{
    public int A_count;
    public int T_count;
    public int C_count;
    public int G_count;
    public char refBase;
    public int refPoint;

    public List<Integer> AQuality;
    public List<Integer> TQuality;
    public List<Integer> CQuality;
    public List<Integer> GQuality;

    public RefUnit(){}
    public RefUnit(int refPoint,char refBase,char readb,int readq)
    {
        this.AQuality=new ArrayList<Integer>();
        this.TQuality=new ArrayList<Integer>();
        this.CQuality=new ArrayList<Integer>();
        this.GQuality=new ArrayList<Integer>();

        this.refPoint=refPoint;
        this.refBase = refBase;
        this.A_count = this.T_count = this.C_count = this.G_count = 0;
        this.addBase(readb,readq);
    }
    public RefUnit(RefUnit a,RefUnit b)
    {
        this.refPoint=a.refPoint;
        this.refBase=a.refBase;
        this.A_count=a.A_count+b.A_count;
        this.T_count=a.T_count+b.T_count;
        this.C_count=a.C_count+b.C_count;
        this.G_count=a.G_count+b.G_count;

        this.AQuality=new ArrayList<Integer>();
        this.TQuality=new ArrayList<Integer>();
        this.CQuality=new ArrayList<Integer>();
        this.GQuality=new ArrayList<Integer>();

        this.AQuality.addAll(a.AQuality);
        this.AQuality.addAll(b.AQuality);
        this.TQuality.addAll(a.TQuality);
        this.TQuality.addAll(b.TQuality);
        this.CQuality.addAll(a.CQuality);
        this.CQuality.addAll(b.CQuality);
        this.GQuality.addAll(a.GQuality);
        this.GQuality.addAll(b.GQuality);
    }
    public void addBase(char base,int readq)
    {
//        if(readq<13)
//            return;

        switch (base) {
            case 'A':
                this.A_count++;
                this.AQuality.add(readq);
                break;
            case 'T':
                this.T_count++;
                this.TQuality.add(readq);
                break;
            case 'C':
                this.C_count++;
                this.CQuality.add(readq);
                break;
            case 'G':
                this.G_count++;
                this.GQuality.add(readq);
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
                +"  G_count="+this.G_count+"\n";
    }
    public String checkSNP(double totalCount,double ARadio,double TRadio,double CRadio,double GRadio)
    {

        double averageQ=0;
        for(int i=0;i<this.AQuality.size();i++)
            averageQ+=this.AQuality.get(i);
        for(int i=0;i<this.TQuality.size();i++)
            averageQ+=this.TQuality.get(i);
        for(int i=0;i<this.CQuality.size();i++)
            averageQ+=this.CQuality.get(i);
        for(int i=0;i<this.GQuality.size();i++)
            averageQ+=this.GQuality.get(i);
        averageQ=averageQ/(this.AQuality.size()+this.TQuality.size()+this.CQuality.size()+this.GQuality.size());


        switch (this.refBase)
        {
            case 'A':
            {
                if(TRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: T"+"  TRadio is: "+TRadio;
                }
                else if(CRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: C"+"  CRadio is: "+CRadio;
                }
                else if(GRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: G"+"  GRadio is: "+GRadio;
                }
                break;
            }
            case 'T':
            {
                if(ARadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: A"+"  ARadio is: "+ARadio;
                }
                else if(CRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: C"+"  CRadio is: "+CRadio;
                }
                else if(GRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: G"+"  GRadio is: "+GRadio;
                }
                break;
            }
            case 'C':
            {
                if(ARadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: A"+"  ARadio is: "+ARadio;
                }
                else if(TRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: T"+"  TRadio is: "+TRadio;
                }
                else if(GRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: G"+"  GRadio is: "+GRadio;
                }
                break;
            }
            case 'G':
            {
                if(ARadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: A"+"  TRadio is: "+ARadio;
                }
                else if(TRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: T"+"  TRadio is: "+TRadio;
                }
                else if(CRadio>=0.2)
                {
                    return "refPoint is: "+this.refPoint+"  refBase is: "+refBase+"  totalCount is: "+totalCount+"  Quality is: "+averageQ+"  SNP is: C"+"  TRadio is: "+CRadio;
                }
                break;
            }
        }
        return null;
    }
}
