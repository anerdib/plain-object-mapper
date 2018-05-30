package com.github.anerdib;


import com.github.anerdib.api.Converter;
import com.github.anerdib.model.GenericTypeReference;
import com.github.anerdib.model.Getter;
import com.github.anerdib.model.Setter;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

@Log
public class ConverterBuilder<S, D, I> {
	private final Supplier<I> supplier;
	private final Function<I, D> finalizer;
	private final Class<S> sourceClass;
	private final Class<D> destinationClass;
	private final Class<I> intermediaryClass;
	private Map<String, Map.Entry<Getter<S, Object>, Setter<I, Object>>> setters = new HashMap<>();

	public ConverterBuilder(Class<S> sourceClass, Class<D> destinationClass, Class<I> intermediaryClass, Supplier<I> factoryMethod, Function<I, D> finalizer) {
		this.supplier = factoryMethod;
		this.finalizer = finalizer;
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;
		this.intermediaryClass = intermediaryClass;
	}


	/**
	 * @param from
	 */
	private void addFromPair(Map.Entry<Getter<S, Object>, Setter<I, Object>> from) {
		Getter<S, Object> getter = from.getKey();
		String getterName = GenericTypeReference.getLambdaMethodName(getter);
		setters.put(getterName, from);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	/**
	 * Build the configured {@link Converter}
	 */
	public final Converter<S, D> build() {
		ConverterImpl<S, D, I> converterInstance = new ConverterImpl<S, D, I>(sourceClass, destinationClass, intermediaryClass, supplier, finalizer);
		for (Method method : sourceClass.getMethods()) {
			String getterName = method.getName();
			if (!getterName.startsWith("get"))
				continue;
			try {
				String setterName = "set" + method.getName().substring(3);
				Method setter = intermediaryClass.getMethod(setterName, new Class<?>[]{method.getReturnType()});
				if (setter != null) {
					Getter<S, Object> getterLambda = (i) -> {
						try {
							return method.invoke(i, new Object[0]);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							log.log(Level.SEVERE, "getter invocation failed for {} in class {}",
									new Object[]{getterName, sourceClass.getName()});
						}
						return method;
					};
					Setter setterLambda = (i, p) -> {
						try {
							setter.invoke(i, p);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							log.log(Level.SEVERE, "setter invocation failed for {} in class {}",
									new Object[]{setterName, intermediaryClass.getName()});
						}
					};
					setters.putIfAbsent(getterName,
							new AbstractMap.SimpleImmutableEntry<Getter<S, Object>, Setter<I, Object>>(getterLambda,
									setterLambda));
				}
			} catch (NoSuchMethodException nsmex) {

			}
		}
		converterInstance.setSetters(setters.values());
		return converterInstance;
	}

	/**
	 * @param getter
	 * @param <ST>
	 * @return
	 */
	public <ST> From from(Getter<S, Object> getter) {

		return new From(getter);
	}

	/**
	 * Ignore the given property
	 *
	 * @param getter
	 * @return
	 */
	public ConverterBuilder<S, D, I> omit(Getter<S, Object> getter) {
		addFromPair(
				new AbstractMap.SimpleImmutableEntry<Getter<S, Object>, Setter<I, Object>>(getter, Setter.omit()));
		return this;
	}

	public class From {
		private Getter<S, Object> getter;
		private Setter<I, Object> setter;

		private From(Getter<S, Object> getter) {
			this.getter = getter;
		}

		public Getter<S, Object> getGetter() {
			return getter;
		}

		public Setter<I, ? extends Object> getSetter() {
			return setter;
		}

		@SuppressWarnings("unchecked")
		public <T extends Object> ConverterBuilder<S, D, I> to(Setter<I, T> setter) {
			this.setter = (Setter<I, Object>) setter;
			ConverterBuilder.this.addFromPair(
					new AbstractMap.SimpleImmutableEntry<Getter<S, Object>, Setter<I, Object>>(this.getter,
							this.setter));
			return ConverterBuilder.this;
		}
	}
}

