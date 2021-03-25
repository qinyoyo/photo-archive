package qinyoyo.photoviewer;

import org.springframework.core.convert.converter.Converter;
import qinyoyo.utils.DateUtil;

import java.util.Date;

public class RequestParameterDateConverter implements Converter<String,Date> {
	    @Override
	    public Date convert(String s) {
	        return DateUtil.string2Date(s);
	    }
}
