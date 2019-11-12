import org.codehaus.jackson.map.MapperConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BaseUnit
{
    public char refBase;
    public int refPoint;
    public int Acount=0;
    public int Tcount=0;
    public int Ccount=0;
    public int Gcount=0;
    public int Delcount=0;
    public boolean isAllele=false;
    public List<SNPInfo> snpList;
    public double [] snpPrior={0.0000001, 0.0000001, 0.0000001, 0.0000001, 0.0000001, 0.0000001, 0.0000001, 0.0000001, 0.027, 0.973};
    public double [] errPrior={0.991, 0.004, 0.002, 0.001, 0.001, 0.001, 0.000002, 0.000005, 0.0000001, 0.000001};
    public double priorErrorC=0.1;
    public double priorSNPC=0.9;
    public BaseUnit(){}
    public BaseUnit(char refBase,int refPoint)
    {
        this.refBase=refBase;
        this.refPoint=refPoint;
        switch (refBase)
        {
            case('A'):{this.Acount++;break;}
            case('T'):{this.Tcount++;break;}
            case('C'):{this.Ccount++;break;}
            case('G'):{this.Gcount++;break;}
        }
        this.snpList=new ArrayList<SNPInfo>();

    }
    public BaseUnit(char refBase,int refPoint,boolean flag)
    {
        this.refBase=refBase;
        this.refPoint=refPoint;
        this.snpList=new ArrayList<SNPInfo>();
        this.Delcount++;
    }
    public BaseUnit(char refBase,int refPoint,char readBase,int qual,int dist_3,boolean nqs,int readLen)
    {
        this.refBase=refBase;
        this.refPoint=refPoint;
        this.snpList=new ArrayList<SNPInfo>();
        switch (readBase)
        {
            case('A'):{this.Acount++;break;}
            case('T'):{this.Tcount++;break;}
            case('C'):{this.Ccount++;break;}
            case('G'):{this.Gcount++;break;}
        }
        this.snpList.add(new SNPInfo(readBase,qual,dist_3,nqs,readLen));
        this.isAllele=true;
    }
    public double getStatistic(char varBase)
    {
        double prErrorj=1;
        for(int i=0;i<snpList.size();i++)
        {
            if(this.snpList.get(i).allele==varBase)
            {
                double qual=this.snpList.get(i).qual-33;
                int dist=this.snpList.get(i).dist;
                int readLen=this.snpList.get(i).readLen;
                boolean nqs=this.snpList.get(i).nqs;
                double relpos=(double)dist/readLen;
                double predict=-9.088+(0.162)*qual+1.645*getInt(nqs)+2.349*relpos;
                double prSNPi=Math.exp(predict)/(1+Math.exp(predict));
                double prErrori=1-prSNPi;
                prErrorj*=prErrori;
//                if(this.refPoint==61131)
//                {
//                    System.out.println(this.refPoint+"  "+qual+"  "+nqs+"  "+relpos+"  "+refBase+" "+predict+"  "+prErrorj);
//                }
            }
        }
        BigDecimal bd1=new BigDecimal(1);
        BigDecimal bd2=new BigDecimal(prErrorj);
        BigDecimal prSNPj=bd1.subtract(bd2);
        BigDecimal bd3=new BigDecimal(10);
        double tmpBin=Math.floor(prSNPj.multiply(bd3).doubleValue());
        int bin=(int)tmpBin;
        if(bin==10)
            bin=9;
        double prSjErrorC=errPrior[bin];
        double prSjSNPC=snpPrior[bin];
        double errorPosterior=prSjErrorC*this.priorErrorC;
        double snpPosterior=prSjSNPC*this.priorSNPC;
        double prSNPSjCJ=1.0/(1+errorPosterior/snpPosterior);
        return prSNPSjCJ;
    }
    public int getInt(boolean nqs)
    {
        if(nqs)
            return 1;
        else
            return 0;
    }
    public int getConv(char varBase)
    {
        switch (varBase)
        {
            case 'A':{return this.Acount;}
            case 'T':{return this.Tcount;}
            case 'C':{return this.Ccount;}
            case 'G':{return this.Gcount;}
        }
        return 0;
    }
    public char getVarBase()
    {
        if(this.refBase=='A')
        {
            if(this.Tcount>=this.Ccount&&this.Tcount>=this.Gcount)
            {
                return 'T';
            }
            else if(this.Ccount>=this.Tcount&&this.Ccount>=this.Gcount)
            {
                return 'C';
            }
            else if(this.Gcount>=this.Tcount&&this.Gcount>=this.Ccount)
            {
                return 'G';
            }
        }
        else if(this.refBase=='T')
        {
            if(this.Acount>=this.Ccount&&this.Acount>=this.Gcount)
            {
                return 'A';
            }
            else if(this.Ccount>=this.Tcount&&this.Ccount>=this.Gcount)
            {
                return 'C';
            }
            else if(this.Gcount>=this.Tcount&&this.Gcount>=this.Ccount)
            {
                return 'G';
            }
        }
        else if(this.refBase=='C')
        {
            if(this.Acount>=this.Tcount&&this.Acount>=this.Gcount)
            {
                return 'A';
            }
            else if(this.Tcount>=this.Acount&&this.Tcount>=this.Gcount)
            {
                return 'T';
            }
            else if(this.Gcount>=this.Acount&&this.Gcount>=this.Tcount)
            {
                return 'G';
            }
        }
        else if(this.refBase=='G')
        {
            if(this.Acount>=this.Tcount&&this.Acount>=this.Ccount)
            {
                return 'A';
            }
            else if(this.Tcount>=this.Acount&&this.Tcount>=this.Ccount)
            {
                return 'T';
            }
            else if(this.Ccount>=this.Acount&&this.Ccount>=this.Tcount)
            {
                return 'C';
            }
        }
        return '*';
    }
}
