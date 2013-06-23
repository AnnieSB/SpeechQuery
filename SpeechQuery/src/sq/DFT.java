package sq;


/**
 * Implements the Discrete Fourier Transformation.
 * @author PhuongAnh
 *
 */
public class DFT {
	
	static double[]  window = new double[Harvester.CHUNK_SIZE];
	
	/**
	 * This method transforms the sound data into frequency domain with the Discrete Fourier Transformation 
	 * @param the Recorder, which read the audio file
	 * @return a double array with the frequencies of all chunks
	 */
	public static Complex[][] transform (byte[] audio){
		buildHammingWindow(window,Harvester.CHUNK_SIZE);
		final int totalSize = audio.length;
		int amountPossible = totalSize/Harvester.CHUNK_SIZE;
		//When turning into frequency domain we'll need complex numbers:
		Complex[][] results = new Complex[amountPossible][];
		//For all the chunks:
		FFT fft = new FFT(Harvester.CHUNK_SIZE);
		Complex[] tmp;
		double[] re;
		double[] im; 
		for(int times = 0;times < amountPossible; times++) {
		    //Complex[] complex = new Complex[Harvester.CHUNK_SIZE];
			re = new double[Harvester.CHUNK_SIZE];
			im = new double[Harvester.CHUNK_SIZE];
		    for(int i = 0;i < Harvester.CHUNK_SIZE;i++) {
		        //Put the time domain data into a complex number with imaginary part as 0:
//		        complex[i] = new Complex(audio[(times*Harvester.CHUNK_SIZE)+i], 0);
//		      //apply Hamming window
//		        if(window.length != 0)
//		        complex[i].setRe(complex[i].re() * window[i]) ;
		        
		        re[i] = audio[(times*Harvester.CHUNK_SIZE)+i];
		        im[i] = 0;
		        if(window.length != 0)
			        re[i] = (re[i] * window[i]) ;

		    }
		    //Perform FFT analysis on the chunk:
		    //results[times] = FFT_princeton.fft(complex);
		    
		    
		    fft.fft(re,im);
		    tmp = new Complex[Harvester.CHUNK_SIZE];
		    for(int o =0; o<Harvester.CHUNK_SIZE; o++){
		    	tmp[o] = new Complex(re[o],im[o]);
		    }
		    results[times] = tmp;
		}
		return results;
	}
	
	private static void buildHammingWindow( double[] window, int size ){
		
	   for( int i=0; i<size; ++i ){
	      window[i] = .54 - .46 * Math.cos( 2 * Math.PI * i / (float) size );
	   }
	}
	
}
