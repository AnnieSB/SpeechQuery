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
		//Complex[] tmp;
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
		    results[times] = FFT_princeton.fft(complex);
//		    double[] x = new double[complex.length];
//		    double[] y = new double[complex.length];
//		    for(int z=0; z<complex.length; z++){
//		    	x[z] = complex[z].re();
//		    	y[z] = complex[z].im();
//		    }
//		    FFT fft = new FFT(complex.length);
//		    fft.fft(x,y);
//		    tmp = new Complex[complex.length];
//		    for(int o =0; o<complex.length; o++){
//		    	Complex c = new Complex(x[o],y[o]);
//		    	tmp[o] = c;
//		    }
//		    results[times] = tmp;
		}
		return results;
	}
	
	private static void buildHammingWindow( double[] window, int size ){
		
	   for( int i=0; i<size; ++i ){
	      window[i] = .54 - .46 * Math.cos( 2 * Math.PI * i / (float) size );
	   }
	}
	
}
