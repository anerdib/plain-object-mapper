package org.anerdib;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.anerdib.api.Converter;
import org.anerdib.model.Getter;
import org.anerdib.model.Setter;

import lombok.extern.java.Log;

/**
 * @param <S>
 * @param <D>
 * @author anerdib
 */
@Log
public class ConverterBuilder<S, D> {
	public class ConverterBuilderStep2<S, D, I> {
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
			public <T extends Object> ConverterBuilderStep2<S, D, I> to(Setter<I, T> setter) {
				this.setter = (Setter<I, Object>) setter;
				ConverterBuilderStep2.this.addFromPair(
						new AbstractMap.SimpleImmutableEntry<Getter<S, Object>, Setter<I, Object>>(this.getter,
								this.setter));
				return ConverterBuilderStep2.this;
			}
		}

		private final Supplier<I> supplier;
		private final Function<I, D> finalizer;

		private Map<String, Map.Entry<Getter<S, Object>, Setter<I, Object>>> setters = new HashMap<>();

		private ConverterBuilderStep2(Supplier<I> factoryMethod, Function<I, D> finalizer) {
			this.supplier = factoryMethod;
			this.finalizer = finalizer;
		}

		public void addFromPair(Map.Entry<Getter<S, Object>, Setter<I, Object>> from) {
			SerializedLambda lambda = null;
			try {
				Getter<S, Object> getter = from.getKey();
				Method write = getter.getClass().getDeclaredMethod("writeReplace");
				write.setAccessible(true);
				lambda = (SerializedLambda) write.invoke(getter);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				log.severe("Error in handling getter");
			}

			if (lambda != null) {
				String getterName = lambda.getImplMethodName();
				setters.put(getterName, from);
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public final Converter<S, D> build() {
			ConverterImpl<S, D, I> converterInstance = new ConverterImpl<S, D, I>(supplier, finalizer);
			for (Method method : firstClass.getMethods()) {
				String getterName = method.getName();
				if (!getterName.startsWith("get"))
					continue;
				try {
					String setterName = "set" + method.getName().substring(3);
					Method setter = secondClass.getMethod(setterName, new Class<?>[] { method.getReturnType() });
					if (setter != null) {
						Getter<S, Object> getterLambda = (i) -> {
							try {
								return method.invoke(i, new Object[0]);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								log.log(Level.SEVERE, "getter invocation failed for {} in class {}",
										new Object[] { getterName, firstClass.getName() });
							}
							return method;
						};
						Setter setterLambda = (i, p) -> {
							try {
								setter.invoke(i, p);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								log.log(Level.SEVERE, "setter invocation failed for {} in class {}",
										new Object[] { setterName, secondClass.getName() });
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

		public <ST> From from(Getter<S, Object> getter) {

			return new From(getter);
		}

		public ConverterBuilderStep2<S, D, I> omit(Getter<S, Object> getter) {
			addFromPair(
					new AbstractMap.SimpleImmutableEntry<Getter<S, Object>, Setter<I, Object>>(getter, Setter.omit()));
			return this;
		}
	}

	private final Class<S> firstClass;

	private final Class<D> secondClass;

	public ConverterBuilder(Class<S> first, Class<D> second) {
		this.firstClass = first;
		this.secondClass = second;
	}

	private <T> Supplier<T> extractConstructor(Class<T> first) {
		try {
			Constructor<T> constructor = first.getConstructor();
			return () -> {
				try {
					return constructor.newInstance();
				} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
					log.log(Level.SEVERE, "Could not obtain an instance for {}", first);
					return null;
				}
			};
		} catch (NoSuchMethodException e) {
			log.log(Level.SEVERE, "Could not obtain a constructor for {}", first);
		}
		return null;
	}

	public <B> ConverterBuilderStep2<S, D, B> withBuilder(Supplier<B> builderConstructor, Function<B, D> finalizer) {
		return new ConverterBuilderStep2<>(builderConstructor, finalizer);
	}

	public ConverterBuilderStep2<S, D, D> withDefaultConstructors() {
		return new ConverterBuilderStep2<>(extractConstructor(secondClass), Function.identity());
	}

	public ConverterBuilderStep2<S, D, D> withFactory(Supplier<D> factoryMethod) {
		return new ConverterBuilderStep2<>(factoryMethod, Function.identity());
	}

}
