package com.github.anerdib;

import com.github.anerdib.model.GenericTypeReference;
import com.github.anerdib.model.SerializableSupplier;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @param <S>
 * @param <D>
 * @author anerdib
 */
@Slf4j
public class ConverterBuilder<S, D> {
	private final Class<S> sourceClass;
	private final Class<D> destinationClass;

	/**
	 * @param first  source class in the conversion
	 * @param second destination class in the conversion
	 */
	public ConverterBuilder(Class<S> first, Class<D> second) {
		this.sourceClass = first;
		this.destinationClass = second;
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
			log.error("Could not obtain a constructor for {}");
		}
		return null;
	}

	/**
	 * Create a Converter using the builder pattern.
	 *
	 * @param builderConstructor the method reference to the builder's constructor
	 * @param finalizer          lambda that will build the final object. Usually it will be just a call or a reference t
	 * @param <B>                generic type for the Builder class used
	 * @return
	 */
	public <B> ConverterBuilderStep2<S, D, B> withBuilder(SerializableSupplier<B> builderConstructor, Function<B, D> finalizer) {
		Class<?> builderClass = GenericTypeReference.getLambdaReturnType(builderConstructor);

		return new ConverterBuilderStep2<S, D, B>(sourceClass, destinationClass, (Class<B>) builderClass, builderConstructor, finalizer);
	}


	/**
	 * Create a Converter using the builder pattern. This method will try to find a <code>build</code>
	 * inside the generic type <code>B</code> and will use that to deliver the final object.
	 *
	 * @param builderConstructor the method reference to the builder's constructor
	 * @param <B>                generic type for the Builder class used
	 * @return
	 */
	public <B> ConverterBuilderStep2<S, D, B> withBuilder(SerializableSupplier<B> builderConstructor) {

		Class<B> builderClass = (Class<B>) GenericTypeReference.getLambdaReturnType(builderConstructor);
		Method buildMethod = null;
		try {
			buildMethod = builderClass.getMethod("build");

		} catch (NoSuchMethodException e) {
			//Maybe there is a method that gives as a D object
			for (Method m : builderClass.getMethods()) {
				if (m.getReturnType().isAssignableFrom(destinationClass))
					buildMethod = m;
			}
			if (buildMethod == null)
				throw new IllegalArgumentException("No build method was found for builder class " + builderClass.getCanonicalName());
		}

		Method finalBuildMethod = buildMethod;
		Function<B, D> finalizer = i -> {
			try {
				return (D) finalBuildMethod.invoke(i);
			} catch (IllegalAccessException | InvocationTargetException e) {
				log.severe("Exception when calling build method");
				throw new IllegalStateException("Build call failed");
			}
		};
		return withBuilder(builderConstructor, finalizer);
	}

	/**
	 * Create a Converter using the builder pattern.
	 *
	 * @param builder the builder instance
	 * @param <B>     generic type for the Builder class used
	 * @return
	 */
	public <B> ConverterBuilderStep2<S, D, B> withBuilder(B builder) {
		return withBuilder(() -> builder);
	}

	/**
	 * Create a converter using the default constructor for the target type.
	 *
	 * @return
	 */
	public ConverterBuilderStep2<S, D, D> withDefaultConstructor() {
		return new ConverterBuilderStep2<>(sourceClass, destinationClass, destinationClass, extractConstructor(destinationClass), Function.identity());
	}

	/**
	 * Create a converter using the given lambda as a factory method. if D::new is given as a parameter basically it's equivalent to withDefaultConstructor.
	 *
	 * @param factoryMethod
	 * @return
	 */
	public ConverterBuilderStep2<S, D, D> withFactory(Supplier<D> factoryMethod) {
		return new ConverterBuilderStep2<>(sourceClass, destinationClass, destinationClass, factoryMethod, Function.identity());
	}
}
