package convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class InpToPed {

	public StringBuilder[] getInds(String head, String pop) {
		StringBuilder[] inds;
		String[] items = head.split("\\s+");
		inds = new StringBuilder[items.length - 6];
		for (int i = 6; i < items.length; i++) {
			inds[i - 6] = new StringBuilder();
			inds[i - 6].append(pop).append(" ").append(items[i])
					.append(" 0 0 -9 -9 ");
		}
		return inds;
	}

	public void convert(String inpf, String outprefix, String pop) {
		BufferedReader br = null;
		BufferedWriter pedbw = null;
		BufferedWriter mapbw = null;
		StringBuilder[] inds;
		try {
			String pedf = outprefix + ".ped";
			String mapf = outprefix + ".map";
			DecimalFormat df = new DecimalFormat("#.########");
			br = new BufferedReader(new FileReader(inpf));
			pedbw = new BufferedWriter(new FileWriter(pedf));
			mapbw = new BufferedWriter(new FileWriter(mapf));
			String line;
			line = br.readLine();
			inds = getInds(line, pop);
			while ((line = br.readLine()) != null) {
				String[] sp1 = line.split("\\s+", 7);
				String[] alleles = sp1[5].split("/");
				// write map file
				StringBuilder mapsb = new StringBuilder();
				mapsb.append(sp1[2]).append(" ").append(sp1[1]).append(" ")
						.append(df.format(Integer.parseInt(sp1[3]) / 1.0e8))
						.append(" ").append(sp1[3]).append("\n");
				mapbw.write(mapsb.toString());
				// read and add genotype to each individual
				String[] genos = sp1[6].split("\\s+");
				for (int i = 0; i < genos.length; i++) {
					inds[i].append(recode(genos[i], alleles)).append(" ");
				}
			}
			for (int i = 0; i < inds.length; i++) {
				inds[i].append("\n");
				pedbw.write(inds[i].toString());
			}
			// close file
			br.close();
			mapbw.flush();
			mapbw.close();
			System.out.println("map info write to " + mapf);
			pedbw.flush();
			pedbw.close();
			System.out.println("Genotype info write to " + pedf);
		} catch (IOException e) {
		}
	}

	private String recode(String geno, String[] alleles) {
		if (geno.equals("A") || geno.equals("0"))
			return alleles[0] + " " + alleles[0];
		else if (geno.equals("H") || geno.equals("1"))
			return alleles[0] + " " + alleles[1];
		else if (geno.equals("B") || geno.equals("2"))
			return alleles[1] + " " + alleles[1];
		else
			return "0 0";
	}

	public void printHelp(){
		System.out.println("**********************************************************************");
		System.out.println("*Program name: InpToPed.jar                                          *");
		System.out.println("*Version: 0.5.0                                                      *");
		System.out.println("*Description:                                                        *");
		System.out.println("*   The program is used to convert inp file into plink format ped and*");
		System.out.println("*map format.                                                         *");
		System.out.println("*Command and arguments:                                              *");
		System.out.println("*    <inpf> <outprefix> <pop>                                        *");
		System.out.println("**********************************************************************");
	}
	
	public static void main(String[] args) {
		InpToPed ip = new InpToPed();
		if (args.length == 3) {
			ip.convert(args[0], args[1], args[2]);
		} else {
			ip.printHelp();
		}

	}

}
