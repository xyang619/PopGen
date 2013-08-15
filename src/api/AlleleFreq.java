package api;

public class AlleleFreq {
	
	private String rsid;
	private int count;
	private double freq;
	
	public AlleleFreq(String rsid) {
		super();
		this.rsid = rsid;
	}
	
	public AlleleFreq(String rsid, int count, double freq) {
		super();
		this.rsid = rsid;
		this.count = count;
		this.freq = freq;
	}
	
	public String getRsid() {
		return rsid;
	}
	
	public void setRsid(String rsid) {
		this.rsid = rsid;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public double getFreq() {
		return freq;
	}
	
	public void setFreq(double freq) {
		this.freq = freq;
	}
	
}
