package enviroment;

import core.Constants;

/**
 * Diese Klasse repraesentiert den Registerkoerper der MI
 */
public class RegisterBody {

    /**
     * Array mit den Registern
     */
    private Register[] registers = new Register[Constants.NUMBER_OF_REGISTER];

    /**
     * Instaziiert den RegisterKoerper
     */
    public RegisterBody() {
        for (int i = 0; i < Constants.NUMBER_OF_REGISTER; i++) {
            registers[i] = (i == Constants.SP_REGISTER) ?
                    new Register(i, true) :
                    new Register(i);
        }
    }

    /**
     * Gibt das Register mit der Nummer nr zurueck
     *
     * @param nr Registernummer
     * @return Register mit der Nummer nr
     */
    public Register getRegister(int nr) {
        return registers[nr];
    }

    /**
     * Gibt das Register mit der Nummer nr zurueck
     *
     * @param nr     Registernummer
     * @param length Länge des Registerzugriffs
     * @return Register mit der Nummer nr
     */
    public MyByte[] getRegister(int nr, int length) {
        return registers[nr].getContent(length);
    }

    /**
     * Setzt den Inhalt eines Registers
     *
     * @param nr      Registernummer
     * @param content Inhalt eines Registers
     */
    public void setContent(int nr, MyByte[] content) {

        this.registers[nr].setContent(content);

    }

    public void reset() {
        for (Register reg : registers) {
            reg.reset();
        }
    }

}
