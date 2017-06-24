/**
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.checks;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class PreconditionsTest {

	@Test
	public void emptyArgsMustGiveMessage() {
		assertEquals("foo", Preconditions.format("foo"));
	}

	@Test
	public void emptyArgsWithPlaceholderMustGiveMessage() {
		assertEquals("foo %s", Preconditions.format("foo %s"));
	}
	
	@Test
	public void oneArgMustGiveMessageWithArg() {
		assertEquals("foo bar", Preconditions.format("foo %s", "bar"));
	}

	@Test
	public void oneArgWithoutPlaceholderMustGiveMessage() {
		assertEquals("foo,bar", Preconditions.format("foo", "bar"));
	}
	
	@Test
	public void oneMoreArgThanPlaceholderMustGiveArgAppendedToTheEnd() {
		assertEquals("foo bar,blub", Preconditions.format("foo %s", "bar", "blub"));
	}

	@Test
	public void oneMorePlaceholderThanArgMustGiveEmpty() {
		assertEquals("foo bar blub <arg2>", Preconditions.format("foo %s %s %s", "bar", "blub"));
	}
	
	@Test
	public void lazyArgumentWillBeEvaluatedLazy() {
		assertEquals("normal: A, lazy: B", Preconditions.format("normal: %s, lazy: %s", "A", Preconditions.lazy(() -> "B")));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void argumentFalseWillFailWithIllegalArgument() {
		Preconditions.checkArgument(false, "foo");
	}
	
	@Test
	public void argumentTrueWillNotFail() {
		Preconditions.checkArgument(true, "foo");
	}
	
	@Test(expected=NullPointerException.class)
	public void checkNullWillFailWithIllegalArgument() {
		Preconditions.checkNotNull(null, "foo");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkPresentWillFailWithIllegalArgument() {
		Preconditions.checkPresent(Optional.empty(), "foo");
	}
	
	@Test
	public void checkNullWillSuccessWithValue() {
		assertEquals("bar", Preconditions.checkNotNull("bar", "foo"));
	}
	
	@Test
	public void checkPresentWillSuccessWithPresentValue() {
		assertEquals("bar", Preconditions.checkPresent(Optional.of("bar"), "foo").get());
	}
	
	@Test
	public void checkNonNullWillNotFail() {
		assertEquals("blub", Preconditions.checkNotNull("blub", "foo"));
	}
}
