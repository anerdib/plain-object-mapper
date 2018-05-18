package org.anerdib.api;

public interface Converter<S,D> {
	D convert(S source);
}
