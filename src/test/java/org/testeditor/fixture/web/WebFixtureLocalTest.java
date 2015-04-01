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

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.testeditor.fixture.core.exceptions.ContinueTestException;
import org.testeditor.fixture.core.exceptions.StopTestException;

/**
 * Tests the {@link WebFixture}.<br />
 * 
 * Maybe we need a dummy web-driver implementation for testing the WebFixture
 * completely (this is not a real test here).
 */
// @Ignore("Only for debug")
public class WebFixtureLocalTest {

	/**
	 * Tests the set element list.
	 */
	@Ignore("only for local test")
	@Test
	public void testSetElementList() {

		System.setProperty("webdriver.ie.driver", "./src/test/resources/IEDriverServer_32.exe");
		WebFixture gf = new WebFixture();

		gf.openBrowser("ie", "");

		gf.navigateToUrl("http://infonet.hh.signal-iduna.de/");

		gf.waitSeconds(2);

		gf.closeBrowser();
	}

	/**
	 * check if ContinueTestException will be thrown.
	 */
	@Ignore("only for local test")
	@Test(expected = ContinueTestException.class)
	public void testContinueTestException() {

		System.setProperty("webdriver.ie.driver", "./src/test/resources/IEDriverServer_32.exe");

		WebFixture gf = new WebFixture();

		try {
			gf.openBrowser("xx", "");
		} catch (Exception e) {

			if (e instanceof StopTestException) {
				throw new ContinueTestException("Browser wurde nicht gefunden trotzdem weitermachen");
			}
		}
	}

	/**
	 * Tests the google search with Firefox. Especially tested with Selenium
	 * Webdriver Version 2.42.2 and Firefox Version 30 result is - it works fine
	 * on Linux :O)
	 */
	@Ignore("When tested on all operating systems this test can be executed")
	@Test
	public void testFirefox() {

		// here you can add your path to your ElementList file
		String pathToElementList = "./src/test/resources/ElementList.conf";

		WebFixture webfixture = new WebFixture();
		webfixture.setElementlist(pathToElementList);
		webfixture.openBrowser("firefox", "");
		webfixture.navigateToUrl("http://www.google.de/");
		webfixture.insertIntoField("akquinet", "searchInput");
		webfixture.waitSeconds(1);
		// now check if the search string will be found
		assertTrue(webfixture.textIsVisible("www.akquinet.de"));

		webfixture.waitSeconds(1);

		webfixture.closeBrowser();
	}

	/**
	 * Tests the local office werker suite and checks whether the login form is
	 * visible.
	 */
	@Ignore
	@Test
	public void testOfficewerkerLokal() {
		WebFixture wf = initiializeWebFixture();
		wf.openBrowser("firefox", "C:\\Users\\srothbucher\\testedit\\windows\\FirefoxPortable.exe");
		wf.navigateToUrl("http://localhost:8080/officewerker-suite-war/");
		wf.waitForElement("loginForm");
		wf.closeBrowser();
	}

	/**
	 * initialize the Webfixture with an elementList.
	 * 
	 * @return the WebFixture
	 */
	private WebFixture initiializeWebFixture() {
		WebFixture wf = new WebFixture();
		wf.setElementlist("./src/test/resources/elementListContent.txt");
		return wf;
	}
}
