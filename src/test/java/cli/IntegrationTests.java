package cli;

import engine.Machine;
import engine.ProgramRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IntegrationTests {

    private PrintingMachine sut;
    private ByteArrayOutputStream result;
    private final String testName;

    public IntegrationTests(String testName) {
        this.testName = testName;
    }

    @Before
    public void setup() {
        result = new ByteArrayOutputStream();
        // Note: PrintingMachine is created in test() after assembleAndLoad to ensure
        // MemoryChangeTracker subscribes to the correct Machine instance
    }

    @Override
    public String toString() {
        return testName;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<String> getTestPrograms() throws URISyntaxException {
        ClassLoader classLoader = IntegrationTests.class.getClassLoader();
        File programsDir = Paths.get(classLoader.getResource("programs").toURI()).toFile();
        LinkedList<String> names = new LinkedList<>();
        for (File file : programsDir.listFiles()) {
            if (file.getName().endsWith(".mi")) {
                names.add(file.getName().replace(".mi", ""));
            }
        }

        return names;
    }

    @Test
    public void test() throws IOException, URISyntaxException {
        // load the program text
        Path programs = Path.of("programs");

        ClassLoader classLoader = getClass().getClassLoader();
        Path programFile = Paths.get(classLoader.getResource(programs.resolve(testName + ".mi").toString()).toURI());
        String programText = new String(Files.readAllBytes(programFile));
        MachineUtils.assembleAndLoad(programText);
        // Create PrintingMachine AFTER assembleAndLoad to ensure MemoryChangeTracker
        // subscribes to the correct (new) Machine instance
        ProgramRunner runner = Machine.getInstance().createRunner();
        sut = new PrintingMachine(runner, new PrintStream(result), false);
        Path expectedFile = Paths.get(classLoader.getResource(programs.resolve(testName + ".expected").toString()).toURI());
        String expectedState = new String(Files.readAllBytes(expectedFile));
        while (!sut.hasHalted()) {
            sut.executeNext();
        }
        assertEquals(expectedState, result.toString());
    }
}