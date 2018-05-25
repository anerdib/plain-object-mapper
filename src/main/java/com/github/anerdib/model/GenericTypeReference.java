package com.github.anerdib.model;

import jdk.nashorn.internal.objects.NativeDebug;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

	public static String getLambdaMethodName(Serializable lambda) {
		SerializedLambda serializedLambda = getSerializedLambda(lambda);
		return serializedLambda.getImplMethodName();
	}

	private static SerializedLambda getSerializedLambda(Serializable lambda) {
		SerializedLambda serializedLambda = null;
		try {
			Method write = lambda.getClass().getDeclaredMethod("writeReplace");
			write.setAccessible(true);
			serializedLambda = (SerializedLambda) write.invoke(lambda);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				         | InvocationTargetException e) {
			new IllegalArgumentException("Error in handling lambda");
		}
		return serializedLambda;
	}


	public static Class<?> getLambdaReturnType(Serializable lambda) {
		SerializedLambda serializedLambda = getSerializedLambda(lambda);
		String signature = serializedLambda.getInstantiatedMethodType();
		int startIndex = signature.indexOf(')')+1;
		String className = signature.substring(startIndex).replace('/', '.');
		if(!className.startsWith("[")){
			className = className.substring(1,className.length()-1);
		}
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class " + className + "could not be found");
		}
	}

	public static Type[] getGenericTypes(Object instance) {
		if (instance.getClass().getGenericSuperclass() instanceof ParameterizedType)
			return ((ParameterizedType) instance.getClass().getGenericSuperclass()).getActualTypeArguments();
		return new Type[0];
	}
}