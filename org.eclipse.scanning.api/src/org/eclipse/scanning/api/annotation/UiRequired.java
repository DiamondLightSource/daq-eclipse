/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
//
// This file is dual licensed under both the LGPL
// (http://www.gnu.org/licenses/lgpl-2.1.html) and the EPL
// (http://www.eclipse.org/org/documents/epl-v10.php). As a
// recipient of Metawidget, you may choose to receive it under either
// the LGPL or the EPL.
//
// Commercial licenses are also available. See http://metawidget.org
// for details.

package org.eclipse.scanning.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the field should be 'required' in the UI. For example, should be displayed with a star
 * after it and, depending on the target UI platform, have 'required' validators attached to it.
 * <p>
 * Note: Metawidget is designed to use <em>existing</em> annotations as much as possible. Clients
 * should use something like <code>javax.persistence.Column(nullable = false)</code> or
 * <code>org.hibernate.validator.NotNull</code> in preference to <code>UiRequired</code>.
 *
 * @author <a href="http://kennardconsulting.com">Richard Kennard</a>
 */

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.METHOD } )
@Deprecated
public @interface UiRequired {
	// Just a marker annotation
}
