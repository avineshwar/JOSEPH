/**
 * JOSEPH - JavaScript Object Signing and Encryption Pentesting Helper
 * Copyright (C) 2016 Dennis Detering
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package eu.dety.burp.joseph.editor;

import burp.*;
import eu.dety.burp.joseph.gui.EditorAttackerPanel;
import eu.dety.burp.joseph.gui.PreferencesPanel;
import eu.dety.burp.joseph.utilities.Decoder;
import eu.dety.burp.joseph.utilities.Finder;
import eu.dety.burp.joseph.utilities.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * JSON Web Token (JWE) Editor.
 * <p>
 * Display decoded JWE syntax highlighted.
 * @author Dennis Detering
 * @version 1.0
 */
public class JweEditor implements IMessageEditorTabFactory {
    private static final Logger loggerInstance = Logger.getInstance();
    private static final Decoder joseDecoder = new Decoder();
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private String joseParameterName = null;

    /**
     * Create JweEditor instance.
     * @param callbacks {@link IBurpExtenderCallbacks}.
     */
    public JweEditor(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
    }

    /**
     * Create a new instance of Burps own request/response viewer (IMessageEditorTab).
     * @param controller {@link IMessageEditorController}
     * @param editable True if message is editable, false otherwise.
     * @return {@link JweEditorTab} instance implementing {@link IMessageEditorTab}
     */
    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
        return new JweEditorTab(controller, editable);
    }

    public class JweEditorTab implements IMessageEditorTab {
        private JTabbedPane JweEditorTabPanel;
        private boolean editable;
        private byte[] currentMessage;
        private boolean isModified = false;

        private ITextEditor sourceViewerHeader;
        private ITextEditor sourceViewerCek;
        private ITextEditor sourceViewerIv;
        private ITextEditor sourceViewerCiphertext;
        private ITextEditor sourceViewerAad;
        // private EditorAttackerPanel editorAttackerPanel;

        JweEditorTab(IMessageEditorController controller, boolean editable) {
            this.editable = editable;
            this.JweEditorTabPanel = new JTabbedPane();

            // Add text editor tab for each JOSE part
            sourceViewerHeader = callbacks.createTextEditor();
            sourceViewerCek = callbacks.createTextEditor();
            sourceViewerIv = callbacks.createTextEditor();
            sourceViewerCiphertext = callbacks.createTextEditor();
            sourceViewerAad = callbacks.createTextEditor();

            JweEditorTabPanel.addTab("Header", sourceViewerHeader.getComponent());
            JweEditorTabPanel.addTab("CEK", sourceViewerCek.getComponent());
            JweEditorTabPanel.addTab("IV", sourceViewerIv.getComponent());
            JweEditorTabPanel.addTab("Ciphertext", sourceViewerCiphertext.getComponent());
            JweEditorTabPanel.addTab("AAD", sourceViewerAad.getComponent());

//            editorAttackerPanel = new EditorAttackerPanel(callbacks, this);
//            if(editable) {
//                JweEditorTabPanel.addTab("Attacker", editorAttackerPanel);
//            }
        }

        @Override
        public String getTabCaption() {
            return "JWE";
        }

        @Override
        public Component getUiComponent() {
            return JweEditorTabPanel;
        }

        @Override
        public boolean isEnabled(byte[] content, boolean isRequest) {
            // Enable this tab for requests containing a JOSE parameter
            if(isRequest) {
                for(Object param: PreferencesPanel.getParameterNames().toArray()) {
                    if(helpers.getRequestParameter(content, param.toString()) != null && Finder.checkJwePattern(helpers.getRequestParameter(content, param.toString()).getValue())) {
                        joseParameterName = helpers.getRequestParameter(content, param.toString()).getName();
                        loggerInstance.log(getClass(), "JWE value found, enable JweEditor.", Logger.LogLevel.DEBUG);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void setMessage(byte[] content, boolean isRequest) {
            if (content == null) {
                // Clear displayed content
                sourceViewerHeader.setText(null);
                sourceViewerHeader.setEditable(false);

                sourceViewerCek.setText(null);
                sourceViewerCek.setEditable(false);

                sourceViewerIv.setText(null);
                sourceViewerIv.setEditable(false);

                sourceViewerCiphertext.setText(null);
                sourceViewerCiphertext.setEditable(false);

                sourceViewerAad.setText(null);
                sourceViewerAad.setEditable(false);

                // editorAttackerPanel.setEnabled(false);
            } else if (joseParameterName != null) {
                // Retrieve JOSE parameter
                IParameter parameter = helpers.getRequestParameter(content, joseParameterName);

                String[] joseParts = joseDecoder.getComponents(parameter.getValue(), 5);

                sourceViewerHeader.setEditable(editable);
                sourceViewerCek.setEditable(editable);
                sourceViewerIv.setEditable(editable);
                sourceViewerCiphertext.setEditable(editable);
                sourceViewerAad.setEditable(editable);

                String header = joseDecoder.getDecoded(joseParts[0]);
                String cek = joseParts[1];
                String iv = joseParts[2];
                String ciphertext = joseParts[3];
                String aad = joseParts[4];

                sourceViewerHeader.setText(helpers.stringToBytes(header));
                sourceViewerCek.setText(helpers.stringToBytes(cek));
                sourceViewerIv.setText(helpers.stringToBytes(iv));
                sourceViewerCiphertext.setText(helpers.stringToBytes(ciphertext));
                sourceViewerAad.setText(helpers.stringToBytes(aad));

                // editorAttackerPanel.updateAttackList();
            }

            // Remember the displayed content
            currentMessage = content;
        }

        @Override
        public byte[] getMessage() {
            String[] components = {
                joseDecoder.getEncoded(sourceViewerHeader.getText()),
                helpers.bytesToString(sourceViewerCek.getText()),
                helpers.bytesToString(sourceViewerIv.getText()),
                helpers.bytesToString(sourceViewerCiphertext.getText()),
                helpers.bytesToString(sourceViewerAad.getText())
            };

            // Update the request with the new parameter value
            return helpers.updateParameter(currentMessage, helpers.buildParameter(joseParameterName, joseDecoder.concatComponents(components), IParameter.PARAM_URL));
        }

        @Override
        public boolean isModified() {
            boolean isModified = (sourceViewerHeader.isTextModified() || sourceViewerCek.isTextModified() || sourceViewerIv.isTextModified()  || sourceViewerCiphertext.isTextModified() || sourceViewerAad.isTextModified() || this.isModified);
            this.isModified = false;
            return isModified;
        }

        @Override
        public byte[] getSelectedData() {
            return null;
        }

        /**
         * Update all related source viewer editors
         * @param header The header JSON string
         * @param cek The CEK base64string
         * @param iv The IV base64string
         * @param ciphertext The ciphertext base64string
         * @param aad The aad base64string

         */
        public void updateSourceViewer(String header, String cek, String iv, String ciphertext, String aad) {
            sourceViewerHeader.setText(helpers.stringToBytes(header));
            sourceViewerCek.setText(helpers.stringToBytes(cek));
            sourceViewerIv.setText(helpers.stringToBytes(iv));
            sourceViewerCiphertext.setText(helpers.stringToBytes(ciphertext));
            sourceViewerAad.setText(helpers.stringToBytes(aad));
            this.isModified = true;
        }

        /**
         * Get the header value from sourceViewerHeader editor as string
         * @return Header JSON string
         */
        public String getHeader() {
            return helpers.bytesToString(sourceViewerHeader.getText());
        }

        /**
         * Get the CEK value from sourceViewerCek editor as string
         * @return CEK JSON string
         */
        public String getCek() {
            return helpers.bytesToString(sourceViewerCek.getText());
        }

        /**
         * Get the IV value from sourceViewerIv editor as string
         * @return IV JSON string
         */
        public String getIv() {
            return helpers.bytesToString(sourceViewerIv.getText());
        }

        /**
         * Get the ciphertext value from sourceViewerCiphertext editor as string
         * @return Ciphertext JSON string
         */
        public String getCiphertext() {
            return helpers.bytesToString(sourceViewerCiphertext.getText());
        }

        /**
         * Get the AAD value from sourceViewerAad editor as string
         * @return AAD JSON string
         */
        public String getAad() {
            return helpers.bytesToString(sourceViewerAad.getText());
        }
    }
}

