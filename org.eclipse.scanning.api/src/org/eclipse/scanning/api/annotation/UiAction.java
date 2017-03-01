/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Richard Kennard.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Richard Kennard - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.scanning.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a method as being an executable action.
 * <p>
 * The method must be public, and must not take any parameters in its signature.
 * <p>
 * Note: Metawidget is designed to use <em>existing</em> metadata as much as possible. Clients
 * should use something like <code>org.jdesktop.application.Action</code> or JBoss jBPM in
 * preference to <code>UiAction</code>.
 *
 * @author <a href="http://kennardconsulting.com">Richard Kennard</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
@Deprecated
public @interface UiAction {
	// Just a marker annotation
}
