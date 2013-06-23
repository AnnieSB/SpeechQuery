package sq;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

 

/**
 * 
 * @author Anh Vu
 *
 * This class is in charge of matching the search input with the database. It contains the possible
 * matching speech recordings and has access to the audio files.
 */
public class SQModel extends Observable{

	static List<Map<Long, DataPoint>> hashDB;
	static List<String> songList;
	static Map<Integer, double[]> melodyScores;
	static Map<Integer, double[]> rhythmScores;
	Recorder rc;

	public SQModel( Recorder rc){
		this.rc = rc;
	}

	/** 
     * This method reads all the speech recordings and creates the database. 
     * @param path 
     * @throws LineUnavailableException 
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
     */
    public void buildAudioDB(String path) throws LineUnavailableException, UnsupportedAudioFileException, IOException{ 
        hashDB = new ArrayList<Map<Long,DataPoint>>(); 
        melodyScores = new HashMap<Integer, double[]>();
        rhythmScores = new HashMap<Integer, double[]>();
        songList = new ArrayList<String>(); 
        
        //Platzhalter für sample Sprachinput 
        songList.add(0,null); 
        int index = 1; 
       
        //ToDo: Datenbank entsprechend speichern! 
        
        System.out.println("Loading speech recordings..."); 
        long startTime = System.nanoTime();
        File folder = new File(path); 
        
        //Thread pool management
//        ExecutorService afThreads = Executors.newCachedThreadPool();
//        List<Future<Map<Long,DataPoint>>> fingerprints = new ArrayList<Future<Map<Long,DataPoint>>>();
        
        ExecutorService meThreads = Executors.newCachedThreadPool();
        List<Future<double[]>> mScores = new ArrayList<Future<double[]>>();
        
        ExecutorService rdThreads = Executors.newCachedThreadPool();
        List<Future<double[]>> rScores = new ArrayList<Future<double[]>>();
        
		int totalFramesRead;
		AudioInputStream ais;
		int bytesPerFrame;
		int numBytes;
		byte[] audioBytes;
		Complex[][] tData;
		Complex[][] tData_complete;
		Complex[][] tmp;
        for (File file : folder.listFiles()){ 
            if (file.isFile() && (file.getName().contains(".wav") || file.getName().contains(".mp3"))){ 
                songList.add(index, file.getName()); 
                
                totalFramesRead = 0;
                ais = AudioSystem.getAudioInputStream(file);
                bytesPerFrame = ais.getFormat().getFrameSize();
        		numBytes = 44100 * bytesPerFrame;
        		audioBytes = new byte[numBytes];
        		tData_complete = null;
        		tData = null;
                try { 
                    //fileStream = rc.captureAudioFile(file);
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
        		      tData = DFT.transform(audioBytes);
        		      if(tData_complete == null)
        		    	  tData_complete = tData;
        		      else{
        		          tmp = tData_complete;
        		          tData_complete = new Complex[tmp.length + tData.length][];
        		          System.arraycopy(tmp, 0, tData_complete, 0, tmp.length);
        		          System.arraycopy(tData, 0, tData_complete, tmp.length, tData.length);
        		      }
        		    }
        		    ais.close();
                } catch (IOException e) { 
                    // TODO Auto-generated catch block 
                    e.printStackTrace(); 
                } 	
                
//                long startime = System.nanoTime();
//                //Complex[][] tData = DFT.transform(fileStream);
//                System.out.println("Transformation time: " + (System.nanoTime() - startime));
                //Map<Long,DataPoint> tmp = fe.index(fileStream,index); 
                
//                Callable<Map<Long,DataPoint>> afWorker = new AcousticFingerprinter(tData_complete,index);
//                Future<Map<Long,DataPoint>> afSubmit = afThreads.submit(afWorker);
//                fingerprints.add(afSubmit);
                
                AcousticFingerprinter af = new AcousticFingerprinter(tData_complete, index);
                if(index==1)
            		hashDB.add(0,null);
                startTime = System.nanoTime();
    			hashDB.add(index, af.computeAF(tData_complete, index));
    			System.out.println("AF: " + ((System.nanoTime() - startTime)%1000000) + " ms");
                
                
                Callable<double[]> meWorker = new MelodyExtractor(tData_complete);
                Future<double[]> meSubmit = meThreads.submit(meWorker);
                mScores.add(meSubmit);
                
                Callable<double[]> rdWorker = new RhythmDetector(tData_complete,index);
                Future<double[]> rdSubmit = rdThreads.submit(rdWorker);
                rScores.add(rdSubmit);
      
//                //melody.recognition 
//                 melodyScores.put(index, worker.getPitchValues());
//                //rhythm.detection 
//                 rhythmScores.put(index, fe.getBeats());
                 
                index++; 
            } 
            else{ 
                System.out.println("File nicht gefunden - " + file.getName()); 
            } 
            
//            //am Anfang Platzhalter für Spracheingabe freihalten 
//            if(index==1) 
//                hashDB.add(0,null); 
//            hashDB.add(index,tmp); 
        }
//        try {
//			System.out.println( "FP size: "+ fingerprints.get(2).get().size());
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (ExecutionException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        for(int k=0; k<melodyScores.size(); k++){
//        	if(k==0)
//        		hashDB.add(0,null);
        	try {
				//hashDB.add(k+1, fingerprints.get(k).get());
				melodyScores.put(k+1,mScores.get(k).get());
				rhythmScores.put(k+1, rScores.get(k).get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}       	
        }
        
        System.out.println("Total loading time: " + ((System.nanoTime() - startTime)%1000000) + " ms");
        System.out.println("Song DataBase loaded! " + "Total: " + (hashDB.size()-1) + ". "); 
        //afThreads.shutdown();
        meThreads.shutdown();
        rdThreads.shutdown();
    } 

    /** 
     * This class is responsible for matching the search query with the hash database. 
     * @param hash 
     */
    public String match(Map<Long,DataPoint> searchInput){ 
        /* 
         * 1. jeden HashCode in der DB suchen 
         * songID und delta T in einer Liste speichern 
         * Paare die am meisten vorkommen -> songID ist Match! 
         */
        int tmp = 0; 
        // speichert SongID und Offset -> DataPoint und jeweilige Anzahl
        Map<String, Integer> matches = new HashMap<String, Integer>();
        boolean inserted = false;
        int matchCount = 1;
        //DataPoint newMatch;
        String matchHash = "";
        for(Map.Entry<Long, DataPoint> entry : searchInput.entrySet()){ 
            //hashDB durchsuchen 
            for(int j=1; j<hashDB.size();j++){ 
                if(hashDB.get(j).keySet().contains(entry.getKey())){ 
                    //jede einzelne Map durchsuchen 
                    for(Map.Entry<Long, DataPoint> entryDB : hashDB.get(j).entrySet()){ 
                        if(entryDB.getKey().equals(entry.getKey())){ 
                        	//Offset berechnen
                            tmp =  Math.abs(entryDB.getValue().getTime() - entry.getValue().getTime()); 
                            matchHash = matchHash + entryDB.getValue().getSongId() + "," + tmp;
                            if(matches.containsKey(matchHash)){
                            	matchCount = matches.get(matchHash) + 1;
                            	matches.remove(matchHash);
                            }
                            matches.put(matchHash, matchCount);
//                            	//matches.put(m.getKey(), m.getValue()+1);
//                            		number = m.getValue();
//                            		matches.remove(m);
//                            		matches.put(new DataPoint(entryDB.getValue().getSongId(), tmp),number++);
//                            		inserted = true;
//                            }
//                            if(!inserted){
//                                matches.put(new DataPoint(entryDB.getValue().getSongId(),tmp), 1); 
//                            } 
//                            else
//                                inserted = false;
                            matchCount = 1;
                            matchHash = "";
                            
                        }
                    } 
                } 
            }     
        } 
        System.out.println(matches.size());
        
        String matchesFound = sortByOccurrence(matches); 
          
        return matchesFound;     
        }
    
    /** 
     * This method sort the map containing all the matching candidates by an descending order and returns a String  
     * representation of it. 
     * @param matches 
     * @return 
     */
    private String sortByOccurrence(Map<String,Integer> matches){ 
        //sortieren 
        
        Map<String,Integer> sortedMap = sortDESC(matches);
          
        String resultString; 
        if(sortedMap.size() != 0){ 
            if(sortedMap.size() == 1) 
                resultString = "Match found: " +  "\n"; 
            else
                resultString = "Matches found: " +  "\n"; 
        int songID = 0; 
        for(Map.Entry<String,Integer> entry : sortedMap.entrySet()){ 
        	String[] songIds = entry.getKey().split(",");
            songID = Integer.parseInt(songIds[0]); 
            //if(!resultString.contains("" + songID)) 
            if(entry.getValue() == 1) 
                resultString = resultString + songList.get(songID) + " with " + entry.getValue() + " match." + "\n"; 
            else
                resultString = resultString + songList.get(songID) + " with " + entry.getValue() + " matches." + "\n"; 
        } 
        } 
        else{ 
            resultString = "No matches found! \n"; 
        } 
        return resultString; 
    } 
    
    /**
     * Sorts by descending Order.
     * @param unsortMap
     * @return
     */
    private static Map<String, Integer> sortDESC(Map<String, Integer> unsortMap){
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>(){
        	
            public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2){
                    return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
    private void sortByMelodyScore(){
    	
    }
    
    private void sortByRhythmScore(){
    	
    }
    

}