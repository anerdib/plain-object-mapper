package com.github.anerdib.model;

import java.io.Serializable;
import java.util.function.Supplier;

public interface SerializableSupplier<B> extends Supplier<B>, Serializable {
}
