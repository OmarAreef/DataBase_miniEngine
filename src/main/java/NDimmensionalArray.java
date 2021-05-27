class NDimensionalArray {
  private Object[] array; // internal representation of the N-dimensional array
  private int[] dimensions; // dimensions of the array
  private int[] multipliers; // used to calculate the index in the internal array

  NDimensionalArray(int[] dimensions) {
    int arraySize = 1;

    multipliers = new int[dimensions.length];
    for (int idx = dimensions.length - 1; idx >= 0; idx--) {
      multipliers[idx] = arraySize;
      arraySize *= dimensions[idx];
    }
    setArray(new Object[arraySize]);
    this.setDimensions(dimensions);
  }
 
  public Object get(int [] indices) {
    assert indices.length == getDimensions().length;
    int internalIndex = 0;

    for (int idx = 0; idx < indices.length; idx++) {
      internalIndex += indices[idx] * multipliers[idx];
    }
    return getArray()[internalIndex];
  }
  public void set (Object o, int[] indices  ) {
	    assert indices.length == getDimensions().length;
	    int internalIndex = 0;

	    for (int idx = 0; idx < indices.length; idx++) {
	      internalIndex += indices[idx] * multipliers[idx];
	    }
	     getArray()[internalIndex] = o;
	  }
  public static void main(String [] args) {
	  int[] dimensions = new int[1];
	  dimensions[0]=10;
	  dimensions[1]=5;
	  NDimensionalArray dm = new NDimensionalArray (dimensions) ;
//	  for (int i = 0 ; i < dm.getArray().length ; i ++ ) {
//		  System.out.println(dm.get(i));
//	  }
  }

public Object[] getArray() {
	return array;
}

public void setArray(Object[] array) {
	this.array = array;
}

public int[] getDimensions() {
	return dimensions;
}

public void setDimensions(int[] dimensions) {
	this.dimensions = dimensions;
}
 
}