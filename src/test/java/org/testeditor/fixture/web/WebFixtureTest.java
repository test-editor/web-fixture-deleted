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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testeditor.fixture.core.exceptions.ElementKeyNotFoundException;
import org.testeditor.fixture.core.exceptions.StopTestException;

/**
 * Tests the {@link WebFixture}.
 * 
 */
public class WebFixtureTest {

	private static final String ELEMENT_LIST = "src/test/resources/ElementList.conf";
	private static final String ELEMENT_LIST_WEB = "src/test/resources/WebApplication/ElementList.conf";
	private static final String ELEMENT_LIST_TXT = "src/test/resources/elementListContent.txt";
	private static final URI WEB_INDEX_PAGE = new File("src/test/resources/WebApplication/index.html").toURI();
	private static final URI WEB_ELEMENTS_PAGE = new File("src/test/resources/WebApplication/elements.html").toURI();

	private HtmlWebFixture fixture;

	// /**
	// * Configure the Logger.
	// */
	// public WebFixtureTest() {
	// // create appender
	// ConsoleAppender console = new ConsoleAppender();
	// // configure the appender
	// String pattern = "%d [%p|%c|%C{1}] %m%n";
	// console.setLayout(new PatternLayout(pattern));
	// console.setThreshold(Level.INFO);
	// console.activateOptions();
	// // add appender to root Logger
	// Logger.getRootLogger().addAppender(console);
	// }

	/**
	 * Run test on a clean fixture.
	 */
	@Before
	public void setUp() {
		fixture = new JUnitWebFixture();
	}

	/**
	 * Test for assertContains.
	 */
	@Test
	public void assertContainsWorksCorrect() {
		assertTrue(fixture.assertContains("Hallo Welt", "Welt"));
		assertTrue(fixture.assertContains("Hausbot", " Haus "));
		assertTrue(fixture.assertContains("Hausbot", " aus"));
		assertTrue(fixture.assertContains("Hausbot", "bot"));
		assertTrue(fixture.assertContains("Eingabe", "Eingabe"));
		assertTrue(fixture.assertContains(null, null));

		assertFalse(fixture.assertContains("Auto", "Bahn"));
		assertFalse(fixture.assertContains("Auto", null));
		assertFalse(fixture.assertContains(null, "Bahn"));
	}

	/**
	 * Test for assertNotContains.
	 */
	@Test
	public void assertNotContainsWorksCorrect() {
		assertTrue(fixture.assertNotContains("Auto", "Bahn"));
		assertTrue(fixture.assertNotContains("Auto", null));
		assertTrue(fixture.assertNotContains(null, "Bahn"));

		assertFalse(fixture.assertNotContains("Hallo Welt", "Welt"));
		assertFalse(fixture.assertNotContains("Hausbot", " Haus "));
		assertFalse(fixture.assertNotContains("Hausbot", " aus"));
		assertFalse(fixture.assertNotContains("Hausbot", "bot"));
		assertFalse(fixture.assertNotContains("Eingabe", "Eingabe"));
		assertFalse(fixture.assertNotContains(null, null));
	}

	/**
	 * Test for assertIsEqual.
	 */
	@Test
	public void assertIsEqualWorksCorrect() {
		assertTrue(fixture.assertIsEqual("A", "A"));
		assertTrue(fixture.assertIsEqual(" A", "A"));
		assertTrue(fixture.assertIsEqual(" A ", "A"));
		assertTrue(fixture.assertIsEqual(" A ", " A"));
		assertTrue(fixture.assertIsEqual(" A ", " A "));
		assertTrue(fixture.assertIsEqual(null, null));
		assertTrue(fixture.assertIsEqual("", ""));

		assertFalse(fixture.assertIsEqual("A", null));
		assertFalse(fixture.assertIsEqual(null, "A"));
		assertFalse(fixture.assertIsEqual(null, ""));
		assertFalse(fixture.assertIsEqual("", null));
		assertFalse(fixture.assertIsEqual("A", "B"));
	}

	/**
	 * Test for assertIsNotEqual.
	 */
	@Test
	public void assertIsNotEqualWorksCorrect() {
		assertTrue(fixture.assertIsNotEqual("A", null));
		assertTrue(fixture.assertIsNotEqual(null, "A"));
		assertTrue(fixture.assertIsNotEqual(null, ""));
		assertTrue(fixture.assertIsNotEqual("", null));
		assertTrue(fixture.assertIsNotEqual("A", "B"));

		assertFalse(fixture.assertIsNotEqual("A", "A"));
		assertFalse(fixture.assertIsNotEqual(" A", "A"));
		assertFalse(fixture.assertIsNotEqual(" A ", "A"));
		assertFalse(fixture.assertIsNotEqual(" A ", " A"));
		assertFalse(fixture.assertIsNotEqual(" A ", " A "));
		assertFalse(fixture.assertIsNotEqual(null, null));
		assertFalse(fixture.assertIsNotEqual("", ""));
	}

	/**
	 * Test for assertIsEmpty.
	 */
	@Test
	public void assertIsEmptyWorksCorrect() {
		assertTrue(fixture.assertIsEmpty(""));
		assertTrue(fixture.assertIsEmpty(" "));
		assertTrue(fixture.assertIsEmpty(null));

		assertFalse(fixture.assertIsEmpty("Value"));
		assertFalse(fixture.assertIsEmpty(" Value"));
	}

	/**
	 * Test for assertIsNotEmpty.
	 */
	@Test
	public void assertIsNotEmptyWorksCorrect() {
		assertTrue(fixture.assertIsNotEmpty("Value"));
		assertTrue(fixture.assertIsNotEmpty(" Value"));

		assertFalse(fixture.assertIsNotEmpty(""));
		assertFalse(fixture.assertIsNotEmpty(" "));
		assertFalse(fixture.assertIsNotEmpty(null));
	}

	/**
	 * Test for setElementlist and getElementlist.
	 * 
	 * @throws ElementKeyNotFoundException
	 *             caused by an error
	 */
	@Test
	public void elementlistInitializeCorrect() throws ElementKeyNotFoundException {
		// initialization test
		assertNull(fixture.elementListService);
		fixture.setElementlist(ELEMENT_LIST);
		assertNotNull(fixture.elementListService);

		assertEquals("Datei", fixture.elementListService.getValue("common.file"));

		fixture.setElementlist(ELEMENT_LIST_TXT);
		assertEquals("username", fixture.elementListService.getValue("x"));
	}

	/**
	 * Test for retrieveLocater.
	 */
	@Test
	public void retrieveLocaterWorksCorrect() {
		fixture.setElementlist(ELEMENT_LIST);

		// known keys
		assertEquals("Datei", fixture.retrieveLocater("common.file"));
		assertEquals("Datei", fixture.retrieveLocater("common.file "));
		assertEquals("TEXT::Abbrechen", fixture.retrieveLocater("wizard.cancelButton"));
		assertEquals("ID::chose.scenario", fixture.retrieveLocater("chose.scenario"));

		try {
			fixture.retrieveLocater("not.exist");
			Assert.fail();
		} catch (StopTestException e) {
			// Exception expected
			assertTrue(true);
		}
	}

	/**
	 * Test for createBy.
	 */
	@Test
	public void createByProvidesCorrectObject() {
		fixture.setElementlist(ELEMENT_LIST);

		assertTrue(fixture.createBy("common.file").toString().startsWith("By.id:"));
		assertTrue(fixture.createBy("view.rename").toString().startsWith("By.xpath:"));
		assertTrue(fixture.createBy("view.run").toString().startsWith("By.xpath:"));

		// test all prefixes
		assertEquals(By.id("myId"), fixture.createBy("prefix_id"));
		assertEquals(By.className("myClass"), fixture.createBy("prefix_classname"));
		assertEquals(By.cssSelector("myCss"), fixture.createBy("prefix_cssselector"));
		assertEquals(By.linkText("myLinkText"), fixture.createBy("prefix_linktext"));
		assertEquals(By.name("myName"), fixture.createBy("prefix_name"));
		assertEquals(By.partialLinkText("myPartial"), fixture.createBy("prefix_partial"));
		assertEquals(By.tagName("myTagName"), fixture.createBy("prefix_tagname"));
		assertEquals(By.xpath("myXPath"), fixture.createBy("prefix_xpath"));

		// value with argument
		assertEquals(By.id("Button 1 id"), fixture.createBy("common.args", "1"));
		assertEquals(By.id("Button new id"), fixture.createBy("common.args", "new"));
		assertEquals(By.id("Button {0} id"), fixture.createBy("common.args", new String[] {}));
		String nullArgument = null;
		assertEquals(By.id("Button null id"), fixture.createBy("common.args", nullArgument));

		// locater without value
		assertEquals(By.id(""), fixture.createBy("empty"));

		try {
			fixture.createBy(null);
			Assert.fail();
		} catch (StopTestException e) {
			// expected, that null is not a valid key
			assertTrue(true);
		}

		// ::Text is not a valid prefix, use id as fallback
		assertTrue(fixture.createBy("wizard.cancelButton").toString().startsWith("By.id:"));

	}

	/**
	 * Test for navigateToUrl.
	 */
	@Test
	public void navigateToUrlWorksCorrect() {
		assertTrue(fixture.navigateToUrl(WEB_INDEX_PAGE.toString()));
		WebElement element = fixture.webDriver.findElement(By.name("user"));
		// check if navigation works
		assertTrue(element.isDisplayed());

		// also for a not existing URL a TRUE result is expected
		assertTrue(fixture.navigateToUrl(new File("not/existing/path").toURI().toString()));
	}

	/**
	 * Test for checkElementIsAvailable.
	 */
	@Test
	public void checkElementIsAvailableWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);

		assertTrue(fixture.checkElementIsAvailable("user"));
		assertTrue(fixture.checkElementIsAvailable("password"));

		try {
			fixture.setTimeout("2");
			fixture.checkElementIsAvailable("gender");
			fail("Timeout is expected, because element not present");
		} catch (StopTestException e) {
			// Timeout is expected, because element not present
			assertTrue(true);
		}

		fixture.setTimeout("2");
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertFalse(fixture.checkElementIsAvailable("invisible"));
	}

	/**
	 * Test for checkElementIsNotAvailable.
	 */
	@Test
	public void checkElementIsNotAvailableWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);
		assertTrue(fixture.checkElementIsNotAvailable("gender"));

		assertFalse(fixture.checkElementIsNotAvailable("user"));
		assertFalse(fixture.checkElementIsNotAvailable("password"));
	}

	/**
	 * Test for pressSpecialKey.
	 */
	@Test
	public void pressSpecialKeyWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);

		assertTrue(fixture.pressSpecialKey(Keys.TAB.name()));

		try {
			fixture.pressSpecialKey("UnknownKey");
			fail("Exception for unknown key is expected");
		} catch (StopTestException e) {
			// exception for unknown key is expected
			assertTrue(true);
		}

		try {
			fixture.pressSpecialKey(null);
			fail("Exception for null is expected");
		} catch (StopTestException e) {
			// exception for null is expected
			assertTrue(true);
		}

		try {
			fixture.pressSpecialKey("");
			fail("Exception for empty key is expected");
		} catch (StopTestException e) {
			// exception for empty key is expected
			assertTrue(true);
		}
	}

	/**
	 * Test for pressSpecialKeyOnElement.
	 */
	@Test
	public void pressSpecialKeyOnElementWorksCorrectWithWrongKeys() {
		startWebApplication(WEB_INDEX_PAGE);

		try {
			fixture.pressSpecialKeyOnElement("OKEY", "user");
		} catch (StopTestException e) {
			// OKEY is not a valid key
			assertTrue(e.getMessage().contains(
					"The specified key 'OKEY' is invalid and could not be found in selenium enum Keys!"));
		}

		try {
			fixture.pressSpecialKeyOnElement("", "user");
		} catch (StopTestException e) {
			// "" is a invalid key
			assertTrue(e.getMessage().contains("Invalid or empty key!"));
		}

		try {
			fixture.pressSpecialKeyOnElement(null, "user");
		} catch (StopTestException e) {
			// null is a invalid key
			assertTrue(e.getMessage().contains("Invalid or empty key!"));
		}
	}

	/**
	 * Test for pressSpecialKeyOnElement.
	 */
	@Test
	public void pressSpecialKeyOnElementWorksCorrectWithKnownKeys() {
		startWebApplication(WEB_INDEX_PAGE);

		fixture.insertIntoField("Max Mustermann", "user");
		fixture.insertIntoField("maxi", "password");
		fixture.pressSpecialKeyOnElement(Keys.TAB.name(), "user");
		fixture.pressSpecialKeyOnElement(Keys.TAB.name(), "user");
		fixture.pressSpecialKeyOnElement(Keys.ENTER.name(), "user");

		fixture.checkTextIsPresentOnPage("Eine Beispiel Web-Applikation");

		/* Not possible with HtmlUnitDriver but works with real Driver */
		// startWebApplication(WEB_ELEMENTS_PAGE);
		// assertTrue(fixture.checkIsSelected("checkbox"));
		// assertTrue(fixture.pressSpecialKeyOnElement(Keys.SPACE.name(),
		// "checkbox"));
		// assertFalse(fixture.checkIsSelected("checkbox"));
	}

	/**
	 * Test for setTimeout.
	 */
	@Test
	public void setTimeoutInitializeCorrect() {
		fixture.setTimeout("2");
		assertEquals(fixture.timeout, 2);

		fixture.setTimeout("0");
		assertEquals(fixture.timeout, 1);

		fixture.setTimeout("-3");
		assertEquals(fixture.timeout, 1);

		try {
			fixture.setTimeout("A5");
			fail();
		} catch (StopTestException e) {
			// expected, because timeout is not a correct interger value
			assertTrue(true);
		}
	}

	/**
	 * Test for insertIntoField.
	 */
	@Test
	public void insertIntoFieldWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);
		assertTrue(fixture.insertIntoField("Max Mustermann", "user"));
		assertTrue(fixture.insertIntoField("", "password"));
		assertTrue(fixture.insertIntoField(null, "password"));
		assertTrue(fixture.insertIntoField("maxi", "password"));
		// append insert
		assertTrue(fixture.insertIntoField("cosi", "password"));

		try {
			fixture.setTimeout("2");
			fixture.insertIntoField("male", "gender");
		} catch (StopTestException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test for clickRadioButtonOrCheckBox.
	 */
	@Test
	public void clickRadioButtonOrCheckBoxWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);

		assertTrue(fixture.checkIsSelected("checkbox"));
		assertTrue(fixture.clickRadioButtonOrCheckBox("HTML", "checkbox"));
		assertFalse(fixture.checkIsSelected("checkbox"));

		assertFalse(fixture.clickRadioButtonOrCheckBox("NIX", "checkbox"));
	}

	/**
	 * Test for selectOption.
	 */
	@Test
	public void selectOptionWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);

		assertTrue(fixture.selectOption("USA", "land"));
		assertFalse(fixture.selectOption("Portugal", "land"));

		try {
			fixture.setTimeout("2");
			fixture.selectOption("Portugal", "unknown_land");
		} catch (StopTestException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test for waitSeconds.
	 */
	@Test
	public void waitSecondsWorksCorrect() {
		long start = System.currentTimeMillis();
		assertTrue(fixture.waitSeconds(3));
		long end = System.currentTimeMillis();

		assertTrue((end - start) / 1000 > 2.9);
		assertTrue((end - start) / 1000 < 3.1);

		assertTrue(fixture.waitSeconds(0));
		assertTrue(fixture.waitSeconds(-2));
	}

	/**
	 * Test for readAttributeFromElement.
	 */
	@Test
	public void readAttributeFromElementWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);
		assertEquals("text", fixture.readAttributeFromElement("type", "user"));
		assertEquals("password", fixture.readAttributeFromElement("type", "password"));
		assertEquals("1", fixture.readAttributeFromElement("size", "land"));
		assertEquals("submit-button", fixture.readAttributeFromElement("class", "login"));
		assertEquals("Eine Beispiel Web-Applikation", fixture.readAttributeFromElement("innertext", "headline"));

		assertNull(fixture.readAttributeFromElement("not-existing", "user"));
	}

	/**
	 * Test for readValueOfElement.
	 */
	@Test
	public void readValueOfElementWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		// Value of input
		assertEquals("MyInput", fixture.readValueOfElement("input_field"));
		// Value of text area
		assertEquals("Enter your comments", fixture.readValueOfElement("textarea"));
		assertFalse("My comments".equals(fixture.readValueOfElement("textarea")));
		fixture.clearElement("textarea");
		assertEquals("", fixture.readValueOfElement("textarea"));
		// Value of combo box
		assertEquals("Deutschland", fixture.readValueOfElement("combobox"));
		// Value of check box
		assertEquals("HTML", fixture.readValueOfElement("checkbox"));
		// Value of img
		assertTrue(fixture.readValueOfElement("image").endsWith("fixture.png"));
		// Value of div
		assertEquals("Web-Applikation mit HTML-Elementen", fixture.readValueOfElement("headline"));
		// Value of radio button
		assertEquals("Second", fixture.readValueOfElement("radio"));
		// Value of option
		assertEquals("Schweden", fixture.readValueOfElement("option_schweden"));
		// Value of input button
		assertEquals("enabled", fixture.readValueOfElement("button_enabled"));
		// Value of button-tag
		assertEquals("button_tag", fixture.readValueOfElement("button"));
		// Value of span
		assertEquals("", fixture.readValueOfElement("span_empty"));
		// Value of li
		assertEquals("6", fixture.readValueOfElement("li_coffee"));
		// Value of param
		assertEquals("true", fixture.readValueOfElement("param_autoplay"));
		// Value of progress
		assertEquals("22", fixture.readValueOfElement("progress"));
		// Value of source
		assertTrue(fixture.readValueOfElement("source").endsWith("horse.mp3"));
	}

	/**
	 * Test for checkIsSelected and checkIsNotSelected.
	 */
	@Test
	public void checkIsSelectedAndIsNotSelectedWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.checkIsSelected("checkbox"));
		assertFalse(fixture.checkIsNotSelected("checkbox"));
		fixture.clickElement("checkbox");
		assertFalse(fixture.checkIsSelected("checkbox"));
		assertTrue(fixture.checkIsNotSelected("checkbox"));
	}

	/**
	 * Test for checkTextIsPresentOnPage.
	 */
	@Test
	public void checkTextIsPresentOnPageWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.checkTextIsPresentOnPage("HTML-Elemente"));
		assertTrue(fixture.checkTextIsPresentOnPage("Enter your comments"));
		assertFalse(fixture.checkTextIsPresentOnPage("http-equiv=\"content-type\""));
		assertFalse(fixture.checkTextIsPresentOnPage("Not-Part-Of-The-Source-Code"));
		assertTrue(fixture.checkTextIsPresentOnPage("MyInput"));
		assertTrue(fixture.checkTextIsPresentOnPage("iFrame"));

		// HTML tags are not visible
		assertFalse(fixture.checkTextIsPresentOnPage("<head>"));
		// hidden elements are not visible
		// TODO: does correctly not work
		// assertFalse(fixture.checkTextIsPresentOnPage("I am Invisible"));
	}

	/**
	 * Test for checkElementIsActive.
	 */
	@Test
	public void checkElementIsActiveWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.checkElementIsActive("button_enabled"));
		assertFalse(fixture.checkElementIsActive("button_disabled"));

		try {
			fixture.checkElementIsActive("hidden_button_disabled");
			fail();
		} catch (StopTestException e) {
			assertTrue(true);
		}

		try {
			fixture.checkElementIsActive("hidden_button_enabled");
			fail();
		} catch (StopTestException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test for checkElementIsInactive.
	 */
	@Test
	public void checkElementIsInactiveWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.checkElementIsInactive("button_disabled"));
		assertFalse(fixture.checkElementIsInactive("button_enabled"));

		try {
			fixture.checkElementIsInactive("hidden_button_disabled");
			fail();
		} catch (StopTestException e) {
			assertTrue(true);
		}

		try {
			fixture.checkElementIsInactive("hidden_button_enabled");
			fail();
		} catch (StopTestException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test for checkTextIsNotPresentOnPage.
	 */
	@Test
	public void checkTextIsNotPresentOnPageWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		// textarea input is visible
		assertFalse(fixture.checkTextIsNotPresentOnPage("Enter your comments"));
		// HTML tags are invisible
		assertTrue(fixture.checkTextIsNotPresentOnPage("<head>"));
		// hidden elements are invisible
		// TODO: does correctly not work
		// assertTrue(fixture.checkTextIsNotPresentOnPage("I am Invisible"));
	}

	/**
	 * Test for checkTextIsPresentOnElement.
	 */
	@Test
	public void checkTextIsPresentOnElementWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.checkTextIsPresentOnElement("Enter your comments", "textarea"));
		assertFalse(fixture.checkTextIsPresentOnElement("Bla Bla", "textarea"));
	}

	/**
	 * Test for checkTextIsNotPresentOnElement.
	 */
	@Test
	public void checkTextIsNotPresentOnElementWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertFalse(fixture.checkTextIsNotPresentOnElement("Enter your comments", "textarea"));
		assertTrue(fixture.checkTextIsNotPresentOnElement("Bla Bla", "textarea"));
	}

	/**
	 * Test for checkValueOfElement.
	 */
	@Test
	public void checkValueOfElementWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.checkValueOfElement("MyInput", "input_field"));
		assertFalse(fixture.checkValueOfElement("YourInput", "input_field"));

		try {
			fixture.checkValueOfElement("Hidden Input", "hidden_input_field");
			fail();
		} catch (StopTestException e) {
			// expected not to find hidden element
			assertTrue(true);
		}

		try {
			assertFalse(fixture.checkValueOfElement("Bla Bla", "hidden_input_field"));
			fail();
		} catch (StopTestException e) {
			// expected not to find hidden element
			assertTrue(true);
		}
	}

	/**
	 * Test for moveMouseToElement.
	 */
	@Test
	public void moveMouseToElementWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.moveMouseToElement("textarea"));

		try {
			fixture.moveMouseToElement("hidden_input_field");
			fail("Can't move mouse to a hidden field");
		} catch (StopTestException e) {
			// Expected not to find hidden field
			assertTrue(true);
		}
	}

	/**
	 * Test for switchToFrame.
	 */
	@Test
	public void switchToFrameWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);
		assertTrue(fixture.switchToFrame("iframe"));

		try {
			fixture.setTimeout("2");
			fixture.switchToFrame("hidden_iframe");
			fail("Can't switch to a hidden iFrame");
		} catch (StopTestException e) {
			// Expected not to find hidden iFrame
			assertTrue(true);
		}
	}

	/**
	 * Test for clickElement.
	 */
	@Test
	public void clickElementWorksCorrect() {
		startWebApplication(WEB_ELEMENTS_PAGE);

		assertTrue(fixture.checkIsSelected("checkbox"));
		assertTrue(fixture.clickElement("checkbox"));
		assertFalse(fixture.checkIsSelected("checkbox"));

		try {
			fixture.clickElement("hidden element");
			fail("Can't click a hidden element");
		} catch (StopTestException e) {
			// Expected not to find hidden element
			assertTrue(true);
		}
	}

	/**
	 * Test for closeBrowser.
	 */
	@Test
	public void closeBrowserWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);

		// close browser
		assertTrue(fixture.closeBrowser());

		try {
			fixture.navigateToUrl(WEB_INDEX_PAGE.toString());
			Assert.fail();
		} catch (WebDriverException e) {
			// expected, that session is closed
			assertTrue(true);
		}
	}

	/**
	 * Test for tearDown.
	 */
	@Test
	public void tearDownWorksCorrect() {
		startWebApplication(WEB_INDEX_PAGE);

		// close browser
		assertTrue(fixture.tearDown());

		try {
			fixture.navigateToUrl(WEB_INDEX_PAGE.toString());
			Assert.fail();
		} catch (WebDriverException e) {
			// expected, that session is closed
			assertTrue(true);
		}
	}

	/**
	 * Test for tearDown.
	 */
	@Test
	public void stopTestExecutionWorksCorrect() {
		try {
			fixture.stopTestExecution();
			Assert.fail();
		} catch (StopTestException e) {
			// StopTestException expected, to stop the test
			assertTrue(true);
		}
	}

	/**
	 * Mock for the WebFixture class. The original web driver is replaced by the
	 * HtmlUnitDriver.
	 */
	private class JUnitWebFixture extends HtmlWebFixture {

		/**
		 * Initialize web driver.
		 */
		public JUnitWebFixture() {
			openBrowser("");
			manageTimeouts();
		}

		/**
		 * Always initialize the HtmlUnitDriver. browserName and browserPath are
		 * ignored.
		 * 
		 * @param browserName
		 *            any String (ignored)
		 * 
		 * @return always TRUE
		 */
		@Override
		public boolean openBrowser(String browserName) {
			webDriver = new HtmlUnitDriver(true);
			return true;
		}
	}

	/**
	 * Test for openBrowser to get maximum code coverage.
	 */
	@Test
	public void testJUnitWebFixture() {
		assertTrue(fixture.webDriver instanceof HtmlUnitDriver);
	}

	/**
	 * Starts the web application. Sets element list and navigates to the given
	 * URI.
	 * 
	 * @param uri
	 *            start page of the web application
	 */
	private void startWebApplication(URI uri) {
		fixture.setElementlist(ELEMENT_LIST_WEB);
		fixture.navigateToUrl(uri.toString());
	}
}
