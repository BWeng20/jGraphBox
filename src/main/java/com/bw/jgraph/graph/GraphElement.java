/*
 *  (c) copyright 2022 Bernd Wengenroth
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 * 
 */
package com.bw.jgraph.graph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class GraphElement
{
	public final Integer id = idGenerator.incrementAndGet();

	private static final AtomicInteger idGenerator = new AtomicInteger(0);

	private final HashMap<Attribute, Object> attributes_ = new HashMap<>();

	public Iterator<Attribute> attributes()
	{
		return attributes_.keySet().iterator();
	}

	public Object getAttribute(Attribute a)
	{
		return attributes_.get(a);
	}

	public void setAttribute(Attribute a, Object value)
	{
		attributes_.put(a, value);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		appendTo(sb);
		return sb.toString();
	}

	/**
	 * Adds textual representation to the StringBuilder.
	 */
	public StringBuilder appendTo(StringBuilder sb)
	{
		sb.append(id).append(":[");
		boolean first = true;
		for ( Map.Entry<Attribute,Object> e : attributes_.entrySet() )
		{
			if ( first) first = false;
			else sb.append(',');
			sb.append( e.getKey().name ).append(':').append(e.getValue());
		}
		sb.append(']');
		return sb;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( this == obj )
			return true;
		else if ( obj instanceof GraphElement ge)
		{
			if ( ge.attributes_.size() == attributes_.size() )
			{
				for (Map.Entry<Attribute, Object> i : attributes_.entrySet() )
				{
					if ( !Objects.equals( ge.getAttribute(i.getKey()), i.getValue() ))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	protected static class FilteredIterator<T> implements Iterator<T> {
		private final Iterator<T> innerIt;
		private final Predicate<T> pred;
		private T next;

		private boolean checkForNext() {
			while (next == null && innerIt.hasNext()) {
				next = innerIt.next();
				if (!pred.test(next)) next = null;
			}
			return next != null;
		}

		public FilteredIterator(Iterator<T> it, Predicate<T> pred) {
			this.innerIt = it;
			this.pred = pred;
		}

		@Override
		public boolean hasNext() {
			return checkForNext();
		}

		@Override
		public T next() {
			if (!checkForNext()) {
				throw new NoSuchElementException();
			}
			T r = next;
			next = null;
			return r;
		}

		@Override
		public void remove() {
			innerIt.remove();
		}
	}

	protected interface Transformer<S, T>
	{
		T transform(S item);
	}

	protected static class TransformedIterator<S, T> implements Iterator<T>
	{

		private final Iterator<S> innerIt;
		private final Transformer<S, T> trans;

		public TransformedIterator(Iterator<S> it, Transformer<S, T> tr)
		{
			innerIt = it;
			trans = tr;
		}

		@Override
		public boolean hasNext()
		{
			return innerIt.hasNext();
		}

		@Override
		public T next()
		{
			return trans.transform(innerIt.next());
		}

		@Override
		public void remove()
		{
			innerIt.remove();
		}
	}
}
