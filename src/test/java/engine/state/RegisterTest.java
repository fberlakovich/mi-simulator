package engine.state;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegisterTest {

    @Test
    public void testFloatContent() throws Exception {
        // given
        Register register = new Register(0);
        MyByte[] bytes = {new MyByte("3F"), new MyByte("80"), new MyByte("00"), new MyByte("00")};


        // when
        register.setContent(bytes);

        // then
        assertArrayEquals(bytes, register.getContent(4));
    }

    @Test
    public void testIntContent() throws Exception {
        // given
        Register register = new Register(0);
        MyByte[] bytes = new MyByte[]{new MyByte("00"), new MyByte("67"), new MyByte("7D"), new MyByte("B6")};

        // when
        register.setContent(bytes);

        // then
        assertEquals(6782390, register.getContentAsNumber(4));
    }
}