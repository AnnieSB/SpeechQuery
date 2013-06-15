package sq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.TreeMap;

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
	FeatureExtractor fe;
	
	public SQModel( Recorder rc, FeatureExtractor fe){
		this.rc = rc;
		this.fe = fe;
	}
	
	/** 
     * This method reads all the speech recordings and creates the database. 
     * @param path 
     * @throws LineUnavailableException 
     */
    public void buildAudioDB(String path) throws LineUnavailableException{ 
        hashDB = new ArrayList<Map<Long,DataPoint>>(); 
        melodyScores = new HashMap<Integer, double[]>();
        rhythmScores = new HashMap<Integer, double[]>();
        songList = new ArrayList<String>(); 
        //Platzhalter für sample Sprachinput 
        songList.add(0,null); 
        int index = 1; 
        byte[] fileStream; 
        //ToDo: Datenbank entsprechend speichern! 
        System.out.println("Loading songs..."); 
        File folder = new File(path); 
        for (File file : folder.listFiles()){ 
            if (file.isFile() && (file.getName().contains(".wav") || file.getName().contains(".mp3"))){ 
                songList.add(index, file.getName()); 
                fileStream = null; 
                try { 
                    fileStream = rc.captureAudioFile(file); 
                } catch (UnsupportedAudioFileException e) { 
                    // TODO Auto-generated catch block 
                    e.printStackTrace(); 
                } catch (IOException e) { 
                    // TODO Auto-generated catch block 
                    e.printStackTrace(); 
                } 
                Map<Long,DataPoint> tmp = fe.index(fileStream,index); 
                //am Anfang Platzhalter für Spracheingabe freihalten 
                if(index==1) 
                    hashDB.add(0,null); 
                hashDB.add(index,tmp); 
      
                //melody.recognition 
                 melodyScores.put(index, fe.getPitchValues());
                //rhythm.detection 
                 rhythmScores.put(index, fe.getBeats());
                 
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
        Map<DataPoint,Integer> matches = new HashMap<DataPoint,Integer>(); 
        DataPoint newMatch; 
        for(Map.Entry<Long, DataPoint> entry : searchInput.entrySet()){ 
            //hashDB durchsuchen 
            for(int j=1; j<hashDB.size();j++){ 
                if(hashDB.get(j).keySet().contains(entry.getKey())){ 
                    //jede einzelne Map durchsuchen 
                    for(Map.Entry<Long, DataPoint> entryDB : hashDB.get(j).entrySet()){ 
                        if(entryDB.getKey().equals(entry.getKey())){ 
                            tmp =  Math.abs(entryDB.getValue().getTime() - entry.getValue().getTime()); 
                            boolean inserted = false; 
                            for(Map.Entry<DataPoint, Integer> m : matches.entrySet()){ 
                                if(m.getKey().getSongId() == entryDB.getValue().getSongId() && m.getKey().getTime() == tmp){ 
                                    matches.put(m.getKey(), m.getValue()+1); 
                                    inserted = true; 
                                } 
                            } 
                            if(!inserted){ 
                                newMatch = new DataPoint(entryDB.getValue().getSongId(),tmp); 
                                matches.put(newMatch, 1); 
                            } 
                            else
                                inserted = false; 
                        } 
                    } 
                } 
            } 
              
        } 
          
        //String matchesFound = "Matches found:"; 
//        String matchesFound = sortByOccurrence(matches); 
//      for(Map.Entry<DataPoint,Integer> entry : matches.entrySet()){ 
//           
//          matchesFound = matchesFound + entry.getKey().getSongId() + " with " +entry.getValue() + " match(es). \n"; 
//      } 
          
//        return matchesFound; 
        return null;
    }
    
    private void sortByMelodyScore(){
    	
    }
    
    private void sortByRhythmScore(){
    	
    }
    
	
}
