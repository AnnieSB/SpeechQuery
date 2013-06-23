package sq;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;



/**
 * 
 * @author Anh Vu
 * 
 * This class is used to start the application.
 */
public class Main{
	
	public static void main (String[] args) throws IOException, LineUnavailableException{
		

		Recorder rc = new Recorder();
		SQModel m = new SQModel( rc);
		
		//Als User input einfügen!
		String path;
		
//		JFrame frame = new JFrame();
//		JFileChooser fc = new JFileChooser();
//		fc.setDialogTitle("Select Speech Database...");
//		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		
//		int returnVal = fc.showOpenDialog(frame);
//		File file = null;
//		if (returnVal == JFileChooser.APPROVE_OPTION) {
//            file = fc.getSelectedFile();} 
//		if(file != null)
//		    path = file.getAbsolutePath();
		
		//Testzwecke
		path = "C:\\Users\\PhuongAnh\\Desktop\\SpeechDB";
		try {
			m.buildAudioDB(path);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Progress Bar
//		JProgressBar progressBar = new JProgressBar(0, task.getLengthOfTask());
//		
//		//Where the GUI is constructed:
//		progressBar.setValue(0);
		
		
		//Starte GUI
		SQController c = new SQController(rc, m);
	}

	
}
