package sq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implements the Discrete Fourier Transformation.
 * @author PhuongAnh
 *
 */
public class DFT {
	
	/**
	 * This method transforms the sound data into frequency domain with the Discrete Fourier Transformation 
	 * @param the Recorder, which read the audio file
	 * @return a double array with the frequencies of all chunks
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static Complex[][] transform (byte[] audio){
		
		double[] window = new double[Harvester.CHUNK_SIZE];
		//build Hamming window
			buildHammingWindow(window,Harvester.CHUNK_SIZE);
		
		final int totalSize = audio.length;
		int amountPossible = totalSize/Harvester.CHUNK_SIZE;
		//When turning into frequency domain we'll need complex numbers:
		Complex[][] results = new Complex[amountPossible][];
		//For all the chunks:
		ExecutorService complexPool = Executors.newCachedThreadPool();
		List<Future<Complex[]>> complex = new ArrayList<Future<Complex[]>>();
		ExecutorService fftPool = Executors.newCachedThreadPool();
		List<Future<Complex[]>> fft = new ArrayList<Future<Complex[]>>();
		
		for(int times = 0;times < amountPossible; times++) {
//		    Complex[] complex = new Complex[Harvester.CHUNK_SIZE];
//		    for(int i = 0;i < Harvester.CHUNK_SIZE;i++) {
//		        //Put the time domain data into a complex number with imaginary part as 0:
//		        complex[i] = new Complex(audio[(times*Harvester.CHUNK_SIZE)+i], 0);
//		      //apply Hamming window
//		        if(window.length != 0)
//		        complex[i].setRe(complex[i].re() * window[i]) ;
//		}
			
			Callable<Complex[]> complexWorker = new ComplexWorker(times, audio, window);
            Future<Complex[]> cwSubmit = complexPool.submit(complexWorker);
            complex.add(cwSubmit);
		        
            Complex[] c = null;
				try {
					c = complex.get(times).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Callable<Complex[]> fftWorker = new FFTWorker(c);
	            Future<Complex[]> fftSubmit = fftPool.submit(fftWorker);
	            fft.add(fftSubmit);
		    
		    //Perform FFT analysis on the chunk:
		    try {
				results[times] = fft.get(times).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		complexPool.shutdown();
		return results;
	}
	
	private static void buildHammingWindow( double[] window, int size ){
	   for( int i=0; i<size; ++i ){
	      window[i] = .54 - .46 * Math.cos( 2 * Math.PI * i / (float) size );
	   }
	}
	
	static class ComplexWorker implements Callable<Complex[]>{
		public int times;
		byte [] audio;
		double[] window;
		
		public ComplexWorker(int times,byte[] audio, double[] window){
			this.times = times;
			this.audio = audio;
			this.window = window;
		}
		@Override
		public Complex[] call() throws Exception {
			// TODO Auto-generated method stub
			 Complex[] complex = new Complex[Harvester.CHUNK_SIZE];
			    for(int i = 0;i < Harvester.CHUNK_SIZE;i++) {
			        //Put the time domain data into a complex number with imaginary part as 0:
			        complex[i] = new Complex(audio[(times*Harvester.CHUNK_SIZE)+i], 0);
			      //apply Hamming window
			        if(window.length != 0)
			        complex[i].setRe(complex[i].re() * window[i]) ;
			    }
			return complex;
		}
		
		public int getIndex(){
			return this.times;
		}
		
	}
	
	static class FFTWorker implements Callable<Complex[]>{
		Complex[] complex;
		
		public FFTWorker(Complex[] complex){
			this.complex = complex;
		}

		@Override
		public Complex[] call() throws Exception {
			// TODO Auto-generated method stub
			Complex[] fft = FFT.fft(complex);
			return fft;
		}
	}
}
