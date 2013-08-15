package annot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotGenotype {

	public String recodeABHU(String geno) {
		if (geno.equals("0"))
			return "A";
		else if (geno.equals("1"))
			return "H";
		else if (geno.equals("2"))
			return "B";
		else
			return "U";
	}

	public Map<String, List<String[]>> getSample(String popf) {
		Map<String, List<String[]>> samples = new HashMap<String, List<String[]>>();
		BufferedReader br = null;
		try {
			int nind = 0; // number of individuals
			br = new BufferedReader(new FileReader(popf));
			String line;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\\s+");
				String pop = cols[2];
				String[] row = { cols[0], cols[1] };
				// String cel = cols[0];
				// String iid = cols[1];
				if (!samples.containsKey(pop)) {
					List<String[]> list = new ArrayList<String[]>();
					samples.put(pop, list);
				}
				samples.get(pop).add(row);
				nind++;
			}
			br.close();
			System.out.println("Read " + nind + " individuals in "
					+ samples.size() + " populations from " + popf);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return samples;
	}

	public Map<String, String[]> getAnnot(String annotf) {
		Map<String, String[]> annot = new HashMap<String, String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(annotf));
			String line;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\\s+");
				String probeId = cols[0];
				// row: rsid chr pos strand allele
				String[] row = { cols[1], cols[2], cols[3], cols[4], cols[5] };
				annot.put(probeId, row);
			}
			br.close();
			System.out.println("Read " + annot.size() + " probes from "
					+ annotf);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return annot;
	}

	public void annot(String birdf, String annotRef, String popf) {
		// record cels sample mapping key/value pair is pop/[cel,individual_id]
		Map<String, List<String[]>> samples = getSample(popf);
		// record annotation, key/value is probe_id/[rsid,...]
		Map<String, String[]> annot = getAnnot(annotRef);
		// prepare for output, each key denotes a population
		Map<String, List<String>> outStrs = new HashMap<String, List<String>>();
		// record the individual ids, each key denotes a population
		Map<String, StringBuilder> title = new HashMap<String, StringBuilder>();

		// init outStrs and title
		for (String pop : samples.keySet()) {
			List<String> row = new ArrayList<String>();
			outStrs.put(pop, row);
			StringBuilder sb = new StringBuilder();
			sb.append("probe_id\tdbSNP_id\tChromosome\tPosition\tStrand\tAllele\t");
			title.put(pop, sb);
		}
		BufferedReader br = null;
		// BufferedWriter[] bws =null;
		try {
			br = new BufferedReader(new FileReader(birdf));
			String line;
			String[] cels = br.readLine().split("\\s+", 2)[1].split("\\s+");
			//record the position of each cel file for each individuals 
			Map<String, List<Integer>> colPos = new HashMap<String, List<Integer>>();
			for(String pop: samples.keySet()){
				List<Integer> pos = new ArrayList<Integer>();
				colPos.put(pop, pos);
			}
			for(int i=0;i<cels.length;i++){
				outer:
				for(String pop:samples.keySet()){
					for(String[] ind:samples.get(pop)){
						if(ind[0].equals(cels[i])){
							colPos.get(pop).add(i+1);
							title.get(pop).append(ind[1]).append("\t");
							break outer;
						}
					}
				}
			}
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\\s+");
				for(String pop:samples.keySet()){
					StringBuilder sb = new StringBuilder();
					sb.append("");
					//for()
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		int m=0;
		for(int i=1;i<10;i++){
			outer:
			for(int j=1;j<10;j++){
				for(int k=1;j<10;j++){
					if(i*j*k%3==0){
						System.out.println(i+" "+j+" "+k);
						break outer;
					}
					else
						System.out.println(++m);
				}
			}
		}

	}

}
