package fst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import api.AlleleFreq;
import api.Population;

public class FstCal {
	
	public Set<String> getRefSNP(String refsnpf) {
		Set<String> refSnp = new HashSet<String>();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(refsnpf));
			String line;
			while((line=br.readLine())!=null){
				refSnp.add(line.trim());
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Read "+refSnp.size()+" ref SNP info from "+refsnpf);
		return refSnp;
	}

	public List<String[]> getGenos(String file) {
		System.out.println("Reading genotyping info from "+file);
		List<String[]> genos = new ArrayList<String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();// skip header
			while ((line = br.readLine()) != null) {
				//each row contains [rsid,A,B,H,U,A...]
				String[] cols = line.split("\\s+");
				String[] row = new String[cols.length - 4];
				row[0]=cols[0];// store rsid in the first column of row
				for (int i = 5; i < cols.length; i++)
					row[i - 4] = cols[i];
				genos.add(row);
			}

			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return genos;
	}

	public List<String[]> getGenos(String file,String refsnpf) {
		Set<String> refSnp = getRefSNP(refsnpf);
		System.out.println("Reading genotyping info from "+file);
		List<String[]> genos = new ArrayList<String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			br.readLine();// skip header
			while ((line = br.readLine()) != null) {
				//check if the snp in refSnp set
				String[] cols = line.split("\\s+");
				if(refSnp.contains(cols[0])){
					String[] row = new String[cols.length - 4];
					row[0]=cols[0];// store rsid in the first column of row
					for (int i = 5; i < cols.length; i++)
						row[i - 4] = cols[i];
					genos.add(row);
				}
			}

			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return genos;
	}

	public int[] getCounts(String[] genos) {
		// array to store the number of genotypes AA, AB, BB and missing which
		// denoted by A, H, B and U/N
		int[] counts = new int[4];
		// initialize all the counts to zero
		for (int i = 0; i < counts.length; i++)
			counts[i] = 0;
		// counts the number of each genotype
		for (String geno : genos) {
			if (geno.equalsIgnoreCase("A"))
				counts[0]++;
			else if (geno.equalsIgnoreCase("H"))
				counts[1]++;
			else if (geno.equalsIgnoreCase("B"))
				counts[2]++;
			else
				counts[3]++;
		}
		return counts;
	}
	
	public int getN(int[] counts){
		return counts[0] + counts[1] + counts[2];
	}

	public double getAf(int[] counts) {
		/* get the allele frequency of given snp for specific population */
		return  (counts[0] + 0.5*counts[1])/getN(counts);
	}

	public Population getPopAf(String inpf){
		String popid = new File(inpf).getName().split("\\.")[0];		
		List<String[]> genos = getGenos(inpf);
		List<AlleleFreq> afs =new ArrayList<AlleleFreq>();
		
		System.out.println("Calculating allele frequencies for "+popid);
		for(String[] geno:genos){
			int[] counts = getCounts(geno);
			int n =2*getN(counts);
			double f = getAf(counts);
			String rsid = geno[0];
			AlleleFreq af = new AlleleFreq(rsid,n,f);
			afs.add(af);
		}
		
		int nmark = genos.size();
		Population pop = new Population(popid,nmark,afs);
		return pop;
	}
	
	public int getSum(AlleleFreq af1,AlleleFreq af2){
		//get sum n
		return af1.getCount()+af2.getCount();
	}
	
	public int getSumSq(AlleleFreq af1,AlleleFreq af2){
		//get sum of n square		
		return af1.getCount()*af1.getCount()+af2.getCount()*af2.getCount();
	}
	
	public double getPbar(AlleleFreq af1,AlleleFreq af2,int sum){
		// calculating pbar, pbar=sum(ni*pi)/sum(ni)
		return (af1.getCount()*af1.getFreq()+af2.getCount()*af2.getFreq())/sum;
	}
	
	public double getMSP(AlleleFreq af1,AlleleFreq af2,double pbar){
		// calculation msp, msp=sum(ni*(pi-pbar)^2)/(r-1)
		double msp =0;
		double p1,p2;
		int n1,n2;
		p1=af1.getFreq();
		p2=af2.getFreq();
		n1=af1.getCount();
		n2=af2.getCount();
		msp=n1*(p1-pbar)*(p1-pbar)+n2*(p2-pbar)*(p2-pbar);
		
		return msp;
	}
	
	public double getMSG(AlleleFreq af1,AlleleFreq af2,int sum){
		// calculating msg, msg=sum(ni*pi*(1-pi))/sum(ni-1)
		double msg =0;
		msg=af1.getCount()*af1.getFreq()*(1-af1.getFreq())+af2.getCount()*af2.getFreq()*(1-af2.getFreq());		
		return msg/(sum-2);
	}
		
	public double getNc(int sum,int sumsq){
		// cal nc, Notes r denote the number of populations
		// nc=(sum(ni)-sum(ni^2)/sum(ni))/(r-1) [r=2 here]
		return (sum-1.0*sumsq/sum);
	}
	
	public double[] getFstL(Population pop1,Population pop2 ) {
		//calculate fst for each marker return as a array
		System.out.println("Calculating locus Fst between "+pop1.getPopid()+" and "+pop2.getPopid());
		double[] fsts= new double[pop1.getNmark()];
		for(int i=0;i<pop1.getNmark();i++){
			AlleleFreq af1,af2;
			af1=pop1.getAfs().get(i);
			af2=pop2.getAfs().get(i);
			int sum = getSum(af1,af2);
			double pbar= getPbar(af1,af2,sum);
			double msp = getMSP(af1,af2,pbar);
			double msg = getMSG(af1,af2,sum);		
			// cal fst
			// fst=(msp-msg)/(msp+(nc-1)*msg)
			double nc= getNc(sum,getSumSq(af1,af2));			
			double fst = 0;
			double dnum=msp+(nc-1)*msg;
			if(dnum!=0)
				fst=(msp-msg)/dnum;
			fsts[i]=fst;
		}
		return fsts;
	}

	public double getFstS(Population pop1,Population pop2) {
		//calculate overall fst
		System.out.println("Calculating overall Fst between "+pop1.getPopid()+" and "+pop2.getPopid());
		double num=0;
		double dnum=0;
		for(int i=0;i<pop1.getNmark();i++){
			AlleleFreq af1,af2;
			af1=pop1.getAfs().get(i);
			af2=pop2.getAfs().get(i);
			int sum = getSum(af1,af2);
			double pbar= getPbar(af1,af2,sum);
			double msp = getMSP(af1,af2,pbar);
			double msg = getMSG(af1,af2,sum);
			double nc= getNc(sum,getSumSq(af1,af2));
			num+=msp-msg;
			dnum+=msp+(nc-1)*msg;
		}
		if(dnum!=0)
			return num/dnum;
		else
			return 0;
	}
	
	public void fstS(String[] inpfs){
		/*calculate overall pairwise fst and write result to external file in mega format */
		Population[] pops = new Population[inpfs.length];
		for(int i=0;i<inpfs.length;i++){
			pops[i]=getPopAf(inpfs[i]);
		}
		
		double[][] fstMat = new double[pops.length][pops.length];
		for(int i=0;i<fstMat.length;i++){
			for(int j=0;j<fstMat[0].length;j++){
				fstMat[i][j]=0;
			}
		}
		for(int i=0;i<inpfs.length-1;i++){
			for(int j=i+1;j<inpfs.length;j++){
				double fst=getFstS(pops[i],pops[j]);
				fst=fst>0?fst:0.0;
				fstMat[j][i]=fst;
			}
		}
		StringBuilder sb =new StringBuilder();
		sb.append("#mega\n").append("!TITLE Fst distance;\n")
				.append("!Format DataType=distance;\n\n");
		for(int i=0;i<pops.length;i++){
			sb.append("#").append(pops[i].getPopid()).append("\n");
		}
		sb.append("\n");
		DecimalFormat df = new DecimalFormat("#.000000");
		for(int i=1;i<fstMat.length;i++){
			for(int j=0;j<i;j++){
				sb.append(df.format(fstMat[i][j])).append("\t");
			}
			sb.append("\n");
		}
		
		
		System.out.println("Writing result to Fst_dist.meg");
		BufferedWriter bw = null;
		try{
			bw =new BufferedWriter(new FileWriter("Fst_dist.meg"));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void fstL(String[] inpfs){
		//calculate locus pairwise fst and write result to external file
		Population[] pops = new Population[inpfs.length];
		for(int i=0;i<inpfs.length;i++){
			pops[i]=getPopAf(inpfs[i]);
		}
		DecimalFormat df = new DecimalFormat("#.000000");
		StringBuilder title =new StringBuilder();
		title.append("rsid\t");
		StringBuilder[] sbs= new StringBuilder[pops[0].getNmark()];
		//init
		for(int i=0;i<sbs.length;i++){
			sbs[i]=new StringBuilder();
			sbs[i].append(pops[0].getAfs().get(i).getRsid()).append("\t");
		}
		for(int i=0;i<inpfs.length-1;i++){
			for(int j=i+1;j<inpfs.length;j++){
				title.append(pops[i].getPopid()).append("_VS_").append(pops[j].getPopid()).append("\t");
				double[] fstl= getFstL(pops[i],pops[j]);
				for(int k=0;k<fstl.length;k++){
					double fst = fstl[k]>0?fstl[k]:0.0;
					sbs[k].append(df.format(fst)).append("\t");
				}
			}
		}
		System.out.println("Writing result to Fst_Locus.txt");
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter("Fst_Locus.txt"));
			bw.write(title.append("\n").toString());
			for(StringBuilder sb:sbs){
				sb.append("\n");
				bw.write(sb.toString());
			}
			bw.flush();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}		
		
	}

	public void printHelp(){
		System.out.println("**********************************************************************");
		System.out.println("*Program name: FstCal.jar                                            *");
		System.out.println("*Version: 0.5.0                                                      *");
		System.out.println("*Description:                                                        *");
		System.out.println("*   The program is used to calculate pairwise Fst for given a list of*");
		System.out.println("*populations, both used for locus Fst or overall Fst calculation.    *");
		System.out.println("*Command and arguments:                                              *");
		System.out.println("*    locus <inpf1> <inpf2> [inpf3...inpfN]                           *");
		System.out.println("*    sum <inpf1> <inpf2> [inpf3...inpfN]                             *");
		System.out.println("**********************************************************************");
	}
	public static void main(String[] args) {
		FstCal fc = new FstCal();
		if(args.length>2 && args[0].equals("sum")){
			String[] inpfs=new String[args.length-1];
			for(int i=1;i<args.length;i++){
				inpfs[i-1]=args[i];
			}
			fc.fstS(inpfs);
		}
		else if(args.length>2 && args[0].equals("locus")){
			String[] inpfs=new String[args.length-1];
			for(int i=1;i<args.length;i++){
				inpfs[i-1]=args[i];
			}
			fc.fstL(inpfs);
		}else{
		fc.printHelp();
		}
	}

}
