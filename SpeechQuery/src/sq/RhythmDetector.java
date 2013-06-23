package sq;

import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;


/**
 * This class computes the rhythm of an input audio signal. Returns the pitch values.
 * @author PhuongAnh
 *
 */
public class RhythmDetector implements Callable<double[]>{

	Complex[][] data;
	int songIndex;
	double[] beats;
	
	
	@Override
	public double[] call() throws Exception {
		// TODO Auto-generated method stub
		
		//Rhythm detection
		long startTime = System.nanoTime();
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
		System.out.println("Rhythm extraction: " + ((System.nanoTime() - startTime)%1000000 + " ms") + ", Songindex: "+songIndex);
		
		return beats;
	}
	
	public static double computeRhythm(Complex[] data){
		double value = 0;
		for(int i=0; i<Harvester.CHUNK_SIZE;i++){
			value += data[i].abs() * data[i].abs();
		}
	
		double l = Math.log(1 + (Harvester.J/Harvester.CHUNK_SIZE) * value);
		
		return l;
	}
}
