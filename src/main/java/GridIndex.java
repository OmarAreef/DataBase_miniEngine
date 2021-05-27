import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

public class GridIndex implements Serializable {
	NDimensionalArray grid;
	Table table;
	String[] columnNames;
	Hashtable<String, Comparable[]> ranges;

	public GridIndex(Table table, String[] columnNames) throws ParseException {
		super();
		this.table = table;
		this.columnNames = columnNames;
		this.ranges = new Hashtable<String,Comparable[]> ();
		ArrayList<String[]> metadata = table.getMetadata();
		int[] dimensions = new int[columnNames.length];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = 10;
		}
		this.grid = new NDimensionalArray(dimensions);
		for (int i = 0; i < metadata.size(); i++) {
			for (int j = 0; j < columnNames.length; j++) {
				if (metadata.get(i)[1].equals(columnNames[j])) {
					metadata.get(i)[4] = "true";
					Object min;
					Object max;
					Object[] range = new Object[10];
					switch (metadata.get(i)[2]) {
					case "java.lang.Double":
						min = Double.parseDouble(metadata.get(i)[5]);
						max = Double.parseDouble(metadata.get(i)[6]);
						range = getRange((double) min, (double) max);
						break;
					case "java.lang.Integer":
						min = Integer.parseInt(metadata.get(i)[5]);
						max = Integer.parseInt(metadata.get(i)[6]);
						range = getRange((int) min, (int) max);
						break;
					case "java.util.Date":
						min = metadata.get(i)[5];
						max = metadata.get(i)[6];
						range = getRangeDate((String) min, (String) max);
						break;
					default:
						min = metadata.get(i)[5];
						max = metadata.get(i)[6];
						range = getRange((String) min, (String) max);
					}
					this.ranges.put(columnNames[j], (Comparable[]) range);
					// ranges.put(columnNames[j], columnNames)
				}
			}
		}
	}

	public static int sumBytes(byte[] bytes) {
		int s = 0;
		for (int el : bytes) {
			s = s + el;
		}
		System.out.println(s);
		// int sum = Integer.parseInt(s);
		return s;
	}

	public static Object[] getRange(String s1, String s2) {
		byte[] s1Bytes = s1.getBytes();
		byte[] s2Bytes = s2.getBytes();
		int k = s2Bytes[0] - s1Bytes[0];
		int interval = k / 9;
		System.out.println(interval);
		byte[] range = new byte[10];
		int value = s1Bytes[0] + interval;
		for (int i = 0; i < range.length; i++) {
			range[i] = (byte) value;
			value = value + interval;
		}
		String[] ranges = new String[10];
		for (int i = 0; i < 10; i++) {
			byte[] word = new byte[s1Bytes.length];
			Arrays.fill(word, range[i]);
			ranges[i] = new String(word);
		}
		return ranges;
	}

	public static Object[] getRange(int s1, int s2) {
		int k = s2 - s1;
		int interval = k / 10;
		Object[] ranges = new Object[10];
		int value = s1 + interval;
		for (int i = 0; i < ranges.length; i++) {
			ranges[i] = (int) value;
			value = value + interval;
		}
		return ranges;
	}

	public static Object[] getRange(double s1, double s2) {
		double k = s2 - s1;
		double interval = k / 10;
		Object[] ranges = new Object[10];
		double value = s1 + interval;
		for (int i = 0; i < ranges.length; i++) {
			ranges[i] = (double) value;
			value = value + interval;
		}
		return ranges;
	}

	public static Object[] getRangeDate(String s1, String s2) throws ParseException {
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date min = null;
		Date max = null;
		min = simpleDateFormat.parse(s1);
		max = simpleDateFormat.parse(s2);
		long minDays = min.getTime();
		long maxDays = max.getTime();
		long k = maxDays - minDays;
		long interval = k / 10;
		Object[] ranges = new Object[10];
		long value = minDays + interval;
		for (int i = 0; i < ranges.length; i++) {
			Date toSet = new Date((long) value);
			ranges[i] = toSet;
			value = value + interval;
		}
		return ranges;
	}

	public static void main(String[] args) throws ParseException {
		// System.out.println(Arrays.toString(
		// "Thequickbrownfoxjumps".split("(?<=\\G.{2})")
		// ));
		String s1 = "AAAAAA";
		String s2 = "zzzzzz";
		// System.out.println("".compareTo("z"));
		// byte[] s1Bytes = s1.getBytes();
		// byte[] s2Bytes = s2.getBytes();
		// int k = s2Bytes[0]- s1Bytes[0];
		// int interval = k/11;
		// System.out.println(interval);
		// byte[] range = new byte[10];
		// int value = s1Bytes[0]+interval;
		// for(int i = 0 ; i < range.length ; i ++ ) {
		//
		// range[i] = (byte) value;
		// value = value + interval ;
		// }
		// String [] ranges = new String[10];
		// for (int i = 0 ; i < 10 ; i++) {
		// byte[] word = new byte[s1Bytes.length];
		// Arrays.fill(word, range[i]);
		// ranges[i] = new String (word);
		//
		// }
		String[] ranges1 = (String[]) getRange(s1, s2);
		Object[] ranges2 = getRangeDate("1990-01-01", "2020-12-31");
		System.out.println();
		for (int i = 0; i < ranges2.length; i++) {
			System.out.print(ranges2[i] + "...");
		}
		// for(int i = 0 ; i < s1Bytes.length ; i ++ ) {
		// System.out.print(s1Bytes[i] + "...");
		// }
		// System.out.println ();
		// for(int i = 0 ; i < s2Bytes.length ; i ++ ) {
		// System.out.print(s2Bytes[i] + "...");
		// }
		// System.out.println ();
		// for(int i = 0 ; i < range.length ; i ++ ) {
		// System.out.print(range[i] + "...");
		// }
		System.out.println();
		for (int i = 0; i < ranges1.length; i++) {
			System.out.print(ranges1[i] + "...");
		}
		// int[] dimensions = new int[2];
		// dimensions[0] = 10;
		// dimensions[1] = 10;
		// NDimensionalArray dm = new NDimensionalArray(dimensions);
		// int[] index = new int[dm.getDimensions().length];
		// System.out.println(dm.getArray().length);
		// // System.out.println(dm.getArray() instanceof Array);
		// ArrayList<Integer> indexes = new ArrayList<Integer>();
		// index[1] = 0;
		// for (int i = 0; i < 10; i++) {
		// index[0] = i;
		// dm.set(i + " I am here", index);
		// }
		// for (int i = 0; i < dm.getArray().length; i++) {
		// System.out.print(dm.getArray()[i] + " ... " + "index : " + i + " ");
		// // System.out.println(dm.get(i/divider) + " iii : " + i );
		// System.out.println();
		// }
		// ArrayList<Integer> original = new ArrayList<Integer>();
		// original.add(10);
		// original.add(5);
		// original.add(3);
		// System.out.println(indexes.size());
		// System.out.println(original.size());
		// accessArray(indexes , original);
	}
	// public static void accessArray (ArrayList<Integer> indexes ,
	// ArrayList<Integer> original) {
	// boolean t = true ;
	// int indexTobeIncremented = 0 ;
	//// System.out.println(indexes.size());
	//// System.out.println(original.size());
	// for (int i = 0 ; i < indexes.size() ; i ++ ) {
	// t = t && (indexes.get(i)> original.get(i));
	// System.out.println(t);
	// }
	// if (t) {
	// return ;
	// }
	// else {
	// String s = "Pos is : " ;
	// for (int i = 0 ; i < indexes.size() ; i ++ ) {
	// s += indexes.get(i) + "...";
	// }
	// System.out.println(s);
	// for (int j = indexes.size()-1 ; j > 0 ; j--) {
	// System.out.print(" j is :" + j );
	// if (indexes.get(j-1)==original.get(j-1)) {
	// indexes.set(j, indexes.get(j)+1) ;
	// indexes.set(j-1, 0) ;
	// //System.out.print("j is :" + j );
	// }
	// if (j==1) {
	// indexes.set(j-1, indexes.get(j-1)+1);
	// }
	//
	// }
	// accessArray(indexes , original);
	//
	//
	// }
	//
	//
	//
	//
	// }
}
