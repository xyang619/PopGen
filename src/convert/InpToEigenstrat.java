package convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class InpToEigenstrat {

	public String recode012(String geno) {
		if (geno.equals("A") || geno.equals("0"))
			return "0";
		else if (geno.equals("H") || geno.equals("1"))
			return "1";
		else if (geno.equals("B") || geno.equals("2"))
			return "2";
		else
			return "9";
	}

	public String recode01(String geno) {
		if (geno.equals("A") || geno.equals("0"))
			return "0 0 ";
		else if (geno.equals("H") || geno.equals("1"))
			return "0 1 ";
		else if (geno.equals("B") || geno.equals("2"))
			return "1 1 ";
		else
			return "9 9 ";
	}

	public void writeInd(String outf, String pop, String inds) {
		BufferedWriter indf = null;
		try {
			indf = new BufferedWriter(new FileWriter(outf));
			String[] items = inds.split("\\s+");
			for (int i = 6; i < items.length; i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(items[i]).append(" ").append("U ").append(pop)
						.append("\n");
				indf.write(sb.toString());
			}
			indf.flush();
			indf.close();
			System.out.println("Individual info write to " + outf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void convert(String inpf, String outprefix, String pop, String type) {
		BufferedReader br = null;
		BufferedWriter snpbw = null;
		BufferedWriter genobw = null;
		try {
			String indf = outprefix + ".ind";
			String snpf = outprefix + ".snp";
			String genof = outprefix + ".geno";
			br = new BufferedReader(new FileReader(inpf));
			snpbw = new BufferedWriter(new FileWriter(snpf));
			genobw = new BufferedWriter(new FileWriter(genof));
			String line;
			DecimalFormat df = new DecimalFormat("#.########");
			// read and write individuals
			line = br.readLine();
			writeInd(indf, pop, line);
			// read and write snp and genotype file
			while ((line = br.readLine()) != null) {
				String[] sp1 = line.split("\\s+", 7);
				String[] alleles = sp1[5].split("/");
				// write SNP
				StringBuilder snp = new StringBuilder();
				snp.append(sp1[1]).append(" ").append(sp1[2]).append(" ")
						.append(df.format(Integer.parseInt(sp1[3]) / 1.0e8))
						.append(" ").append(sp1[3]).append(" ")
						.append(alleles[0]).append(" ").append(alleles[1])
						.append("\n");
				snpbw.write(snp.toString());
				// write genotype
				String[] genos = sp1[6].split("\\s+");
				StringBuilder geno = new StringBuilder();
				if (type.equals("geno")) {
					for (String g : genos) {
						geno.append(recode012(g));
					}
				} else {
					for (String g : genos) {
						geno.append(recode01(g));
					}
				}
				geno.append("\n");
				genobw.write(geno.toString());
			}
			// close files
			br.close();
			System.out.println("SNP info write to " + snpf);
			System.out.println("Genotype info write to " + genof);
			snpbw.flush();
			snpbw.close();
			genobw.flush();
			genobw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void convert(String inpf, String outprefix, String pop,
			String refsnpf, String type) {
		BufferedReader br = null;
		BufferedWriter snpbw = null;
		BufferedWriter genobw = null;
		Set<String> refSnp = getRefSNP(refsnpf);
		try {
			String indf = outprefix + ".ind";
			String snpf = outprefix + ".snp";
			String genof = outprefix + ".geno";
			br = new BufferedReader(new FileReader(inpf));
			snpbw = new BufferedWriter(new FileWriter(snpf));
			genobw = new BufferedWriter(new FileWriter(genof));
			String line;
			DecimalFormat df = new DecimalFormat("#.########");
			// read and write individuals
			line = br.readLine();
			writeInd(indf, pop, line);
			// read and write snp and genotype file
			while ((line = br.readLine()) != null) {
				String[] sp1 = line.split("\\s+", 7);
				// check if snp in reference snps
				if (refSnp.contains(sp1[1])) {
					String[] alleles = sp1[5].split("/");
					// write SNP
					StringBuilder snp = new StringBuilder();
					snp.append(sp1[1])
							.append(" ")
							.append(sp1[2])
							.append(" ")
							.append(df.format(Integer.parseInt(sp1[3]) / 1.0e8))
							.append(" ").append(sp1[3]).append(" ")
							.append(alleles[0]).append(" ").append(alleles[1])
							.append("\n");
					snpbw.write(snp.toString());
					// write genotype
					String[] genos = sp1[6].split("\\s+");
					StringBuilder geno = new StringBuilder();
					if (type.equals("geno")) {
						for (String g : genos) {
							geno.append(recode012(g));
						}
					} else {
						for (String g : genos) {
							geno.append(recode01(g));
						}
					}
					geno.append("\n");
					genobw.write(geno.toString());
				}
			}
			// close files
			br.close();
			System.out.println("SNP info write to " + snpf);
			System.out.println("Genotype info write to " + genof);
			snpbw.flush();
			snpbw.close();
			genobw.flush();
			genobw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getRefSNP(String refsnpf) {
		Set<String> refSnp = new HashSet<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(refsnpf));
			String line;
			while ((line = br.readLine()) != null) {
				refSnp.add(line.trim());
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Read " + refSnp.size() + " ref SNP info from "
				+ refsnpf);
		return refSnp;
	}

	public void printHelp() {
		System.out.println("**********************************************************************");
		System.out.println("*Program name: InpToEigenstrat.jar                                   *");
		System.out.println("*Version: 1.0.0                                                      *");
		System.out.println("*Description:                                                        *");
		System.out.println("*   The program is used to convert inp file into eigenstrat format   *");
		System.out.println("*including ind,geno,and snp file.                                    *");
		System.out.println("*Command and arguments:                                              *");
		System.out.println("*    geno <inpf> <outprefix> <pop> [RefSnp]                          *");
		System.out.println("*    hap  <inpf> <outprefix> <pop> [RefSnp]                          *");
		System.out.println("**********************************************************************");
	}

	public static void main(String[] args) {
		InpToEigenstrat ie = new InpToEigenstrat();
		if (args.length == 4) {
			if(args[0].equals("geno"))
				ie.convert(args[1], args[2],args[3],"geno");
			if(args[0].equals("hap"))
				ie.convert(args[1], args[2],args[3],"hap");
		} else if (args.length == 5) {
			if(args[0].equals("geno"))
				ie.convert(args[1], args[2], args[3],args[4],"geno");
			if(args[0].equals("hap"))
				ie.convert(args[1], args[2], args[3],args[4],"hap");
		} else {
			ie.printHelp();
		}
		/*
		 * DecimalFormat df = new DecimalFormat("#.########");
		 * System.out.println(df.format(Integer.parseInt("24659832") / 1e8));
		 * System.out.println("A/G".split("/")[0]);
		 * System.out.print("abc\n".trim());
		 * System.out.println("A/G".split("/")[0]);
		 */
	}

}
