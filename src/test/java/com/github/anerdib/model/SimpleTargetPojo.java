package com.github.anerdib.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SimpleTargetPojo {
	private String stringField;
	private int primitiveField;
	private Object referenceField;
}
