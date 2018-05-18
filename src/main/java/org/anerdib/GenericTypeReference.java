package org.anerdib;


import java.lang.reflect.ParameterizedType;

/**
 * This class is used to bypass the type erasure mechanism of Java
 *
 * @param <S>
 * @author Andrei Bisceanu
 */
public abstract class GenericTypeReference<S> {

	private ParameterizedType type;

	public GenericTypeReference() {
		ParameterizedType thisClass = (ParameterizedType) getClass().getGenericSuperclass();
		type = (ParameterizedType) thisClass.getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public Class<S> getGenericClass() {
		return (Class<S>) type.getRawType();
	}
}