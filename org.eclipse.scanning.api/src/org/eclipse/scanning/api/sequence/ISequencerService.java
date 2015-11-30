package org.eclipse.scanning.api.sequence;

/**
 * 
 * A service to create sequencers for different scan types.
 * 
 * @author fcp94556
 *
 */
public interface ISequencerService {

	ISequencer createSequencer(String name) throws SequenceException; 
}
