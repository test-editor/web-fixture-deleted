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

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testeditor.fixture.core.exceptions.ContinueTestException;
import org.testeditor.fixture.core.exceptions.StopTestException;

/**
 * Web driver for Eclipse RAP web pages.
 * 
 */
public class RapWebFixture extends AbstractWebFixture {

	private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

	/**
	 * Selects an option from an available drop down element by the visible
	 * text.
	 * 
	 * <p/>
	 * <b>Hint:</b> Doesn't work for drop down lists with duplicate entries.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the {@code value} was selectable, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean selectOption(String value, String elementListKey, String... replaceArgs) throws StopTestException {
		/*
		 * Because RAP doesn't render drop downs as select-tags with options, we
		 * can't use the standard web driver Select-class. RAP creates combo
		 * boxes as input field with attached divs. Input field and divs don't
		 * share a common XPATH.
		 */

		boolean result = false;
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

		element.click();
		// start on top of the list
		element.sendKeys(Keys.PAGE_UP);

		String currentValue = element.getAttribute("value");
		String before = null;

		do {
			if (value.equals(currentValue)) {
				element.click();
				result = true;
			} else {
				before = currentValue;
				// go on to the next element in the list
				element.sendKeys(Keys.DOWN);
				currentValue = element.getAttribute("value");
			}
			// stop execution, if last and current analyzed value are the same
		} while (!currentValue.equals(before) && !result);

		return result;
	}

	/**
	 * Selects an option from an available drop down element by the visible
	 * text.
	 * 
	 * <p/>
	 * <b>Hint:</b> Doesn't work for drop down lists with duplicate entries.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the {@code value} was selectable, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean selectOption(String value, String elementListKey) throws StopTestException {
		return selectOption(value, elementListKey, new String[] {});
	}

	/**
	 * Finds a tab by its element list locator and returns its title. If the tab
	 * represents a new or changed document/object, then the title will have an
	 * asterisk (*) appended, which will be removed by this function.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the title or name of the tab
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readTabName(String elementListKey, String... replaceArgs) throws StopTestException {

		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

		String tabTitle = element.getText();
		if (tabTitle.endsWith("*")) {
			tabTitle = tabTitle.substring(0, tabTitle.length() - 1);
		}
		return tabTitle;
	}

	/**
	 * Finds a tab by its element list locator and returns its title. If the tab
	 * represents a new or changed document/object, then the title will have an
	 * asterisk (*) appended, which will be removed by this function.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return the title or name of the tab
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readTabName(String elementListKey) throws StopTestException {
		return readTabName(elementListKey, new String[] {});
	}

	/**
	 * Checks text is <b>not</b> present on a tab. The tab is identified by its
	 * element list locator.
	 * 
	 * @param text
	 *            the text not expected on the tab
	 * @param elementListKey
	 *            in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if {@code name} is unequal the text on the tab,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsNotPresentOnTab(String text, String elementListKey, String... replaceArgs)
			throws StopTestException {
		return !text.equals(readTabName(elementListKey, replaceArgs));
	}

	/**
	 * Checks text is <b>not</b> present on a tab. The tab is identified by its
	 * element list locator.
	 * 
	 * @param text
	 *            the text not expected on the tab
	 * @param elementListKey
	 *            in the element list to find the technical locator
	 * @return {@code true} if {@code name} is unequal the text on the tab,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsNotPresentOnTab(String text, String elementListKey) throws StopTestException {
		return checkTextIsNotPresentOnTab(text, elementListKey, new String[] {});
	}

	/**
	 * Checks text is present on a tab. The tab is identified by its element
	 * list locator.
	 * 
	 * @param text
	 *            the text not expected on the tab
	 * @param elementListKey
	 *            in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if {@code name} is equal the text on the tab,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsPresentOnTab(String text, String elementListKey, String... replaceArgs)
			throws StopTestException {
		return text.equals(readTabName(elementListKey, replaceArgs));
	}

	/**
	 * Checks text is present on a tab. The tab is identified by its element
	 * list locator.
	 * 
	 * @param text
	 *            the text not expected on the tab
	 * @param elementListKey
	 *            in the element list to find the technical locator
	 * @return {@code true} if {@code name} is equal the text on the tab,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkTextIsPresentOnTab(String text, String elementListKey) throws StopTestException {
		return checkTextIsPresentOnTab(text, elementListKey, new String[] {});
	}

	/**
	 * Use
	 * {@link #insertIntoDateField(String, String, String, String, String...)}
	 * instead.
	 * <p/>
	 * 
	 * Inserts the given date value into a date field. The technical locator of
	 * the field gets identified by the element list matching the given key.
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
	 * @return {@code true} if date was insert successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if date format don't match the date value, element is not
	 *             available (hidden, not present) or a timeout occurred
	 */
	@Deprecated
	public boolean deprecatedInsertIntoDateField(String value, String dateFormat, String elementListKey,
			String... replaceArgs) throws StopTestException {

		boolean result = true;

		final DateFormat df = new SimpleDateFormat(dateFormat);
		final Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(df.parse(value));
		} catch (ParseException e) {
			throw new StopTestException("Date format don't match with date value");
		}

		// get outer calendar element
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

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

		return result;
	}

	/**
	 * Use {@link #insertIntoDateField(String, String, String, String)} instead.
	 * <p/>
	 * 
	 * Inserts the given date value into a date field. The technical locator of
	 * the field gets identified by the element list matching the given key.
	 * 
	 * @param value
	 *            value for the input
	 * @param dateFormat
	 *            the used format of the date value
	 * @param elementListKey
	 *            key to find the technical locator (older IE-Browser needs an
	 *            extra /div[2] to locate the outer date div)
	 * @return {@code true} if date was insert successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if date format don't match the date value, element is not
	 *             available (hidden, not present) or a timeout occurred
	 */
	@Deprecated
	public boolean deprecatedInsertIntoDateField(String value, String dateFormat, String elementListKey)
			throws StopTestException {
		return deprecatedInsertIntoDateField(value, dateFormat, elementListKey, new String[] {});
	}

	/**
	 * Use {@link #insertIntoDateField(String, String, String, String )}
	 * instead.
	 * <p/>
	 * 
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
	 * @return {@code true} if date was insert successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if date format don't match "dd.MM.yyyy", element is not
	 *             available (hidden, not present) or a timeout occurred
	 */
	@Deprecated
	public boolean deprecatedInsertIntoDateField(String value, String elementListKey) throws StopTestException {
		return deprecatedInsertIntoDateField(value, DEFAULT_DATE_FORMAT, elementListKey, new String[] {});
	}

	/**
	 * Inserts the given date value into a date field. The technical locator of
	 * the field gets identified by the element list matching the given key.
	 * 
	 * @param dayValue
	 *            the day of the date (1-31)
	 * @param monthValue
	 *            the month of the date (1-12)
	 * @param yearValue
	 *            the year of the date
	 * @param elementListKey
	 *            key to find the technical locator (older IE-Browser needs an
	 *            extra /div[2] to locate the outer date div)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if date was insert successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean insertIntoDateField(String dayValue, String monthValue, String yearValue, String elementListKey,
			String... replaceArgs) throws StopTestException {

		// get outer calendar element
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

		// Find div element that contains day/month/year divs. This is
		// necessary, because RAP renders different HTML for chrome, firefox
		// and ie.
		WebElement parent = element.findElement(By.xpath(".//div[5]/.."));

		boolean result = true;

		// YEAR
		WebElement year = parent.findElement(By.xpath("./div[5]"));
		result &= sendDateValue(yearValue, year);

		// MONTH
		WebElement month = parent.findElement(By.xpath("./div[1]"));
		result &= sendDateValue(monthValue, month);

		// DAY
		WebElement day = parent.findElement(By.xpath("./div[3]"));
		result &= sendDateValue(dayValue, day);

		return result;
	}

	/**
	 * Inserts the given date value into a date field. The technical locator of
	 * the field gets identified by the element list matching the given key.
	 * 
	 * @param dayValue
	 *            the day of the date (1-31)
	 * @param monthValue
	 *            the month of the date (1-12)
	 * @param yearValue
	 *            the year of the date
	 * @param elementListKey
	 *            key to find the technical locator (older IE-Browser needs an
	 *            extra /div[2] to locate the outer date div)
	 * @return {@code true} if date was insert successful, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean insertIntoDateField(String dayValue, String monthValue, String yearValue, String elementListKey)
			throws StopTestException {
		return insertIntoDateField(dayValue, monthValue, yearValue, elementListKey, new String[] {});
	}

	/**
	 * Inserts a value into the date div element.
	 * 
	 * @param value
	 *            the value to insert
	 * @param element
	 *            the date element (day, month or year field)
	 * @return {@code true} if {@code value} was insert successful,
	 *         {@code false} otherwise
	 */
	private boolean sendDateValue(String value, WebElement element) {
		if (element != null && element.isDisplayed()) {
			element.click();
			element.sendKeys(value);
			element.click();
			return true;
		} else {
			return false;
		}
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
	 * @return the date in the german format (dd.MM.yyyy)
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readDateField(String elementListKey, String... replaceArgs) throws StopTestException {
		// get outer calendar element
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

		// Find div element that contains day/month/year divs. This is
		// necessary, because RAP renders different HTML for chrome, firefox
		// and ie.
		WebElement parent = element.findElement(By.xpath(".//div[5]/.."));

		// YEAR
		WebElement year = parent.findElement(By.xpath("./div[5]"));

		// MONTH
		WebElement month = parent.findElement(By.xpath("./div[1]"));

		// DAY
		WebElement day = parent.findElement(By.xpath("./div[3]"));

		return day.getText() + "." + month.getText() + "." + year.getText();
	}

	/**
	 * Return the date of the date field.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @return the date in the german format (dd.MM.yyyy)
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readDateField(String elementListKey) throws StopTestException {
		return readDateField(elementListKey, new String[] {});
	}

	/**
	 * Checks the value of a date field is present.
	 * 
	 * @param text
	 *            the expected value of the date field
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if date value is like expected, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             values to replace the place holders in the element list entry
	 *             with
	 */
	public boolean checkTextIsPresentOnDateField(String text, String elementListKey, String... replaceArgs)
			throws StopTestException {
		return text.equals(readDateField(elementListKey, replaceArgs));
	}

	/**
	 * Checks the value of a date field is present.
	 * 
	 * @param text
	 *            the expected value of the date field
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @return {@code true} if date value is like expected, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             values to replace the place holders in the element list entry
	 *             with
	 */
	public boolean checkTextIsPresentOnDateField(String text, String elementListKey) throws StopTestException {
		return checkTextIsPresentOnDateField(text, elementListKey, new String[] {});
	}

	/**
	 * Checks the value of a date field is <b>not</b> present.
	 * 
	 * @param text
	 *            the <b>not</b> expected value of the date field
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if text is <b>not</b> present on the date field,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             values to replace the place holders in the element list entry
	 *             with
	 */
	public boolean checkTextIsNotPresentOnDateField(String text, String elementListKey, String... replaceArgs)
			throws StopTestException {
		return !text.equals(readDateField(elementListKey, replaceArgs));
	}

	/**
	 * Checks the value of a date field is <b>not</b> present.
	 * 
	 * @param text
	 *            the <b>not</b> expected value of the date field
	 * @param elementListKey
	 *            key to find the technical locator (the IE needs an extra
	 *            /div[2] to locate the outer date div)
	 * @return {@code true} if text is <b>not</b> present on the date field,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             values to replace the place holders in the element list entry
	 *             with
	 */
	public boolean checkTextIsNotPresentOnDateField(String text, String elementListKey) throws StopTestException {
		return checkTextIsNotPresentOnDateField(text, elementListKey, new String[] {});
	}

	/**
	 * Searches for a validator icon by its element key locator and returns if
	 * it's present.
	 * 
	 * @param imageName
	 *            name of the icon image without extension (e.g. 9f835836)
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true}, if the validator icon is present for the element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationImageIsPresent(String imageName, String elementListKey, String... replaceArgs)
			throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

		WebElement image = element.findElement(By.xpath("./following::img[1]"));
		return image != null && image.isDisplayed() && image.getAttribute("outerHTML").contains(imageName);
	}

	/**
	 * Searches for a validator icon by its element locator and returns if it's
	 * present.
	 * 
	 * @param imageName
	 *            name of the icon image without extension (e.g. 9f835836)
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true}, if the validator icon is present for the element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationImageIsPresent(String imageName, String elementListKey) throws StopTestException {
		return checkValidationImageIsPresent(imageName, elementListKey, new String[] {});
	}

	/**
	 * Searches for a validator icon by its element locator and returns if it's
	 * <b>not</b> present.
	 * 
	 * @param imageName
	 *            name of the icon image without extension (e.g. 9f835836)
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true}, if the validator icon is <b>not</b> present for the
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationImageIsNotPresent(String imageName, String elementListKey, String... replaceArgs)
			throws StopTestException {
		return !checkValidationImageIsPresent(imageName, elementListKey, replaceArgs);
	}

	/**
	 * Searches for a validator icon by its element locator and returns if it's
	 * <b>not</b> present.
	 * 
	 * @param imageName
	 *            name of the icon image without extension (e.g. 9f835836)
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true}, if the validator icon is <b>not</b> present for the
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationImageIsNotPresent(String imageName, String elementListKey) throws StopTestException {
		return !checkValidationImageIsPresent(imageName, elementListKey, new String[] {});
	}

	/**
	 * Searches for the mandatory icon on a given element by its element locator
	 * and returns if it's present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_MANDATORY).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true}, if the mandatory icon is present for the element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationMandatoryIsPresent(String elementListKey, String... replaceArgs)
			throws StopTestException {
		String imageName = readConfigurationFromElementList("RAP_VALIDATION_MANDATORY");
		return checkValidationImageIsPresent(imageName, elementListKey, replaceArgs);
	}

	/**
	 * Searches for the mandatory icon on a given element by its element locator
	 * and returns if it's present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_MANDATORY).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true}, if the mandatory icon is present for the element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationMandatoryIsPresent(String elementListKey) throws StopTestException {
		return checkValidationMandatoryIsPresent(elementListKey, new String[] {});
	}

	/**
	 * Searches for the mandatory icon on a given element by its element locator
	 * and returns if it's <b>not</b> present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_MANDATORY).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true}, if the mandatory icon is <b>not</b> present for the
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationMandatoryIsNotPresent(String elementListKey, String... replaceArgs)
			throws StopTestException {
		return !checkValidationMandatoryIsPresent(elementListKey, replaceArgs);
	}

	/**
	 * Searches for the mandatory icon on a given element by its element locator
	 * and returns if it's <b>not</b> present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_MANDATORY).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true}, if the mandatory icon is <b>not</b> present for the
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationMandatoryIsNotPresent(String elementListKey) throws StopTestException {
		return checkValidationMandatoryIsNotPresent(elementListKey, new String[] {});
	}

	/**
	 * Searches for the failed validation icon on a given element by its element
	 * locator and returns if it's present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_FAILED).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true}, if the mandatory icon is present for the element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationFailedIsPresent(String elementListKey, String... replaceArgs)
			throws StopTestException {
		String imageName = readConfigurationFromElementList("RAP_VALIDATION_FAILED");
		return checkValidationImageIsPresent(imageName, elementListKey, replaceArgs);
	}

	/**
	 * Searches for the failed validation icon on a given element by its element
	 * locator and returns if it's present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_FAILED).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true}, if the mandatory icon is present for the element,
	 *         {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationFailedIsPresent(String elementListKey) throws StopTestException {
		return checkValidationFailedIsPresent(elementListKey, new String[] {});
	}

	/**
	 * Searches for the failed validation icon on a given element by its element
	 * locator and returns if it's <b>not</b> present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_FAILED).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true}, if the mandatory icon is <b>not</b> present for the
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationFailedIsNotPresent(String elementListKey, String... replaceArgs)
			throws StopTestException {
		return !checkValidationFailedIsPresent(elementListKey, replaceArgs);
	}

	/**
	 * Searches for the failed validation icon on a given element by its element
	 * locator and returns if it's <b>not</b> present.
	 * 
	 * The icon is configured in the element list (RAP_VALIDATION_FAILED).
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true}, if the mandatory icon is <b>not</b> present for the
	 *         element, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValidationFailedIsNotPresent(String elementListKey) throws StopTestException {
		return checkValidationFailedIsNotPresent(elementListKey, new String[] {});
	}

	/**
	 * Checks if a check-box or radio-button is selected.
	 * 
	 * <p />
	 * (Horrible) Workaround to read RAP a check-box or radio-button. This
	 * method gets the element (possibly by its id) and then reads its inner
	 * HTML text. In RAP applications at least two {@code div} elements are
	 * nested within the target {@code div} (possibly a third, when the
	 * check-box/radio-button is focused). One of them sets the background image
	 * to display the current state of the check-box/radio-button. The current
	 * state of the check-box/radio-button is identified by looking up the image
	 * names from the element list.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if check-box/radio-button is selected, {@code false}
	 *         otherwise
	 * @throws ContinueTestException
	 *             if a check-box or radio-button image is wrong configured in
	 *             the element list
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkIsSelected(String elementListKey, String... replaceArgs) throws StopTestException,
			ContinueTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		String innerHTML = element.getAttribute("innerHTML");

		// Check-Boxes
		String uncheckedCheckBoxImg = readConfigurationFromElementList("RAP_CHECKBOX_UNCHECKED");
		String uncheckedCheckBoxHoverImg = readConfigurationFromElementList("RAP_CHECKBOX_UNCHECKED_HOVER");
		String checkedCheckBoxImg = readConfigurationFromElementList("RAP_CHECKBOX_CHECKED");
		String checkedCheckBoxHoverImg = readConfigurationFromElementList("RAP_CHECKBOX_CHECKED_HOVER");

		// Radio-Buttons
		String uncheckedRadioButtonImg = readConfigurationFromElementList("RAP_RADIOBUTTON_UNCHECKED");
		String uncheckedRadioButtonHoverImg = readConfigurationFromElementList("RAP_RADIOBUTTON_UNCHECKED_HOVER");
		String checkedRadioButtonImg = readConfigurationFromElementList("RAP_RADIOBUTTON_CHECKED");
		String checkedRadioButtonHoverImg = readConfigurationFromElementList("RAP_RADIOBUTTON_CHECKED_HOVER");

		if (innerHTML.contains(uncheckedCheckBoxImg) || innerHTML.contains(uncheckedCheckBoxHoverImg)
				|| innerHTML.contains(uncheckedRadioButtonImg) || innerHTML.contains(uncheckedRadioButtonHoverImg)) {
			return false;
		} else if (innerHTML.contains(checkedCheckBoxImg) || innerHTML.contains(checkedCheckBoxHoverImg)
				|| innerHTML.contains(checkedRadioButtonImg) || innerHTML.contains(checkedRadioButtonHoverImg)) {
			return true;
		} else {
			throw new ContinueTestException(
					"Please check the configuration for Check-Boxes and Radio-Buttons in your element list.");
		}
	}

	/**
	 * Reads a configuration from the element list.
	 * 
	 * @param configurationKey
	 *            the key for the configured value (also called locater)
	 * @return the configured value or an exception if configuration is missing
	 * @throws ContinueTestException
	 *             if the configuration key is missing or not set in the element
	 *             list
	 */
	private String readConfigurationFromElementList(String configurationKey) throws ContinueTestException {
		String config = retrieveLocater(configurationKey).trim();
		if (config.isEmpty()) {
			throw new ContinueTestException("Please configure " + configurationKey + " in your element list.");
		} else {
			return config;
		}
	}

	/**
	 * Checks if a check-box or radio-button is selected.
	 * 
	 * <p />
	 * For more details see {@link #checkIsSelected(String, String...) }.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true} if check-box/radio-button is selected, {@code false}
	 *         otherwise
	 * @throws ContinueTestException
	 *             if a check-box or radio-button image is wrong configured in
	 *             the element list
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkIsSelected(String elementListKey) throws StopTestException, ContinueTestException {
		return checkIsSelected(elementListKey, new String[] {});
	}

	/**
	 * Checks if a check-box or radio-button is <b>not</b> selected.
	 * 
	 * <p />
	 * For more details see {@link #checkIsSelected(String, String...) }.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @return {@code true} if check-box/radio-button is <b>not</b> selected,
	 *         {@code false} otherwise
	 * @throws ContinueTestException
	 *             if a check-box or radio-button image is wrong configured in
	 *             the element list
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkIsNotSelected(String elementListKey) throws StopTestException, ContinueTestException {
		return !checkIsSelected(elementListKey);
	}

	/**
	 * Checks if a check-box or radio-button is <b>not</b> selected.
	 * 
	 * <p />
	 * For more details see {@link #checkIsSelected(String, String...) }.
	 * 
	 * @param elementListKey
	 *            key to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 *            with
	 * @return {@code true} if check-box/radio-button is <b>not</b> selected,
	 *         {@code false} otherwise
	 * @throws ContinueTestException
	 *             if a check-box or radio-button image is wrong configured in
	 *             the element list
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkIsNotSelected(String elementListKey, String... replaceArgs) throws StopTestException,
			ContinueTestException {
		return !checkIsSelected(elementListKey, replaceArgs);
	}

	/**
	 * Returns the value of the given web element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the {@code value} of the element
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readValueOfElement(String elementListKey, String... replaceArgs) throws StopTestException {
		return readAttributeFromElement("value", elementListKey, replaceArgs);
	}

	/**
	 * Returns the value of the given web element.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return the {@code value} of the element
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readValueOfElement(String elementListKey) throws StopTestException {
		return readAttributeFromElement("value", elementListKey, new String[] {});
	}
}
