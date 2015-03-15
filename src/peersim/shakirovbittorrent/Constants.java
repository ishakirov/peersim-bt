package peersim.shakirovbittorrent;

public final class Constants {
	
	// Messages constants
	public static final int PING = 1;
	public static final int PONG = 2;
	public static final int TRACKER = 3;
	public static final int PEERSET = 4;
	public static final int CHOKE = 5;
	public static final int UNCHOKE = 6;
	public static final int INTERESTED = 7;
	public static final int NOT_INTERESTED = 8;
	public static final int HAVE = 9;
	public static final int BITFIELD = 10;
	public static final int REQUEST = 11;
	public static final int PIECE = 12;
	public static final int CANCEL = 13;
	public static final int DOWNLOAD_COMPLETED = 100;
	public static final int BAD_PEER = 101;
	
	// Events constants
	public static final int CHECKALIVE_TIME = 14;
	public static final int CHOKE_TIME = 15;
	public static final int OPTIMISTIC_TIME = 16;
		
	// Configuration file constants
	public static final String PAR_PROT = "protocol";
	public static final String PAR_TRANSPORT = "transport";
	public static final String PAR_NEWER_DISTR = "newer_distr";
	public static final String PAR_SEEDER_DISTR = "seeder_distr";
	public static final String PAR_MALICIOUS_DISTR = "malicious_distr"; 
	public static final String PAR_SIZE = "file_size";
	public static final String PAR_SWARM = "max_swarm_size";
	public static final String PAR_PEERSET_SIZE = "peerset_size";
	public static final String PAR_FIRST_HEUR = "first_heur";
	public static final String PAR_SECOND_HEUR = "second_heur";
	public static final String PAR_ANTI_MALICIOUS = "anti_mali";
	
}
