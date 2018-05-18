package org.anerdib.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SimpleSourcePojo {
	private String stringField;
	private int primitiveField;
	private Object referenceField;

}
