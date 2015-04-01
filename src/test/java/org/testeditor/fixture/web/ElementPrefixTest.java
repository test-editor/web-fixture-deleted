/*******************************************************************************
 * Copyright (c) 2012 - 2014 Signal Iduna Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@code ElementPrefix}.
 * 
 */
public class ElementPrefixTest {

	/**
	 * Test if all {@code ElementPrefix} entries ends with '::'.
	 */
	@Test
	public void testSuffixOfAllElementPrefixes() {
		for (ElementPrefix prefix : ElementPrefix.values()) {
			assertTrue(prefix.toString().endsWith("::"));
		}
	}

	/**
	 * {@code toString} and {@code getName} should return the same value.
	 */
	@Test
	public void testToStringIsEqualsGetName() {
		assertEquals(ElementPrefix.ID.getName(), ElementPrefix.ID.toString());
		assertEquals(ElementPrefix.NAME.getName(), ElementPrefix.NAME.toString());
		assertEquals(ElementPrefix.XPATH.getName(), ElementPrefix.XPATH.toString());
	}
}
