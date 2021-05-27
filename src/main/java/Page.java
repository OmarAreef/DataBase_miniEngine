

import java.io.Serializable;
import java.util.Vector;

public class Page implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3956677499136298238L;
	int nmElemnts;
	String table_name ; 
	int page_index ;
	int nor_index ; 
	Vector <Record> records ;
	int overflow ; 
	Comparable Max ;
	
	
	public Page(String table_name, int page_index) {
		super();
		int newindex = page_index*100 ;
		this.table_name = table_name;
		this.nor_index = newindex;
		this.page_index = page_index ;
		records = new Vector <Record>();
		int overflow = 0;
		Max = 0 ;
		
	}
	public Page(String table_name, int page_index , int offset) {
		super();
		int newindex = page_index + offset;
		this.table_name = table_name;
		this.nor_index = newindex;
		this.page_index = page_index ;
		records = new Vector <Record>();
		overflow = offset ;
		Max = 0 ;
		
	}

	public int getNmElemnts() {
		return nmElemnts;
	}

	public Comparable getMax() {
		return Max;
	}

	public void setMax(Comparable max) {
		Max = max;
	}

	public void setNmElemnts(int nmElemnts) {
		this.nmElemnts = nmElemnts;
	}

	public String getTable_name() {
		return table_name ;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public int getPage_index() {
		return page_index;
	}

	public void setPage_index(int page_index) {
		this.page_index = page_index;
	}

	public Vector<Record> getRecords() {
		return records;
	}

	public void setRecords(Vector<Record> records) {
		this.records = records;
	} 
	public String getPage_name() {
		return table_name + nor_index ;
	}

	@Override
	public String toString() {
		return "Page [nmElemnts=" + nmElemnts + ", table_name=" + table_name + ", page_index=" + page_index
				+ ", records=" + records + ", Max=" + Max + "]";
	}
	

}
