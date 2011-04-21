package org.systemsbiology.gaggle.boss;


public interface BossUI {
    public void gooseAdded(String name);
    public void gooseUnregistered(String name);
    public void gooseRenamed(String oldName, String uniqueName);

    public void refresh();
    public void refresh(boolean resetTableColumnWidths);
    public String[] getListeningGeese();
    public void broadcastToPlugins(String[] names);
    public void show();
    public void hide();
    public boolean isListening(String gooseName);
    public void displayErrorMessage(String message);
}
