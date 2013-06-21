package sq;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * 
 * @author Anh Vu
 *
 * This class coordinates the user interaction with the GUI. It captures the search input and passes it on.
 */
public class SQController implements ActionListener{
	
	SpeechQueryView view;
	SQModel model;
	Recorder recorder;
	//FeatureExtractor fe;
	
	boolean recordClicked = false;
	boolean playClicked = false;
	
	Map<Long, DataPoint> tsearchKey;
	double[] tsearchMelody;
	double[] tsearchRhythm;
	
	ExecutorService thread;
	
	List<byte[]> inputList = new ArrayList<byte[]>();

	public SQController( Recorder recorder, SQModel model){
		
		view = new SpeechQueryView(this);
	    view.setVisible(true);
	    
	    this.model = model;
	    model.addObserver(view);
	    
	    this.recorder = recorder;
	    //this.fe = fe;
	    
	    tsearchKey = new HashMap<Long, DataPoint>();
	    
	    thread = Executors.newSingleThreadExecutor();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		String cmd = event.getActionCommand();
		if(cmd.equals("Record")){	
			if(view.getStandard().isSelected() && !this.recordClicked){
				setClicked(true);
				view.addText("Start recording...");
				try {
					recorder.captureAudio();
					//af.fingerprint(recorder.out.toByteArray());
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (this.recordClicked){
				view.addText("Done recording.");
				recorder.stopCapturing();
				setClicked(false);
				view.activateRecord();
				view.activatePlayInputButton();
				view.activateSearch();
				
				//save to later play
				inputList.add(recorder.getStream());
			}
			else if(view.getTest().isSelected()){
				//view.getSearchAF().setEnabled(true);
				view.getSearchMelody().setEnabled(true);
				view.getSearchRhythm().setEnabled(true);
			}
			
		}
		if(cmd.equals("PlayInput")){
				view.deactivatePlayInputButton();
				// searchInput abspielen
				// immer letzte Suchaufnahme abspielen
				playAudio();
				//view.activatePlayInputButton();
		}
		if(cmd.equals("Search")){
			view.deactivateSearch();
			view.deactivatePlayInputButton();
			
			if(recorder.getStream().length!=0){
				view.addText("Indexing the search input...");
				//tsearchKey = fe.index(recorder.getStream(), 0);
				Complex[][] data = DFT.transform(recorder.getStream());
				Callable<Map<Long,DataPoint>> fe = new AcousticFingerprinter(data,0);
				Future<Map<Long,DataPoint>> fu = thread.submit(fe);
				try {
					tsearchKey = fu.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				tsearchMelody = fe.pitchValues;
//				tsearchRhythm = fe.beats;
				view.addText("Searching in Database...");
			    String matches = model.match(tsearchKey);
			    view.addText(matches);
				//if matches > 1
//				//rhythm detect
//				//melody extract
				
			}else{
				System.out.println("Recording input was null");
				view.addText("Record input was null! Try again!");
			}
			view.activateRecord();
			
			
	}
		if(cmd.equals("SearchRhythm")){
			
		}
		if(cmd.equals("SearchMelody")){
			
		}
        if(cmd.equals("SearchAF")){
        	
        }
		if(cmd.equals("Standard")){
			view.getStandard().setSelected(true);
			view.getTest().setSelected(false);
			view.getSearch().setVisible(true);
			view.getSearchRhythm().setVisible(false);
			view.getSearchMelody().setVisible(false);
		}
		if(cmd.equals("Test")){
			view.getStandard().setSelected(false);
			view.getTest().setSelected(true);
			view.getSearch().setVisible(false);
			view.getSearchRhythm().setVisible(true);
			view.getSearchMelody().setVisible(true);
		}
				
	}
	
	private void setClicked(boolean value){
		this.recordClicked = value;
	}
	
	private void setPlayed(boolean value){
		this.playClicked = value;
	}
	
	
	/**
	 * for playing the last search input
	 */
	private void playAudio() {
		    try {
		      byte audio[] = inputList.get(inputList.size()-1);
		      InputStream input = new ByteArrayInputStream(audio);
		      final AudioFormat format = recorder.getFormat();
		      final AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
		      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		      final SourceDataLine line = (SourceDataLine)AudioSystem.getLine(info);
		      line.open(format);
		      line.start();

		      Runnable runner = new Runnable() {
		        int bufferSize = (int) format.getSampleRate() 
		          * format.getFrameSize();
		        byte buffer[] = new byte[bufferSize];
		 
		        public void run() {
		          try {
		            int count;
		            while ((count = ais.read(
		                buffer, 0, buffer.length)) != -1) {
		              if (count > 0) {
		                line.write(buffer, 0, count);
		              }
		            }
		            line.drain();
		            line.close();
		          } catch (IOException e) {
		            System.err.println("I/O problems: " + e);
		            System.exit(-3);
		          }
		        }
		      };
		      Thread playThread = new Thread(runner);
		      playThread.start();
		    } catch (LineUnavailableException e) {
		      System.err.println("Line unavailable: " + e);
		      System.exit(-4);
		    } 
		  }

}
