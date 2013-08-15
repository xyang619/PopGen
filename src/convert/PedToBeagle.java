package convert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Beagle Format:
 * I	id			1001	1001	1002	1002	
 * A	diabetes	1		1		2		2	
 * M	rs2298323	A		G		G		A
 * M	rs1089434	C		T		C		C
 * 
 * Ped Format:
 * CEU	NA12313	0	0	1	-9	A	G	C	T
 * CEU	NA23442	0	0	2	-9	G	A	C	C
 * 
 * Map Format:
 * 1	rs2298323	0.2398	1156131
 * 1	rs1089434	0.2401	1290809
 */
public class PedToBeagle {

	@SuppressWarnings("resource")
	public List<String> getRsid(String mapf) {
		List<String> rsidList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(mapf));
			String line;
			while ((line = br.readLine()) != null) {
				rsidList.add(line.split("\\s", 3)[1]);
			}
		} catch (FileNotFoundException e) {
			System.out.println("mapfile " + mapf + " not found");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return rsidList;
	}

	public void convert(String pedf, String mapf, String prefix) {
		List<String> snpList = getRsid(mapf);
		List<String> inds = new ArrayList<String>();
		List<StringBuilder> genotype = new ArrayList<StringBuilder>();
		// initialize empty string builder
		for (int i = 0; i < snpList.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("M ").append(snpList.get(i));
			genotype.add(sb);
		}

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(pedf));
			String line;
			while ((line = br.readLine()) != null) {
				String[] sp1 = line.split("\\s", 7);
				inds.add(sp1[1]);
				String[] sp2 = sp1[6].split("\\s");
				for (int i = 0; i < sp2.length / 2; i++) {
					genotype.get(i).append(" ").append(sp2[2 * i]).append(" ")
							.append(sp2[2 * i + 1]);
				}
			}
			br.close();
			String outfile = prefix+".bgl";
			bw = new BufferedWriter(new FileWriter(outfile));
			StringBuilder header = new StringBuilder();
			header.append("I id");
			for(String ind: inds){
				header.append(" ").append(ind).append(" ").append(ind);
			}
			bw.write(header.toString());
			bw.newLine();
			for(StringBuilder sb: genotype){
				bw.write(sb.toString());
				bw.newLine();
			}
			
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void printHelp(){
		System.out.println("**********************************************************************");
		System.out.println("*Program name: PedToBeagle.jar                                       *");
		System.out.println("*Version: 0.5.0                                                      *");
		System.out.println("*Description:                                                        *");
		System.out.println("*   The program is used to convert ped file into beagle format       *");
		System.out.println("*including marker and bgl file.                                      *");
		System.out.println("*Command and arguments:                                              *");
		System.out.println("*    <ped> <map> <prefix>                                            *");
		System.out.println("**********************************************************************");
	
	}
	
	public static void main(String[] args){
		PedToBeagle pb =new PedToBeagle();
		if(args.length==3)
			pb.convert(args[0], args[1], args[2]);
		else
			pb.printHelp();
	}

}
