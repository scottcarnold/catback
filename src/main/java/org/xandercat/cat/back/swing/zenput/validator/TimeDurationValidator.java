package org.xandercat.cat.back.swing.zenput.validator;

import java.util.Properties;

import org.xandercat.cat.back.swing.zenput.annotation.ValidateTimeDuration;
import org.xandercat.swing.datetime.TimeDuration;
import org.xandercat.swing.zenput.error.ValidationException;
import org.xandercat.swing.zenput.validator.AbstractValidator;

/**
 * Zenput validator for the TimeDuration class.
 * 
 * @author Scott Arnold
 */
public class TimeDurationValidator extends AbstractValidator<TimeDuration> {

	public static TimeDurationValidator newValidator(ValidateTimeDuration annotation) {
		return new TimeDurationValidator();
	}
	
	public static void addMessageProperties(Properties messageProperties) {
		if (messageProperties != null) {
			messageProperties.put("validator.timeDuration.unit", "Unit requires a value.");
			messageProperties.put("validator.timeDuration.value", "Value must be positive.");
		}
	}
	
	@Override
	public void validate(String fieldName, TimeDuration value) throws ValidationException {
		if (value.getUnit() == null) {
			throw new ValidationException(fieldName, "validator.timeDuration.unit");
		}
		if (value.getValue() < 0) {
			throw new ValidationException(fieldName, "validator.timeDuration.value");
		}
	}

	@Override
	public Class<TimeDuration> getValueType() {
		return TimeDuration.class;
	}
}
