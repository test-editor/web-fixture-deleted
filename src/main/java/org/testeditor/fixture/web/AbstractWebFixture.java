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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testeditor.fixture.core.elementlist.ElementListService;
import org.testeditor.fixture.core.exceptions.ContinueTestException;
import org.testeditor.fixture.core.exceptions.ElementKeyNotFoundException;
import org.testeditor.fixture.core.exceptions.StopTestException;
import org.testeditor.fixture.core.interaction.Fixture;
import org.testeditor.fixture.core.interaction.StoppableFixture;

/**
 * Abstract class for all web based fixtures.
 * 
 */
public abstract class AbstractWebFixture implements StoppableFixture, Fixture {

	/** Represents a map with user defined names and application elements. */
	protected ElementListService elementListService;
	/** Maximum wait time in seconds for each test step. */
	protected int timeout = 10;
	/** The web driver. */
	protected WebDriver webDriver;

	/**
	 * Creates the element list instance representing the GUI-Map for widget
	 * element id's of an application and the user defined names for this
	 * represented GUI element. Often used in a FitNesse ScenarioLibrary for
	 * configuration purpose.
	 * 
	 * @param elementList
	 *            relative path of the element list content.txt wiki site on a
	 *            FitNesse Server where WikiPages is the directory where all the
	 *            Wiki Sites of the recent project are
	 */
	public void setElementlist(String elementList) {
		this.elementListService = ElementListService.instanceFor(elementList);
	}

	/**
	 * Sets the maximum wait time in seconds for each test step.
	 * 
	 * @param timeout
	 *            timeout in seconds
	 * @throws StopTestException
	 *             if timeout is not a correct integer value
	 */
	public void setTimeout(String timeout) throws StopTestException {
		try {
			this.timeout = Integer.valueOf(timeout);
			if (this.timeout < 1) {
				this.timeout = 1;
			}
		} catch (NumberFormatException e) {
			throw new StopTestException("Timeout must be an integer greater or equal 1 second. ", e);
		}
	}

	/**
	 * Opens a specific browser (e.g. Firefox, Google-Chrome or Microsoft
	 * Internet Explorer), it is possible to use 'firefox', 'chrome' or 'ie' as
	 * browser name.
	 * <p/>
	 * 
	 * 
	 * For <b>Firefox</b> the path to the executable is needed:
	 * -Dwebdriver.firefox.bin<br/>
	 * For <b>Internet Explorer</b> the path to the web driver is needed:
	 * -Dwebdriver.ie.driver<br/>
	 * For <b>Chrome</b> the path to the web driver is needed:
	 * -Dwebdriver.chrome.driver
	 * 
	 * @param browserName
	 *            name of browser ('ie', 'chrome' or 'firefox')
	 * @return {@code true}, if browser starts successful, {@code false}
	 *         otherwise
	 */
	public boolean openBrowser(String browserName) {

		if ("firefox".equalsIgnoreCase(browserName)) {
			initFirefoxDriver();
		} else if ("ie".equalsIgnoreCase(browserName)) {
			initIEDriver();
		} else if ("chrome".equalsIgnoreCase(browserName)) {
			initChromeDriver();
		} else {
			throw new StopTestException("Browser '" + browserName + "' not available.");
		}

		manageTimeouts();

		return true;
	}

	/**
	 * Manages the timeouts for the web driver.
	 */
	protected void manageTimeouts() {
		webDriver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
	}

	/**
	 * Initialize web driver for the Internet Explorer.
	 */
	protected void initIEDriver() {
		DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
		cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, "true");
		webDriver = new InternetExplorerDriver(cap);
	}

	/**
	 * Initialize web driver for Google Chrome.
	 */
	protected void initChromeDriver() {
		webDriver = new ChromeDriver();
	}

	/**
	 * Initialize web driver for Firefox.
	 */
	protected void initFirefoxDriver() {
		String osName = System.getProperty("os.name");
		if (osName != null && osName.contains("Linux")) {
			System.setProperty("webdriver.firefox.profile", "testing");
		}

		String firefoxBin = System.getProperty("webdriver.firefox.bin");
		if (firefoxBin == null) {
			webDriver = new FirefoxDriver();
			// throw new StopTestException(
			// "Web driver initialisation error. Please specify system property -Dwebdriver.firefox.bin properly.");
		} else if (!(new File(firefoxBin)).exists()) {
			throw new StopTestException("Web driver initialisation error. Browser path '" + firefoxBin
					+ "' does not exist.");
		} else {
			webDriver = new FirefoxDriver();
		}
	}

	/**
	 * Close the browser instance.
	 * 
	 * @return always {@code true} to show inside FitNesse a positive result
	 */
	public boolean closeBrowser() {
		// checks if Browser is Chrome because Chrome-Driver does not function
		// with Close-Method
		if (webDriver instanceof ChromeDriver) {
			webDriver.quit();
		} else {
			webDriver.close();
			// necessary wait, at least for Firefox-Portable
			try {
				Thread.sleep(500);
				// CHECKSTYLE:OFF
			} catch (InterruptedException e) {
				// CHECKSTYLE:ON
				// try to quit browser anyway
			}
			// best effort
			try {
				webDriver.quit();
				// CHECKSTYLE:OFF
			} catch (Exception e) {
				// CHECKSTYLE:ON
				// At least Firefox-Portable is down after close()
			}
		}
		return true;
	}

	/**
	 * Navigates to a new web page in the current browser window.
	 * 
	 * @param url
	 *            URL of page to navigate to
	 * @return always {@code true} to show inside FitNesse a positive result
	 */
	public boolean navigateToUrl(String url) {
		webDriver.get(url);
		return true;
	}

	/**
	 * Checks if a given string {@code second} is found within {@code first}
	 * string.
	 * 
	 * @param first
	 *            the string to analyze
	 * @param second
	 *            the string to be found within {@code first}
	 * @return {@code true} if {@code first} contains {@code second},
	 *         {@code false} otherwise
	 */
	public boolean assertContains(String first, String second) {
		boolean result = false;

		if (first == null && second == null) {
			result = true;
		} else if (first != null && second != null && first.trim().contains(second.trim())) {
			result = true;
		}

		return result;
	}

	/**
	 * Checks if a given string {@code second} is <b>not</b> part of
	 * {@code first} string.
	 * 
	 * @param first
	 *            the string to analyze
	 * @param second
	 *            the string not to be part of {@code first}
	 * @return {@code true} if {@code first} <b>not</b> part of {@code second},
	 *         {@code false} otherwise
	 */
	public boolean assertNotContains(String first, String second) {
		return !assertContains(first, second);
	}

	/**
	 * Checks if a given value is empty (i.e. a {@code null}-value or an empty
	 * string.
	 * 
	 * @param value
	 *            the value to compare
	 * @return {@code true} if {@code value} is an empty string, {@code false}
	 *         otherwise
	 */
	public boolean assertIsEmpty(String value) {
		boolean result = false;
		if (value == null || value.trim().isEmpty()) {
			result = true;
		}
		return result;
	}

	/**
	 * Checks if a given value is <b>not</b> empty (i.e. not a {@code null}
	 * -value or an empty string.
	 * 
	 * @param value
	 *            the value to compare
	 * @return {@code true} if {@code value} is <b>not</b> an empty string,
	 *         {@code false} otherwise
	 */
	public boolean assertIsNotEmpty(String value) {
		return !assertIsEmpty(value);
	}

	/**
	 * Compares a given value with another value for equality.
	 * 
	 * @param first
	 *            the first value to compare
	 * @param second
	 *            the second value to compare
	 * @return {@code true} if {@code first} is equal to {@code second},
	 *         {@code false} otherwise
	 */
	public boolean assertIsEqual(String first, String second) {
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
	 * @param first
	 *            the first value to compare
	 * @param second
	 *            the second value to compare
	 * @return {@code true} if {@code first} is <b>not</b> equal to
	 *         {@code second}, {@code false} otherwise
	 */
	public boolean assertIsNotEqual(String first, String second) {
		return !assertIsEqual(first, second);
	}

	/**
	 * Waits for the given period of time before executing the next test step.
	 * 
	 * @param secondsToWait
	 *            Time to wait in seconds
	 * @return {@code true} if waiting was successful, {@code false} otherwise
	 */
	public boolean waitSeconds(long secondsToWait) {
		if (secondsToWait <= 0) {
			return true;
		}

		try {
			Thread.sleep(secondsToWait * 1000);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if a web element is present in the DOM and visible on the web
	 * page.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the web element is displayed on the page,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkElementIsAvailable(String elementListKey, String... replaceArgs) throws StopTestException {
		List<WebElement> elements = findWebElements(elementListKey, replaceArgs);
		for (WebElement webElement : elements) {
			if (webElement != null && webElement.isDisplayed()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a web element is present in the DOM and visible on the web
	 * page.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the web element is displayed on the page,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkElementIsAvailable(String elementListKey) throws StopTestException {
		return checkElementIsAvailable(elementListKey, new String[] {});
	}

	/**
	 * Checks if a web element is <b>not</b> present in the DOM or present but
	 * <b>not</b> displayed on web page.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the web element is <b>not</b> displayed on the
	 *         page, {@code false} otherwise
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkElementIsNotAvailable(String elementListKey, String... replaceArgs) throws StopTestException {
		try {
			WebElement element = webDriver.findElement(createBy(elementListKey, replaceArgs));
			return element == null || !element.isDisplayed();
		} catch (NoSuchElementException e) {
			return true;
		} catch (TimeoutException e) {
			return true;
		} catch (StaleElementReferenceException e) {
			return true;
		}
	}

	/**
	 * Checks if a web element is <b>not</b> present in the DOM or present but
	 * <b>not</b> displayed on web page.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the web element is <b>not</b> displayed on the
	 *         page, {@code false} otherwise
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkElementIsNotAvailable(String elementListKey) throws StopTestException {
		return checkElementIsNotAvailable(elementListKey, new String[] {});
	}

	/**
	 * Checks if a web element is active (Enabled).
	 * 
	 * Therefore it is ensured that the element is available (present in DOM and
	 * visible on web page). If element is not available a
	 * {@code StopTestException} is thrown.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the web element is active (enabled),
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkElementIsActive(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		return element.isEnabled();
	}

	/**
	 * Checks if a web element is active (Enabled).
	 * 
	 * Therefore it is ensured that the element is available (present in DOM and
	 * visible on web page). If element is not available a
	 * {@code StopTestException} is thrown.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the web element is active (enabled),
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkElementIsActive(String elementListKey) throws StopTestException {
		return checkElementIsActive(elementListKey, new String[] {});
	}

	/**
	 * Checks if a web element is inactive (disabled).
	 * 
	 * Therefore it is ensured that the element is available (present in DOM and
	 * visible on web page). If element is not available a
	 * {@code StopTestException} is thrown.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the web element is inactive (disabled),
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkElementIsInactive(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		return !element.isEnabled();
	}

	/**
	 * Checks if a web element is inactive (disabled).
	 * 
	 * Therefore it is ensured that the element is available (present in DOM and
	 * visible on web page). If element is not available a
	 * {@code StopTestException} is thrown.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the web element is inactive (disabled),
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkElementIsInactive(String elementListKey) throws StopTestException {
		return checkElementIsInactive(elementListKey, new String[] {});
	}

	/**
	 * Moves the mouse cursor to the displayed web element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean moveMouseToElement(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		Actions actions = new Actions(webDriver);
		actions.moveToElement(element).build().perform();
		return true;
	}

	/**
	 * Moves the mouse cursor to the displayed web element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean moveMouseToElement(String elementListKey) throws StopTestException {
		return moveMouseToElement(elementListKey, new String[] {});
	}

	/**
	 * Switches to another frame (e.g. iFrame).
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean switchToFrame(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement webElement = findAvailableWebElement(elementListKey, replaceArgs);
		webDriver.switchTo().frame(webElement);

		return true;
	}

	/**
	 * Switches to another frame (e.g. iFrame).
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean switchToFrame(String elementListKey) throws StopTestException {
		return switchToFrame(elementListKey, new String[] {});
	}

	/**
	 * Switches to the default content (main frame).
	 * 
	 * @return true if switch successed
	 */
	public boolean switchToDefaultContent() {
		webDriver.switchTo().defaultContent();

		return true;
	}

	/**
	 * Performs a mouse click on the displayed web element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean clickElement(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		element.click();
		return true;
	}

	/**
	 * Performs a mouse click on the displayed web element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean clickElement(String elementListKey) throws StopTestException {
		return clickElement(elementListKey, new String[] {});
	}

	/**
	 * Double clicks on an element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean doubleClickElement(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		Actions action = new Actions(webDriver);
		action.moveToElement(element).doubleClick().build().perform();
		return true;
	}

	/**
	 * Double clicks on an element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean doubleClickElement(String elementListKey) throws StopTestException {
		return doubleClickElement(elementListKey, new String[] {});
	}

	/**
	 * Inserts a value into a field (e.g. an input field or text area).
	 * 
	 * @param value
	 *            the value to insert
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the {@code value} was insert successful,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean insertIntoField(String value, String elementListKey, String... replaceArgs) throws StopTestException {
		if (value == null) {
			return true;
		}

		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		String oldValue = element.getAttribute("value");
		element.click();
		element.sendKeys(value);
		return oldValue.concat(value).equals(element.getAttribute("value"));
	}

	/**
	 * Inserts a value into a field (e.g. an input field or text area).
	 * 
	 * @param value
	 *            the value to insert
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the {@code value} was insert successful,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean insertIntoField(String value, String elementListKey) throws StopTestException {
		return insertIntoField(value, elementListKey, new String[] {});
	}

	/**
	 * Searches for a given text on the page.
	 * 
	 * @param text
	 *            to be searched for
	 * @return {@code true} if the {@code text} is present on the page,
	 *         {@code false} otherwise
	 */
	public boolean checkTextIsPresentOnPage(final String text) {
		// waitForPage();
		try {
			int interval = (int) Math.floor(Math.sqrt(timeout));
			Wait<WebDriver> wait = new FluentWait<WebDriver>(webDriver).withTimeout(timeout, TimeUnit.SECONDS)
					.pollingEvery(interval, TimeUnit.SECONDS)
					.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
			return wait.until(new ExpectedCondition<Boolean>() {

				@Override
				public Boolean apply(WebDriver driver) {
					String source = webDriver.getPageSource();
					source = source.replaceFirst("(?i:<HEAD[^>]*>[\\s\\S]*</HEAD>)", "");
					return source.contains(text.trim());
				}
			});
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Waits for page is complete loaded. Therefore the "document.readyState" is
	 * checked.
	 * 
	 * @return {@code true} if complete loaded during the timeout, {@code false}
	 *         otherwise
	 */
	public boolean waitForPage() {
		int interval = (int) Math.floor(Math.sqrt(timeout));
		Wait<WebDriver> wait = new FluentWait<WebDriver>(webDriver).withTimeout(timeout, TimeUnit.SECONDS)
				.pollingEvery(interval, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
		try {
			return wait.until(new ExpectedCondition<Boolean>() {

				@Override
				public Boolean apply(WebDriver arg) {
					return ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals(
							"complete");
				}
			});
		} catch (TimeoutException e) {
			throw new ContinueTestException("Page could not be loaded within the timeout.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return true;
		}
	}

	/**
	 * Checks that a text is <b>not</b> present on the page.
	 * 
	 * @param text
	 *            not expected on the page
	 * @return {@code true} if the {@code text} is <b>not</b> present on the
	 *         page, {@code false} otherwise
	 */
	public boolean checkTextIsNotPresentOnPage(String text) {
		return !checkTextIsPresentOnPage(text);
	}

	/**
	 * Searches for a given text on the available web element.
	 * 
	 * <p />
	 * <b>Hint:</b> for input field use
	 * {@link HtmlWebFixture#checkValueOfElement(String, String, String...)}
	 * 
	 * @param text
	 *            to be searched for
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the {@code text} is present on the web element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsPresentOnElement(String text, String elementListKey, String... replaceArgs)
			throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		return element.getText().contains(text.trim());
	}

	/**
	 * Searches for a given text on the available web element.
	 * 
	 * <p />
	 * <b>Hint:</b> for input field use
	 * {@link HtmlWebFixture#checkValueOfElement(String, String)}
	 * 
	 * @param text
	 *            to be searched for
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the {@code text} is present on the web element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsPresentOnElement(String text, String elementListKey) throws StopTestException {
		return checkTextIsPresentOnElement(text, elementListKey, new String[] {});
	}

	/**
	 * Checks that a text is <b>not</b> present on the web element.
	 * 
	 * @param text
	 *            not expected on the web element
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the {@code text} is <b>not</b> present on the web
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsNotPresentOnElement(String text, String elementListKey, String... replaceArgs)
			throws StopTestException {
		return !checkTextIsPresentOnElement(text, elementListKey, replaceArgs);
	}

	/**
	 * Checks that a text is <b>not</b> present on the web element.
	 * 
	 * @param text
	 *            not expected on the web element
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the {@code text} is <b>not</b> present on the web
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsNotPresentOnElement(String text, String elementListKey) throws StopTestException {
		return checkTextIsNotPresentOnElement(text, elementListKey, new String[] {});
	}

	/**
	 * Clears a element (e.g. an input field or text area).
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean clearElement(String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		element.clear();
		return true;
	}

	/**
	 * Clears a element (e.g. an input field or text area).
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return always {@code true} to show inside FitNesse a positive result
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean clearElement(String elementListKey) throws StopTestException {
		return clearElement(elementListKey, new String[] {});
	}

	/**
	 * Returns the value for the given {@code attribute} of the web element. Use
	 * 'innerText' as {@code attribute} to get the complete inner text of the
	 * web element.
	 * 
	 * @param attribute
	 *            the attribute of a web element or 'innertext'
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the value of the {@code attribute} or {@code null} if the
	 *         {@code attribute} does not exists
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readAttributeFromElement(String attribute, String elementListKey, String... replaceArgs)
			throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		if ("innerText".equalsIgnoreCase(attribute)) {
			return element.getText();
		} else {
			return element.getAttribute(attribute);
		}
	}

	/**
	 * Returns the value for the given {@code attribute} of the web element. Use
	 * 'innerText' as {@code attribute} to get the complete inner text of the
	 * web element.
	 * 
	 * @param attribute
	 *            the attribute of a web element or 'innertext'
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return the value of the {@code attribute} or {@code null} if the
	 *         {@code attribute} does not exists
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readAttributeFromElement(String attribute, String elementListKey) throws StopTestException {
		return readAttributeFromElement(attribute, elementListKey, new String[] {});
	}

	/**
	 * Finds and returns all web element in the DOM matching the technical
	 * locator. This does not necessarily mean that the elements are visible.
	 * The configured {@code timeout} is used to specify the time to wait for
	 * the element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the web elements or {@code null} if no matching element is
	 *         present in the DOM
	 * @throws StopTestException
	 *             if a timeout occurred while finding the web elements
	 */
	protected List<WebElement> findWebElements(String elementListKey, String... replaceArgs) throws StopTestException {
		int interval = (int) Math.floor(Math.sqrt(timeout));
		Wait<WebDriver> wait = new FluentWait<WebDriver>(webDriver).withTimeout(timeout, TimeUnit.SECONDS)
				.pollingEvery(interval, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
		try {
			return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(createBy(elementListKey, replaceArgs)));
		} catch (TimeoutException e) {
			throw new StopTestException("There was a timeout while finding the element '"
					+ createBy(elementListKey, replaceArgs) + "'!");
		}
	}

	/**
	 * Finds and returns a web element displayed on the page. Always returns an
	 * element or an exception.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the web element
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	protected WebElement findAvailableWebElement(String elementListKey, String... replaceArgs) throws StopTestException {
		List<WebElement> elements = findWebElements(elementListKey, replaceArgs);

		for (WebElement webElement : elements) {
			if (webElement != null && webElement.isDisplayed()) {
				return webElement;
			}
		}

		throw new StopTestException("The specified Gui-Element for the Key '" + createBy(elementListKey, replaceArgs)
				+ "' could not be found on web page!");
	}

	/**
	 * Finds and returns a list of web element displayed on the page. If non
	 * matching element, an empty list is returned.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return a list of available (present and not hidden) web elements
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	protected List<WebElement> findAllAvailableWebElements(String elementListKey, String... replaceArgs)
			throws StopTestException {
		int interval = (int) Math.floor(Math.sqrt(timeout));
		Wait<WebDriver> wait = new FluentWait<WebDriver>(webDriver).withTimeout(timeout, TimeUnit.SECONDS)
				.pollingEvery(interval, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
		try {
			return wait.until(ExpectedConditions
					.visibilityOfAllElementsLocatedBy(createBy(elementListKey, replaceArgs)));
		} catch (TimeoutException e) {
			throw new StopTestException("There was a timeout while finding the element '"
					+ createBy(elementListKey, replaceArgs) + "'!");
		}
	}

	/**
	 * Returns the locator for a given key.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return locator as String or an ElementKeyNotFoundException if locater
	 *         not found (locater is never null)
	 * 
	 */
	protected String retrieveLocater(String elementListKey) {
		try {
			if (elementListKey == null) {
				throw new ElementKeyNotFoundException("Null is not a valid key!");
			}
			return elementListService.getValue(elementListKey);
		} catch (ElementKeyNotFoundException e) {
			throw new StopTestException("The specified Key '" + elementListKey
					+ "' could not be found in element list!");
		}
	}

	/**
	 * A special keyboard key is pressed.
	 * 
	 * @param specialKey
	 *            the key to press (@see Keys)
	 * @return {@code true} if pressing the key was successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if key is invalid
	 */
	public boolean pressSpecialKey(String specialKey) throws StopTestException {
		if (specialKey == null || specialKey.trim().isEmpty()) {
			throw new StopTestException("Invalid or empty key!");
		}

		try {
			Keys seleniumKey = Keys.valueOf(specialKey.trim().toUpperCase());
			new Actions(webDriver).sendKeys(seleniumKey).build().perform();
			return true;
		} catch (IllegalArgumentException e) {
			throw new StopTestException("The specified key '" + specialKey.trim().toUpperCase()
					+ "' is invalid and could not be found in selenium enum Keys!");
		}
	}

	/**
	 * A special keyboard key is pressed on a displayed web element identified
	 * by the element list key.
	 * 
	 * Therefore it is ensured that the element is available (present in DOM and
	 * visible on web page). If element is not available a
	 * {@code StopTestException} is thrown.
	 * 
	 * @param specialKey
	 *            the key to press (@see Keys)
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if pressing the key was successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if key is invalid, the element is not available or a timeout
	 *             occurred
	 */
	public boolean pressSpecialKeyOnElement(String specialKey, String elementListKey, String... replaceArgs)
			throws StopTestException {
		if (specialKey == null || specialKey.trim().isEmpty()) {
			throw new StopTestException("Invalid or empty key!");
		}

		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		return pressSpecialKeyOnElement(specialKey, element);
	}

	/**
	 * A special keyboard key is pressed on a displayed web element identified
	 * by the element list key.
	 * 
	 * Therefore it is ensured that the element is available (present in DOM and
	 * visible on web page). If element is not available a
	 * {@code StopTestException} is thrown.
	 * 
	 * @param specialKey
	 *            the key to press (@see Keys)
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if pressing the key was successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if key is invalid, the element is not available or a timeout
	 *             occurred
	 */
	public boolean pressSpecialKeyOnElement(String specialKey, String elementListKey) throws StopTestException {
		return pressSpecialKeyOnElement(specialKey, elementListKey, new String[] {});
	}

	/**
	 * A special keyboard key is pressed on a web element.
	 * 
	 * @param specialKey
	 *            the key to press (@see Keys)
	 * @param element
	 *            available web element
	 * @return {@code true} if pressing the key was successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if key is not a selenium {@code Keys}
	 */
	protected boolean pressSpecialKeyOnElement(String specialKey, WebElement element) throws StopTestException {
		try {
			Keys seleniumKey = Keys.valueOf(specialKey.trim().toUpperCase());
			element.sendKeys(seleniumKey);
			return true;
		} catch (IllegalArgumentException e) {
			throw new StopTestException("The specified key '" + specialKey.trim().toUpperCase()
					+ "' is invalid and could not be found in selenium enum Keys!");
		}
	}

	/**
	 * Creates a Selenium identifier for a GUI-element by the given element list
	 * key and optional values for the element list entry place holders.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return a Selenium identifier for a GUI-element
	 */
	// CHECKSTYLE:OFF
	protected By createBy(String elementListKey, String... replaceArgs) {
		// CHECKSTYLE:ON
		String locator = retrieveLocater(elementListKey);

		// replace arguments (e.g. {0}) in locater
		if (replaceArgs.length > 0) {
			Object[] args = Arrays.copyOf(replaceArgs, replaceArgs.length, Object[].class);
			locator = MessageFormat.format(locator, args);
		}

		// Apostrophes in X-Paths must escaped
		// locator = locator.replace("'", "''");

		if (locator.startsWith(ElementPrefix.ID.getName())) {
			locator = locator.substring(ElementPrefix.ID.getName().length());
			return By.id(locator);
		} else if (locator.startsWith(ElementPrefix.XPATH.getName())) {
			locator = locator.substring(ElementPrefix.XPATH.getName().length());
			return By.xpath(locator);
		} else if (locator.startsWith(ElementPrefix.CLASSNAME.getName())) {
			locator = locator.substring(ElementPrefix.CLASSNAME.getName().length());
			return By.className(locator);
		} else if (locator.startsWith(ElementPrefix.CSSSELECTOR.getName())) {
			locator = locator.substring(ElementPrefix.CSSSELECTOR.getName().length());
			return By.cssSelector(locator);
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
		} else if (locator.startsWith("//")) {
			return By.xpath(locator);
		} else {
			return By.id(locator);
		}
	}

	/**
	 * Stops the current test execution.
	 * 
	 * @throws StopTestException
	 *             always thrown to stop the test
	 */
	public void stopTestExecution() throws StopTestException {
		throw new StopTestException("Test execution stopped!");
	}

	@Override
	public boolean tearDown() {
		return closeBrowser();
	}

	@Override
	public String getTestName() {
		return null;
	}

	@Override
	public void postInvoke(Method arg0, Object arg1, Object... arg2) throws InvocationTargetException,
			IllegalAccessException {
	}

	@Override
	public void preInvoke(Method arg0, Object arg1, Object... arg2) throws InvocationTargetException,
			IllegalAccessException {
	}

	@Override
	public void setTestName(String arg0) {
	}

}
