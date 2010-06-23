package apapl;

public class LogEntry {
	
	public String data;
	public String comment;

	public LogEntry(String data, String comment) {

		this.data = data;
		this.comment = comment;
	
	}
	
	public String toString() {
		
		String ret = "";
		
		ret += data + "(" + comment + ")";
		
		return ret;
		
	}
	
}