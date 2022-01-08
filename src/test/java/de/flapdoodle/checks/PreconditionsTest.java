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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
	
	@Test
	public void argumentFalseWillFailWithIllegalArgument() {
		assertThatThrownBy(() -> Preconditions.checkArgument(false, "foo"))
			.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	public void argumentTrueWillNotFail() {
		Preconditions.checkArgument(true, "foo");
	}
	
	@Test
	public void checkNullWillFailWithIllegalArgument() {
		assertThatThrownBy(() -> Preconditions.checkNotNull(null, "foo"))
			.isInstanceOf(NullPointerException.class);
	}
	
	@Test
	public void checkPresentWillFailWithIllegalArgument() {
		assertThatThrownBy(() -> Preconditions.checkPresent(Optional.empty(), "foo"))
			.isInstanceOf(IllegalArgumentException.class);
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
