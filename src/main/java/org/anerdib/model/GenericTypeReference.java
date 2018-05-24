package org.anerdib.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is used to bypass the type erasure mechanism of Java
 *
 * @param <S>
 * @author Andrei Bisceanu
 */
public abstract class GenericTypeReference<S> {

	private Type type;

	public GenericTypeReference() {
		ParameterizedType thisClass = (ParameterizedType) getClass().getGenericSuperclass();
		type = thisClass.getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public Class<S> getGenericClass() {
		return (Class<S>) ((ParameterizedType) type).getRawType();
	}

	public static Type[] getGenericTypes(Object instance) {
		if (instance.getClass().getGenericSuperclass() instanceof ParameterizedType)
			return ((ParameterizedType) instance.getClass().getGenericSuperclass()).getActualTypeArguments();
		return new Type[0];
	}
}