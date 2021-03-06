/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.util;

import com.google.common.base.CharMatcher;

import java.util.function.Predicate;

/**
 * Set of common predicates
 *
 * @author Andrei Varabyeu
 */
public class Predicates {

	private static final String SPECIAL_CHARACTERS = "-/@#$%^&_+=()";

	private Predicates() {
		//statics only
	}

	/**
	 * Checker whether string contains special characters only
	 */
	public static final Predicate<String> SPECIAL_CHARS_ONLY = str -> CharMatcher.anyOf(SPECIAL_CHARACTERS).matchesAllOf(str);
}
