package org.eclipse.scanning.api.ui.auto;

/**
 * 
 * An OSGi service for automatically generating user interfaces from annotated beans.
 * 
 * Currently the service is backed by an SWT generator.
 * 
 * @author Matthew Gerring
 *
 */
public interface IInterfaceService {

	/**
	 * T is the type of the shell for instance swt.Shell for an SWT generator
	 * O is the type of the bean. The bean may be any proper bean class and may have fields annotated with @FieldDescriptor
	 * 
	 * @param shell
	 * @return Dialog a class which can be used to edit any model.
	 * @throws InterfaceInvalidException, for instance if T is a user interface type which is unsupported e.g. FXDialog
	 */
	<T,O> IModelDialog<O> createModelDialog(T shell) throws InterfaceInvalidException;
}
