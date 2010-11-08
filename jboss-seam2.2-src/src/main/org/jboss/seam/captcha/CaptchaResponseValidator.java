package org.jboss.seam.captcha;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;



/**
 * Validates that the input entered by the user matches
 * the captcha image.
 * 
 * @author Gavin King
 *
 */
public class CaptchaResponseValidator implements ConstraintValidator<CaptchaResponse,String>
{

   public void initialize(CaptchaResponse constraintAnnotation)
   {
      // TODO Auto-generated method stub
      
   }

   public boolean isValid(String value, ConstraintValidatorContext context)
   {
      boolean valid = Captcha.instance().validateResponse(value );
      if(!valid)
      {
         context.disableDefaultConstraintViolation();
         context.buildConstraintViolationWithTemplate("org.jboss.seam.captcha.error").addConstraintViolation();
      }
      return valid;
   }

}
