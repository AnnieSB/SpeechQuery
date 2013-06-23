package sq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

 

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
        
        System.out.println("Loading speech recordings..."); 
        File folder = new File(path); 
               
		int totalFramesRead;
		AudioInputStream ais;
		int bytesPerFrame;
		int numBytes;
		byte[] audioBytes;
		byte[] tmp;
		byte[] data_complete;
        MelodyExtractor me;
        double[] pitchValues;
		double[] beats;
		Map<Long,DataPoint> hashes;
		long startTime;
        for (File file : folder.listFiles()){ 
            if (file.isFile() && (file.getName().contains(".wav") || file.getName().contains(".mp3"))){ 
                songList.add(index, file.getName()); 
                
                totalFramesRead = 0;
                ais = AudioSystem.getAudioInputStream(file);
                bytesPerFrame = ais.getFormat().getFrameSize();
        		numBytes = 44100 * bytesPerFrame;
        		audioBytes = new byte[numBytes];
        		data_complete = null;
        		tmp = null;
                try { 
                    int numBytesRead = 0;
        		    int numFramesRead = 0;
        		    // Try to read numBytes bytes from the file.
        		    while ((numBytesRead = ais.read(audioBytes)) != -1) {
        		      // Calculate the number of frames actually read.
        		      numFramesRead = numBytesRead / bytesPerFrame;
        		      totalFramesRead += numFramesRead;
        		      
        		      if(data_complete == null)
        		    	  data_complete = audioBytes;
        		      else{
        		    	  tmp = data_complete;
        		    	  data_complete = new byte[tmp.length + audioBytes.length];
        		    	  System.arraycopy(tmp, 0, data_complete, 0, tmp.length);
        		          System.arraycopy(audioBytes, 0, data_complete, tmp.length, audioBytes.length);
        		      }
        		    }ais.close();
                } catch (IOException e) { e.printStackTrace(); } 	
                
                Complex[][] tData_complete = DFT.transform(data_complete);
                
                //af = new AcousticFingerprinter(tData_complete, index);
                me = new MelodyExtractor(tData_complete);
                //rd = new RhythmDetector(tData_complete, index);
                
                if(index==1)
            		hashDB.add(0,null);
                
    			//Melody extraction:
    			pitchValues = new double[tData_complete.length];
    			beats = new double[tData_complete.length];
    			hashes = new HashMap<Long,DataPoint>();
    			startTime = System.nanoTime();
    			Map<Long,DataPoint> tm;
    			for(int i=0; i<tData_complete.length;i++){
    				tm = AcousticFingerprinter.computeAF_DB(tData_complete[i], index, i);
    				hashes.putAll(tm);
    				pitchValues[i] = me.computePitch(tData_complete[i]);
    				beats[i] = RhythmDetector.computeRhythm(tData_complete[i]);
    			}
    		
    			hashDB.add(index, hashes);
				melodyScores.put(index, pitchValues);
				
    			System.out.println("Melody+ AF extraction: " + ((System.nanoTime() - startTime)%1000000) + " ms");
    			startTime = System.nanoTime();
    			//rhythm
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
    			rhythmScores.put(index, beats);
    			System.out.println("Rhythm extraction: " + ((System.nanoTime() - startTime)%1000000 + " ms") + ", Songindex: "+index);
    			
                index++; 
            } 
            else{ 
                System.out.println("File nicht gefunden - " + file.getName()); 
            } 
                 	
       }
        
        System.out.println("Song DataBase loaded! " + "Total: " + (hashDB.size()-1) + ". "); 
        
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
                            matchHash = entryDB.getValue().getSongId() + "," + tmp;
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
        
        String matchesFound = rank(matches); 
          
        return matchesFound;     
        }
    
    static List<Integer> matchIds;
    static List<Integer> matchCount;
    /** 
     * This method sort the map containing all the matching candidates by an descending order and returns a String  
     * representation of it. 
     * @param matches 
     * @return 
     */
    private String rank(Map<String,Integer> matches){ 
        //sortieren 
    	matchIds = new ArrayList<Integer>();
    	matchCount = new ArrayList<Integer>();
        sortAF(matches);
        
        
        String resultString = ""; 
//        if(sortedMap.size() != 0){ 
//            if(sortedMap.size() == 1) 
//                resultString = "Match found: " +  "\n"; 
//            else
//                resultString = "Matches found: " +  "\n"; 
//        int songID = 0; 
//        for(Map.Entry<String,Integer> entry : sortedMap.entrySet()){ 
//        	String[] songIds = entry.getKey().split(",");
//            songID = Integer.parseInt(songIds[0]); 
//            //if(!resultString.contains("" + songID)) 
//            if(entry.getValue() == 1) 
//                resultString = resultString + songList.get(songID) + " with " + entry.getValue() + " match." + "\n"; 
//            else
//                resultString = resultString + songList.get(songID) + " with " + entry.getValue() + " matches." + "\n"; 
//        } 
//        } 
//        else{ 
//            resultString = "No matches found! \n"; 
//        } 
        return resultString; 
    } 
    

    
    /**
     * Sorts by fingerprint on descending order.
     * @param unsortMap
     * @return
     */
    private static void sortAF(Map<String, Integer> unsortMap){
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>(){
        	
            public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2){
//            	    int r = o2.getValue().compareTo(o1.getValue());
//            	    
//            	    if(r == 0){
//            	    	int key1 = Integer.parseInt(o1.getKey().split(",")[0]);
//            	    	int key2 = Integer.parseInt(o2.getKey().split(",")[0]);
//            	    	r = (key1-key2);
//            	    }
                    return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
//        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
//        String t1;
//        boolean in = false;
//        for (Entry<String, Integer> entry : list){
//        	String[] tmp = entry.getKey().split(",");
//        	t1 = tmp[0];
//        	for(String hash : sortedMap.keySet()){
//        		if(hash.contains(t1)){
//        		   in = true;
//        		   break;
//        		}
//        	}
//        	if(!in)
//                sortedMap.put(entry.getKey(), entry.getValue());
//        	in = false;
//        }
       
        String t1;
        boolean in = false;
        for(Entry<String,Integer> entry : list){
        	t1 = entry.getKey().split(",")[0];
        	if(matchIds.contains(Integer.parseInt(t1)))
        		in = true;
        	if(!in){
        		matchIds.add(Integer.parseInt(t1));
        		matchCount.add(entry.getValue());
        	}
        	
        }
    }
    
    private void sortByMelodyScore(List<Integer> matchIDs){
    	// Melody of Input double[]
    	DTW dtw;
    	double[] costs = new double[matchIDs.size()];
    	for(int i=0; i<matchIDs.size(); i++){
    		//dtw = new DTW(melodyScores.get(matchIDs.get(i)));
    		//costs[i] = dtw.cost(melodyScores.get(matchIDs.get(i)).length,);
    	}
    	
    	//index of cost  -> matchIDs.get(i) = AFRanking!
    	int[] melodyRank = new int[matchIDs.size()];
    	for(int a=0; a<matchIDs.size(); a++){
    		melodyRank[a] = matchIDs.get(a);
    	}
    	int tmp;
    	for(int i=0; i<costs.length-1; i++){
			if(costs[i] > costs[i+1]){
				//switch
				tmp = melodyRank[i];
				melodyRank[i] = melodyRank[i+1];
				melodyRank[i+1] = tmp;
			}
		}
    }
    
    private void sortByRhythmScore(){
    	
    }
    

}