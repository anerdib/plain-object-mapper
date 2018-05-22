package org.anerdib.api;

import java.util.Collection;

public interface Converter<S, D> {

	/**
	 * Convert the given source parameter to a destination type given by the generic
	 * type <code>D</code>
	 * 
	 * @param source
	 * @return
	 */
	D convert(S source);

	/**
	 * Convert the given source Collection of generic type <code>S</code> to a
	 * Collection of generic type <code>D</code>
	 * 
	 * @param source
	 * @return
	 */
	<C extends Collection<D>> C convert(Collection<S> source);

	/**
	 * Convert a given array of the generic type <code>S</code> to an array of
	 * generic type <code>D</code>
	 * 
	 * @return
	 */
	D[] convert(S[] source);
}
