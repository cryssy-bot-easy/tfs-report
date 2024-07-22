package net.ipc.utils;

import static org.junit.Assert.*;
import net.ipc.utils.DateUtils;
import org.junit.Test;
import java.text.SimpleDateFormat;

class DateUtilsTest {

	@Test
	public void successfullyParseDb2Date() {
		assertEquals("2013-10-01",DateUtils.formatToDb2Date("10/01/2013"));
	}

}
