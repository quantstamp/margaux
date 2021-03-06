package edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.uw.ece.alloy.util.Utils;

/**
 * A watchdog for monitoring any thread implementing ThreadToBeMonitored
 * 
 * @author vajih
 *
 */
public class ThreadMonitor implements Runnable {

	public final long monitorInterval;
	public final int RecoveryAttemtps;

	protected final static Logger logger = Logger.getLogger(
			ThreadMonitor.class.getName() + "--" + Thread.currentThread().getName());

	private final List<ThreadToBeMonitored> monitoredThreads;

	final Thread monitor = new Thread(this);

	public ThreadMonitor(long monitorInterval, int RecoveryAttemtps) {
		super();
		this.monitorInterval = monitorInterval;
		this.RecoveryAttemtps = RecoveryAttemtps;
		this.monitoredThreads = new LinkedList<>();
	}

	public List<ThreadToBeMonitored> getMonitoredThreads() {
		return this.monitoredThreads;
	}

	public void addThreadToBeMonitored(ThreadToBeMonitored thread) {
		this.monitoredThreads.add(thread);
	}

	protected void monitor() {

		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(this.monitorInterval / 2);
				for (ThreadToBeMonitored thread : this.monitoredThreads) {
					try {
						final long isd = thread.isDelayed();
						if (isd != 0) {
							logger.warning(Utils.threadName() + thread.amIStuck());
							thread.actionOnStuck();
						} else {
							thread.actionOnNotStuck();
						}
					} catch (Throwable tr) {
						logger.severe(Utils.threadName()
								+ "Watchdog main loop BADLY faced an exception!");
					}
				}
				System.gc();
				Thread.sleep(this.monitorInterval / 2);
			} catch (InterruptedException e) {
				logger.severe(Utils.threadName() + "Watchdog main loop is interrpted.");

			}
		}
	}

	public void cancelThreads() {
		this.monitor.interrupt();
	}

	public void startMonitoring() {
		this.monitor.start();
	}

	@Override
	public void run() {
		monitor();
	}

}
