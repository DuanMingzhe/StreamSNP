import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;


//从CIGAR里找出M对应的区域(read,ref)
public class HandleCigar
{
    public int[] digit;
    public char[] tag;
    public int tagLen;
    public String cigar;
    public int startPos;
    public HandleCigar(){}
    public HandleCigar(String cigar, int pos)
    {
        this.cigar=cigar;
        String[] rawDigit=cigar.split("\\D");
        int tagAmount=rawDigit.length;
        this.digit=new int[tagAmount];
        this.tag=new char[tagAmount];
        for(int i=0;i<tagAmount;i++)
        {
            this.digit[i]=Integer.parseInt(rawDigit[i]);
        }
        int j=0;
        for(int i=0;i<cigar.length();i++)
        {
            char c=cigar.charAt(i);
            if(c>='A'&& c <='Z')
            {
                this.tag[j]=c;
                j++;
            }
        }
        this.tagLen=tagAmount;
        this.startPos=pos;
    }
    public List<Pair<Integer,Integer>> extractReadMapping()
    {
        List<Pair<Integer,Integer>> readMapPoint=new ArrayList<Pair<Integer,Integer>>();
        int zeroPoint=0;
        for(int i=0;i<this.tagLen;i++)
        {
            if(this.tag[i]=='M')
            {
                readMapPoint.add(new Pair<Integer, Integer>(zeroPoint,zeroPoint+this.digit[i]-1));
                zeroPoint=zeroPoint+this.digit[i];
            }
            else if(this.tag[i]=='S'||this.tag[i]=='I')
            {
                zeroPoint=zeroPoint+this.digit[i];
            }
        }
        return readMapPoint;


    }
    public List<Pair<Integer, Integer>> extractRefMapping()
    {
        List<Pair<Integer, Integer>> refMapPoint=new ArrayList<Pair<Integer, Integer>>();
        for(int i=0;i<this.tagLen;i++)
        {
            if(this.tag[i]=='M')
            {
                refMapPoint.add(new Pair<Integer, Integer>(this.startPos,this.startPos+this.digit[i]-1));
                this.startPos+=this.digit[i];
            }
            else if(this.tag[i]=='D')
            {
                this.startPos+=this.digit[i];
            }
        }
        return refMapPoint;
    }
}
