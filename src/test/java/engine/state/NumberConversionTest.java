package engine.state;

import engine.util.NumberConversion;
import org.junit.Test;

import static org.junit.Assert.*;

public class NumberConversionTest {

    @Test
    public void testByteToIntConversion() throws Exception {
        // given
        MyByte[] bytes = {new MyByte("00"), new MyByte("67"), new MyByte("7D"), new MyByte("B6")};

        // when
        int result = NumberConversion.myBytetoIntWithoutSign(bytes);

        // then
        assertEquals(6782390, result);
    }

    @Test
    public void testIntToByteConversion() throws Exception {
        // given
        int number = 6782390;

        // when
        MyByte[] result = NumberConversion.intToByte(number, 4);

        // then
        assertArrayEquals(new MyByte[]{new MyByte("00"), new MyByte("67"), new MyByte("7D"), new MyByte("B6")}, result);
    }
}