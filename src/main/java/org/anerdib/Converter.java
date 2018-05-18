package org.anerdib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public class Converter<S, D> {

	public class Builder<S, D> extends Converter<S, D> {
		public Builder(Converter<S, D> sdConverter) {
			super(sdConverter.firstClass, sdConverter.secondClass);
		}
	}

	private Supplier<S> firstConstructor;
	private Supplier<D> secondConstructor;
	private final Class<S> firstClass;
	private final Class<D> secondClass;

	private Converter<D, S> reverseConverter = new Converter(this);


	public Converter(Class<S> first, Class<D> second) {
		this.firstClass = first;
		this.secondClass = second;
		this.firstConstructor = extractConstructor(first);
		this.secondConstructor = extractConstructor(second);
	}


	/*public Converter(Supplier<S> first, Supplier<D> second) {
		this.firstConstructor = first;
		this.secondConstructor = second;

		firstClass = first.getClass().getMethods()[0].getGenericReturnType();
		secondClass = second.getClass().getMethods()[0].getGenericReturnType();
	}*/
	private Converter(Converter<D, S> dtConverter) {
		this.reverseConverter = dtConverter;
		this.firstClass = dtConverter.secondClass;
		this.secondClass = dtConverter.firstClass;
	}


	public Converter<S, D> withBuilder(Supplier<D> secondConstructor) {
		this.secondConstructor = secondConstructor;
		return new Builder<S, D>(this);
	}

	public Converter<S, D> withReverseConverter(Converter<D, S> dtConverter) {
		this.reverseConverter = dtConverter;
		return this;
	}

	public <T> Converter<S, D> ignoreInSource(Supplier<T> getter) {
		return this;
	}

	public D convert(S source) {
		D instance = secondConstructor.get();


		return instance;
	}

	public S convertReverse(D source) {
		return reverseConverter.convert(source);
	}


	private <T> Supplier<T> extractConstructor(Class<T> first) {
		try {
			Constructor<T> constructor = first.getConstructor(null);
			return () -> {
				try {
					return constructor.newInstance();
				} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
					//TODO Add logging
					e.printStackTrace();
					return null;
				}
			};
		} catch (NoSuchMethodException e) {
			//TODO Add logging
			e.printStackTrace();
		}
		return null;
	}
}