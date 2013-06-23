package sq;

/**
 * Contains the algorithms for computing the Dynamic Time Warping. Is used for ranking purposes.
 * @author PhuongAnh
 *
 */
public class DTW {

	// epsilon constant
	final int E = 1; 
	
	double[] s1;
	double[] s2;
	
	public DTW(double[] s1, double[] s2){
		this.s1 = s1;
		this.s2 = s2;
	}
	
	double cost = 0;
	/**
	 * 
	 * @param n size of s1
	 * @param m size of s2
	 * @return
	 */
	public double cost (int n, int m){
		if(n>=0 && m>=0)
		   cost = Math.abs(s1[n]-s2[m]) * E + Math.min(Math.min(cost(n-2,m-1),cost(n-1,m-1)),cost(n-1,m-2));
		else
		   cost = 0;
		return cost;
	}
	

	
}
