package sq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;



/**
 * 
 * @author PhuongAnh
 *
 * This class is responsible for pattern induction. It computes the Discrete Fourier Transformation on the audio signal
 * and computes the pitch, rhythm and the acoustic finger print for indexing purposes.
 * It provides a list of hashes and the corresponding melody and rhythm scores of the audio signal.
 * 
 */
 

public class AcousticFingerprinter implements Callable<Map<Long,DataPoint>>{
	
//	final int SAMPLE_RATE = 44100;
	
//	double[] freqTable;
//    double[] notePitchTable;
//	//double[] window;
//	String[] noteNameTable;
	
	double[] pitchValues;
	double[] beats;
	
	Complex[][] data;
	int songIndex;
	Map<Long,DataPoint> fingerprint;
	
	/**
	 * Initializes the FeatureExtractor. Builds the frequency table.
	 */
	public AcousticFingerprinter(Complex[][] data, int songIndex){
		this.data = data;
		this.songIndex = songIndex;
	}
	
	
	public static final int[] RANGE = new int[] {40,80,120,180,240, Harvester.UPPER_LIMIT+1};
	public static double[] highscores = new double[RANGE.length];
	public static int[] recordPoints = new int[RANGE.length];
	public static int[] secondRecordPoints = new int[RANGE.length];
	public static int[] thirdRecordPoints = new int[RANGE.length];
	
	/**
	 * This method determines the key music points of the transformed input data.
	 * The most important frequencies in a data line are saved in recordPoints[].
	 * For the search input, the 'songID' is zero.
	 **/
	public Map<Long,DataPoint> computeAF(Complex[][] results, int songID){
		Map<Long,DataPoint> hashcodes = new HashMap<Long,DataPoint>();
		//List<DataPoint> datapoints = new ArrayList<DataPoint>();
		
		System.out.println(results.length);
		DataPoint dp;
		String recordNumbers = ""; 
		for(int i = 0; i < results.length; i++) {
		for (int freq = Harvester.LOWER_LIMIT; freq < Harvester.UPPER_LIMIT-1; freq++) {
			    //Get the magnitude:
			   double mag = Math.log(results[i][freq].abs() + 1);
			    //Find out which range we are in:
			    int index = getIndex(freq);
			    //Save the highest magnitude and corresponding frequency:
			    if (mag > highscores[index]) {
			    	thirdRecordPoints[index] = secondRecordPoints[index];
			    	secondRecordPoints[index] = recordPoints[index];
			        highscores[index] = mag;
			        recordPoints[index] = freq;
			    }
		}
		
		 //Punkte als Text speichern
		 for(int k=0; k <5;k++){
			 recordNumbers = recordNumbers + recordPoints[k] + "," + secondRecordPoints[k] + "," + thirdRecordPoints[k] + "\t";
			 
		 }
		 
		 //Hashes generieren und mit DataPunkten in HashDB speichern
		 
				 dp = new DataPoint(songID, i);
				 
		    List<Long> hashes = hash(recordNumbers);
		    for(int n=0; n<hashes.size();n++){
			   hashcodes.put(hashes.get(n), dp);	
		    }

			//Reset recordNumbers
			recordNumbers = "";
		}
		//System.out.println("Indexing done!");
		return hashcodes;
	} 
	
	/**
	 * This method determines the key music points of the transformed input data.
	 * The most important frequencies in a data line are saved in recordPoints[].
	 * For the search input, the 'songID' is zero.
	 **/
	public static Map<Long,DataPoint> computeAF_DB(Complex[] data, int songID, int time){
		Map<Long,DataPoint> hashcodes = new HashMap<Long,DataPoint>();
		DataPoint dp;
		String recordNumbers = "";
		for (int freq = Harvester.LOWER_LIMIT; freq < Harvester.UPPER_LIMIT-1; freq++) {
			    //Get the magnitude:
			   double mag = Math.log(data[freq].abs() + 1);
			    //Find out which range we are in:
			    int index = getIndex(freq);
			    //Save the highest magnitude and corresponding frequency:
			    if (mag > highscores[index]) {
			    	thirdRecordPoints[index] = secondRecordPoints[index];
			    	secondRecordPoints[index] = recordPoints[index];
			        highscores[index] = mag;
			        recordPoints[index] = freq;
			    }
		}
		
		 //Punkte als Text speichern
		 for(int k=0; k <5;k++){
			 recordNumbers = recordNumbers + recordPoints[k] + "," + secondRecordPoints[k] + "," + thirdRecordPoints[k] + "\t";
			 
		 }
		 
		 //Hashes generieren und mit DataPunkten in HashDB speichern
		 
				 dp = new DataPoint(songID, time);
				 
		    List<Long> hashes = hash(recordNumbers);
		    for(int n=0; n<hashes.size();n++){
			   hashcodes.put(hashes.get(n), dp);	
		    }

			//Reset recordNumbers
			recordNumbers = "";
		
		//System.out.println("Indexing done!");
		return hashcodes;
	}
	
	//Find out in which range
	private static int getIndex(int freq) {
	    int i = 0;
	    while(RANGE[i] < freq) i++;
	        return i;
	    }
	
	//hashing
	private static final int FUZ_FACTOR = 2;
	
	/**
	 * This method creates a hash code for the retrieved key points sequence.
	 * @param keypoints
	 * @return
	 */
	private static List<Long> hash(String line) {
	    String[] p = line.split("\t");
	    List<Long> results = new ArrayList<Long>();
	    long p1;
	    long p2;
	    long p3;
	    for(int i=0; i<p.length; i++){
	    	String[] ppoints = p[i].split(",");
	    	p1 = Long.parseLong(ppoints[0]);
	    	p2 = Long.parseLong(ppoints[1]);
	    	p3 = Long.parseLong(ppoints[2]);
	    	results.add((p1-(p1%FUZ_FACTOR) * 100 + (p2-(p2%FUZ_FACTOR))));
	    	results.add((p1-(p1%FUZ_FACTOR)) * 100 + (p3-(p3%FUZ_FACTOR)));
	    	//results.add((p2-(p2%FUZ_FACTOR)) * 100 + (p3-(p3%FUZ_FACTOR)));
	    }
	    return results;
	}

	public double[] getPitchValues() {
		return pitchValues;
	}

	public double[] getBeats() {
		return beats;
	}

	public Map<Long, DataPoint> getFingerprint() {
		return fingerprint;
	}

	@Override
	public Map<Long, DataPoint> call() throws Exception {
		// TODO Auto-generated method stub
		
		long startTime = System.nanoTime();
		//fingerprint
		this.fingerprint = computeAF(data,this.songIndex);
		System.out.println("AF: " + ((System.nanoTime() - startTime)%1000000) + " ms");
		
		return this.fingerprint;
	}

	
		

}