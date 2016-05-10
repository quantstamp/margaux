/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Fikayo Odunayo
 * 
 */
public abstract class ProcessingParam
		implements Comparable<ProcessingParam>, Serializable {

	protected final Integer priority;
	protected final File tmpLocalDirectory;
	protected final UUID analyzingSessionID;

	public ProcessingParam(final Integer priority,
			final File tmpLocalDirectory, final UUID analyzingSessionID) {
		this.priority = priority;
		this.tmpLocalDirectory = tmpLocalDirectory;
		this.analyzingSessionID = analyzingSessionID;
	}

	public ProcessingParam prepareToUse() throws Exception {
		return this;
	}

	public ProcessingParam prepareToSend() throws Exception {
		return this;
	}

	public Optional<File> getTmpLocalDirectory(){
		return Optional.ofNullable(tmpLocalDirectory);
	}
	
	public Optional<Integer> getPriority(){
		return Optional.ofNullable(priority);
	}
	
	public Optional<UUID> getAnalyzingSessionID(){
		return Optional.ofNullable(analyzingSessionID);
	}
	
	public abstract boolean isEmptyParam();

	@Override
	public int compareTo(ProcessingParam o) {

		if (o == null)
			return -1;

		if (o.priority == this.priority)
			return 0;

		if (o.priority < this.priority)
			return 1;

		return -1;
	}

	public abstract ProcessingParam createItself();

	public abstract ProcessingParam changeTmpLocalDirectory(final File tmpDirectory);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analyzingSessionID == null) ? 0 : analyzingSessionID.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result
				+ ((tmpLocalDirectory == null) ? 0 : tmpLocalDirectory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProcessingParam other = (ProcessingParam) obj;
		if (analyzingSessionID == null) {
			if (other.analyzingSessionID != null) {
				return false;
			}
		} else if (!analyzingSessionID.equals(other.analyzingSessionID)) {
			return false;
		}
		if (priority == null) {
			if (other.priority != null) {
				return false;
			}
		} else if (!priority.equals(other.priority)) {
			return false;
		}
		if (tmpLocalDirectory == null) {
			if (other.tmpLocalDirectory != null) {
				return false;
			}
		} else if (!tmpLocalDirectory.equals(other.tmpLocalDirectory)) {
			return false;
		}
		return true;
	}

}