package sap.ass02.gui.GUI.dialogs;

import sap.ass02.gui.GUI.EBikeApp;
import sap.ass02.gui.utils.Triple;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.Map;

/**
 * A Dialog exclusive for the Admins
 * it shows all the users registered in the system,
 * it permits the admin to delete them
 */
public class AllUsersDialog extends JDialog {

    private final EBikeApp app;
    private final JDialog dialog;
    private Map<Integer, Triple<String, Integer, Boolean>> users;
    @Serial
    private static final long serialVersionUID = 5L;

    /**
     * Instantiates a new All users' dialog.
     *
     * @param app the EBikeApp
     */
    public AllUsersDialog(EBikeApp app) {

        dialog = new JDialog();
        this.app = app;
        this.app.requestReadUser(0,"").onComplete(x -> {
            if (!x.result().isEmpty()) {
                this.users = x.result();
                initialiseDialog();
            } else {
                showNonBlockingMessage("Something went wrong when retrieving users", "Fail", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void initialiseDialog() {
        dialog.setTitle("All Users Registered");
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.setLayout(new BorderLayout());

        JPanel listPanel = getjPanel();

        JScrollPane scrollPane = new JScrollPane(listPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> dialog.dispose());

        JPanel backPanel = new JPanel();
        backPanel.add(backButton);
        dialog.add(backPanel, BorderLayout.SOUTH);

        dialog.setSize(400, 400);

        dialog.setVisible(true);
    }

    private JPanel getjPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        buildList(listPanel);
        return listPanel;
    }

    private void buildList(JPanel listPanel) {
        for (Map.Entry<Integer, Triple<String, Integer, Boolean>> entry : users.entrySet() ) {
            String item = "#" + entry.getKey() + " Username: " + entry.getValue().first() + " -- Credit: " + entry.getValue().second().toString();
            JPanel itemPanel = new JPanel(new BorderLayout());
            JLabel itemLabel = new JLabel(item);
            JButton deleteButton = getDeleteButton(listPanel, entry.getKey());

            itemPanel.add(itemLabel, BorderLayout.CENTER);
            itemPanel.add(deleteButton, BorderLayout.EAST);
            listPanel.add(itemPanel);
        }
    }

    private JButton getDeleteButton(JPanel listPanel, int key) {
        JButton deleteButton = new JButton("Delete");

        deleteButton.addActionListener(e -> this.app.requestDeleteUser(key).onComplete(x -> {
            if (x.result()) {
                showNonBlockingMessage("Successfully deleted user", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.users.remove(key);
                refreshList(listPanel);
            } else {
                showNonBlockingMessage("Something went wrong", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }));
        return deleteButton;
    }

    private void refreshList(JPanel listPanel) {
        this.app.requestReadUser(0,"").onComplete(x -> {
            if (!x.result().isEmpty()) {
                this.users = x.result();
                listPanel.removeAll();
                buildList(listPanel);
                listPanel.revalidate();
                listPanel.repaint();
                dialog.setSize(400, users.size()*100);
            } else {
                showNonBlockingMessage("Something went wrong when retrieving users", "Fail", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showNonBlockingMessage(String message, String title, int messageType) {

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(AllUsersDialog.this, message, title, messageType);
            }
        }.execute();
    }
}
