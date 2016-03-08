// Metawidget
//
// This file is dual licensed under both the LGPL
// (http://www.gnu.org/licenses/lgpl-2.1.html) and the EPL
// (http://www.eclipse.org/org/documents/epl-v10.php). As a
// recipient of Metawidget, you may choose to receive it under either
// the LGPL or the EPL.
//
// Commercial licenses are also available. See http://metawidget.org
// for details.

/**
 * This package contains a copy of most of the files from org.metawidget.inspector.annotation. The annotations had to
 * be separated from the MetawidgetAnnotationInspector because the inspector has other dependencies (via
 * metawidget-core) which cannot be satisfied in all places where access to the annotation classes is needed.
 * <p>
 * It also contains some annotations copied from org.eclipse.richbeans.api.generator, so they can also be used on
 * classes in the scanning project.
 * <p>
 * Original metawidget java doc:
 * <p>
 * Inspectors: Metawidget annotations support.
 *
 * @author <a href="http://kennardconsulting.com">Richard Kennard</a>
 */

package org.eclipse.scanning.api.annotation;