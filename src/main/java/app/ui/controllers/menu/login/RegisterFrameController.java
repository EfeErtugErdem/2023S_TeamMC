package app.ui.controllers.menu.login;

import app.common.AppConfig;
import app.domain.services.base.FileService;
import app.ui.views.login.LoginFrame;
import app.ui.views.login.RegisterFrame;
import app.util.ActionListenerUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class RegisterFrameController extends Component implements ActionListener {

    private RegisterFrame registerFrame;

    public RegisterFrameController(RegisterFrame registerFrame) {
        this.registerFrame = registerFrame;
        ActionListenerUtil.addActionListener(registerFrame.backButton, this);
        ActionListenerUtil.addActionListener(registerFrame.registerButton, this);
        ActionListenerUtil.addActionListener(registerFrame.showPasswordBox, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerFrame.registerButton) {
            String inputName = registerFrame.usernameField.getText();
            char[] inputPassword = registerFrame.passwordField.getPassword();
            char[] inputPasswordAgain = registerFrame.passwordField.getPassword();
            if (!inputName.isEmpty() && inputPassword.length > 0 && inputPasswordAgain.length > 0) {
                if (Arrays.equals(inputPassword, inputPasswordAgain)) {
                    boolean response = FileService.checkData(inputName, inputPassword);
                    if (!response) {
                        FileService.writeData(inputName, inputPassword);
                        JOptionPane.showMessageDialog(registerFrame, "You successfully registered!");
                        registerFrame.dispose();
                        LoginFrame loginFrame = new LoginFrame(AppConfig.appSize);
                        LoginFrameController loginFrameController = new LoginFrameController(loginFrame);
                    } else {
                        JOptionPane.showMessageDialog(registerFrame, "User already exists!");
                    }
                } else {
                    JOptionPane.showMessageDialog(registerFrame, "Passwords do not match.");
                }
            } else {
                JOptionPane.showMessageDialog(registerFrame, "Please fill all fields.");
            }

        } else if (e.getSource() == registerFrame.backButton) {
            registerFrame.dispose();
            LoginFrame loginFrame = new LoginFrame(AppConfig.appSize);
            LoginFrameController loginFrameController = new LoginFrameController(loginFrame);

        } else if (e.getSource() == registerFrame.showPasswordBox) {
            if (registerFrame.showPasswordBox.isSelected()) {
                registerFrame.passwordField.setEchoChar((char) 0);
            } else {
                registerFrame.passwordField.setEchoChar('●');
            }
        }
    }
}
