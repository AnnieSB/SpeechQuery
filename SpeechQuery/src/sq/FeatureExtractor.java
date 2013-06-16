package sq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;



/**
 * 
 * @author PhuongAnh
 *
 * This class is responsible for pattern induction. It computes the Discrete Fourier Transformation on the audio signal
 * and computes the pitch, rhythm and the acoustic finger print for indexing purposes.
 * It provides a list of hashes and the corresponding melody and rhythm scores of the audio signal.
 * 
 */
 

public class FeatureExtractor {
	
	final int SAMPLE_RATE = 44100;
	final int J = 1000;
	double[] freqTable;
    double[] notePitchTable;
	double[] window;
	String[] noteNameTable;
	
	double[] pitchValues;
	double[] beats;
	
	/**
	 * Initializes the FeatureExtractor. Builds the frequency table.
	 */
	public FeatureExtractor(){
		 freqTable = new double[Harvester.CHUNK_SIZE];
		 noteNameTable = new String[Harvester.CHUNK_SIZE];
		 notePitchTable = new double[Harvester.CHUNK_SIZE];
		 window = new double[Harvester.CHUNK_SIZE];
		 
		 
		//build Hamming window
			buildHammingWindow(window,Harvester.CHUNK_SIZE);
			
			//freq/note tables
			   for( int i=0; i<Harvester.CHUNK_SIZE; ++i ) {
			      freqTable[i] = ( SAMPLE_RATE * i ) / (float) ( Harvester.CHUNK_SIZE );
				  noteNameTable[i] = "";
			      notePitchTable[i] = -1;
			   }
			   for( int i=0; i<127; ++i ) {
			      double pitch = ( 440.0 / 32.0 ) * Math.pow( 2, (i-9.0)/12.0 ) ;
			      if( pitch > SAMPLE_RATE / 2.0 )
			         break;
			      //find the closest frequency using brute force.
			      double min = 1000000000.0;
			      int index = -1;
			      for( int j=0; j<Harvester.CHUNK_SIZE; ++j ) {
			         if( Math.abs( freqTable[j]-pitch ) < min ) {
			             min = Math.abs( freqTable[j]-pitch );
			             index = j;
			         }
			      }
			      notePitchTable[index] = pitch;
			   }
	}
	
	/**
	 * Indexes the features of the input audio signal.
	 * @param audio
	 */
	/**
	 * @param audio
	 * @param songIndex
	 * @return
	 */
	public Map<Long, DataPoint> index(byte[] audio, int songIndex){
		
		//FFT of the audio signal
		Complex[][] data = transform(audio);
		
		long startTime = System.nanoTime();
		//Melody extraction:
		pitchValues = new double[data.length];
		for(int i=0; i<data.length;i++){
			pitchValues[i] = computePitch(data[i]);	
		}
		System.out.println("Melody extraction: " + ((System.nanoTime() - startTime)%1000000) + " ms");
		
		startTime = System.nanoTime();
		//Rhythm detection
		beats = new double[data.length];
		for(int j=0; j<data.length;j++){
			beats[j] = computeRhythm(data[j]);
		}
		//Normalization
		double minVal = beats[0];
	    int minIndex = 0;
		for(int k=1; k<beats.length;k++){
			if( beats[k] < minVal ) {
	            minVal = beats[k];
	            minIndex = k;
	         }
		}
		StandardDeviation sd = new StandardDeviation();
		double o = sd.evaluate(beats);
		for(int n=0; n<beats.length;n++){
			beats[n] = (beats[n] - beats[minIndex]) / o;
		}
		System.out.println("Rhythm extraction: " + ((System.nanoTime() - startTime)%1000000 + " ms"));
		
		startTime = System.nanoTime();
		//fingerprint
		Map<Long, DataPoint> fingerprint = computeAF(data,songIndex);
		System.out.println("AF: " + ((System.nanoTime() - startTime)%1000000) + " ms");
		
		return fingerprint;
	}
	
	/**
	 * 
	 * @param data
	 * @return the Pitch value of one Chunk
	 */
	private double computePitch(Complex[] data){
	
		 //find the peak
		      double maxVal = -1;
		      int maxIndex = -1;
		      for( int j=0; j<Harvester.CHUNK_SIZE/2; ++j ) {
		         double v = data[j].abs() ;
		         if( v > maxVal ) {
		            maxVal = v;
		            maxIndex = j;
		         }
		      }
		      
		      //double freq = freqTable[maxIndex];
		      
		    //find the nearest note:
		      int nearestNoteDelta=0;
//		      while( true ) {
//		          if( nearestNoteDelta < maxIndex && !noteNameTable[maxIndex-nearestNoteDelta].equals("") ) {
//		             nearestNoteDelta = -nearestNoteDelta;
//		             break;
//		          } else if( nearestNoteDelta + maxIndex < Harvester.CHUNK_SIZE && !noteNameTable[maxIndex+nearestNoteDelta].equals("") ) {
//		             break;
//		          }
//		          ++nearestNoteDelta;
//		       }
		      
		      double nearestNotePitch = notePitchTable[maxIndex];
		      
		      return nearestNotePitch;
	}
	
	private double computeRhythm(Complex[] data){
		double value = 0;
		for(int i=0; i<Harvester.CHUNK_SIZE;i++){
			value += data[i].abs() * data[i].abs();
		}
	
		double l = Math.log(1 + (J/Harvester.CHUNK_SIZE) * value);
		
		return l;
	}
	
	
	public static final int[] RANGE = new int[] {40,80,120,180,240, Harvester.UPPER_LIMIT+1};
	public double[] highscores = new double[RANGE.length];
	public int[] recordPoints = new int[RANGE.length];
	public int[] secondRecordPoints = new int[RANGE.length];
	public int[] thirdRecordPoints = new int[RANGE.length];
	String recordNumbers = "";
	
	/**
	 * This method determines the key music points of the transformed input data.
	 * The most important frequencies in a data line are saved in recordPoints[].
	 * For the search input, the 'songID' is zero.
	 **/
	public Map<Long,DataPoint> computeAF(Complex[][] results, int songID){
		Map<Long,DataPoint> hashcodes = new HashMap<Long,DataPoint>();
		//List<DataPoint> datapoints = new ArrayList<DataPoint>();
		
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
		 DataPoint dp;

			//for(int j=0; j<recordPoints.length;j++){
				 //Zeit herausfinden
				 dp = new DataPoint(songID, i);
				// datapoints.add(dp);
			 //} 
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
	
	//Find out in which range
	public static int getIndex(int freq) {
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
	private List<Long> hash(String line) {
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
//	    	results.add((p2-(p2%FUZ_FACTOR)) * 100 + (p3-(p3%FUZ_FACTOR)));
	    }
	    return results;
	}
	
	/**
	 * This method transforms the sound data into frequency domain with the Discrete Fourier Transformation 
	 * @param the Recorder, which read the audio file
	 * @return a double array with the frequencies of all chunks
	 */
	private Complex[][] transform (byte[] audio){
		
		final int totalSize = audio.length;
		int amountPossible = totalSize/Harvester.CHUNK_SIZE;
		//When turning into frequency domain we'll need complex numbers:
		Complex[][] results = new Complex[amountPossible][];
		//For all the chunks:
		for(int times = 0;times < amountPossible; times++) {
		    Complex[] complex = new Complex[Harvester.CHUNK_SIZE];
		    for(int i = 0;i < Harvester.CHUNK_SIZE;i++) {
		        //Put the time domain data into a complex number with imaginary part as 0:
		        complex[i] = new Complex(audio[(times*Harvester.CHUNK_SIZE)+i], 0);
		      //apply Hamming window
		        if(window.length != 0)
		        complex[i].setRe(complex[i].re() * window[i]) ;
		        
		    }
		    //Perform FFT analysis on the chunk:
		    results[times] = FFT.fft(complex);
		}
		return results;
	}
	
	private void buildHammingWindow( double[] window, int size ){
	   for( int i=0; i<size; ++i ){
	      window[i] = .54 - .46 * Math.cos( 2 * Math.PI * i / (float) size );
	   }
	}

	public double[] getPitchValues() {
		return pitchValues;
	}

	public double[] getBeats() {
		return beats;
	}

		

}