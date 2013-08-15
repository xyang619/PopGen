package api;

public class SNP {
	private String rsid;
	private int chr;
	private double gd;
	private int pd;
	private String a;
	private String b;
	public String getRsid() {
		return rsid;
	}
	public void setRsid(String rsid) {
		this.rsid = rsid;
	}
	public int getChr() {
		return chr;
	}
	public void setChr(int chr) {
		this.chr = chr;
	}
	public double getGd() {
		return gd;
	}
	public void setGd(double gd) {
		this.gd = gd;
	}
	public int getPd() {
		return pd;
	}
	public void setPd(int pd) {
		this.pd = pd;
	}
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public String getB() {
		return b;
	}
	public void setB(String b) {
		this.b = b;
	}
	
	
	public SNP(String rsid, int chr, String a, String b) {
		super();
		this.rsid = rsid;
		this.chr = chr;
		this.a = a;
		this.b = b;
	}
	
	public SNP(String rsid, int chr, double gd, int pd, String a, String b) {
		super();
		this.rsid = rsid;
		this.chr = chr;
		this.gd = gd;
		this.pd = pd;
		this.a = a;
		this.b = b;
	}

}
