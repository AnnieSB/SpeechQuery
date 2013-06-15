package sq;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;



/**
 * 
 * @author Anh Vu
 * 
 * This class is used to start the application.
 */
public class Main {

	
	public static void main (String[] args) throws IOException, LineUnavailableException{
		

		Recorder rc = new Recorder();
		FeatureExtractor fe = new FeatureExtractor();
		
		//Songdatenbank laden
		SQModel m = new SQModel( rc,fe);
		//Als User input einfügen!
		String path = "C:\\Users\\PhuongAnh\\Desktop\\phatt_y_wave\\2263";
		
		//Starte GUI
		SQController c = new SQController(rc, m,fe);
	}
	
}
