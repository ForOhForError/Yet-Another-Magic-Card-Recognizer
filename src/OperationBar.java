import javax.swing.*;

class OperationBar extends JProgressBar
{
    private static final long serialVersionUID = 1L;
    private String taskName;
    private int taskMax;
    private int taskProgress;
    private boolean inProgress;
    private String subtaskName;

    public OperationBar()
    {
        super();
        setStringPainted(true);
        endTask();
    }

    public synchronized boolean setTask(String name, int max)
    {
        if (max == 0)
        {
            return true;
        }
        if (!inProgress)
        {
            setMaximum(max);
            taskName = name;
            subtaskName = "";
            taskMax = max;
            taskProgress = 0;
            inProgress = true;
            updateDisplay();
            return true;
        }
        showErrorMessage();
        return false;
    }

    public void setSubtaskName(String name)
    {
        if (taskMax != 0)
        {
            subtaskName = name;
            updateDisplay();
        }
    }

    public synchronized void progressTask()
    {
        taskProgress += 1;
        if (taskProgress == taskMax)
        {
            endTask();
        } else
        {
            setValue(taskProgress);
            updateDisplay();
        }
    }

    private void updateDisplay()
    {
        if (inProgress)
        {
            if (subtaskName.length() > 0)
            {
                setString(String.format("%s: %d/%d - %s...", taskName, taskProgress, taskMax, subtaskName));
            } else
            {
                setString(String.format("%s: %d/%d...", taskName, taskProgress, taskMax));
            }
        } else
        {
            setString("");
        }
    }

    public void endTask()
    {
        taskName = "";
        subtaskName = "";
        taskMax = 0;
        taskProgress = 0;
        inProgress = false;
        setMaximum(taskMax);
        setValue(taskProgress);
        updateDisplay();
    }

    public synchronized boolean isOperating()
    {
        return inProgress;
    }

    public void showErrorMessage()
    {
        JOptionPane.showMessageDialog(null, "Cannot start a new opperation; wait for the current one to complete.");
    }
}