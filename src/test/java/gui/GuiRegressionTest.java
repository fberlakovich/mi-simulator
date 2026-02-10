package gui;

import engine.Machine;
import engine.MachineContext;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Settings;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.*;
import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GUI regression tests using AssertJ Swing.
 * Tests basic functionality to ensure refactoring hasn't broken the GUI.
 *
 * <p>These tests require a display. To run without stealing focus, use:</p>
 * <pre>
 * # macOS/Linux with Xvfb:
 * xvfb-run ./gradlew test --tests "gui.*"
 *
 * # Or skip GUI tests:
 * ./gradlew test -PskipGuiTests
 * </pre>
 */
@Category(GuiTest.class)
public class GuiRegressionTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @BeforeClass
    public static void checkDisplayAvailable() {
        // Skip tests if running in headless mode or no display available
        Assume.assumeFalse("Skipping GUI tests in headless mode",
                GraphicsEnvironment.isHeadless());

        // Check if display is actually available
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.getDefaultScreenDevice();
        } catch (Exception e) {
            Assume.assumeNoException("Skipping GUI tests - no display available", e);
        }
    }

    @Override
    protected void onSetUp() {
        // Configure robot for less intrusive testing
        Settings settings = robot().settings();
        settings.delayBetweenEvents(50);  // Faster execution
        settings.eventPostingDelay(20);

        // Create fresh Machine instance for each test
        MachineContext machine = Machine.createInstance();
        Window frame = GuiActionRunner.execute(() -> new Window(machine));
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            // Get the actual Window frame before cleanup
            java.awt.Window frame = window.target();
            window.cleanUp();
            // Explicitly dispose the frame to release resources
            if (frame != null) {
                frame.dispose();
            }
            window = null;
        }
        // Encourage garbage collection to release 1MB Memory objects
        System.gc();
    }

    @Test
    public void windowShouldOpen() {
        window.requireVisible();
        assertThat(window.target().getTitle()).contains("MI");
    }

    @Test
    public void shouldHaveAssembleButton() {
        JButtonFixture assembleBtn = findButtonByText("Assemble");
        assertThat(assembleBtn).isNotNull();
    }

    @Test
    public void shouldHaveRunButton() {
        JButtonFixture runBtn = findButtonByText("Run");
        assertThat(runBtn).isNotNull();
    }

    @Test
    public void shouldHaveStepButton() {
        JButtonFixture stepBtn = findButtonByText("Step");
        assertThat(stepBtn).isNotNull();
    }

    @Test
    public void shouldHaveStopButton() {
        JButtonFixture stopBtn = findButtonByText("Stop");
        assertThat(stopBtn).isNotNull();
    }

    @Test
    public void shouldHaveRestartButton() {
        JButtonFixture restartBtn = findButtonByText("Restart");
        assertThat(restartBtn).isNotNull();
    }

    @Test
    public void shouldAssembleSimpleProgram() {
        // Enter a simple program
        JTextComponentFixture textPane = window.textBox(new GenericTypeMatcher<JTextPane>(JTextPane.class) {
            @Override
            protected boolean isMatching(JTextPane component) {
                return component.isEditable();
            }
        });

        textPane.deleteText();
        textPane.enterText("MOVE W I 42, R0\nHALT");

        // Click assemble
        JButtonFixture assembleBtn = findButtonByText("Assemble");
        assembleBtn.click();

        // Wait for UI to update after assembly
        robot().waitForIdle();

        // Run button should now be enabled
        JButtonFixture runBtn = findButtonByText("Run");
        runBtn.requireEnabled();
    }

    @Test
    public void shouldExecuteSimpleProgram() {
        // Enter a simple program
        JTextComponentFixture textPane = window.textBox(new GenericTypeMatcher<JTextPane>(JTextPane.class) {
            @Override
            protected boolean isMatching(JTextPane component) {
                return component.isEditable();
            }
        });

        textPane.deleteText();
        textPane.enterText("MOVE W I 42, R0\nHALT");

        // Assemble
        findButtonByText("Assemble").click();

        // Step through
        JButtonFixture stepBtn = findButtonByText("Step");
        stepBtn.click();

        // Give some time for execution
        robot().waitForIdle();
    }

    @Test
    public void shouldShowErrorForInvalidProgram() {
        // Enter invalid program
        JTextComponentFixture textPane = window.textBox(new GenericTypeMatcher<JTextPane>(JTextPane.class) {
            @Override
            protected boolean isMatching(JTextPane component) {
                return component.isEditable();
            }
        });

        textPane.deleteText();
        textPane.enterText("INVALID_INSTRUCTION R0, R1");

        // Click assemble
        findButtonByText("Assemble").click();

        // Run button should be disabled (assembly failed)
        JButtonFixture runBtn = findButtonByText("Run");
        runBtn.requireDisabled();
    }

    @Test
    public void shouldHaveMenuBar() {
        window.menuItem("file_menu").requireVisible();
    }

    @Test
    public void runButtonShouldBeDisabledInitially() {
        JButtonFixture runBtn = findButtonByText("Run");
        runBtn.requireDisabled();
    }

    @Test
    public void stepButtonShouldBeDisabledInitially() {
        JButtonFixture stepBtn = findButtonByText("Step");
        stepBtn.requireDisabled();
    }

    @Test
    public void stopButtonShouldBeDisabledInitially() {
        JButtonFixture stopBtn = findButtonByText("Stop");
        stopBtn.requireDisabled();
    }

    @Test
    public void restartButtonShouldBeDisabledInitially() {
        JButtonFixture restartBtn = findButtonByText("Restart");
        restartBtn.requireDisabled();
    }

    @Test
    public void shouldHandleArithmeticProgram() {
        JTextComponentFixture textPane = window.textBox(new GenericTypeMatcher<JTextPane>(JTextPane.class) {
            @Override
            protected boolean isMatching(JTextPane component) {
                return component.isEditable();
            }
        });

        // Test arithmetic operations
        textPane.deleteText();
        textPane.enterText("MOVE W I 10, R0\nMOVE W I 20, R1\nADD W R0, R1\nHALT");

        findButtonByText("Assemble").click();
        robot().waitForIdle();

        // Should compile successfully
        JButtonFixture runBtn = findButtonByText("Run");
        runBtn.requireEnabled();
    }

    @Test
    public void shouldHandleLoopProgram() {
        JTextComponentFixture textPane = window.textBox(new GenericTypeMatcher<JTextPane>(JTextPane.class) {
            @Override
            protected boolean isMatching(JTextPane component) {
                return component.isEditable();
            }
        });

        // Test program with labels and jumps
        textPane.deleteText();
        textPane.enterText("MOVE W I 5, R0\nloop: SUB W I 1, R0\nCMP W I 0, R0\nJNE loop\nHALT");

        findButtonByText("Assemble").click();
        robot().waitForIdle();

        JButtonFixture runBtn = findButtonByText("Run");
        runBtn.requireEnabled();
    }

    @Test
    public void shouldHandleMemoryOperations() {
        JTextComponentFixture textPane = window.textBox(new GenericTypeMatcher<JTextPane>(JTextPane.class) {
            @Override
            protected boolean isMatching(JTextPane component) {
                return component.isEditable();
            }
        });

        // Test memory operations
        textPane.deleteText();
        textPane.enterText("MOVE W I 100, R0\nMOVE W I 42, !R0\nMOVE W !R0, R1\nHALT");

        findButtonByText("Assemble").click();
        robot().waitForIdle();

        JButtonFixture runBtn = findButtonByText("Run");
        runBtn.requireEnabled();
    }

    private JButtonFixture findButtonByText(String text) {
        return window.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return text.equals(button.getText());
            }
        });
    }
}
