package tools;
/**
 * 
 * @author vliopard
 */
public class FileQueue
{
    private int    typeOfAction;
    private String pathToFile;

    /**
     * 
     */
    public FileQueue()
    {
    }

    /**
     * 
     * @param action
     * @param pathAndFile
     */
    public void set(int action,
                    String pathAndFile)
    {
        typeOfAction=action;
        pathToFile=pathAndFile;
    }

    /**
     * 
     * @return Returns an <code>int</code> that represents the type of action:
     *         <code>Settings.FileCreated</code>,
     *         <code>Settings.FileModified</code>,
     *         <code>Settings.FileDeleted</code> and
     *         <code>Settings.FileRenamed</code>.
     */
    public int getType()
    {
        return typeOfAction;
    }

    /**
     * 
     * @return Returns a <code>String</code> containing the path to the file
     *         that is associated to a <code>typeOfAction</code>.
     */
    public String getPath()
    {
        return pathToFile;
    }
}
