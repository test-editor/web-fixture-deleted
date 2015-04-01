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

/**
 * Class to mock the manageTimeouts() call.
 */
public class HtmlWebFixtureMock extends HtmlWebFixture {

	@Override
	protected void manageTimeouts() {
		// do nothing, to prevent null pointer
	}
}
