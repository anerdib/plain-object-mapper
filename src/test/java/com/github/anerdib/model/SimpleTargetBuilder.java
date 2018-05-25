package com.github.anerdib.model;

public final class SimpleTargetBuilder {
	private final SimpleTargetPojo instance;

	public SimpleTargetBuilder() {
		instance = new SimpleTargetPojo();
	}

	public void setStringField(String value) {
		instance.setStringField(value);
	}

	public void setPrimitiveField(int value) {
		instance.setPrimitiveField(value);
	}

	public void setReference(Object ref) {
		instance.setReferenceField(ref);
	}

	public SimpleTargetPojo getInstance() {
		return instance;
	}
}
