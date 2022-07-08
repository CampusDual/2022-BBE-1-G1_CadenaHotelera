package utilities;

import com.ontimize.jee.common.dto.EntityResult;

public class Control {
	
	
	public void setErrorMessage(EntityResult result, String message) {
		result.setMessage(message);
		result.setCode(1);
	}


}
