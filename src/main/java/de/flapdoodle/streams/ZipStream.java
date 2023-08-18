/*
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
package de.flapdoodle.streams;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ZipStream {
	
	private ZipStream() {
		// no instance
	}
	
	static class ZipSpliterator<L, R, O> implements Spliterator<O> {

	    static <L, R, O> Spliterator<O> zipping(Spliterator<L> lefts, Spliterator<R> rights, BiFunction<L, R, O> combiner) {
	        return new ZipSpliterator<>(lefts, rights, combiner);
	    }

	    private final Spliterator<L> lefts;
	    private final Spliterator<R> rights;
	    private final BiFunction<L, R, O> combiner;
	    private boolean rightHadNext = false;

	    private ZipSpliterator(Spliterator<L> lefts, Spliterator<R> rights, BiFunction<L, R, O> combiner) {
	        this.lefts = lefts;
	        this.rights = rights;
	        this.combiner = combiner;
	    }

	    @Override
	    public boolean tryAdvance(Consumer<? super O> action) {
	        rightHadNext = false;
	        boolean leftHadNext = lefts.tryAdvance(l ->
	            rights.tryAdvance(r -> {
	                rightHadNext = true;
	                action.accept(combiner.apply(l, r));
	            }));
	        return leftHadNext && rightHadNext;
	    }

	    @Override
	    public Spliterator<O> trySplit() {
	        return null;
	    }

	    @Override
	    public long estimateSize() {
	        return Math.min(lefts.estimateSize(), rights.estimateSize());
	    }

	    @Override
	    public int characteristics() {
	        return lefts.characteristics() & rights.characteristics()
	                & ~(Spliterator.DISTINCT | Spliterator.SORTED);
	    }
	}	
	public static <L,R,Z> Stream<Z> of(Stream<L> left, Stream<R> right, BiFunction<L, R, Z> combiner) {
		return StreamSupport.stream(ZipSpliterator.zipping(left.spliterator(), right.spliterator(), combiner), false);
	}
}