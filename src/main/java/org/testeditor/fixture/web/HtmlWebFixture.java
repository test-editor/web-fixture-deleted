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

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testeditor.fixture.core.exceptions.StopTestException;

/**
 * Web driver for plain HTML web pages.
 * 
 * 
 */
public class HtmlWebFixture extends AbstractWebFixture {

	/**
	 * Selects an option from an available drop down element by the visible
	 * text.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if option was selectable, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean selectOption(String value, String elementListKey, String... replaceArgs) throws StopTestException {
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);
		Select selection = new Select(element);

		try {
			selection.selectByVisibleText(value);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * Selects an option from an available drop down element by the visible
	 * text.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if option was selectable, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean selectOption(String value, String elementListKey) throws StopTestException {
		return selectOption(value, elementListKey, new String[] {});
	}

	/**
	 * Returns the value of the given web element. If the element has no value
	 * attribute, the element text is returned.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the {@code value} or {@code text} of the element
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	// CHECKSTYLE:OFF
	public String readValueOfElement(String elementListKey, String... replaceArgs) throws StopTestException {
		// CHECKSTYLE:ON
		WebElement element = findAvailableWebElement(elementListKey, replaceArgs);

		String value = null;

		switch (element.getTagName()) {
		case "input":
			String type = element.getAttribute("type");
			// handle check-box and radio-button different
			if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
				value = getSelectionOfRadioButtonOrCheckBox(elementListKey, replaceArgs);
				break;
			}
		case "option":
		case "li":
		case "button":
		case "param":
		case "progress":
			value = element.getAttribute("value");
			break;
		case "select":
			value = new Select(element).getFirstSelectedOption().getAttribute("value");
			break;
		case "img":
		case "source":
			value = element.getAttribute("src");
			break;
		default:
			// used by <a>, <body>, <div>, <textarea>, ...
			value = element.getText();
			break;
		}
		// CHECKSTYLE:OFF
		return value == null ? "" : value.trim();
		// CHECKSTYLE:ON
	}

	/**
	 * Returns the value of the given web element. If the element has no value
	 * attribute, the element text is returned.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return the {@code value} or {@code text} of the element
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public String readValueOfElement(String elementListKey) throws StopTestException {
		return readValueOfElement(elementListKey, new String[] {});
	}

	/**
	 * Checks if the value attribute (for some element the text) of the web
	 * element is equals to the expected value.
	 * 
	 * @param expectedValue
	 *            the expected value of the element (e.g. the value attribute)
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if the element value (text) is equal to the expected
	 *         value, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValueOfElement(String expectedValue, String elementListKey, String... replaceArgs)
			throws StopTestException {
		String value = readValueOfElement(elementListKey, replaceArgs);
		return expectedValue.trim().equals(value);
	}

	/**
	 * Checks if the value attribute (for some element the text) of the web
	 * element is equals to the expected value.
	 * 
	 * @param expectedValue
	 *            the expected value of the element (e.g. the value attribute)
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if the element value (text) is equal to the expected
	 *         value, {@code false} otherwise
	 * @throws StopTestException
	 *             if element not available (hidden, not present) or a timeout
	 *             occurred
	 */
	public boolean checkValueOfElement(String expectedValue, String elementListKey) throws StopTestException {
		return checkValueOfElement(expectedValue, elementListKey, new String[] {});
	}

	/**
	 * Selects or deselects a radio-button or check-box.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if same element could be clicked, {@code false}
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean clickRadioButtonOrCheckBox(String value, String elementListKey, String... replaceArgs)
			throws StopTestException {
		List<WebElement> elements = findAllAvailableWebElements(elementListKey, replaceArgs);
		for (WebElement webElement : elements) {
			if (value.equalsIgnoreCase(webElement.getAttribute("value"))) {
				webElement.click();
				return true;
			}
		}
		return false;
	}

	/**
	 * Selects or deselects a radio-button or check-box.
	 * 
	 * @param value
	 *            the value to select
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if same element could be clicked, {@code false}
	 *         otherwise
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean clickRadioButtonOrCheckBox(String value, String elementListKey) throws StopTestException {
		return clickRadioButtonOrCheckBox(value, elementListKey, new String[] {});
	}

	/**
	 * Returns the current selection of a radio-button or check-box.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return the current selection or {@code null} if nothing is selected
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	private String getSelectionOfRadioButtonOrCheckBox(String elementListKey, String... replaceArgs)
			throws StopTestException {
		List<WebElement> elements = findAllAvailableWebElements(elementListKey, replaceArgs);
		for (WebElement webElement : elements) {
			if (webElement.isSelected()) {
				return webElement.getAttribute("value");
			}
		}
		return null;
	}

	/**
	 * Checks if a radio-button or check-box is selected.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if selected, {@code false otherwise}
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkIsSelected(String elementListKey, String... replaceArgs) throws StopTestException {
		return getSelectionOfRadioButtonOrCheckBox(elementListKey, replaceArgs) != null;
	}

	/**
	 * Checks if a radio-button or check-box is selected.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if selected, {@code false otherwise}
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkIsSelected(String elementListKey) throws StopTestException {
		return checkIsSelected(elementListKey, new String[] {});
	}

	/**
	 * Checks if a radio-button or check-box is <b>not</b> selected.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @param replaceArgs
	 *            values to replace the place holders in the element list entry
	 * @return {@code true} if <b>not</b> selected, {@code false otherwise}
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkIsNotSelected(String elementListKey, String... replaceArgs) throws StopTestException {
		return getSelectionOfRadioButtonOrCheckBox(elementListKey, replaceArgs) == null;
	}

	/**
	 * Checks if a radio-button or check-box is <b>not</b> selected.
	 * 
	 * @param elementListKey
	 *            key in the element list to find the technical locator
	 * @return {@code true} if <b>not</b> selected, {@code false otherwise}
	 * @throws StopTestException
	 *             if a timeout occurred
	 */
	public boolean checkIsNotSelected(String elementListKey) throws StopTestException {
		return checkIsNotSelected(elementListKey, new String[] {});
	}
}
