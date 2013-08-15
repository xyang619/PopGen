package convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import api.SNP;

public class PhasedBglToHapmix {

	public void writeSnpRate(String bimf) {
		Map<Integer, List<SNP>> snps = new HashMap<Integer, List<SNP>>();
		// Read all snps
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(bimf));
			String line;
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\\s+");
				int chr = Integer.parseInt(items[0]);
				String rsid = items[1];
				double gd = Double.parseDouble(items[2]);
				int pd = Integer.parseInt(items[3]);
				String a = items[4];
				String b = items[5];
				SNP snp = new SNP(rsid, chr, gd, pd, a, b);
				if (!snps.containsKey(chr)) {
					List<SNP> rows = new ArrayList<SNP>();
					snps.put(chr, rows);
				}
				snps.get(chr).add(snp);
			}
			br.close();
			System.out.println("Finished reading snps and rate from " + bimf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// write snp file and rate file
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		DecimalFormat df = new DecimalFormat("#.#######");
		for (int i = 1; i <= 22; i++) {
			try {
				bw1 = new BufferedWriter(new FileWriter("chr" + i + ".snpfile"));
				bw2 = new BufferedWriter(new FileWriter("chr" + i + ".rate"));
				StringBuilder sb1 = null;
				StringBuilder sb2 = new StringBuilder(1024);
				StringBuilder sb3 = new StringBuilder(1024);
				int nsnp = snps.get(i).size(); // number of snp in chromosome i
				sb2.append(":sites:").append(nsnp).append("\n");
				for (SNP snp : snps.get(i)) {
					sb1 = new StringBuilder(256);
					sb1.append(snp.getRsid()).append("\t").append(snp.getChr())
							.append("\t").append(df.format(snp.getGd() / 100))
							.append("\t").append(snp.getPd()).append("\t")
							.append(snp.getA()).append("\t").append(snp.getB())
							.append("\n");
					bw1.write(sb1.toString());
					sb2.append(snp.getPd()).append(" ");
					sb3.append(snp.getGd()).append(" ");
				}
				sb2.append("\n");
				sb3.append("\n");
				bw2.write(sb2.toString());
				bw2.write(sb3.toString());
				bw1.flush();
				bw1.close();
				bw2.flush();
				bw2.close();
				System.out.println("Write snpfile to chr "+i+".snpfile");
				System.out.println("Write rates to chr "+i+".rate");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, String[]> getSNPs(String snpfile) {
		Map<String, String[]> snps = new HashMap<String, String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(snpfile));
			String line;
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\\s+");
				String rsid = items[0];
				String[] alleles = new String[2];
				alleles[0] = items[4];
				alleles[1] = items[5];
				snps.put(rsid, alleles);
			}
			br.close();
			System.out.println("Read " + snps.size() + " snps from " + snpfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return snps;
	}

	public String recode01(String base, String[] als) {
		if (base.equals(als[0]))
			return "0";
		else if (base.equals(als[1]))
			return "1";
		else
			return "9";
	}

	public String recode0129(String bp, String[] als) {
		if (bp.equals(als[0] + als[0])) { // AA
			return "0";
		} else if (bp.equals(als[1] + als[1])) { // BB
			return "2";
		} else if (bp.equals(als[0] + als[1]) || bp.equals(als[1] + als[0])) { // AB or BA																			
			return "1";
		} else { // Missing
			return "9";
		}
	}

	public void writeGenoRef(String phasedBgl, String snpfile, String pop) {
		Map<String, String[]> snps = getSNPs(snpfile);
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(phasedBgl))));
			String outf = pop + "_" + new File(snpfile).getName().split("\\.")[0] + ".genofile";
			bw = new BufferedWriter(new FileWriter(outf));
			String line;
			line = br.readLine();// Skip header
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\\s+");
				if ((items.length) % 2 != 0) {
					System.err.println("There exists error in input file");
					break;
				}
				String rsid = items[1];
				if (snps.containsKey(rsid)) {
					StringBuilder sb = new StringBuilder(128);
					for (int i = 2; i < items.length; i++) {
						sb.append(recode01(items[i], snps.get(rsid)));
					}
					sb.append("\n");
					bw.write(sb.toString());
				}
			}
			br.close();
			bw.flush();
			bw.close();
			System.out.println("Finished convert " + phasedBgl + " and write result to " + outf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeGenoAdmix(String phasedBgl, String snpfile, String pop) {
		Map<String, String[]> snps = getSNPs(snpfile);
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(phasedBgl))));
			String outf = pop + "_" + new File(snpfile).getName().split("\\.")[0] + ".genofile";
			bw = new BufferedWriter(new FileWriter(outf));
			String line;
			line = br.readLine();// Skip header
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\\s+");
				if ((items.length) % 2 != 0) {
					System.err.println("There exists error in input file");
					break;
				}
				String rsid = items[1];
				if (snps.containsKey(rsid)) {
					StringBuilder sb = new StringBuilder(128);
					for (int i = 2; i < items.length; i += 2) {
						String bp = items[i] + items[i + 1];
						sb.append(recode0129(bp, snps.get(rsid)));
					}
					sb.append("\n");
					bw.write(sb.toString());
				}
			}
			br.close();
			bw.flush();
			bw.close();
			System.out.println("Finished convert " + phasedBgl + " and write result to " + outf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void printHelp(){
		System.out.println("**********************************************************************");
		System.out.println("*Program name: ConvertHapmix.jar                                     *");
		System.out.println("*Version: 0.5.0                                                      *");
		System.out.println("*Description:                                                        *");
		System.out.println("*   The program is used to convert the genotype data phased by Beagle*");
		System.out.println("*into hapmix format files including .snp,.rate and .geno file        *");
		System.out.println("*Command and arguments:                                              *");
		System.out.println("*    snp <bimfile>                                                   *");
		System.out.println("*    ref <phasedBgl[.gz]> <snpfile> <pop>                            *");
		System.out.println("*    mix <phasedBgl[.gz]> <snpfile> <pop>                            *");
		System.out.println("**********************************************************************");
	}
	public static void main(String[] args) {
		PhasedBglToHapmix ch = new PhasedBglToHapmix();		
		if(args.length>1 && args[0].equals("snp")){
			ch.writeSnpRate(args[1]);				
		}
		else if(args.length>3 && args[0].equals("ref")){
			ch.writeGenoRef(args[1], args[2], args[3]);
		}
		else if(args.length>3 && args[0].equals("mix")){
			ch.writeGenoAdmix(args[1], args[2], args[3]);
		}else{
			ch.printHelp();
		}
	}
}

