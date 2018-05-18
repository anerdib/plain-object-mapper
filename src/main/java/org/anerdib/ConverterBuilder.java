package org.anerdib;

import lombok.extern.java.Log;
import org.anerdib.api.Converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @param <S>
 * @param <D>
 * @author anerdib
 */
@Log
public class ConverterBuilder<S, D> {
	private final Class<S> firstClass;
	private final Class<D> secondClass;

	public ConverterBuilder(Class<S> first, Class<D> second) {
		this.firstClass = first;
		this.secondClass = second;
	}

	public <B> ConverterBuilderStep2<S, D, B> withBuilder(Supplier<B> builderConstructor, Function<B, D> finalizer) {
		return new ConverterBuilderStep2<>(builderConstructor, finalizer);
	}

	public ConverterBuilderStep2<S, D, D> withFactory(Supplier<D> factoryMethod) {
		return new ConverterBuilderStep2<>(factoryMethod, Function.identity());
	}

	public ConverterBuilderStep2<S, D, D> withDefaultConstructors() {
		return new ConverterBuilderStep2<>(extractConstructor(secondClass), Function.identity());
	}


	private <T> Supplier<T> extractConstructor(Class<T> first) {
		try {
			Constructor<T> constructor = first.getConstructor(null);
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

	public class ConverterBuilderStep2<S, D, I> {
		private final Supplier<I> supplier;
		private final Function<I, D> finalizer;


		private ConverterBuilderStep2(Supplier<I> factoryMethod, Function<I, D> finalizer) {
			this.supplier = factoryMethod;
			this.finalizer = finalizer;
		}

		public final Converter<S, D> build() {
			ConverterImpl<S, D, I> converterInstance = new ConverterImpl<S, D, I>(supplier, finalizer);
			return converterInstance;
		}

		public <T> From<T> from(Function<S, T> getter) {


		}

		public class From<ST, DT> {
			public ConverterBuilderStep2<S, D, I> to(BiConsumer<I, DT> setter) {
				return ConverterBuilderStep2.this;
			}
		}
	}


}
