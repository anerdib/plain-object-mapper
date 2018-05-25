package com.github.anerdib.model;

import java.io.Serializable;

@FunctionalInterface
public interface Getter<T, V> extends Serializable {
	public V apply(T target);

	public static <T> Getter<T, T> identity() {
		return t -> t;
	}

}
