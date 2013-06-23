package sq;

import java.util.concurrent.Callable;

/**
 * This class extracts the melody of the audio signal by computing the pitch.
 * @author PhuongAnh
 *
 */
public class MelodyExtractor implements Callable<double[]>{
	
	double[] freqTable;
    double[] notePitchTable;
	//double[] window;
	String[] noteNameTable;
	
	double[] pitchValues;
	Complex[][] data;
	
	public MelodyExtractor(Complex[][] data){
		this.data = data;
		
		freqTable = new double[Harvester.CHUNK_SIZE];
		 noteNameTable = new String[Harvester.CHUNK_SIZE];
		 notePitchTable = new double[Harvester.CHUNK_SIZE];
		 
		//freq/note tables
		   for( int i=0; i<Harvester.CHUNK_SIZE; ++i ) {
		      freqTable[i] = ( Harvester.SAMPLE_RATE * i ) / (float) ( Harvester.CHUNK_SIZE );
			  noteNameTable[i] = "";
		      notePitchTable[i] = -1;
		   }
		   for( int i=0; i<127; ++i ) {
		      double pitch = ( 440.0 / 32.0 ) * Math.pow( 2, (i-9.0)/12.0 ) ;
		      if( pitch > Harvester.SAMPLE_RATE / 2.0 )
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
	 * 
	 * @param data
	 * @return the Pitch value of one Chunk
	 */
	public double computePitch(Complex[] data){
	
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
//		      int nearestNoteDelta=0;
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

	@Override
	public double[] call() throws Exception {
		// TODO Auto-generated method stub
		
		long startTime = System.nanoTime();
		//Melody extraction:
		pitchValues = new double[data.length];
		for(int i=0; i<data.length;i++){
			pitchValues[i] = computePitch(data[i]);	
		}
		System.out.println("Melody extraction: " + ((System.nanoTime() - startTime)%1000000) + " ms");
		return pitchValues;
	}

}
