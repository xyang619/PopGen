package convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InpToBeagle {

	public String getTitle(String title) {
		StringBuilder indssb = new StringBuilder();
		// add first two columns I id to the string builder
		indssb.append("I id ");
		String[] inds = title.split("\\s+");
		// add each individual twice
		for (int i = 6; i < inds.length; i++) {
			indssb.append(inds[i]).append(" ").append(inds[i]).append(" ");
		}
		indssb.append("\n");
		return indssb.toString();
	}

	public String recode(String geno, String[] alleles) {
		if (geno.equals("A") || geno.equals("0"))
			return alleles[0] + " " + alleles[0];
		else if (geno.equals("H") || geno.equals("1"))
			return alleles[0] + " " + alleles[1];
		else if (geno.equals("B") || geno.equals("2"))
			return alleles[1] + " " + alleles[1];
		else
			return "? ?";
	}

	public void convert(String inpf, String pop) {
		BufferedReader br = null;
		BufferedWriter mkbw = null;
		BufferedWriter gtbw = null;
		Map<String, List<String>> markers = new HashMap<String, List<String>>();
		Map<String, List<String>> genotypes = new HashMap<String, List<String>>();
		String title = null;
		try {
			System.out.println("Read genotype and marker info from "+inpf);
			DecimalFormat df = new DecimalFormat("#.######");
			br = new BufferedReader(new FileReader(inpf));
			String line;
			// read title
			title = getTitle(br.readLine());
			// read markers and genotypes and add to the maps
			while ((line = br.readLine()) != null) {
				String[] sp1 = line.split("\\s+", 7);
				String chr = sp1[2];
				String[] alleles = sp1[5].split("/");

				// marker format: rsid genetic_distance(centiMorgan) allele1
				// allele2
				StringBuilder marker = new StringBuilder();
				marker.append(sp1[1]).append("  ")
						.append(df.format(Integer.parseInt(sp1[3]) / 1.0e6))
						.append(" ").append(alleles[0]).append(" ")
						.append(alleles[1]).append("\n");
				if (!markers.containsKey(chr)) {// has no key
					List<String> row = new ArrayList<String>();
					markers.put(chr, row);
				}

				markers.get(chr).add(marker.toString());

				// bgl format: M rsid genotypes
				StringBuilder genotype = new StringBuilder();				
				genotype.append("M ").append(sp1[1]).append(" ");
				for (String geno : sp1[6].split("\\s+")) {
					genotype.append(recode(geno, alleles)).append(" ");
				}
				genotype.append("\n");

				if (!genotypes.containsKey(chr)) {
					List<String> row = new ArrayList<String>();
					genotypes.put(chr, row);
				}
				genotypes.get(chr).add(genotype.toString());
			}
			br.close();
			System.out.println("Finish reading "+inpf);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File outDir = new File(pop);
		// if the output directory does not exist, then create it
		if (!outDir.exists() && !outDir.isDirectory()) {
			outDir.mkdirs();
		}
		// output the convert result

		for (String ch : markers.keySet()) {
			String mkf = outDir.getAbsolutePath() + File.separator.toString()
					+ "chr" + ch + ".marker";
			String gtf = outDir.getAbsolutePath() + File.separator.toString()
					+ "chr" + ch + ".bgl";

			try {
				mkbw = new BufferedWriter(new FileWriter(mkf));
				for (String mk : markers.get(ch)) {
					mkbw.write(mk);
				}
				mkbw.flush();
				mkbw.close();
				System.out.println("Write markers on chr " +ch+" to "+mkf);
				
				gtbw = new BufferedWriter(new FileWriter(gtf));
				gtbw.write(title);
				for (String gt : genotypes.get(ch)) {
					gtbw.write(gt);
				}
				gtbw.flush();
				gtbw.close();
				
				System.out.println("Write genotype on chr " +ch+" to "+mkf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void printHelp(){
		System.out.println("**********************************************************************");
		System.out.println("*Program name: InpToBeagle.jar                                       *");
		System.out.println("*Version: 0.5.0                                                      *");
		System.out.println("*Description:                                                        *");
		System.out.println("*   The program is used to convert inp file into beagle format       *");
		System.out.println("*including marker and bgl file.                                      *");
		System.out.println("*Command and arguments:                                              *");
		System.out.println("*    <inpf> <pop>                                                    *");
		System.out.println("**********************************************************************");
	}

	public static void main(String[] args) {
		//DecimalFormat df = new DecimalFormat("#.######");
		//System.out.println(df.format(4323.32323568));
		InpToBeagle ib= new InpToBeagle();
		if(args.length==2){
			ib.convert(args[0], args[1]);
		}else{
			ib.printHelp();
		}

	}

}
