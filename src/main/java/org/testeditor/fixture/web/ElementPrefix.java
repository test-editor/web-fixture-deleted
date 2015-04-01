/*******************************************************************************
 * Copyright (c) 2012 - 2015 Signal Iduna Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Signal Iduna Corporation - initial API and implementation
 * akquinet AG
 *******************************************************************************/
package org.testeditor.fixture.web;

/**
 * enumeration for the element-prefixes.
 * 
 */
public enum ElementPrefix {
	CLASSNAME("CLASSNAME::"), CSSSELECTOR("CSSSELECTOR::"), ID("ID::"), LINKTEXT("LINKTEXT::"), NAME("NAME::"), PARTIAL(
			"PARTIAL::"), TAGNAME("TAGNAME::"), XPATH("XPATH::");
	private String name;

	/**
	 * 
	 * @param name
	 *            the name
	 */
	private ElementPrefix(String name) {

		this.name = name;
	}

	/**
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
