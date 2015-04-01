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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testeditor.fixture.core.exceptions.StopTestException;
import org.testeditor.fixture.core.utils.ExceptionUtils;

/**
 * Provides methods to test Eclipse RAP (Remote Application Platform) web
 * applications. This class overrides some of the basic behavior of WebFixture
 * to improve handling of RAP components, like auto complete text boxes.
 */
@Deprecated
// "Use RapWebFixture instead"
public class EclipseRapFixture extends WebFixture {
	private static final Logger LOGGER = Logger.getLogger(EclipseRapFixture.class);

	private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

	/**
	 * Refines the behavior of the super class, such that the target element is
	 * clicked once more, after the given <code>value</code> has been inserted
	 * into the target element.
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
	@Override
	public boolean insertIntoField(String value, String elementListKey, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			element.click();
			element.sendKeys(value);
			// for a non-wrapping textarea this click caused an exception
			if (!element.getTagName().equalsIgnoreCase("textarea")) {
				waitTime(500);
				element.click();
			}

			String expectedValue;

			if (element.getTagName().equalsIgnoreCase("input")) {
				expectedValue = readAttributeFromField("value", elementListKey, replaceArgs);
			} else {
				// resolve to cheating (i.e. no plausibility checks) for
				// problematic cases e.g. for div elements in date selection
				// widgets
				expectedValue = value;
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
	 * Inserts the given date value into a date field. The technical locator of
	 * the field gets identified by the element list matching the given key. <br />
	 * 
	 * @param value
	 *            value for the input
	 * @param dateFormat
	 *            the used format of the date value
	 * @param elementListKey
	 *            key to find the technical locator (older IE-Browser needs an
	 *            extra /div[2] to locate the outer date div)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true if date was insert successful, otherwise false
	 */
	public boolean insertIntoDateField(String value, String dateFormat, String elementListKey, String... replaceArgs) {

		boolean result = true;

		final DateFormat df = new SimpleDateFormat(dateFormat);
		final Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(df.parse(value));
		} catch (ParseException e) {
			throw new StopTestException("Date format don't match with date value");
		}

		// get outer calendar element
		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			// Find div element that contains day/month/year divs. This is
			// necessary, because RAP renders different HTML for chrome, firefox
			// and ie.
			WebElement parent = element.findElement(By.xpath(".//div[5]/.."));

			// YEAR
			WebElement year = parent.findElement(By.xpath("./div[5]"));
			if (year != null && year.isDisplayed()) {
				year.click();
				year.sendKeys(calendar.get(Calendar.YEAR) + "");
				year.click();
			} else {
				result = false;
			}

			// MONTH
			WebElement month = parent.findElement(By.xpath("./div[1]"));
			if (month != null && month.isDisplayed()) {
				month.click();
				// add +1 because month field is 0-based
				month.sendKeys(calendar.get(Calendar.MONTH) + 1 + "");
				month.click();
			} else {
				result = false;
			}

			// DAY
			WebElement day = parent.findElement(By.xpath("./div[3]"));
			if (day != null && day.isDisplayed()) {
				day.click();
				day.sendKeys(calendar.get(Calendar.DAY_OF_MONTH) + "");
				day.click();
			} else {
				result = false;
			}
		} else {
			result = false;
		}

		return result;
	}

	/**
	 * Works just like
	 * <code>insertIntoDateField(value, dateFormat, elementListKey, replaceArgs)</code>
	 * except the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param value
	 *            value for the input
	 * @param dateFormat
	 *            the used format of the date value
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @return true if date was insert successful, otherwise false
	 */
	public boolean insertIntoDateField(String value, String dateFormat, String elementListKey) {
		return insertIntoDateField(value, dateFormat, elementListKey, new String[] {});
	}

	/**
	 * Works just like
	 * <code>insertIntoDateField(value, elementListKey, replaceArgs)</code>
	 * except the argument <code>replaceArgs</code> is always an empty array and
	 * the used date format is "dd.MM.yyyy".
	 * 
	 * @param value
	 *            value for the input
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @return true if date was insert successful, otherwise false
	 */
	public boolean insertIntoDateField(String value, String elementListKey) {
		return insertIntoDateField(value, DEFAULT_DATE_FORMAT, elementListKey, new String[] {});
	}

	/**
	 * Return the date of the date field.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the date in the application format
	 */
	public String readDateField(String elementListKey, String... replaceArgs) {
		WebElement element = findWebelement(elementListKey, replaceArgs);
		if (element != null && element.isDisplayed() && element.getText() != null) {
			return element.getText().replace("\n", "").replace("\r", "").trim();
		} else {
			throw new StopTestException("Value wasn't read correctly");
		}
	}

	/**
	 * Works just like <code>readDateField(elementListKey, replaceArgs)</code>
	 * except the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @return the date in the application format
	 */
	public String readDateField(String elementListKey) {
		return readDateField(elementListKey, new String[] {});
	}

	/**
	 * (Horrible) Workaround to read RAP a checkbox. This method gets the
	 * element (possibly by its id) and then reads its inner HTML text. In RAP
	 * applications at least two <code>div</code> elements are nested within the
	 * target <code>div</code> (possibly a third, when the checkbox is focused).
	 * One of them sets the background image to display the current state of the
	 * checkbox. The current state of the checkbox is identified by looking up
	 * the image names from the element list.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the value of the target element; A StopTestException is thrown,
	 *         if the element could not be found or is not visible (i.e. hidden
	 *         using CSS, etc.).
	 */
	@Override
	public boolean readCheckbox(String elementListKey, String... replaceArgs) {
		String innerHTML = readAttributeFromField("innerHTML", elementListKey, replaceArgs);

		boolean result = false;
		boolean error = false;

		String uncheckedImg = getLocatorFromElementList("RAP_CHECK_UNCHECKED");
		String uncheckedHoverImg = getLocatorFromElementList("RAP_CHECK_UNCHECKED_HOVER");
		String checkedImg = getLocatorFromElementList("RAP_CHECK_CHECKED");
		String checkedHoverImg = getLocatorFromElementList("RAP_CHECK_CHECKED_HOVER");

		if (innerHTML.contains(uncheckedImg) || innerHTML.contains(uncheckedHoverImg)) {
			result = false;
		} else if (innerHTML.contains(checkedImg) || innerHTML.contains(checkedHoverImg)) {
			result = true;
		} else {
			error = true;
		}

		if (error) {
			String logMessage = "Checkbox '" + elementListKey + " not found, not visible or in an unknown state.";
			LOGGER.error(logMessage);
			throw new StopTestException(logMessage);
		}

		return result;
	}

	/**
	 * Works just like <code>readCheckbox(elementListKey, replaceArgs)</code>
	 * except the argument <code>replaceArgs</code> is always an empty array.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return the value of the target element; A StopTestException is thrown,
	 *         if the element could not be found or is not visible (i.e. hidden
	 *         using CSS, etc.).
	 */
	public boolean readCheckbox(String elementListKey) {
		return readCheckbox(elementListKey, new String[] {});
	}

	/**
	 * Finds a tab by its element list locator and returns its title. If the tab
	 * represents a new or changed document/object, then the title will have an
	 * asterisk (*) appended, which will be removed by this function.
	 * 
	 * FitNesse usage..: |read name of tab;|arg1|[arg2, arg3, ...]| <br />
	 * FitNesse example: |read name of tab;|XPathToFindNewTabInRow{0}Col{1}|[5,
	 * 3]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return the title or name of the target tab
	 */
	public String readNameOfTab(String elementListKey, String... replaceArgs) {
		String result = "";

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			String tabTitle = element.getText();
			if (tabTitle.endsWith("*")) {
				tabTitle = tabTitle.substring(0, tabTitle.length() - 1);
			}
			result = tabTitle;
		}

		return result;
	}

	/**
	 * Finds a validator icon by its element list locator and returns if it's
	 * present.
	 * 
	 * FitNesse usage..: |check for validation image;|arg1|arg2|arg3| <br />
	 * FitNesse example: |check for validation image;|input_digits|9f835836|[]| <br />
	 * <br />
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param imageName
	 *            name of the icon image without extension (e.g. 9f835836)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return true, if the validator icon is present for the element (technical
	 *         locator)
	 */
	public boolean checkForValidationImage(String elementListKey, String imageName, String... replaceArgs) {
		boolean result = false;

		WebElement element = findWebelement(elementListKey, replaceArgs);

		if (element != null && element.isDisplayed()) {
			try {
				element = element.findElement(By.xpath("./following::img[1]"));
			} catch (TimeoutException e) {
				LOGGER.error(elementListKey);
				LOGGER.error(e.getMessage());
				ExceptionUtils.handleNoSuchElementException(elementListKey, e);
			}
		}

		if (element != null && element.isDisplayed()) {
			String outerHTML = element.getAttribute("outerHTML").toLowerCase();

			if (outerHTML.contains(imageName.toLowerCase())) {
				result = true;
			} else {
				String logMessage = "Validator icon with image name='" + imageName + "' not found or not visible.";
				LOGGER.error(logMessage);
				throw new StopTestException(logMessage);
			}
		}
		return result;
	}

	/**
	 * Selects the given value from drop down.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            the input field of the combo box
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return true if value was selectable
	 */
	public boolean selectInDropDown(String value, String elementListKey, String... replaceArgs) {
		boolean result = false;

		/*
		 * Because RAP doesn't render drop downs as select-tags with options, we
		 * can't use the standard web driver Select-class. RAP creates combo
		 * boxes as input field with attached divs. Input field and divs don't
		 * share a common XPATH.
		 */

		WebElement element = findWebelement(elementListKey, replaceArgs);

		element.click();
		element.sendKeys(Keys.PAGE_UP);

		String currentValue = element.getAttribute("value");
		String before = null;

		do {
			if (value.equals(currentValue)) {
				element.click();
				result = true;
			} else {
				before = currentValue;
				element.sendKeys(Keys.DOWN);
				currentValue = element.getAttribute("value");
			}
		} while (!currentValue.equals(before) && !result);

		return result;
	}
}
