package tools;

import tools.Storage;

/**
 * FileQueue class is an structure just for helping associate a file path to some action.
 * 
 * @author vliopard
 */
public class FileQueue
{
    private int  typeOfAction;
    private Storage pathToFile1;
    private Storage pathToFile2;

    /**
     * File Queue constructor. It does not need to do anything.
     */
    public FileQueue( )
    {
    }

    /**
     * This method sets a pair of action and path to the file that suffers the action.
     * 
     * @param action
     *                        A <code>int</code> that contains the action representing number.
     * @param pathAndFile
     *                        A <code>String</code> containing the path to the file related to the action.
     */
    public void set(int action, Storage pathAndFile)
    {
        typeOfAction = action;
        pathToFile1 = pathAndFile;
    }

    public void set(int action, Storage pathAndFile1, Storage pathAndFile2)
    {
        typeOfAction = action;
        pathToFile1 = pathAndFile1;
        pathToFile2 = pathAndFile2;
    }

    /**
     * This method informs what is the action is file suffered.
     * 
     * @return Returns an <code>int</code> that represents the type of action: <code>Settings.FileCreated</code>,
     *         <code>Settings.FileModified</code>, <code>Settings.FileDeleted</code> and
     *         <code>Settings.FileRenamed</code>.
     */
    public int getType( )
    {
        return typeOfAction;
    }

    /**
     * This method informs the file that is suffering some action.
     * 
     * @return Returns a <code>String</code> containing the path to the file that is associated to a
     *         <code>typeOfAction</code>.
     */
    public Storage getStorage( )
    {
        return pathToFile1;
    }

    public Storage getStorageTarget( )
    {
        return pathToFile2;
    }
}
