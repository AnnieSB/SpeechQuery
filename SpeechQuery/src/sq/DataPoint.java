 package sq;

public class DataPoint implements Comparable{

	private int time;
	private int songId;
	    
	    public DataPoint(int songId, int time) {
	        this.songId = songId;
	        this.time = time;
	    }

	    public int getTime() {
	        return time;
	    }

	    public int getSongId() {
	        return songId;
	    }

		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			
			return (this.songId - ((DataPoint) o).getSongId());
		}

	
}
