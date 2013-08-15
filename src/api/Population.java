package api;

import java.util.List;

public class Population {

	private String popid;
	private int nmark;
	private List<AlleleFreq> afs;
	public Population(String popid, int nmark) {
		super();
		this.popid = popid;
		this.nmark = nmark;
	}
	
	
	public Population(String popid, int nmark, List<AlleleFreq> afs) {
		super();
		this.popid = popid;
		this.nmark = nmark;
		this.afs = afs;
	}

	public String getPopid() {
		return popid;
	}
	public void setPopid(String popid) {
		this.popid = popid;
	}
	public int getNmark() {
		return nmark;
	}
	public void setNmark(int nmark) {
		this.nmark = nmark;
	}
	public List<AlleleFreq> getAfs() {
		return afs;
	}
	public void setAfs(List<AlleleFreq> afs) {
		this.afs = afs;
	}
	

}
