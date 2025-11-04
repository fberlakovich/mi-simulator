/**
 *
 */
package simulator;

import core.ProgramExecutor;
import enviroment.Enviroment;

import javax.swing.*;

/**
 * Klasse für die GUI-basierte Programmausführung.
 * Diese Klasse ist ein Adapter zwischen ProgramExecutor und der GUI.
 *
 * @author Matthias Oehme
 */
public class RunProgram extends Thread {

    /**
     * Run-button
     */
    private JButton button_run;

    /**
     * Restart-Button
     */
    private JButton button_restart;

    /**
     * Stop-Button
     */
    private JButton button_stop;

    /** Step-Button */
    private JButton button_step;

    /** The executor */
    private ProgramExecutor executor;

    /**
     * Konstruktor für einen Programmablauf
     *
     * @param btnRun
     *            Run-button
     * @param btnStop
     *            Stop-Button
     * @param btnStep
     *            Step-Button
     * @param btnRestart
     *            Restart-Button
     */
    public RunProgram(JButton btnRun, JButton btnStop, JButton btnStep, JButton btnRestart) {
        button_run = btnRun;
        button_stop = btnStop;
        button_step = btnStep;
        button_restart = btnRestart;

        executor = new ProgramExecutor(new ProgramExecutor.ExecutionStateListener() {
            @Override
            public void onExecutionStart() {
                button_stop.setEnabled(true);
                button_run.setEnabled(false);
                button_step.setEnabled(false);
                button_restart.setEnabled(false);
                Enviroment.frame.updateUI();
            }

            @Override
            public void onExecutionStop(ProgramExecutor.StopReason reason) {
                if (Enviroment.getText() != null) {
                    Enviroment.getText().highlightNextCommand();
                }
                button_stop.setEnabled(false);
                button_restart.setEnabled(true);
                Command next = Enviroment.getNextCommand();
                button_run.setEnabled(!(next == null || next instanceof Halt));
                button_step.setEnabled(!(next == null || next instanceof Halt));
                Enviroment.frame.updateUI();
            }

            @Override
            public void onCommandExecuted(Command command) {
                // No special handling needed for each command in GUI mode
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        executor.executeProgram();
    }

    /**
     * Stoppt den Programmablauf
     *
     */
    public void stopProgram() {
        executor.stop();
    }

}
