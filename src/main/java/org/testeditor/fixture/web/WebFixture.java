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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.testeditor.fixture.core.elementlist.ElementListService;
import org.testeditor.fixture.core.exceptions.ElementKeyNotFoundException;
import org.testeditor.fixture.core.exceptions.StopTestException;
import org.testeditor.fixture.core.interaction.Fixture;
import org.testeditor.fixture.core.interaction.StoppableFixture;
import org.testeditor.fixture.core.utils.ExceptionUtils;
import org.testeditor.fixture.core.utils.StringUtils;

/**
 * Provides methods for basic Web GUI-testing like click on type input. Projects
 * may inherit from this generic fixture and could use the protected methods for
 * any extensions.
 */
public class WebFixture implements StoppableFixture, Fixture {
	protected static final String LINUX = "Linux";
	protected static final String MAC_OS = "Mac OS";
	protected static final String WINDOWS = "Windows";
	private static final Logger LOGGER = Logger.getLogger(WebFixture.class);

	private Integer waitInMillis = 250;
	private Integer waitCounter = 100;

	private ElementListService elementListService;
	protected WebDriver webDriver;
	private int timeout;

	/**
	 * Creates the element list instance representing the GUI-Map for widget
	 * element id's of an application and the user defined names for this
	 * represented GUI element. Often used in a FitNesse ScenarioLibrary for
	 * configuration purpose. <br />
	 * 
	 * FitNesse usage..: |set elementlist|arg1| <br/>
	 * FitNesse example: |set elementlist|../ElementList/content.txt| <br />
	 * <br />
	 * 
	 * @param elementList
	 *            relative path of the element list content.txt wiki site on a
	 *            FitNesse Server where WikiPages is the directory where all the
	 *            Wiki Sites of the recent project are
	 */
	public void setElementlist(String elementList) {
		elementListService = ElementListService.instanceFor(elementList);
	}

	/**
	 * @return the elementListService
	 */
	protected ElementListService getElementlist() {
		return elementListService;
	}

	/**
	 * @return the webDriver
	 */
	protected WebDriver getWebDriver() {
		return webDriver;
	}

	/**
	 * The value is used by the Method waitForElement.
	 * 
	 * @param waitInMillis
	 *            the waitInMillis to set
	 */
	public void setWaitInMillis(Integer waitInMillis) {
		this.waitInMillis = waitInMillis;
	}

	/**
	 * @return the waitInMillis as an {@link Integer}
	 */
	protected Integer getWaitInMillis() {
		return waitInMillis;
	}

	/**
	 * The value is used by the Method waitForElement.
	 * 
	 * @param waitCounter
	 *            the waitCounter to set
	 */
	public void setWaitCounter(Integer waitCounter) {
		this.waitCounter = waitCounter;
	}

	/**
	 * @return the waitCounter
	 */
	protected Integer getWaitCounter() {
		return waitCounter;
	}

	/**
	 * Opens a specific browser (e.g. Firefox, Google-Chrome or Microsoft
	 * Internet Explorer), it is possible to use 'firefox', 'chrome' or 'ie' as
	 * the browserName. <br />
	 * 
	 * FitNesse usage..: |open Browser|arg1| <br />
	 * FitNesse example: |open Browser|firefox| <br />
	 * <br />
	 * 
	 * Please note that for Firefox there is a system property
	 * 'webdriver.firefox.bin' which should be set to the path of the used
	 * browser.
	 * 
	 * @param browserName
	 *            name of browser ('ie', 'chrome' or 'firefox')
	 * @param browserPath
	 *            path to the browser
	 * @return true, if browser starts successful, otherwise false
	 */
	public boolean openBrowser(String browserName, String browserPath) {

		String osName = System.getProperty("os.name");
		LOGGER.debug("open browser IN PROCESS - operating System: " + osName + ", browserName: " + browserName);

		try {

			if ("firefox".equalsIgnoreCase(browserName)) {
				openFirefox(osName, browserPath);
			} else if ("ie".equalsIgnoreCase(browserName)) {
				DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
				cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, "true");
				webDriver = new InternetExplorerDriver(cap);
			} else if ("chrome".equalsIgnoreCase(browserName)) {
				System.setProperty("webdriver.chrome.driver", browserPath);
				webDriver = new ChromeDriver();
			} else {
				String logMessage = "browser '" + browserName + " not available";
				LOGGER.error(logMessage);
				throw new StopTestException(logMessage);
			}

		} catch (WebDriverException e) {
			// here will be thrown an exception if installed browser was not
			// found
			LOGGER.error(e.getMessage(), e);
			throw new StopTestException(e.getMessage());
		}

		return true;
	}

	/**
	 * Helper method for openBrowser which handles the specifics to open
	 * Firefox.
	 * 
	 * @param osName
	 *            the name of the OS (should contain "Windows", "Mac OS" or
	 *            "Linux")
	 * @param browserPath
	 *            path to the (portable) Firefox executable to be started
	 */
	private void openFirefox(String osName, String browserPath) {

		try {
			if (browserPath != null && !browserPath.equals("")) {

				if (osName.contains(WINDOWS)) {
					System.setProperty("webdriver.firefox.bin", browserPath);
				} else if (osName.contains(LINUX)) {
					System.setProperty("webdriver.firefox.bin", browserPath);
					System.setProperty("webdriver.firefox.profile", "testing");

				} else if (osName.contains(MAC_OS)) {
					System.setProperty("webdriver.firefox.bin", browserPath);
				}

				// check if given browser path exists
				// and throw an exception if not exists
				if (!(new File(browserPath)).exists()) {
					String logMessage = "browserPath '" + browserPath + " does not exist";
					LOGGER.error(logMessage);
					throw new StopTestException(logMessage);
				}
			}

			webDriver = new FirefoxDriver();
		} catch (WebDriverException e) {
			// here will be thrown an exception if installed browser was not
			// found
			LOGGER.error(e.getMessage(), e);
			throw new StopTestException(e.getMessage());
		}
	}

	/**
	 * Navigates to a new web page in the current browser window. <br />
	 * 
	 * Usage for FitNesse: |navigate to Url|http://www.example.org|<br />
	 * 
	 * FitNesse usage..: |navigate to Url|arg1| <br />
	 * FitNesse example: |navigate to Url|http://www.example.org| <br />
	 * <br />
	 * 
	 * @param url
	 *            URL of page to navigate to
	 * @return always true to show inside FitNesse a positive result
	 */
	public boolean navigateToUrl(String url) {
		webDriver.get(url);
		return true;
	}

	/**
	 * Checks if a given value is empty (i.e. a <code>null</code>-value or an
	 * empty string.
	 * 
	 * FitNesse usage..: |assert|arg1|is empty| <br />
	 * FitNesse example: |assert|Some Text|is empty| <br />
	 * <br />
	 * 
	 * @param value
	 *            the value to compare
	 * @return <code>true</code> if <code>value</code> is an empty string,
	 *         <code>false</code> otherwise
	 */
	public boolean assertIsEmpty(String value) {
		boolean result = false;
		if (value == null || value.trim().isEmpty()) {
			result = true;
		}
		return result;
	}

	/**
	 * Checks if a given value is not empty (i.e. not a <code>null</code>-value
	 * or an empty string.
	 * 
	 * FitNesse usage..: |assert|arg1|is not empty| <br />
	 * FitNesse example: |assert|Some Text|is not empty| <br />
	 * <br />
	 * 
	 * @param value
	 *            the value to compare
	 * @return <code>true</code> if <code>value</code> is not an empty string,
	 *         <code>false</code> otherwise
	 */
	public boolean assertIsNotEmpty(String value) {
		return !assertIsEmpty(value);
	}

	/**
	 * Compares a given value with another value for equality.
	 * 
	 * FitNesse usage..: |assert|arg1|is equal to|arg2| <br />
	 * FitNesse example: |assert|Some Text|is equal to|Some Other Text| <br />
	 * <br />
	 * 
	 * @param first
	 *            the first value to compare
	 * @param second
	 *            the second value to compare
	 * @return <code>true</code> if <code>first</code> is equal to
	 *         <code>second</code>, <code>false</code> otherwise
	 */
	public boolean assertIsEqualTo(String first, String second) {
		boolean result = false;

		if (first == null && second == null) {
			result = true;
		} else if (first != null && second != null && (first.trim()).equals(second.trim())) {
			result = true;
		}

		return result;
	}

	/**
	 * Compares a given value with another value for inequality.
	 * 
	 * FitNesse usage..: |assert|arg1|is not equal to|arg2| <br />
	 * FitNesse example: |assert|Some Text|is not equal to|Some Other Text| <br />
	 * <br />
	 * 
	 * @param first
	 *            the first value to compare
	 * @param second
	 *            the second value to compare
	 * @return <code>true</code> if <code>first</code> is not equal to
	 *         <code>second</code>, <code>false</code> otherwise
	 */
	public boolean assertIsNotEqualTo(String first, String second) {
		return !assertIsEqualTo(first, second);
	}

	/**
	 * Checks if a given string is found within another string.
	 * 
	 * FitNesse usage..: |assert|arg1|contains|arg2| <br />
	 * FitNesse example: |assert|Some Text|contains|me Te| <br />
	 * <br />
	 * 
	 * @param first
	 *            the string to analyze
	 * @param second
	 *            the string to be found within <code>first</code>
	 * @return <code>true</code> if <code>first</code> contains
	 *         <code>second</code>, <code>false</code> otherwise
	 */
	public boolean assertContains(String first, String second) {
		boolean result = false;

		if (first == null && second == null) {
			result = true;
		} else if (first != null && second != null && first.contains(second.trim())) {
			result = true;
		}

		return result;
	}

	/**
	 * Searches elements using XPath from the element list. This test asserts,
	 * that the XPath query yields at least one hit.<br />
	 * <br />
	 * 
	 * FitNesse usage..: |assert element|arg1|found|[arg2, arg3, ...]| <br />
	 * FitNesse example: |assert element|TextboxInRow{0}Col{1}|found|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true, if at least one element matches the XPath query; false
	 *         otherwise.
	 */
	public boolean assertElementFound(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null) {
			result = true;
		}

		return result;
	}

	/**
	 * Searches elements by key from the element list. This method asserts, that
	 * the element does not exists.
	 * 
	 * FitNesse usage..: |assert element|arg1|not found|[arg2, arg3, ...]| <br />
	 * FitNesse example: |assert element|TextboxInRow{0}Col{1}|not found|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true, if no elements with the given key exists; false otherwise.
	 */
	public boolean assertElementNotFound(String elementListKey, String... replaceArgs) {
		boolean result = false;
		Integer waitCounterOriginalValue = waitCounter;

		// If the target element cannot be found, don't wait too.
		waitCounter = 2;
		try {
			// If no exception is thrown, then the target element was found...
			WebElement element = findWebelement(elementListKey, false, replaceArgs);
			if (element == null) {
				result = true;
			}
		} catch (TimeoutException e) {
			// Search for the target element timed out, which is exactly what we
			// wanted. In this case the exception will be silently discarded and
			// the result of this method is true.
			result = true;
		}
		// Restore the original timeout wait counter.
		waitCounter = waitCounterOriginalValue;

		return result;
	}

	/**
	 * Finds an element on the web page by its Element List key and inputs a
	 * special key stroke in its context. <br />
	 * <br />
	 * 
	 * FitNesse usage..: |enter special key|arg1| <br />
	 * FitNesse example: |enter special key|RIGHT| <br />
	 * <br />
	 * 
	 * @param key
	 *            the key stroke to enter (see
	 *            https://code.google.com/p/selenium
	 *            /source/browse/java/client/src/org/openqa/selenium/Keys.java)
	 * @return the value of the target element; empty String, if the element
	 *         could not be found or is not visible (i.e. hidden using CSS,
	 *         etc.).
	 */
	public boolean enterSpecialKey(String key) {
		boolean result = false;

		Keys seleniumKey = Keys.NULL;
		try {
			seleniumKey = Keys.valueOf(key.toUpperCase());
		} catch (IllegalArgumentException e) {
			String message = "The specified key \"" + key
					+ "\" is invalid and could not be found in selenium enum Keys!";
			LOGGER.error(message, e);
			throw new StopTestException(message);
		}

		Actions action = new Actions(webDriver);
		action.sendKeys(seleniumKey).build().perform();
		result = true;

		return result;
	}

	/**
	 * Finds a textbox by its Element List key and gets its value. <br />
	 * <br />
	 * 
	 * FitNesse usage..: |$var=|read textbox;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |$result=|read textbox;|TextboxInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the value of the target element; empty String, if the element
	 *         could not be found or is not visible (i.e. hidden using CSS,
	 *         etc.).
	 */
	public String readTextbox(String elementListKey, String... replaceArgs) {
		return readAttributeFromField("value", elementListKey, replaceArgs);
	}

	/**
	 * Finds a combobox by its Element List key and gets its value. <br />
	 * <br />
	 * 
	 * FitNesse usage..: |$var=|read combobox;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |$result=|read combobox;|ComboboxInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the value of the target element; empty String, if the element
	 *         could not be found or is not visible (i.e. hidden using CSS,
	 *         etc.).
	 */
	public String readCombobox(String elementListKey, String... replaceArgs) {
		return readAttributeFromField("value", elementListKey, replaceArgs);
	}

	/**
	 * Finds a checkbox by its Element List key and gets its value. <br />
	 * <br />
	 * 
	 * FitNesse usage..: |$var=|read checkbox;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |$result=|read checkbox;|CheckboxInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true, if the target element is checked; false, if the element is
	 *         not checked or the element could not be found or is not visible
	 *         (i.e. hidden using CSS, etc.).
	 */
	public boolean readCheckbox(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			result = element.isSelected();
		}

		return result;
	}

	/**
	 * Finds an element by its Element List key and gets the value of an
	 * associated attribute (e.g. <code>"value"</code> or
	 * <code>"innerText"</code> ). <br />
	 * <br />
	 * 
	 * FitNesse usage..: |$var=|read attribute|arg1|from field;|arg2|[arg3,
	 * arg4, ...]| <br />
	 * FitNesse example: |$result=|read attribute|value|from
	 * field;|CheckboxInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param attribute
	 *            the attribute to get from the target element
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the value associated with the given <code>attribute</code> of the
	 *         target element; empty String, if the element could not be found
	 *         or is not visible (i.e. hidden using CSS, etc.).
	 */
	public String readAttributeFromField(String attribute, String elementListKey, String... replaceArgs) {
		String result = privateReadAttributeFromField(attribute, elementListKey, replaceArgs);

		return result;
	}

	/**
	 * 
	 * @param attribute
	 *            the attribute to get from the target element
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the value associated with the given <code>attribute</code> of the
	 *         target element; empty String, if the element could not be found
	 *         or is not visible (i.e. hidden using CSS, etc.).
	 */
	private String privateReadAttributeFromField(String attribute, String elementListKey, String... replaceArgs) {
		String result = "";

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			if (attribute.equalsIgnoreCase("innertext")) {
				result = element.getText();
			} else {
				result = element.getAttribute(attribute);
			}
		}
		return result;
	}

	/**
	 * Inserts the given value into an input field and checks if input was
	 * successful. The technical locator of the field gets identified by the
	 * element list matching the given key. <br />
	 * 
	 * FitNesse usage..: |insert|arg1|into field;|arg2|[arg3, arg4, ...]| <br />
	 * FitNesse example: |insert|Some Text|into field;|TextboxInRow{0}Col{1}|[5,
	 * 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param value
	 *            value for the input
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if input was successful, otherwise false
	 */
	public boolean insertIntoField(String value, String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			element.click();
			element.sendKeys(value);
			String expectedValue = null;
			if (element.getTagName().equalsIgnoreCase("select")) {
				Select s = new Select(element);
				expectedValue = s.getFirstSelectedOption().getText();
			} else {
				expectedValue = readAttributeFromField("value", elementListKey, replaceArgs);
			}

			if (assertIsEqualTo(value, expectedValue)) {
				result = true;
			} else {
				throw new StopTestException("Value wasn't inserted correctly");
			}
		}

		return result;
	}

	/**
	 * Works just like
	 * <code>insertIntoField(value, elementListKey, replaceArgs)</code> except
	 * the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param value
	 *            value for the input
	 * @return true if input was successful, otherwise false
	 */
	public boolean insertIntoField(String value, String elementListKey) {
		return insertIntoField(value, elementListKey, new String[] {});
	}

	/**
	 * Clears a given input field. The technical locator of the field gets
	 * identified by the element list matching the given key.<br />
	 * 
	 * FitNesse usage..: |clear;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |clear;|TextboxInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if clear was successful, otherwise false
	 */
	public boolean clear(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			element.clear();
			result = true;
		}

		return result;
	}

	/**
	 * Works just like <code>clear(elementListKey, replaceArgs)</code> except
	 * the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return true if clear was successful, otherwise false
	 */
	public boolean clear(String elementListKey) {
		return clear(elementListKey, new String[] {});
	}

	/**
	 * Checks if a given input field is enabled (i.e. editable). The technical
	 * locator of the field gets identified by the element list matching the
	 * given key.<br />
	 * 
	 * FitNesse usage..: |assert element|arg1|enabled|[arg2, arg3, ...]| <br />
	 * FitNesse example: |assert element|TextboxInRow{0}Col{1}|enabled|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if the field is enabled, otherwise false
	 */
	public boolean assertElementEnabled(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed() && element.isEnabled()) {
			result = true;
		}

		return result;
	}

	/**
	 * Checks if a given input field is not enabled (i.e. not editable). The
	 * technical locator of the field gets identified by the element list
	 * matching the given key.<br />
	 * 
	 * FitNesse usage..: |assert element|arg1|disabled|[arg2, arg3, ...]| <br />
	 * FitNesse example: |assert element|TextboxInRow{0}Col{1}|disabled|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if the field is not enabled, otherwise false
	 */
	public boolean assertElementDisabled(String elementListKey, String... replaceArgs) {
		return !assertElementEnabled(elementListKey, replaceArgs);
	}

	/**
	 * Waits for the given period of time before executing the next command.<br />
	 * 
	 * FitNesse usage..: |wait seconds|arg1| <br />
	 * FitNesse example: |wait seconds|2| <br />
	 * <br />
	 * 
	 * @param timeToWait
	 *            Time to wait in seconds
	 * @return always true to show inside FitNesse a positive result
	 */
	public boolean waitSeconds(long timeToWait) {
		waitTime(timeToWait * 1000);
		return true;
	}

	/**
	 * Waits for the given period.
	 * 
	 * @param milliseconds
	 *            Time to wait in milliseconds
	 */
	protected void waitTime(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage());
		}
	}

	/**
	 * Clicks on a element or button. <br />
	 * <br />
	 * 
	 * FitNesse usage..: |click;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |click;|ButtonInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if click was successful; otherwise false
	 */
	public boolean click(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			element.click();
			result = true;
		}

		return result;
	}

	/**
	 * Works just like <code>click(elementListKey, replaceArgs)</code> except
	 * the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return true if click was successful; otherwise false
	 */
	public boolean click(String elementListKey) {
		return click(elementListKey, new String[] {});
	}

	/**
	 * Double clicks on a element or button. <br />
	 * <br />
	 * 
	 * FitNesse usage..: |double click;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |double click;|ButtonInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if click was successful; otherwise false
	 */
	public boolean doubleClick(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			Actions action = new Actions(webDriver);
			action.doubleClick(element).build().perform();
			result = true;
		}

		return result;
	}

	/**
	 * Searches for a given text in the HTML source and returns true if found.
	 * If the text is not found immediately, this method will retry for as long
	 * as this class would normally also wait for a widget to found.
	 * 
	 * FitNesse usage..: |wait for text|arg1| <br />
	 * FitNesse example: |wait for text|Login successful| <br />
	 * <br />
	 * 
	 * @param text
	 *            to be searched for
	 * @return true if the String-Value of <code>text</code> is present; throws
	 *         a StopTestException otherwise.
	 */
	public boolean waitForText(String text) {
		boolean result = false;
		int counter = 0;

		while (counter < waitCounter) {
			result = webDriver.getPageSource().contains(text);
			if (result) {
				break;
			}

			waitTime(waitInMillis);
			counter++;
		}

		if (!result) {
			String message = "The specified text \"" + text + "\" could not be found!";
			LOGGER.error(message);
			throw new StopTestException(message);
		}

		return result;
	}

	/**
	 * Searches for a given text in the HTML source and returns true if found.
	 * 
	 * FitNesse usage..: |text|arg1|is visible| <br />
	 * FitNesse example: |text|Login successful|is visible| <br />
	 * <br />
	 * 
	 * @param text
	 *            to be searched for
	 * @return true if the String-Value of <code>text</code> is present, false
	 *         otherwise
	 */
	public boolean textIsVisible(String text) {
		boolean result = webDriver.getPageSource().contains(text);
		if (!result) {
			String message = "The specified text \"" + text + "\" could not be found!";
			LOGGER.error(message);
		}

		return result;
	}

	/**
	 * Searches for a given text in the HTML source and returns true if found.
	 * 
	 * FitNesse usage..: |text|arg1|is visible| <br />
	 * FitNesse example: |text|Login successful|is visible| <br />
	 * <br />
	 * 
	 * @param text
	 *            to be searched for
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if the String-Value of <code>text</code> is present, false
	 *         otherwise
	 */
	public boolean textIsVisibleInField(String text, String elementListKey, String... replaceArgs) {
		String result = privateReadAttributeFromField("innertext", elementListKey, replaceArgs);
		return result.equalsIgnoreCase(text);
	}

	/**
	 * Switches to another frame (e.g. iFrame).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return true if switch successed
	 */
	public boolean switchToFrame(String elementListKey) {
		WebElement webElement = findWebelement(elementListKey);
		webDriver.switchTo().frame(webElement);

		return true;
	}

	/**
	 * Searches for a given text in the HTML source and returns true if not
	 * found.
	 * 
	 * FitNesse usage..: |text|arg1|is unvisible| <br />
	 * FitNesse example: |text|Login successful|is unvisible| <br />
	 * <br />
	 * 
	 * @param text
	 *            to be searched for
	 * @return true if the String-Value of <code>text</code> isn't present,
	 *         false otherwise
	 */
	public boolean textIsUnvisible(String text) {
		boolean result = !webDriver.getPageSource().contains(text);
		if (!result) {
			String message = "The specified text \"" + text + "\" could be found!";
			LOGGER.error(message);
		}

		return result;
	}

	/**
	 * Close the browser instance.
	 * 
	 * FitNesse usage..: |close browser| <br />
	 * FitNesse example: |close browser| <br />
	 * <br />
	 * 
	 * @return always true to show inside FitNesse a positive result
	 */
	public boolean closeBrowser() {
		// checks if Browser is Chrome because Chromedriver does not function
		// with Close-Method of WebDriver
		if (webDriver instanceof ChromeDriver) {
			webDriver.quit();
		} else {
			webDriver.close();
			// necessary wait, at least for FF portable
			waitTime(500);
			// best effort
			try {
				webDriver.quit();
				// CHECKSTYLE:OFF
			} catch (Throwable t) { // disable checkstyle for empty block
				// CHECKSTYLE:ON
				// NFA - at least Firefox portable is down after close()
			}
		}
		return true;
	}

	/**
	 * Simulates a MouseOver on a Menu. Moves to the given Gui-Element.
	 * 
	 * FitNesse usage..: |move to element;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |move to element;|IconInRow{0}Col{1}|[5, 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            the Gui-Element where to move.
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if WebElement is found and Mouse moved to this Element to
	 *         perform an Action.
	 */
	public boolean moveToElement(String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("element found ready for Action (moveToElement)");
			}
			Actions actions = new Actions(webDriver);
			actions.moveToElement(element).build().perform();
			result = true;
		}

		return result;
	}

	/**
	 * Move to element and click menu.
	 * 
	 * @param elementListKey
	 *            key of element in elementList.conf.
	 * @param menuEntryKey
	 *            key of menu.
	 * @return true if element is found and menu is activated.
	 */
	public boolean moveToElementAndClickMenu(String elementListKey, String menuEntryKey) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, new String[] {});

		if (element != null && element.isDisplayed()) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("element found ready for Action (moveToElement)");
			}
			Actions actions = new Actions(webDriver);
			actions.moveToElement(element).build().perform();
			result = true;
		}
		result = result && click(menuEntryKey);

		return result;

	}

	/**
	 * Works just like <code>moveToElement(elementListKey, replaceArgs)</code>
	 * except the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param elementListKey
	 *            the Gui-Element where to move.
	 * @return true if WebElement is found and Mouse moved to this Element to
	 *         perform an Action.
	 */
	public boolean moveToElement(String elementListKey) {
		return moveToElement(elementListKey, new String[] {});
	}

	/**
	 * This Method finds WebElements with a given Key as XPATH expression or
	 * id-value.
	 * 
	 * @param elementListKey
	 *            key in the ElementList
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the webElement if element is not found, then return null
	 */
	protected WebElement findWebelement(String elementListKey, String... replaceArgs) {
		return findWebelement(elementListKey, true, replaceArgs);
	}

	/**
	 * This Method finds WebElements with a given Key as XPATH expression or
	 * id-value.
	 * 
	 * @param elementListKey
	 *            key in the ElementList
	 * @param handleTimeout
	 *            specifies if a TimeoutException should be handled by this
	 *            method (default: true!) or in special cases (e.g.
	 *            assertElementNotFound) should be handled by the calling method
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the webElement if element is not found, then return null
	 */
	protected WebElement findWebelement(String elementListKey, boolean handleTimeout, String... replaceArgs) {
		WebElement element = null;

		try {
			element = waitForElement(createByFromElementList(elementListKey, replaceArgs));
		} catch (TimeoutException e) {
			if (handleTimeout) {
				LOGGER.error(elementListKey);
				LOGGER.error(e.getMessage());
				ExceptionUtils.handleNoSuchElementException(elementListKey, e);
			} else {
				// Don't handle the exception here. Just pass it on to the
				// caller. For instance, when the caller expects that finding a
				// certain element will fail.
				throw e;
			}
		}

		return element;
	}

	/**
	 * Wait for a element with a given Key as XPATH expression or id-value.
	 * 
	 * @param elementListKey
	 *            key in the ElementList
	 * @return true if WebElement is found and displayed
	 */
	public boolean waitForElement(String elementListKey) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey);
		if (element != null && element.isDisplayed()) {
			result = true;
		}
		return result;
	}

	/**
	 * This Method finds WebElements with a given {@link By}. If the given
	 * {@link By} is an XPath expression and matches multiple elements, this
	 * method will return the first visible element (as determined by selenium's
	 * {@link isDisplayed} method) that matches.
	 * 
	 * @param context
	 *            the search context or subtree to query. Argument
	 *            <code>context</code> may be <code>null</code> to search the
	 *            whole document.
	 * @param by
	 *            Mechanism used to locate elements within a document
	 * @return the webElement
	 * @throws TimeoutException
	 *             if element is not found
	 */
	protected WebElement waitForElement(final WebElement context, final By by) throws TimeoutException {
		List<WebElement> elements = null;
		WebElement result = null;
		int counter = 0;

		while (elements == null) {
			if (counter >= waitCounter) {
				break;
			}

			try {
				if (context == null) {
					elements = webDriver.findElements(by);
				} else {
					elements = context.findElements(by);
				}
			} catch (Exception e) {
				elements = null;
			}

			if (elements != null) {
				result = getFirstDisplayed(elements);
				if (result != null) {
					break;
				}
			}

			elements = null;
			waitTime(waitInMillis);
			counter++;
		}

		if (result == null || !result.isDisplayed()) {
			throw new TimeoutException("Timeout: no element was found");
		}

		return result;
	}

	/**
	 * Iterates over a list of web elements and returns the first that is
	 * visible (as determined by selenium's {@link isDisplayed} method).
	 * 
	 * @param elements
	 *            the element list to search
	 * @return the first visible element from the supplied list; null if the
	 *         list is empty or not web element is visible
	 */
	private WebElement getFirstDisplayed(List<WebElement> elements) {
		WebElement result = null;
		if (elements != null) {
			for (WebElement element : elements) {
				if (element.isDisplayed()) {
					result = element;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * This Method finds WebElements with a given {@link By}.
	 * 
	 * @param by
	 *            Mechanism used to locate elements within a document
	 * @return the webElement
	 * @throws TimeoutException
	 *             if element is not found
	 */
	protected WebElement waitForElement(final By by) throws TimeoutException {
		return waitForElement(null, by);
	}

	/**
	 * Returns the locator for a given key.
	 * 
	 * @param elementListKey
	 *            key in the ElementList
	 * @return locator as String
	 */
	protected String getLocatorFromElementList(String elementListKey) {

		try {
			return elementListService.getValue(elementListKey);
		} catch (ElementKeyNotFoundException e) {
			return defaultHandelKeyNotFoundException(elementListKey, e);
		}

	}

	/**
	 * 
	 * @param elementListKey
	 *            key in the ElementList, that isn't found.
	 * @param e
	 *            ElementKeyNotFoundException always thrown
	 */
	protected String defaultHandelKeyNotFoundException(String elementListKey, ElementKeyNotFoundException e) {
		ExceptionUtils.handleElementKeyNotFoundException(elementListKey, e);
		return "";
	}

	/**
	 * This Method create a By instance with a given Key as XPATH expression or
	 * id-value.
	 * 
	 * @param elementListKey
	 *            key (e.g. inputUsername)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return By
	 */
	protected By createByFromElementList(String elementListKey, String... replaceArgs) {
		String locator = getLocatorFromElementList(elementListKey);
		// Apostrophes in X-Paths must not be wiped out - hence escape
		if (locator.contains("%")) {
			LOGGER.info("contains % " + locator);
			locator = createXPathFromLocator(locator);
			LOGGER.info("replaced % " + locator);
		}
		if (locator != null) {
			locator = locator.replace("'", "''");
		}

		Object[] args;
		if (replaceArgs != null) {
			args = replaceArgs;
		} else {
			args = new Object[] {};
		}
		locator = MessageFormat.format(locator, args);

		By by;
		if (hasElementPrefix(locator)) {
			by = getByFromLacatorWithPraefix(locator);
		} else if (StringUtils.isXPath(locator)) {
			by = By.xpath(locator);
		} else {
			by = By.id(locator);
		}
		LOGGER.info(by);
		return by;
	}

	/**
	 * 
	 * 
	 * @param locator
	 *            as a String
	 * @return true if the locator starts with an ElementPrefix
	 */
	protected boolean hasElementPrefix(String locator) {
		return locator.startsWith(ElementPrefix.CLASSNAME.getName())
				|| locator.startsWith(ElementPrefix.CSSSELECTOR.getName())
				|| locator.startsWith(ElementPrefix.ID.getName())
				|| locator.startsWith(ElementPrefix.LINKTEXT.getName())
				|| locator.startsWith(ElementPrefix.NAME.getName())
				|| locator.startsWith(ElementPrefix.PARTIAL.getName())
				|| locator.startsWith(ElementPrefix.TAGNAME.getName())
				|| locator.startsWith(ElementPrefix.XPATH.getName());
	}

	/**
	 * 
	 * @param locator
	 *            identified by an element of the {@link ElementPrefix}
	 * @return By
	 */
	private By getByFromLacatorWithPraefix(String locator) {
		if (locator.startsWith(ElementPrefix.CLASSNAME.getName())) {
			locator = locator.substring(ElementPrefix.CLASSNAME.getName().length());
			return By.className(locator);
		} else if (locator.startsWith(ElementPrefix.CSSSELECTOR.getName())) {
			locator = locator.substring(ElementPrefix.CSSSELECTOR.getName().length());
			return By.cssSelector(locator);
		} else if (locator.startsWith(ElementPrefix.ID.getName())) {
			locator = locator.substring(ElementPrefix.ID.getName().length());
			return By.id(locator);
		} else if (locator.startsWith(ElementPrefix.LINKTEXT.getName())) {
			locator = locator.substring(ElementPrefix.LINKTEXT.getName().length());
			return By.linkText(locator);
		} else if (locator.startsWith(ElementPrefix.NAME.getName())) {
			locator = locator.substring(ElementPrefix.NAME.getName().length());
			return By.name(locator);
		} else if (locator.startsWith(ElementPrefix.PARTIAL.getName())) {
			locator = locator.substring(ElementPrefix.PARTIAL.getName().length());
			return By.partialLinkText(locator);
		} else if (locator.startsWith(ElementPrefix.TAGNAME.getName())) {
			locator = locator.substring(ElementPrefix.TAGNAME.getName().length());
			return By.tagName(locator);
		} else if (locator.startsWith(ElementPrefix.XPATH.getName())) {
			locator = locator.substring(ElementPrefix.XPATH.getName().length());
			return By.xpath(locator);
		}
		return By.id(locator);
	}

	/**
	 * 
	 * @param locator
	 *            the locator including '%'
	 * @return the xpath with contains the locator.
	 */
	protected String createXPathFromLocator(String locator) {
		String tempLocator = locator.replaceAll("%", "");
		LOGGER.info(tempLocator);
		String lc = "//*[contains(@id,'" + tempLocator + "')]";
		LOGGER.info(lc);
		return lc;

	}

	/**
	 * Invalid JavaDoc: Sets the implicit wait timeout in seconds for each test
	 * step.<br />
	 * 
	 * FitNesse usage..: |set timeout|arg1| <br />
	 * FitNesse example: |set timeout|1| <br />
	 * <br />
	 * 
	 * @param timeout
	 *            timeout in seconds
	 */
	public void setTimeout(String timeout) {
		this.timeout = Integer.valueOf(timeout);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(">>>> Set timeout to: " + this.timeout + " seconds. <<<<<");
		}
	}

	/**
	 * Checks if value is visible and if not stops the test.
	 * 
	 * @param value
	 *            Value to be found on website
	 * @return result True if value was found
	 */
	public boolean checkTextAndTearDown(String value) {
		boolean result = webDriver.getPageSource().contains(value);
		if (!result) {
			String message = "The specified text \"" + value + "\" could not be found!";
			result = true;
			throw new StopTestException(message, new Throwable());
		}
		return result;
	}

	@Override
	public boolean tearDown() {
		return closeBrowser();
	}

	public String getTestName() {
		return null;
	}

	public void postInvoke(Method arg0, Object arg1, Object... arg2) throws InvocationTargetException,
			IllegalAccessException {
	}

	public void preInvoke(Method arg0, Object arg1, Object... arg2) throws InvocationTargetException,
			IllegalAccessException {
	}

	public void setTestName(String arg0) {
	}
}
