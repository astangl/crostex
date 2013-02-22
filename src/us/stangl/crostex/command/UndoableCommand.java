/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

/**
 * Interface to a command that can be applied to an object and then later unapplied.
 * This is used for undo/redo by apply and unapplying in strict sequential order.
 * @author Alex Stangl
 *
 * @param <T> type of object command is applied to
 */
public interface UndoableCommand<T> {
	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	void apply(T obj);
	
	/**
	 * Unapply (undo) command to object.
	 * Assumes object is in state just subsequent to command having been performed,
	 * or has been undone back to that same state.
	 */
	void unApply(T obj);
}
