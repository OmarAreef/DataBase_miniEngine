

import java.io.Serializable;
import java.util.Hashtable;

public class Record implements Serializable{
	 Hashtable colNameValue ; 
	

	public Hashtable getColNameValue() {
		return colNameValue;
	}


	


	@Override
	public String toString() {
		return "Record [colNameValue=" + colNameValue + "]";
	}





	public Record(Hashtable colNameValue) {
		super();
		this.colNameValue = colNameValue;
	}

	
	
}
