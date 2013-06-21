package sq;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;


public class Recorder {
	
	ByteArrayOutputStream out;
	ByteArrayOutputStream fileOut;
	static boolean running = true;
	
	public Recorder(){
		out = new ByteArrayOutputStream();
		//rd = new RhythmDetecter();
	}
	
	public void captureAudio() throws LineUnavailableException{
		try {
		      final AudioFormat format = getFormat();
		      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		      final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
		      line.open(format);
		      line.start();
		      Runnable runner = new Runnable() {
		        int bufferSize = (int)format.getSampleRate() 
		          * format.getFrameSize();
		        byte buffer[] = new byte[bufferSize];
		 
		        public void run() {
		          out = new ByteArrayOutputStream();
		          running = true;
		          try {
		            while (running) {
		              int count = 
		                line.read(buffer, 0, buffer.length);
		              if (count > 0) {
		            	  //rhythm methods
		            	  
		                  out.write(buffer, 0, count);
		              }
		            }
		            out.close();
		            line.close();
		          } catch (IOException e) {
		            System.err.println("I/O problems: " + e);
		            System.exit(-1);
		          }
		        }
		      };
		      //line.close();
		      Thread captureThread = new Thread(runner);
		      captureThread.start();
		    } catch (LineUnavailableException e) {
		      System.err.println("Line unavailable: " + e);
		      System.exit(-2);
		    }
	}
	

	/**
	 * This method captures the audio signal of an audio file.
	 * @param file
	 * @return
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public byte[] captureAudioFile(File file) throws UnsupportedAudioFileException, IOException{
		long startTime = System.nanoTime();
		int totalFramesRead = 0;
		AudioInputStream ais = AudioSystem.getAudioInputStream(file);
		int bytesPerFrame = ais.getFormat().getFrameSize();
		int numBytes = 44100 * bytesPerFrame;
		byte[] audioBytes = new byte[numBytes];
		fileOut = new ByteArrayOutputStream();
		byte[] data = null;
		try {
		    int numBytesRead = 0;
		    int numFramesRead = 0;
		    // Try to read numBytes bytes from the file.
		    while ((numBytesRead = ais.read(audioBytes)) != -1) {
		      // Calculate the number of frames actually read.
		      numFramesRead = numBytesRead / bytesPerFrame;
		      totalFramesRead += numFramesRead;
		      // Here, do something useful with the audio data that's 
		      // now in the audioBytes array...
		      //rhythm detection
		      fileOut.write(audioBytes, 0, numBytesRead);
		    }
			ais.close();
			fileOut.close();
		    data = fileOut.toByteArray();
		  } catch (Exception ex) { 
		    // Handle the error...
			  ex.printStackTrace();
		  }
		  System.out.println("Capture time: " + (System.nanoTime() - startTime));
		  return data;
	}
	
	
	public void stopCapturing(){
		running = false;
	}
	
	public AudioFormat getFormat() {
		    float sampleRate = 44100;
		    int sampleSizeInBits = 8;
		    int channels = 1; //mono
		    boolean signed = true;
		    boolean bigEndian = true;
		    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		}

	/**
	 * Returns the correspondent byte-Array of the read Outputstream.
	 * @return
	 */
	public byte[] getStream(){
		  return out.toByteArray();
	}
}
