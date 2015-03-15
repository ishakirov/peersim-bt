package peersim.shakirovbittorrent;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class NetworkInitializer implements Control {

	int pid;
	int tid;
	NodeInitializer init;
	
	public NetworkInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + Constants.PAR_PROT);
		tid = Configuration.getPid(prefix + "." + Constants.PAR_TRANSPORT);
		init = new NodeInitializer(prefix);
	}
	
	@Override
	public boolean execute() {
		Node tracker = Network.get(0);
		
		for (int i = 1; i < Network.size(); i++) {
			((BitTorrent)Network.get(0).getProtocol(pid)).addNeighbor(Network.get(i));
			init.initialize(Network.get(i));
		}
		
		System.out.println("tracker: " + ((BitTorrent)Network.get(0).getProtocol(pid)).getNeighborsSize());
		
		for (int i = 1; i < Network.size(); i++) {
			Node n = Network.get(i);
			long latency = ((Transport)n.getProtocol(tid)).getLatency(n, tracker);
			Object trackerMessage = new SimpleMessage(Constants.TRACKER, n);
			EDSimulator.add(latency, trackerMessage, tracker, pid);
			Object checkAliveEvent = new SimpleEvent(Constants.CHECKALIVE_TIME);
			EDSimulator.add(120000, checkAliveEvent, n, pid);
			Object chokeTimeEvent = new SimpleEvent(Constants.CHOKE_TIME);
			EDSimulator.add(10000, chokeTimeEvent, n, pid);
		}
		
		return true;
	}

}
