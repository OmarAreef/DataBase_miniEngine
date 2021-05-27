import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class DBApp implements DBAppInterface {
	ArrayList<String[]> metadata;
	boolean meta;

	@Override
	public void init() {
		String path = "src/main/resources/Data";
		File file = new File(path);
		boolean bool;
		if (!file.exists()) {
			// Creating the directory
			bool = file.mkdir();
		}
		path = "src/main/resources/Data/Tables";
		file = new File(path);
		if (!file.exists()) {
			// Creating the directory
			bool = file.mkdir();
		}
		path = "src/main/resources/Data/Pages";
		file = new File(path);
		if (!file.exists()) {
			// Creating the directory
			bool = file.mkdir();
		}
		// if(bool){
		// System.out.println("Directory created successfully");
		// }else{
		// System.out.println("Sorry couldn’t create specified directory");
		// }
		// meta= true ;
		path = "src/main/resources/metadata.csv";
		file = new File(path);
		if (file.length() == 0) {
			try {
				FileWriter fw = new FileWriter("src/main/resources/metadata.csv", true);
				String s = "Table Name, Column Name, Column Type, ClusteringKey, Indexed, min, max\n";
				StringBuilder sb = new StringBuilder();
				sb.append(s);
				fw.append(sb.toString());
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.meta = true;
		}
	}

	public DBApp() {
		super();
		this.meta = false;
	}

	@Override
	public void createTable(String tableName, String clusteringKey, Hashtable<String, String> colNameType,
			Hashtable<String, String> colNameMin, Hashtable<String, String> colNameMax) throws DBAppException {
		// TODO Auto-generated method stub
		Table t = null;
		try {
			t = new Table(tableName, clusteringKey, colNameType, colNameMin, colNameMax);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String file_output = t.getTable_name();
		// FileOutputStream fileOut = new FileOutputStream();
		File file = new File("src/main/resources/Data/Tables/" + file_output + ".class");
		if (!file.exists()) {
			ArrayList<String[]> metadata = getMetadataString(tableName);
			t.setMetadata(metadata);
			saveTable(t);
		}
		// ObjectOutputStream out = new ObjectOutputStream(fileOut);
		// out.writeObject(t);
		// out.close();
		// fileOut.close();
	}

	@Override
	public void createIndex(String tableName, String[] columnNames) throws DBAppException {
		// TODO Auto-generated method stub
	}

	@Override
	public void insertIntoTable(String tableName, Hashtable<String, Object> colNameValue) throws DBAppException {
		// deseralization
		Table table = loadTable(tableName);
		// System.out.println(table.getTable_name());
		// metadata as array
		ArrayList<String[]> metadata2 = table.getMetadata();
		CheckTypes(colNameValue, metadata2, "insert");
		String ClusterName = "";
		String ClusterType = "";
		for (int i = 0; i < metadata2.size(); i++) {
			String[] line = metadata2.get(i);
			if (line[3].equals("true")) {
				ClusterName = line[1];
				ClusterType = line[2];
			}
		}
		Comparable ck = (Comparable) colNameValue.get(ClusterName);
		if (ClusterType.equals("java.util.Date")) {
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			try {
				ck = (Comparable) simpleDateFormat
						.parse(((Date) ck).getYear() + "-" + ((Date) ck).getMonth() + "-" + ((Date) ck).getDate());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// config file reading
		Properties config = new Properties();
		FileInputStream inConfig = null;
		try {
			inConfig = new FileInputStream("src/main/resources/DBApp.config");
			config.load(inConfig);
			inConfig.close();
		} catch (IOException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}
		int MaxRows = Integer.parseInt(config.getProperty("MaximumRowsCountinPage"));
		// System.out.println(MaxRows);
		// first insert
		Vector<String> pageNames = table.getPages();
		// System.out.println(pageNames.toString());
		if (pageNames.size() == 0) {
			Page table_initial_page = new Page(tableName, 0);
			pageNames.add(table_initial_page.getPage_name());
			table.setPages(pageNames);
			Vector<Record> newRecords = new Vector<Record>();
			Record r = new Record(colNameValue);
			newRecords.add(r);
			// System.out.println(table_initial_page.getPage_name());
			table_initial_page.setMax((Comparable) colNameValue.get(ClusterName));
			table_initial_page.setRecords(newRecords);
			savePage(table_initial_page);
			saveTable(table);
			return;
		}
		// general insert
		for (int i = 0; i < pageNames.size(); i++) {
			String pageName = pageNames.get(i);
			// System.out.println(pageName);
			Page p1 = loadPage(pageName);
			int high = p1.getRecords().size() - 1;
			// System.out.println(high);
			if ((high + 2) > MaxRows) {
				if (p1.getMax().compareTo(ck) > 0) {
					int index = binarySearchInsert(p1.getRecords(), ck, ClusterName, high);
					if (index == -1)
						return;
					Vector<Record> rec = p1.getRecords();
					rec.add(index, new Record(colNameValue));
					p1.setRecords(rec);
					Page nextPage = null;
					Record lastRec = p1.getRecords().remove(p1.getRecords().size() - 1);
					savePage(p1);
					if (i == pageNames.size() - 1) {
						nextPage = new Page(tableName, p1.getPage_index() + 1);
						nextPage.records.add(lastRec);
						nextPage.setMax(ck);
						String file_output = nextPage.getPage_name();
						table.getPages().add(file_output);
						savePage(nextPage);
						// savePage(p1);
						saveTable(table);
						return;
					} else {
						// Record lastRec = p1.getRecords().remove(p1.getRecords().size() - 1);
						// for (int j = i + 1; j < pageNames.size(); j++) {
						String nextPageName = pageNames.get(i + 1);
						nextPage = loadPage(nextPageName);
						int high1 = nextPage.getRecords().size() - 1;
						if ((high1 + 1) >= MaxRows) {
							Page newPage = new Page(tableName, nextPage.nor_index, -1);
							newPage.getRecords().add(lastRec);
							newPage.setMax((Comparable) lastRec.getColNameValue().get(ClusterName));
							pageNames.add(i + 1, newPage.getPage_name());
							// System.out.println ("table : " +table.getTable_name() + "Elements: " +
							// table.getPages().toString());
							savePage(newPage);
							saveTable(table);
							return;
						} else {
							nextPage.getRecords().add(0, lastRec);
							savePage(nextPage);
							return;
						}
					}
				} else {
					if (i != pageNames.size() - 1) {
						continue;
					} else {
						Page p2 = new Page(tableName, p1.getPage_index() + 1);
						p2.records.add(new Record(colNameValue));
						p2.setMax(ck);
						p2.setNmElemnts(p2.getNmElemnts() + 1);
						savePage(p2);
						Vector<String> newPages = table.getPages();
						newPages.addElement(p2.getPage_name());
						table.setPages(newPages);
						String file_output1 = table.getTable_name();
						saveTable(table);
						return;
					}
				}
			} else {
				// p1.Max= p1.records.get
				int index = binarySearchInsert(p1.getRecords(), ck, ClusterName, high);
				if (index == -1)
					return;
				p1.getRecords().add(index, new Record(colNameValue));
				if (p1.getMax().compareTo(ck) < 0) {
					p1.setMax(ck);
				}
				savePage(p1);
			}
		}
		saveTable(table);
	}

	// @Override
	// @SuppressWarnings({ "rawtypes", "unchecked", "unlikely-arg-type" })
	// @Override
	public void updateTable(String tableName, String clusteringKeyValue, Hashtable<String, Object> columnNameValue)
			throws DBAppException {
		// check input matches metadata types+constraints
		ArrayList<String[]> metadata2 = getMetadataString(tableName);
		// CheckTypes (columnNameValue, metadata2 , "update");
		// convert clustering key to type comparable
		// CheckTypes(colNameValue, metadata2);
		String ClusterName = "";
		String ClusterType = "";
		// System.out.println(metadata2.toString());
		for (int i = 0; i < metadata2.size(); i++) {
			String[] line = metadata2.get(i);
			// System.out.println(line.toString());
			if (line[3].equals("true")) {
				ClusterName = line[1];
				ClusterType = line[2];
			}
		}
		Comparable ck = (Comparable) clusteringKeyValue;
		if (!(columnNameValue.get(ClusterName) == null)) {
			throw new DBAppException("user entered ck");
		}
		switch (ClusterType) {
		case ("java.lang.Integer"):
			ck = (Comparable) Integer.parseInt(clusteringKeyValue);
			break;
		case ("java.lang.Double"):
			ck = (Comparable) Double.parseDouble(clusteringKeyValue);
			break;
		case ("java.util.Date"):
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			try {
				ck = (Comparable) simpleDateFormat.parse((String) ck);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		Table t = (Table) loadTable(tableName);
		// int p=binarySearchPage(t,ck);
		int r;
		for (int i = 0; i < t.getPages().size(); i++) {
			Page p1 = loadPage(t.getPages().get(i));
			Vector<Record> records = p1.getRecords();
			r = binarySearchUpdate(records, ck, ClusterName);
			ArrayList<String> keys = Collections.list(columnNameValue.keys());
			Hashtable toUpdate = records.get(0).getColNameValue();
			for (String key : keys) {
				if (toUpdate.get(key) == null) {
					throw new DBAppException("msh mawgod");
				} else {
					toUpdate.replace(key, columnNameValue.get(key));
				}
			}
			// System.out.println(t.getPages().size() + " index is : " + i );
			if (r != -1 && p1.getMax().compareTo(ck) > 0) {
				toUpdate = records.get(r).getColNameValue();
				for (String key : keys) {
					if (toUpdate.get(key) == null) {
						throw new DBAppException("msh mawgod");
					} else {
						toUpdate.replace(key, columnNameValue.get(key));
					}
				}
				p1.setRecords(records);
				savePage(p1);
				return;
			} else {
				continue;
			}
		}
	}

	@Override
	public void deleteFromTable(String tableName, Hashtable<String, Object> columnNameValue) throws DBAppException {
		// TODO Auto-generated method stub
		Table table = loadTable(tableName);
		String ClusterName = table.getClustering_key();
		Vector<String> currPages = table.getPages();
		Comparable ClusteringValue = (Comparable) columnNameValue.get(ClusterName);
		if (ClusteringValue != null) {
			for (int i = 0; i < currPages.size(); i++) {
				Page currPage = loadPage(currPages.get(i));
				if (currPage.getMax().compareTo(ClusteringValue) >= 0) {
					int index = binarySearchUpdate(currPage.getRecords(), ClusteringValue, ClusterName);
					if (index == -1)
						return;
					Record currRecord = currPage.getRecords().get(index);
					Hashtable RecTable = currRecord.getColNameValue();
					ArrayList<String> keys = Collections.list(RecTable.keys());
					boolean isMatch = true;
					boolean isMax = false;
					Comparable ClusterValue = (Comparable) RecTable.get(ClusterName);
					if (ClusterValue.compareTo(currPage.getMax()) == 0) {
						isMax = true;
					}
					for (String key : keys) {
						if (columnNameValue.get(key) == null)
							break;
						isMatch = isMatch && (RecTable.get(key).equals(columnNameValue.get(key)));
					}
					if (isMatch) {
						currPage.getRecords().remove(currRecord);
						// currPage.nmElemnts = currPage.nmElemnts - 1;
						// j--;
						if (isMax) {
							currPage.setMax((Comparable) currPage.getRecords().get(currPage.getRecords().size() - 1)
									.getColNameValue().get(ClusterName));
						}
					}
				} else {
					continue;
				}
				if (currPage.getRecords().isEmpty()) {
					Path path = FileSystems.getDefault()
							.getPath("src/main/resources/Data/Pages/" + currPages.get(i) + ".class");
					try {
						Files.delete(path);
						currPages.remove(i);
						saveTable(table);
					} catch (NoSuchFileException x) {
						System.err.format("%s: no such" + " file or directory%n", path);
					} catch (IOException x) {
						System.err.println(x);
					}
				}
			}
		} else {
			for (int i = 0; i < currPages.size(); i++) {
				Page currPage = loadPage(currPages.get(i));
				for (int j = 0; j < currPage.getRecords().size(); j++) {
					Record currRecord = currPage.getRecords().get(j);
					Hashtable RecTable = currRecord.getColNameValue();
					ArrayList<String> keys = Collections.list(RecTable.keys());
					boolean isMatch = true;
					boolean isMax = false;
					Comparable ClusterValue = (Comparable) RecTable.get(ClusterName);
					if (ClusterValue.compareTo(currPage.getMax()) == 0) {
						isMax = true;
					}
					for (String key : keys) {
						if (columnNameValue.get(key) == null)
							break;
						isMatch = isMatch && (RecTable.get(key).equals(columnNameValue.get(key)));
					}
					if (isMatch) {
						currPage.getRecords().remove(currRecord);
						currPage.nmElemnts = currPage.nmElemnts - 1;
						j--;
						if (isMax) {
							currPage.setMax((Comparable) currPage.getRecords().get(currPage.getRecords().size() - 1)
									.getColNameValue().get(ClusterName));
						}
					} else {
						continue;
					}
				}
				// int index=3;
				// p1.records.remove(index);
				//
				if (currPage.getRecords().isEmpty()) {
					Path path = FileSystems.getDefault()
							.getPath("src/main/resources/Data/Pages/" + currPages.get(i) + ".class");
					try {
						Files.delete(path);
						currPages.remove(i);
						saveTable(table);
					} catch (NoSuchFileException x) {
						System.err.format("%s: no such" + " file or directory%n", path);
					} catch (IOException x) {
						System.err.println(x);
					}
				}
			}
		}
	}

	@Override
	@SuppressWarnings({ "unlikely-arg-type", "unchecked" })
	public Iterator selectFromTable(SQLTerm[] sqlTerms, String[] arrayOperators) throws DBAppException {
		// check table names match
		String tablename = sqlTerms[0]._strTableName;
		for (int i = 1; i < sqlTerms.length; i++) {
			if (!(tablename.equals(sqlTerms[i]))) {
				throw new DBAppException("Different table names");
			}
		}
		// check column name exists in table
		ArrayList<String[]> metadata = getMetadataString(tablename);
		for (int i = 0; i < sqlTerms.length; i++) {
			for (int j = 0; j < metadata.size(); j++) {
				if ((metadata.get(j))[0].equals(tablename)) {
					for (int k = 0; k < (metadata.get(j)).length; k++) {
						if (!((metadata.get(j))[k].equals(sqlTerms[i]._strColumnName))) {
							throw new DBAppException("Column names don't match metadata types");
						}
					}
				}
			}
		}
		// load table
		Table table = loadTable(tablename);
		ArrayList<Record> r = new ArrayList<Record>();
		ArrayList<Record> r2 = new ArrayList<Record>();
		// load pages
		Vector<String> pages = table.getPages();
		ArrayList<Record> temp;
		// go over input sqlTerms
		for (int i = 0; i < sqlTerms.length; i++) {
			temp = new ArrayList<Record>();
			// add records of page
			for (int j = 0; j < pages.size(); j++) {
				Page cur = loadPage(pages.get(i));
				Vector<Record> records = cur.getRecords();
				// add records to temp
				for (int k = 0; k < records.size(); k++) {
					Comparable x = (Comparable) (records.get(k).getColNameValue()).get(sqlTerms[0]._strColumnName);
					Comparable y = (Comparable) (sqlTerms[i]._objValue);
					switch (sqlTerms[i]._strOperator) {
					case ">":
						if ((x.compareTo(y) > 0))
							temp.add(records.get(k));
					case ">=":
						if ((x.compareTo(y) >= 0))
							temp.add(records.get(k));
					case "<":
						if ((x.compareTo(y) < 0))
							temp.add(records.get(k));
					case "<=":
						if ((x.compareTo(y) <= 0))
							temp.add(records.get(k));
					case "!=":
						if ((x.compareTo(y) > 0) || (x.compareTo(y) < 0))
							temp.add(records.get(k));
					case "=":
						if ((x.compareTo(y) == 0))
							temp.add(records.get(k));
					}
				}
			}
			String op = arrayOperators[i];
			switch (op) {
			case "AND":
				for (Record t : temp) {
					if (r.contains(t)) {
						r2.add(t);
					}
				}
			case "OR":
				r2.addAll(temp);
			case "XOR":
				// CODE FOR XOR
			}
		}
		Iterator<Record> result = r2.iterator();
		return result;
	}

	public static int binarySearchUpdate(Vector<Record> records, Comparable key, String cluster) throws DBAppException {
		int low = 0;
		int high = records.size() - 1;
		int mid;
		while (low <= high) {
			mid = low + (high - low) / 2;
			Comparable cur = (Comparable) (((records.get(mid)).getColNameValue()).get(cluster));
			// Check if key is present at mid
			if (cur == null)
				throw new DBAppException("element not found");
			if (cur.compareTo(key) == 0)
				return mid;
			// If key greater, ignore left half
			if (cur.compareTo(key) < 0)
				low = mid + 1;
			// If key is smaller, ignore right half
			else
				high = mid - 1;
		}
		// if we reach here, then element was not present
		return -1;
	}

	public static int binarySearchPage(Table t, Comparable key) throws IOException, ClassNotFoundException {
		int low = 0;
		Vector<String> pages = t.getPages();
		int high = pages.size() - 1;
		int mid;
		while (low <= high) {
			mid = low + (high - low) / 2;
			FileInputStream fileIn = new FileInputStream("src/Pages/" + t.getTable_name() + mid + ".ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Page p1 = (Page) in.readObject();
			in.close();
			fileIn.close();
			Comparable max = p1.getMax();
			// Check if key is present at mid (last record in page)
			if (max.compareTo(key) == 0) {
				return mid;
			}
			// If key greater than max, ignore left half
			if (max.compareTo(key) < 0)
				low = mid + 1;
			// If key is smaller than max, ignore right half
			else
				high = mid - 1;
		}
		// if we reach here, then element was not present
		return -1;
	}

	private ArrayList<String[]> getMetadataString(String tableName) {
		ArrayList<String[]> metadata2 = new ArrayList<String[]>();
		try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/metadata.csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].equals(tableName))
					metadata2.add(values);
			}
		} catch (FileNotFoundException e6) {
			// TODO Auto-generated catch block
			e6.printStackTrace();
		} catch (IOException e6) {
			// TODO Auto-generated catch block
			e6.printStackTrace();
		}
		return metadata2;
	}

	public Page loadPage(String pageName) {
		FileInputStream fileIn;
		ObjectInputStream in;
		Page p1 = null;
		try {
			fileIn = new FileInputStream("src/main/resources/Data/Pages/" + pageName + ".class");
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

	public void saveTable(Table table) {
		FileOutputStream fileOut;
		ObjectOutputStream out;
		try {
			fileOut = new FileOutputStream("src/main/resources/Data/Tables/" + table.getTable_name() + ".class");
			out = new ObjectOutputStream(fileOut);
			out.writeObject(table);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void savePage(Page table_initial_page) {
		FileOutputStream fileOut;
		ObjectOutputStream out;
		try {
			fileOut = new FileOutputStream(
					"src/main/resources/Data/Pages/" + table_initial_page.getPage_name() + ".class");
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

	public static Table loadTable(String tableName) {
		FileInputStream fileIn;
		ObjectInputStream in = null;
		Table table = null;
		try {
			fileIn = new FileInputStream("src/main/resources/Data/Tables/" + tableName + ".class");
			in = new ObjectInputStream(fileIn);
			table = (Table) in.readObject();
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException | IOException e7) {
			// TODO Auto-generated catch block
			e7.printStackTrace();
		}
		return table;
	}

	public static int binarySearchInsert(Vector<Record> records, Comparable key, String Cluster, int high2)
			throws DBAppException {
		int low = 0, high = high2;
		int mid = low + (high - low) / 2;
		while (low <= high) {
			mid = low + (high - low) / 2;
			Comparable index = ((Comparable) records.get(mid).getColNameValue().get(Cluster));
			// System.out.println(key.toString() + " Index is : " + index.toString() + " loc
			// : " + mid);
			// Check if x is present at mid
			if (index.compareTo(key) == 0) {
				return -1;
				// System.out.println(key.toString() + " Index is : " + index.toString() + " loc
				// : " + mid + " Duplicateeess ");
				// throw new DBAppException("KEY ALREADY FOUND");
				// return mid ;
			}
			// If x greater, ignore left half
			if (index.compareTo(key) < 0)
				low = mid + 1;
			// If x is smaller, ignore right half
			else
				high = mid - 1;
		}
		// if we reach here, then element was
		// not present
		// Comparable index = ((Comparable)
		// records.get(mid).getColNameValue().get(Cluster));
		return mid;
	}

	private void CheckTypes(Hashtable<String, Object> colNameValue, ArrayList<String[]> metadata2, String type)
			throws NumberFormatException, DBAppException {
		for (int i = 0; i < metadata2.size(); i++) {
			String[] line = metadata2.get(i);
			Comparable value = (Comparable) colNameValue.get(metadata2.get(i)[1]);
			if (value != null) {
				Class<? extends Object> c = value.getClass();
				boolean cond = (line[2].equals(c.getName()));
				switch (c.getName()) {
				case ("java.lang.Integer"):
					int v = (int) c.cast(value);
					if (!(cond && (v >= Integer.parseInt(line[5])) && (v <= Integer.parseInt(line[6])))) {
						throw new DBAppException("incompatible types  " + v);
					}
					break;
				case ("java.lang.String"):
					String u = (String) c.cast(value);
					if ((u.matches("[0-9]+") && u.length() > 2) && line[5].matches("[0-9]+") && line[5].length() > 2
							&& line[6].matches("[0-9]+") && line[6].length() > 2) {
						int newValue = Integer.parseInt(u);
						int min = Integer.parseInt(line[5]);
						int max = Integer.parseInt(line[6]);
						if (!(cond && newValue >= min && newValue <= max)) {
							throw new DBAppException("incompatible types " + u + " line : " + line[1] + "  " + cond);
						}
					} else {
						if (!(cond && ((u.compareTo(line[5]) >= 0 && (u.compareTo(line[6]) <= 0))))) {
							// if (u.compareTo(line.get(6)) > 0 && u.length() <= line.get(6).length() ) {
							// break ;
							// }
							// System.out.println("value is : " + u + " line : " + line.get(0) + " " +
							// line.get(1));
							throw new DBAppException("incompatible types " + u + " line : " + line[1] + "  " + cond);
						}
						break;
					}
				case ("java.util.Date"):
					String pattern = "yyyy-MM-dd";
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
					Comparable min = null;
					Comparable max = null;
					try {
						min = (Comparable) simpleDateFormat.parse(line[5]);
						max = (Comparable) simpleDateFormat.parse(line[6]);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						return;
						// throw new DBAppException();
						// e.printStackTrace();
					}
					// System.out.println(min + " " + max + " " + value);
					if (!(cond && (value.compareTo(min) >= 0 && (value.compareTo(max) <= 0))))
						throw new DBAppException("incompatible types");
					break;
				case ("java.lang.Double"):
					double x = (double) c.cast(value);
					if (!(cond && (x >= Double.parseDouble(line[5])) && (x <= Double.parseDouble(line[6])))) {
						// System.out.println(x);
						// System.out.println(cond);
						throw new DBAppException("incompatible types");
					}
					break;
				default:
					// throw new DBAppException("incompatible types");
				}
			} else {
				if (line[3].equals("true") && type.equals("insert"))
					throw new DBAppException("Invalid insertion key not provided");
			}
		}
	}

	public static void main(String[] args) {
		Comparable a = 1111;
		Comparable b = 202;
		// System.out.println(b.compareTo(a));
		DBApp db = new DBApp();
		Page p1 = db.loadPage("courses0");
		Page p2 = db.loadPage("pcs100");
		System.out.println("p1 size :" + p1.getRecords().size() + " p2 size : " + p2.getRecords().size());
	}
}
