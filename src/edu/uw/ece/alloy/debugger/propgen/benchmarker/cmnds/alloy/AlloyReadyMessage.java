package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ReadyMessage;

public class AlloyReadyMessage extends ReadyMessage {

	private static final long serialVersionUID = -6479019132107993293L;

	public AlloyReadyMessage(RemoteProcess process) {
		super(process);
	}

	public AlloyReadyMessage(RemoteProcess process, long creationTime) {
		super(process, creationTime);
	}

	@Override
	public void onAction(Map<String, Object> context) throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<RemoteProcess> processIsReady = (Consumer<RemoteProcess>) context.get("processIsReady");
		try {
			processIsReady.accept(process);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
