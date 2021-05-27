import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

public class indexBucket implements Serializable {
	Vector<keyPointer> records ;
	String bucketName;
	
	
	public Page loadPage(String pageName) {
		FileInputStream fileIn;
		ObjectInputStream in;
		Page p1 = null;
		try {
			fileIn = new FileInputStream("src/main/resources/Data/Buckets/" + pageName + ".class");
			in = new ObjectInputStream(fileIn);
			p1 = (Page) in.readObject();
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException | IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		return p1;
	}
	
	public void savePage(Page table_initial_page) {
		FileOutputStream fileOut;
		ObjectOutputStream out;
		try {
			fileOut = new FileOutputStream(
					"src/main/resources/Data/Buckets/" + table_initial_page.getPage_name() + ".class");
			out = null;
			out = new ObjectOutputStream(fileOut);
			out.writeObject(table_initial_page);
			out.close();
			fileOut.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	
}
