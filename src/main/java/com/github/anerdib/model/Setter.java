package com.github.anerdib.model;

import java.io.Serializable;

@FunctionalInterface
public interface Setter<T, V> extends Serializable {
	public void apply(T target, V value);

	public static <T, V> Setter<T, V> omit() {
		return (t, v) -> {
		};
	}
}
