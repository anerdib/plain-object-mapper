package com.github.anerdib;

import com.github.anerdib.model.GenericTypeReference;
import com.github.anerdib.model.SerializableSupplier;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <S>
 * @param <D>
 * @author anerdib
 */
@Slf4j
public class ConverterBuilderFactory<S, D> {
	private final Class<S> sourceClass;
	private final Class<D> destinationClass;

	/**
	 * @param first  source class in the conversion
	 * @param second destination class in the conversion
	 */
	public ConverterBuilderFactory(Class<S> first, Class<D> second) {
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
					log.error("Could not obtain an instance for {}", first);
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
	public <B> ConverterBuilder<S, D, B> withBuilder(SerializableSupplier<B> builderConstructor, Function<B, D> finalizer) {
		Class<?> builderClass = GenericTypeReference.getLambdaReturnType(builderConstructor);

		return new ConverterBuilder<S, D, B>(sourceClass, destinationClass, (Class<B>) builderClass, builderConstructor, finalizer);
	}


	/**
	 * Create a Converter using the builder pattern. This method will try to find a <code>build</code>
	 * inside the generic type <code>B</code> and will use that to deliver the final object.
	 *
	 * @param builderConstructor the method reference to the builder's constructor
	 * @param <B>                generic type for the Builder class used
	 * @return
	 */
	public <B> ConverterBuilder<S, D, B> withBuilder(SerializableSupplier<B> builderConstructor) {

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
				log.error("Exception when calling build method");
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
	public <B> ConverterBuilder<S, D, B> withBuilder(B builder) {
		return withBuilder(() -> builder);
	}

	/**
	 * Create a converter using the default constructor for the target type.
	 *
	 * @return
	 */
	public ConverterBuilder<S, D, D> withDefaultConstructor() {
		return new ConverterBuilder<>(sourceClass, destinationClass, destinationClass, extractConstructor(destinationClass), Function.identity());
	}

	/**
	 * Create a converter using the given lambda as a factory method. if D::new is given as a parameter basically it's equivalent to withDefaultConstructor.
	 *
	 * @param factoryMethod
	 * @return
	 */
	public ConverterBuilder<S, D, D> withFactory(Supplier<D> factoryMethod) {
		return new ConverterBuilder<>(sourceClass, destinationClass, destinationClass, factoryMethod, Function.identity());
	}
}
