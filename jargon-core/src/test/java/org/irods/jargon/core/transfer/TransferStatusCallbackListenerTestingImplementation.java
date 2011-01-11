package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;

/***
 * Testing implementation of a status callback listener that can keep telemetry
 * on callbacks
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferStatusCallbackListenerTestingImplementation implements
		TransferStatusCallbackListener {

	private int putCallbackCtr = 0;
	private int getCallbackCtr = 0;
	private int exceptionCallbackCtr = 0;
	private int replicateCallbackCtr = 0;

	private int pauseAfter = 0;
	private int cancelAfter = 0;
	private TransferControlBlock transferControlBlock = null;

	private boolean cancelEncountered = false;
	private boolean pauseEncountered = false;

	public TransferStatusCallbackListenerTestingImplementation() {

	}

	public TransferStatusCallbackListenerTestingImplementation(
			final TransferControlBlock transferControlBlock,
			final int pauseAfter, final int cancelAfter) {
		this.transferControlBlock = transferControlBlock;
		this.pauseAfter = pauseAfter;
		this.cancelAfter = cancelAfter;
	}

	@Override
	public synchronized void statusCallback(final TransferStatus transferStatus)
			throws JargonException {

		if (transferStatus.getTransferType() == TransferType.GET) {
			getCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.PUT) {
			putCallbackCtr++;
		} else if (transferStatus.getTransferType() == TransferType.REPLICATE) {
			replicateCallbackCtr++;
		}

		if (transferStatus.getTransferState() == TransferState.FAILURE) {
			exceptionCallbackCtr++;
		}

		if (transferStatus.getTransferState() == TransferState.CANCELLED) {
			cancelEncountered = true;
		}

		if (transferStatus.getTransferState() == TransferState.PAUSED) {
			pauseEncountered = true;
		}

		int totalCallback = getCallbackCtr + putCallbackCtr
				+ exceptionCallbackCtr + replicateCallbackCtr;

		if (pauseAfter > 0 && totalCallback == pauseAfter) {
			if (transferControlBlock != null) {
				transferControlBlock.setPaused(true);
			}
		}

		if (cancelAfter > 0 && totalCallback == cancelAfter) {
			if (transferControlBlock != null) {
				transferControlBlock.setCancelled(true);
			}
		}

	}

	public synchronized final int getPutCallbackCtr() {
		return putCallbackCtr;
	}

	public synchronized final int getExceptionCallbackCtr() {
		return exceptionCallbackCtr;
	}

	public synchronized final int getGetCallbackCtr() {
		return getCallbackCtr;
	}

	public synchronized int getReplicateCallbackCtr() {
		return replicateCallbackCtr;
	}

	public synchronized void setReplicateCallbackCtr(
			final int replicateCallbackCtr) {
		this.replicateCallbackCtr = replicateCallbackCtr;
	}

	public synchronized int getPauseAfter() {
		return pauseAfter;
	}

	public synchronized int getCancelAfter() {
		return cancelAfter;
	}

	public synchronized TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	public synchronized boolean isCancelEncountered() {
		return cancelEncountered;
	}

	public synchronized boolean isPauseEncountered() {
		return pauseEncountered;
	}

}
