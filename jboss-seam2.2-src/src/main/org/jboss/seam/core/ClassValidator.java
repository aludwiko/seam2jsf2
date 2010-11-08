package org.jboss.seam.core;

import java.io.Serializable;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

public class ClassValidator <T>
implements Serializable
{
   private static final Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
   private final Class<T> beanClass;
   public ClassValidator(Class<T> beanClass)
   {
     this.beanClass=beanClass;
   }
   public Set<ConstraintViolation<T>> getPotentialInvalidValues(String propertyName, Object value)
   {
      return validator.validateValue(beanClass, propertyName, value);
   }
   
}
